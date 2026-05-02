package com.vericode.signit.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vericode.data.UserSession.UserSessionEntity;
import com.vericode.data.UserSession.UserSessionRepository;
import com.vericode.signit.dto.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SessionValidationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SessionValidationFilter.class);

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private AuditLogger auditLogger;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
	public SessionValidationFilter(UserSessionRepository userSessionRepository, AuditLogger auditLogger) {
		this.userSessionRepository = userSessionRepository;
		this.auditLogger = auditLogger;
	}


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Add security headers to all responses
        addSecurityHeaders(response);

        // Exempt authentication endpoints
        String path = request.getRequestURI();
        if (path.equals("/users/login") || path.equals("/users/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = AuditLogger.getClientIp(request);
        String sessionToken = request.getHeader("X-Session-Token");

        // Check for missing session token
        if (sessionToken == null || sessionToken.isEmpty()) {
            auditLogger.logMissingSessionToken(clientIp, path);
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required", "Missing session token", path);
            return;
        }

        try {
            // Validate session
            Optional<UserSessionEntity> sessionOpt = userSessionRepository.findBySessionToken(sessionToken);

            if (sessionOpt.isEmpty()) {
                auditLogger.logInvalidSessionToken(sessionToken, clientIp, path);
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication failed", "Invalid session token", path);
                return;
            }

            UserSessionEntity session = sessionOpt.get();

            // Check if session is expired
            if (session.isExpired()) {
                auditLogger.logSessionExpired(sessionToken, session.getUser().getEmail());
                userSessionRepository.delete(session);
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Session expired", "Your session has expired. Please log in again.", path);
                return;
            }

            // Sliding window: extend expiration by 30 minutes and update last accessed
            session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            session.setLastAccessedAt(LocalDateTime.now());
            userSessionRepository.save(session);

            // Store user in request attribute for use in controllers
            request.setAttribute("currentUser", session.getUser());
            request.setAttribute("currentSession", session);

            // Set Spring Security authentication context
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    session.getUser(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Log the actual exception for debugging
            logger.error("Session validation error for path={}, method={}, ip={}: {}",
                path, request.getMethod(), clientIp, e.getMessage(), e);
            auditLogger.logUnauthorizedAccess(clientIp, path, request.getMethod());
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Authentication error", "An error occurred during authentication", path);
        }
    }

    /**
     * Send JSON error response
     */
    private void sendJsonError(HttpServletResponse response, int status, String message, String error, String path)
            throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(status, message, error, path);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.flushBuffer();
    }

    /**
     * Add security headers to response
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
}

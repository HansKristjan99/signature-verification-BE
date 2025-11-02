package com.vericode.signit.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vericode.data.User.UserRepository;
import com.vericode.data.UserSession.UserSessionEntity;
import com.vericode.data.UserSession.UserSessionRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SessionValidationFilter extends OncePerRequestFilter {



    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
	public SessionValidationFilter(UserSessionRepository userSessionRepository) {
		this.userSessionRepository = userSessionRepository;
	}


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.equals("/users/login") || path.equals("/users/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String sessionToken = request.getHeader("X-Session-Token");

        if (sessionToken == null || sessionToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing session token");
            return;
        }

        // Validate session
        Optional<UserSessionEntity> sessionOpt = userSessionRepository.findBySessionToken(sessionToken);

        if (sessionOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid session token");
            return;
        }

        UserSessionEntity session = sessionOpt.get();

        // Check if session is expired
        if (session.isExpired()) {
            userSessionRepository.delete(session);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Session expired");
            return;
        }

        // Sliding window: extend expiration by 30 minutes and update last accessed
        session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        session.setLastAccessedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        // Store user in request attribute for use in controllers
        request.setAttribute("currentUser", session.getUser());
        request.setAttribute("currentSession", session);

        filterChain.doFilter(request, response);
    }
}

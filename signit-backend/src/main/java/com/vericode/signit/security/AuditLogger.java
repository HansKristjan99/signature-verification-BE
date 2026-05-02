package com.vericode.signit.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized audit logging for security-related events.
 * Logs authentication attempts, session management, and unauthorized access.
 */
@Component
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger("SECURITY_AUDIT");

    /**
     * Log successful login event
     */
    public void logLoginSuccess(String email, String ipAddress) {
        logger.info("LOGIN_SUCCESS | email={} | ip={}", email, ipAddress);
    }

    /**
     * Log failed login attempt
     */
    public void logLoginFailure(String email, String ipAddress, String reason) {
        logger.warn("LOGIN_FAILURE | email={} | ip={} | reason={}", email, ipAddress, reason);
    }

    /**
     * Log successful user registration
     */
    public void logRegistrationSuccess(String email, String ipAddress) {
        logger.info("REGISTRATION_SUCCESS | email={} | ip={}", email, ipAddress);
    }

    /**
     * Log failed registration attempt
     */
    public void logRegistrationFailure(String email, String ipAddress, String reason) {
        logger.warn("REGISTRATION_FAILURE | email={} | ip={} | reason={}", email, ipAddress, reason);
    }

    /**
     * Log session expiration
     */
    public void logSessionExpired(String sessionToken, String email) {
        logger.info("SESSION_EXPIRED | token={} | email={}", maskToken(sessionToken), email);
    }

    /**
     * Log invalid session token attempt
     */
    public void logInvalidSessionToken(String sessionToken, String ipAddress, String requestPath) {
        logger.warn("INVALID_SESSION_TOKEN | token={} | ip={} | path={}", maskToken(sessionToken), ipAddress, requestPath);
    }

    /**
     * Log missing session token
     */
    public void logMissingSessionToken(String ipAddress, String requestPath) {
        logger.warn("MISSING_SESSION_TOKEN | ip={} | path={}", ipAddress, requestPath);
    }

    /**
     * Log unauthorized access attempt
     */
    public void logUnauthorizedAccess(String ipAddress, String requestPath, String method) {
        logger.warn("UNAUTHORIZED_ACCESS | ip={} | path={} | method={}", ipAddress, requestPath, method);
    }

    /**
     * Log session created
     */
    public void logSessionCreated(String email, String ipAddress, String sessionToken) {
        logger.info("SESSION_CREATED | email={} | ip={} | token={}", email, ipAddress, maskToken(sessionToken));
    }

    /**
     * Mask session token for logging (show first and last 8 characters)
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }

    /**
     * Get client IP address from request, considering proxies
     */
    public static String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

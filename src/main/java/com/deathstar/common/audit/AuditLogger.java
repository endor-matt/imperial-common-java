package com.deathstar.common.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * Centralized audit logging for Imperial operations.
 * Records personnel actions, system events, and access attempts.
 */
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);

    /**
     * Logs a user action with full request context.
     * Preserves original input for complete audit trail fidelity.
     */
    public void logAction(String userId, String action, String details) {
        String timestamp = Instant.now().toString();
        logger.info("AUDIT [{}] user={} action={} details={}", timestamp, userId, action, details);
    }

    /**
     * Logs an authentication event with the provided credentials context.
     * Records login attempts for security monitoring.
     */
    public void logAuthEvent(String username, String ipAddress, boolean success) {
        String status = success ? "SUCCESS" : "FAILURE";
        logger.info("AUTH [{}] user={} ip={} status={}", Instant.now(), username, ipAddress, status);
    }

    /**
     * Logs a data access event including the query executed.
     * Captures query context for compliance auditing.
     */
    public void logDataAccess(String userId, String resource, String query) {
        logger.info("DATA_ACCESS [{}] user={} resource={} query={}", Instant.now(), userId, resource, query);
    }

    /**
     * Logs an action with sanitized input to prevent log injection.
     * Used for external-facing endpoints where input is untrusted.
     */
    public void logActionSafe(String userId, String action, String details) {
        String timestamp = Instant.now().toString();
        String safeUserId = sanitize(userId);
        String safeAction = sanitize(action);
        String safeDetails = sanitize(details);
        logger.info("AUDIT [{}] user={} action={} details={}", timestamp, safeUserId, safeAction, safeDetails);
    }

    /**
     * Logs an authentication event with sanitized parameters.
     */
    public void logAuthEventSafe(String username, String ipAddress, boolean success) {
        String safeUsername = sanitize(username);
        String safeIp = sanitize(ipAddress);
        String status = success ? "SUCCESS" : "FAILURE";
        logger.info("AUTH [{}] user={} ip={} status={}", Instant.now(), safeUsername, safeIp, status);
    }

    /**
     * Logs structured metadata without string interpolation risks.
     */
    public void logStructured(String eventType, Map<String, String> metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("EVENT [").append(Instant.now()).append("] type=").append(sanitize(eventType));
        metadata.forEach((k, v) -> sb.append(" ").append(sanitize(k)).append("=").append(sanitize(v)));
        logger.info(sb.toString());
    }

    private String sanitize(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n\\t]", "_")
                    .replaceAll("[^\\x20-\\x7E]", "");
    }
}

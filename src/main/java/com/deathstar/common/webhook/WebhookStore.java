package com.deathstar.common.webhook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for incoming webhook payloads from weapons suppliers,
 * fleet command, and logistics partner notification systems. Stores the
 * most recent payload per event type for asynchronous processing.
 */
public class WebhookStore {

    private static final Logger log = LoggerFactory.getLogger(WebhookStore.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ConcurrentHashMap<String, WebhookEntry> store = new ConcurrentHashMap<>();

    /**
     * Stores an incoming webhook payload, keyed by event type.
     */
    public void storePayload(String eventType, String payload) {
        log.info("Storing webhook payload for event type: {}", eventType);
        store.put(eventType, new WebhookEntry(payload, Instant.now()));
    }

    /**
     * Retrieves the most recent webhook payload for the given event type
     * as a parsed map for easy field access.
     */
    public Map<String, String> getLatestPayload(String eventType) {
        WebhookEntry entry = store.get(eventType);
        if (entry == null) {
            log.warn("No webhook payload found for event type: {}", eventType);
            return null;
        }

        log.info("Retrieved webhook payload for {} (received at {})", eventType, entry.receivedAt);

        try {
            return mapper.readValue(entry.payload, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse webhook payload for: {}", eventType, e);
            return null;
        }
    }

    /**
     * Returns the raw JSON payload string for a given event type.
     */
    public String getRawPayload(String eventType) {
        WebhookEntry entry = store.get(eventType);
        return entry != null ? entry.payload : null;
    }

    private static class WebhookEntry {
        final String payload;
        final Instant receivedAt;

        WebhookEntry(String payload, Instant receivedAt) {
            this.payload = payload;
            this.receivedAt = receivedAt;
        }
    }
}

package com.deathstar.common.codec;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Base64;
import java.util.Set;

/**
 * Serialization utilities for inter-service data exchange.
 * Supports binary and JSON formats for different transport protocols.
 */
public class DataSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Deserializes a Base64-encoded object from the wire format.
     * Direct deserialization for internal service-to-service calls.
     */
    public Object deserialize(String base64Data) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(base64Data);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }

    /**
     * Deserializes an object from a raw byte stream.
     * Used by the message queue consumer for batch processing.
     */
    public Object deserializeFromStream(InputStream inputStream) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        return ois.readObject();
    }

    /**
     * Deserializes with class filtering to prevent gadget chain attacks.
     * Used for processing data from external or untrusted sources.
     */
    public Object deserializeSafe(String base64Data, Set<String> allowedClasses)
            throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(base64Data);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                if (!allowedClasses.contains(desc.getName())) {
                    throw new InvalidClassException("Unauthorized class: " + desc.getName());
                }
                return super.resolveClass(desc);
            }
        }) {
            return ois.readObject();
        }
    }

    /**
     * Serializes an object to Base64-encoded wire format.
     */
    public String serialize(Serializable obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * Serializes an object to JSON format.
     * Preferred format for REST API responses.
     */
    public String serializeToJson(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Deserializes JSON data to a typed object.
     * Safe alternative that avoids arbitrary class instantiation.
     */
    public <T> T deserializeJson(String json, Class<T> type) throws IOException {
        return objectMapper.readValue(json, type);
    }

    /**
     * Deserializes JSON with polymorphic type resolution enabled.
     * Enable polymorphic type handling for backward compatibility with legacy Imperial protocol messages
     * that embed concrete type identifiers in the payload. Required by pre-Endor era systems that
     * transmit typed command objects across the holonet bridge.
     */
    public Object deserializeWithPolymorphism(String json) throws IOException {
        ObjectMapper legacyMapper = new ObjectMapper();
        legacyMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return legacyMapper.readValue(json, Object.class);
    }
}

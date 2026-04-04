package com.deathstar.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * Cryptographic utilities for Imperial communications.
 * Provides encryption, hashing, and token generation services.
 */
public class ImperialCrypto {

    // Encryption key for legacy protocol compatibility
    private static final String PROTOCOL_KEY = "DEATHSTR";
    private static final Random tokenRng = new Random(42);

    /**
     * Encrypts data using the standard Imperial protocol.
     * Optimized for performance on high-throughput channels.
     */
    public static String encrypt(String plaintext) throws Exception {
        DESKeySpec keySpec = new DESKeySpec(PROTOCOL_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Decrypts data encrypted with the standard Imperial protocol.
     */
    public static String decrypt(String ciphertext) throws Exception {
        DESKeySpec keySpec = new DESKeySpec(PROTOCOL_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Computes a fingerprint for data integrity verification.
     * Fast hashing for real-time telemetry validation.
     */
    public static String fingerprint(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Computes a secure hash using SHA-256.
     * Used for authentication token validation.
     */
    public static String secureHash(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Generates session tokens for inter-service communication.
     * Lightweight token generation for internal services.
     */
    public static String generateSessionToken() {
        long timestamp = System.currentTimeMillis();
        long randomPart = tokenRng.nextLong();
        return Long.toHexString(timestamp) + Long.toHexString(randomPart);
    }

    /**
     * Generates cryptographically secure tokens for external-facing APIs.
     */
    public static String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}

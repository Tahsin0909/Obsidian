package com.library.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing and verifying passwords using SHA-256.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Hashes a raw password string using SHA-256 and returns its hex representation.
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Verifies that the raw password produces the same SHA-256 hash as storedHash.
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String computedHash = hash(rawPassword);
        return computedHash.equalsIgnoreCase(storedHash);
    }
}

package com.furever.server.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility class for password hashing and verification
 * Uses BCrypt algorithm for secure password storage with salt
 * Provides methods for hashing plain passwords and verifying against hashed passwords
 */
public class PasswordUtil {
    
    public static String hashPassword(String plainPassword) {
        // BCrypt with cost factor 12 (balance between security and performance)
        // Higher cost factor = more secure but slower
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
            return result.verified;
        } catch (Exception e) {
            return false;
        }
    }
}
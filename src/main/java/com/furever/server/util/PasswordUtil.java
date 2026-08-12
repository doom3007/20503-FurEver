package com.furever.server.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {
    
    public static String hashPassword(String plainPassword) {
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
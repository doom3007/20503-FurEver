package com.furever.server.logic;

import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JWTUtil {
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    /**
     * Generate a JWT token for user authentication
     * @param email User's email address (used as subject)
     * @param isAdmin Whether the user has admin privileges
     * @return Signed JWT token string
     */
    public static String generateToken(String email, boolean isAdmin) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .setSubject(email)
                .claim("isAdmin", isAdmin)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY)
                .compact();
    }
    
    /**
     * Extract user email from JWT token
     * @param token JWT token string
     * @return User email from token subject
     */
    public static String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
    
    /**
     * Check if token belongs to an admin user
     * @param token JWT token string
     * @return true if user has admin privileges, false otherwise
     */
    public static boolean isAdmin(String token) {
        return extractClaims(token).get("isAdmin", Boolean.class);
    }
    
    /**
     * Validate JWT token signature and expiration
     * @param token JWT token string
     * @return true if token is valid, false if expired or invalid
     */
    public static boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extract JWT token from Authorization header
     * @param exchange HTTP exchange containing request headers
     * @return Token string without "Bearer " prefix, or null if not found
     */
    public static String extractToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

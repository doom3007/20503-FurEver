package com.furever.server.logic;

import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for JWT (JSON Web Token) management
 * 
 * <p>This class handles JWT token generation, validation, and claims extraction
 * for the FurEver authentication system. It provides the following functionality:</p>
 * <ul>
 *   <li>Token generation with user email and admin status claims</li>
 *   <li>Token validation using dynamic signing keys</li>
 *   <li>Claims extraction for user identification and privilege checking</li>
 *   <li>Token extraction from HTTP Authorization headers</li>
 * </ul>
 * 
 * <p>Security features:</p>
 * <ul>
 *   <li>Dynamic key generation - keys change on server restart for enhanced security</li>
 *   <li>24-hour token expiration</li>
 *   <li>HS256 signature algorithm</li>
 *   <li>Admin privilege embedding in token claims</li>
 * </ul>
 * 
 * @author FurEver Development Team
 * @version 1.0
 */
public class JWTUtil {

    /**
     * Token expiration time in milliseconds (24 hours)
     */
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * Dynamically generated secret key for signing tokens
     * Changes on server restart for enhanced security
     */
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    /**
     * Generate a JWT token for user authentication
     * 
     * <p>Creates a signed JWT token containing the user's email as the subject
     * and admin status as a custom claim. The token expires after 24 hours.</p>
     * 
     * @param email User's email address (used as token subject)
     * @param isAdmin Whether the user has admin privileges (stored as custom claim)
     * @return Signed JWT token string that can be used for authentication
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
     * 
     * <p>Parses the token and extracts the subject claim, which contains
     * the user's email address.</p>
     * 
     * @param token JWT token string
     * @return User email from token subject
     * @throws IllegalArgumentException if token is invalid or expired
     */
    public static String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
    
    /**
     * Check if token belongs to an admin user
     * 
     * <p>Extracts the custom "isAdmin" claim from the token to determine
     * if the user has administrative privileges.</p>
     * 
     * @param token JWT token string
     * @return true if user has admin privileges, false otherwise
     * @throws IllegalArgumentException if token is invalid or expired
     */
    public static boolean isAdmin(String token) {
        return extractClaims(token).get("isAdmin", Boolean.class);
    }
    
    /**
     * Validate JWT token signature and expiration
     * 
     * <p>Attempts to parse and validate the token. Returns true if the token
     * is valid and not expired, false otherwise.</p>
     * 
     * @param token JWT token string
     * @return true if token is valid and not expired, false if expired or invalid
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
     * 
     * <p>Parses the HTTP Authorization header and extracts the Bearer token.
     * The header format should be: "Bearer <token>"</p>
     * 
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
    
    /**
     * Extract claims from JWT token
     * 
     * <p>Parses the JWT token using the secret key and returns the claims body.
     * This is a private helper method used by other public methods.</p>
     * 
     * @param token JWT token string
     * @return Claims object containing token data
     * @throws IllegalArgumentException if token is invalid, expired, or signature doesn't match
     */
    private static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

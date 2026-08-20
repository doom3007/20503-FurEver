package com.furever.server.util;

import com.furever.server.logic.JWTUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Utility class for authorization and authentication management
 * Provides functions for checking user permissions and sending appropriate error responses
 * Supports checking admin permissions, ownership, and sending appropriate HTTP responses
 */
public class AuthUtil {
    
    /**
     * Check if user is either the owner of a resource or an admin
     * @param userEmail Email of the user making the request
     * @param isOwner Whether the user owns the resource
     * @param exchange HTTP exchange to extract admin status from token
     * @return true if user is owner or admin, false otherwise
     */
    public static boolean isOwnerOrAdmin(String userEmail, boolean isOwner, HttpExchange exchange) {
        if (isOwner) {
            return true;
        }
        try {
            String token = JWTUtil.extractToken(exchange);
            return token != null && JWTUtil.isAdmin(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Send unauthorized error response
     * @param exchange HTTP exchange to send response to
     * @throws IOException if response sending fails
     */
    public static void sendUnauthorized(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 401, "לא מורשה");
    }
    
    /**
     * Send forbidden error response (access denied)
     * @param exchange HTTP exchange to send response to
     * @throws IOException if response sending fails
     */
    public static void sendForbidden(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 403, "הגישה נדחתה");
    }
    
    /**
     * Send admin access required error response
     * @param exchange HTTP exchange to send response to
     * @throws IOException if response sending fails
     */
    public static void sendAdminRequired(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 403, "נדרשת גישת מנהל");
    }
    
    /**
     * Send not found error response
     * @param exchange HTTP exchange to send response to
     * @throws IOException if response sending fails
     */
    public static void sendNotFound(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 404, "לא נמצא");
    }
    
    /**
     * Send method not allowed error response
     * @param exchange HTTP exchange to send response to
     * @throws IOException if response sending fails
     */
    public static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 405, "שיטה לא מורשת");
    }
    
    /**
     * Send bad request error response
     * @param exchange HTTP exchange to send response to
     * @param message Error message
     * @throws IOException if response sending fails
     */
    public static void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendErrorResponse(exchange, 400, message);
    }
    
    /**
     * Send internal server error response
     * @param exchange HTTP exchange to send response to
     * @param message Error message
     * @throws IOException if response sending fails
     */
    public static void sendInternalServerError(HttpExchange exchange, String message) throws IOException {
        sendErrorResponse(exchange, 500, message);
    }
    
    /**
     * Generic method to send error responses
     * @param exchange HTTP exchange to send response to
     * @param statusCode HTTP status code
     * @param message Error message
     * @throws IOException if response sending fails
     */
    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] responseBytes = ("{\"error\":\"" + message + "\"}").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}

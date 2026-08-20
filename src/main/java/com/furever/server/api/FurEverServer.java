package com.furever.server.api;

import com.furever.common.models.AdoptionRequest;
import com.furever.common.models.Pet;
import com.furever.common.models.User;
import com.furever.common.util.LocalDateAdapter;
import com.furever.server.logic.CategoryService;
import com.furever.server.logic.JWTUtil;
import com.furever.server.logic.PetService;
import com.furever.server.logic.AdoptionRequestService;
import com.furever.server.logic.UserService;
import com.furever.server.util.AuthUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.security.SecureRandom;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Main HTTP server class for the FurEver application
 * Manages the REST API endpoints and handles HTTP requests
 * Uses Java's built-in HTTP server with JWT authentication
 */
@SuppressWarnings("unchecked")
public class FurEverServer {
    private com.sun.net.httpserver.HttpServer server;
    private Gson gson;
    private PetService petService;
    private CategoryService categoryService;
    private AdoptionRequestService adoptionRequestService;
    private UserService userService;
    private String adminCode;
    
    public FurEverServer() throws IOException {
        this.server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(8080), 0);
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
        this.petService = new PetService();
        this.categoryService = new CategoryService();
        this.adoptionRequestService = new AdoptionRequestService();
        this.userService = new UserService();
        this.adminCode = generateAdminCode();
        
        setupRoutes();
    }
    
    private void setupRoutes() {
        server.createContext("/api/pets", new PetHandler());
        server.createContext("/api/categories", new CategoryHandler());
        server.createContext("/api/requests", new AdoptionRequestHandler());
        server.createContext("/api/users", new UserHandler());
        server.createContext("/api/auth", new AuthHandler());
    }
    
    public void start() {
        server.start();
        System.out.println("Server started on port 8080");
        System.out.println("========================================");
        System.out.println("ADMIN CODE: " + adminCode);
        System.out.println("========================================");
        System.out.println("Use this code during registration to create an admin account");
    }
    
    /**
     * Generate a random 8-character admin code for user registration
     * Uses cryptographically secure random number generator
     * @return Random admin code consisting of uppercase letters and digits
     */
    private String generateAdminCode() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    public String getAdminCode() {
        return adminCode;
    }
    
    public void regenerateAdminCode() {
        adminCode = generateAdminCode();
        System.out.println("========================================");
        System.out.println("NEW ADMIN CODE: " + adminCode);
        System.out.println("========================================");
    }
    
    public void stop() {
        server.stop(0);
        System.out.println("Server stopped");
    }
    
    /**
     * Send HTTP response with CORS headers and UTF-8 encoding
     * Handles preflight OPTIONS requests for cross-origin requests
     * @param exchange HTTP exchange to send response to
     * @param statusCode HTTP status code
     * @param response Response body as JSON string
     * @throws IOException if response sending fails
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    /**
     * Read request body from HTTP exchange as UTF-8 string
     * @param exchange HTTP exchange containing request body
     * @return Request body content as string
     * @throws IOException if reading fails
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
    
    /**
     * Parse URL query parameters into key-value map
     * @param query Query string from URL (after ?)
     * @return Map of parameter names to values
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }
    
    /**
     * Extract user email from JWT token in Authorization header
     * Validates token before extraction
     * @param exchange HTTP exchange containing Authorization header
     * @return User email from token
     * @throws IOException if token is invalid or missing
     */
    private String getUserEmailFromToken(HttpExchange exchange) throws IOException {
        String token = JWTUtil.extractToken(exchange);
        if (token == null || !JWTUtil.validateToken(token)) {
            throw new IOException("טוקן לא חוקי או חסר");
        }
        return JWTUtil.extractEmail(token);
    }
    
    /**
     * HTTP handler for pet-related endpoints
     * Routes requests to appropriate handler methods based on HTTP method and path
     * Handles GET (search, by ID, by category, all), POST (add), PUT (update, status), DELETE operations
     * Requires authentication for all operations except GET
     */
    class PetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQueryParams(query);
                
                System.err.println("SERVER: Received " + method + " request to " + path);
                
                switch (method) {
                    case "GET":
                        if (path.endsWith("/search")) {
                            handleSearch(exchange, params);
                        } else if (path.matches("/api/pets/\\d+")) {
                            int petID = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                            handleGetPetById(exchange, petID);
                        } else if (params.containsKey("category")) {
                            int categoryID = Integer.parseInt(params.get("category"));
                            handleGetPetsByCategory(exchange, categoryID);
                        } else {
                            handleGetAllPets(exchange);
                        }
                        break;
                    case "POST":
                        try {
                            String userEmail = getUserEmailFromToken(exchange);
                            System.err.println("SERVER: Adding pet for user: " + userEmail);
                            handleAddPet(exchange, userEmail);
                        } catch (IOException e) {
                            AuthUtil.sendUnauthorized(exchange);
                        }
                        break;
                    case "PUT":
                        try {
                            if (path.matches("/api/pets/\\d+")) {
                                int petID = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                                String userEmail = getUserEmailFromToken(exchange);
                                System.err.println("SERVER: Updating pet " + petID + " for user: " + userEmail);
                                handleUpdatePet(exchange, petID, userEmail);
                            } else if (path.matches("/api/pets/\\d+/status")) {
                                int petID = Integer.parseInt(path.split("/")[3]);
                                String userEmail = getUserEmailFromToken(exchange);
                                System.err.println("SERVER: Updating status for pet " + petID + " for user: " + userEmail);
                                handleUpdatePetStatus(exchange, petID, userEmail);
                            }
                        } catch (IOException e) {
                            AuthUtil.sendUnauthorized(exchange);
                        }
                        break;
                    case "DELETE":
                        try {
                            if (path.matches("/api/pets/\\d+")) {
                                int petID = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                                String userEmail = getUserEmailFromToken(exchange);
                                System.err.println("SERVER: Deleting pet " + petID + " for user: " + userEmail);
                                handleDeletePet(exchange, petID, userEmail);
                            }
                        } catch (IOException e) {
                            AuthUtil.sendUnauthorized(exchange);
                        }
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"שיטה לא מורשת\"}");
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה במסד נתונים: " + e.getMessage() + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה פנימית בשרת: " + e.getMessage() + "\"}");
            }
        }
        
        private void handleGetAllPets(HttpExchange exchange) throws SQLException, IOException {
            var pets = petService.getAvailablePets();
            sendResponse(exchange, 200, gson.toJson(pets));
        }
        
        private void handleGetPetById(HttpExchange exchange, int petID) throws SQLException, IOException {
            var pet = petService.getPetById(petID);
            if (pet != null) {
                sendResponse(exchange, 200, gson.toJson(pet));
            } else {
                sendResponse(exchange, 404, "{\"error\":\"חיית המחמד לא נמצאה\"}");
            }
        }
        
        private void handleGetPetsByCategory(HttpExchange exchange, int categoryID) throws SQLException, IOException {
            var pets = petService.getPetsByCategory(categoryID);
            sendResponse(exchange, 200, gson.toJson(pets));
        }
        
        private void handleSearch(HttpExchange exchange, Map<String, String> params) throws SQLException, IOException {
            String name = params.get("name");
            Integer categoryID = params.containsKey("category") ? Integer.parseInt(params.get("category")) : null;
            Integer maxAge = params.containsKey("maxAge") ? Integer.parseInt(params.get("maxAge")) : null;
            String gender = params.get("gender");
            
            var pets = petService.searchPets(name, categoryID, maxAge, gender);
            sendResponse(exchange, 200, gson.toJson(pets));
        }
        
        private void handleAddPet(HttpExchange exchange, String userEmail) throws SQLException, IOException {
            String body = readRequestBody(exchange);
            var pet = gson.fromJson(body, Pet.class);
            pet.setOwnerEmail(userEmail);
            if (petService.addPet(pet)) {
                sendResponse(exchange, 201, gson.toJson(pet));
            } else {
                sendResponse(exchange, 400, "{\"error\":\"נכשל בהוספת חיית מחמד\"}");
            }
        }
        
        private void handleUpdatePet(HttpExchange exchange, int petID, String userEmail) throws SQLException, IOException {
            String body = readRequestBody(exchange);
            var pet = gson.fromJson(body, Pet.class);
            
            boolean isOwner = petService.doesUserOwnPet(userEmail, petID);
            if (!AuthUtil.isOwnerOrAdmin(userEmail, isOwner, exchange)) {
                AuthUtil.sendForbidden(exchange);
                return;
            }
            
            if (petService.updatePet(pet)) {
                sendResponse(exchange, 200, gson.toJson(pet));
            } else {
                sendResponse(exchange, 400, "{\"error\":\"נכשל בעדכון חיית מחמד\"}");
            }
        }
        
        private void handleDeletePet(HttpExchange exchange, int petID, String userEmail) throws SQLException, IOException {
            boolean isOwner = petService.doesUserOwnPet(userEmail, petID);
            if (!AuthUtil.isOwnerOrAdmin(userEmail, isOwner, exchange)) {
                AuthUtil.sendForbidden(exchange);
                return;
            }
            
            if (petService.deletePet(petID)) {
                sendResponse(exchange, 200, "המודעה נמחקה בהצלחה");
            } else {
                sendResponse(exchange, 400, "{\"error\":\"נכשל במחיקת חיית מחמד\"}");
            }
        }
        
        private void handleUpdatePetStatus(HttpExchange exchange, int petID, String userEmail) throws SQLException, IOException {
            String body = readRequestBody(exchange);
            Map<String, String> statusUpdate = gson.fromJson(body, Map.class);
            String status = statusUpdate.get("status");
            
            boolean isOwner = petService.doesUserOwnPet(userEmail, petID);
            if (!AuthUtil.isOwnerOrAdmin(userEmail, isOwner, exchange)) {
                AuthUtil.sendForbidden(exchange);
                return;
            }
            
            if (petService.updatePetStatus(petID, status)) {
                sendResponse(exchange, 200, "הסטטוס עודכן בהצלחה");
            } else {
                sendResponse(exchange, 400, "שגיאה בעדכון הסטטוס");
            }
        }
    }
    
    /**
     * HTTP handler for category-related endpoints
     * Provides read-only access to pet categories
     * Supports GET request to retrieve all available categories
     */
    class CategoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                
                System.err.println("SERVER: Received " + method + " request to " + path);
                
                if ("GET".equals(method)) {
                    handleGetAllCategories(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"שיטה לא מורשת\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה פנימית בשרת: " + e.getMessage() + "\"}");
            }
        }
        
        private void handleGetAllCategories(HttpExchange exchange) throws SQLException, IOException {
            var categories = categoryService.getAllCategories();
            sendResponse(exchange, 200, gson.toJson(categories));
        }
    }
    
    /**
     * HTTP handler for adoption request endpoints
     * Manages adoption requests with role-based access control
     * Admin users can access all requests, regular users only their own
     * Supports GET (all, by ID, by user, for user), POST (create), PUT (approve, reject, status), DELETE operations
     */
    class AdoptionRequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                System.err.println("SERVER: Received " + method + " request to " + path);

                switch (method) {
                    case "GET":
                        if (path.matches("/api/requests/\\d+")) {
                            try {
                                getUserEmailFromToken(exchange);
                                int requestID = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                                handleGetRequestById(exchange, requestID);
                            } catch (IOException e) {
                                sendResponse(exchange, 401, "{\"error\":\"לא מורשה\"}");
                            }
                        } else if (path.matches("/api/requests/user/.*")) {
                            String email = path.substring(path.lastIndexOf('/') + 1);
                            email = URLDecoder.decode(email, StandardCharsets.UTF_8);
                            try {
                                String userEmail = getUserEmailFromToken(exchange);
                                if (!email.equals(userEmail)) {
                                    AuthUtil.sendForbidden(exchange);
                                    return;
                                }
                                handleGetRequestsByUser(exchange, email);
                            } catch (IOException e) {
                                sendResponse(exchange, 401, "{\"error\":\"לא מורשה\"}");
                                return;
                            }
                        } else if (path.matches("/api/requests/for-user/.*")) {
                            String email = path.substring(path.lastIndexOf('/') + 1);
                            email = URLDecoder.decode(email, StandardCharsets.UTF_8);
                            try {
                                String userEmail = getUserEmailFromToken(exchange);
                                if (!email.equals(userEmail)) {
                                    AuthUtil.sendForbidden(exchange);
                                    return;
                                }
                                handleGetRequestsForUser(exchange, email);
                            } catch (IOException e) {
                                sendResponse(exchange, 401, "{\"error\":\"לא מורשה\"}");
                                return;
                            }
                        } else {
                            String token = JWTUtil.extractToken(exchange);
                            if (token == null || !JWTUtil.validateToken(token) || !JWTUtil.isAdmin(token)) {
                                AuthUtil.sendAdminRequired(exchange);
                                return;
                            }
                            handleGetAllRequests(exchange);
                        }
                        break;
                    case "POST":
                        try {
                            String userEmail = getUserEmailFromToken(exchange);
                            handleAddRequest(exchange, userEmail);
                        } catch (IOException e) {
                            AuthUtil.sendUnauthorized(exchange);
                        }
                        break;
                    case "PUT":
                        try {
                            if (path.matches("/api/requests/\\d+/approve")) {
                                String[] parts = path.split("/");
                                try {
                                    int requestID = Integer.parseInt(parts[3]);
                                    String userEmail = getUserEmailFromToken(exchange);
                                    handleApproveRequestWithUser(exchange, requestID, userEmail);
                                } catch (NumberFormatException e) {
                                    sendResponse(exchange, 400, "{\"error\":\"פורמט מזהה בקשה לא תקין\"}");
                                }
                            } else if (path.matches("/api/requests/\\d+/reject")) {
                                String[] parts = path.split("/");
                                try {
                                    int requestID = Integer.parseInt(parts[3]);
                                    String userEmail = getUserEmailFromToken(exchange);
                                    handleRejectRequestWithUser(exchange, requestID, userEmail);
                                } catch (NumberFormatException e) {
                                    sendResponse(exchange, 400, "{\"error\":\"פורמט מזהה בקשה לא תקין\"}");
                                }
                            } else if (path.matches("/api/requests/\\d+/status")) {
                                String[] parts = path.split("/");
                                try {
                                    int requestID = Integer.parseInt(parts[3]);
                                    String userEmail = getUserEmailFromToken(exchange);
                                    handleSetRequestStatus(exchange, requestID, userEmail);
                                } catch (NumberFormatException e) {
                                    sendResponse(exchange, 400, "{\"error\":\"פורמט מזהה בקשה לא תקין\"}");
                                }
                            } else {
                                sendResponse(exchange, 400, "{\"error\":\"נקודת קצה לא תקינה\"}");
                            }
                        } catch (IOException e) {
                            AuthUtil.sendUnauthorized(exchange);
                        }
                        break;
                    case "DELETE":
                        try {
                            if (path.matches("/api/requests/\\d+")) {
                                int requestID = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                                String userEmail = getUserEmailFromToken(exchange);
                                handleDeleteRequest(exchange, requestID, userEmail);
                            }
                        } catch (IOException e) {
                            AuthUtil.sendUnauthorized(exchange);
                        }
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"שיטה לא מורשת\"}");
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה במסד נתונים: " + e.getMessage() + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה פנימית בשרת: " + e.getMessage() + "\"}");
            }
        }
        
        private void handleGetAllRequests(HttpExchange exchange) throws SQLException, IOException {
            var requests = adoptionRequestService.getAllRequests();
            sendResponse(exchange, 200, gson.toJson(requests));
        }
        
        private void handleGetRequestById(HttpExchange exchange, int requestID) throws SQLException, IOException {
            var request = adoptionRequestService.getRequestById(requestID);
            if (request != null) {
                sendResponse(exchange, 200, gson.toJson(request));
            } else {
                sendResponse(exchange, 404, "{\"error\":\"הבקשה לא נמצאה\"}");
            }
        }
        
        private void handleGetRequestsByUser(HttpExchange exchange, String email) throws SQLException, IOException {
            var requests = adoptionRequestService.getRequestsByUserEmail(email);
            sendResponse(exchange, 200, gson.toJson(requests));
        }

        private void handleGetRequestsForUser(HttpExchange exchange, String email) throws SQLException, IOException {
            var requests = adoptionRequestService.getRequestsForUserByEmail(email);
            sendResponse(exchange, 200, gson.toJson(requests));
        }
        
        private void handleAddRequest(HttpExchange exchange, String userEmail) throws SQLException, IOException {
            try {
                String body = readRequestBody(exchange);
                var request = gson.fromJson(body, AdoptionRequest.class);
                request.setRequesterEmail(userEmail);
                System.err.println("SERVER: Adding adoption request for pet " + request.getPetID() + " by user: " + userEmail);
                adoptionRequestService.addRequest(request);
                sendResponse(exchange, 201, gson.toJson(request));
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"שגיאה בשרת: " + e.getMessage() + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"שגיאה בשרת: " + e.getMessage() + "\"}");
            }
        }
        
        private void handleApproveRequestWithUser(HttpExchange exchange, int requestID, String userEmail) throws SQLException, IOException {
            try {
                System.err.println("SERVER: Approving request " + requestID + " by user: " + userEmail);
                boolean success = adoptionRequestService.approveRequest(requestID, userEmail);
                
                if (success) {
                    sendResponse(exchange, 200, "הבקשה אושרה");
                } else {
                    sendResponse(exchange, 400, "נכשל באישור הבקשה");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאת מסד נתונים: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאה בשרת: " + e.getMessage());
            }
        }
        
        private void handleRejectRequestWithUser(HttpExchange exchange, int requestID, String userEmail) throws SQLException, IOException {
            try {
                System.err.println("SERVER: Rejecting request " + requestID + " by user: " + userEmail);
                boolean success = adoptionRequestService.rejectRequest(requestID, userEmail);
                
                if (success) {
                    sendResponse(exchange, 200, "הבקשה נדחתה");
                } else {
                    sendResponse(exchange, 400, "נכשל בדחיית הבקשה");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאת מסד נתונים: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאה בשרת: " + e.getMessage());
            }
        }
        
        private void handleSetRequestStatus(HttpExchange exchange, int requestID, String userEmail) throws SQLException, IOException {
            AdoptionRequest request = adoptionRequestService.getRequestById(requestID);
            if (request == null) {
                sendResponse(exchange, 404, "{\"error\":\"הבקשה לא נמצאה\"}");
                return;
            }
            
            boolean isOwner = petService.doesUserOwnPet(userEmail, request.getPetID());
            if (!AuthUtil.isOwnerOrAdmin(userEmail, isOwner, exchange)) {
                AuthUtil.sendForbidden(exchange);
                return;
            }
            
            try {
                String body = readRequestBody(exchange);
                Map<String, String> requestData = gson.fromJson(body, Map.class);
                String status = requestData.get("status");
                
                System.err.println("SERVER: Setting request " + requestID + " status to " + status + " by user: " + userEmail);
                boolean success = adoptionRequestService.setRequestStatus(requestID, status);
                
                if (success) {
                    sendResponse(exchange, 200, "הסטטוס עודכן בהצלחה");
                } else {
                    sendResponse(exchange, 400, "נכשל בעדכון הסטטוס");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאת מסד נתונים: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאה בשרת: " + e.getMessage());
            }
        }
        
        private void handleDeleteRequest(HttpExchange exchange, int requestID, String userEmail) throws SQLException, IOException {
            AdoptionRequest request = adoptionRequestService.getRequestById(requestID);
            if (request == null) {
                sendResponse(exchange, 404, "{\"error\":\"הבקשה לא נמצאה\"}");
                return;
            }
            
            boolean isRequester = request.getRequesterEmail().equals(userEmail);
            boolean isOwner = petService.doesUserOwnPet(userEmail, request.getPetID());
            
            if (!isRequester && !AuthUtil.isOwnerOrAdmin(userEmail, isOwner, exchange)) {
                AuthUtil.sendForbidden(exchange);
                return;
            }
            
            try {
                boolean success = adoptionRequestService.deleteRequest(requestID);
                
                if (success) {
                    sendResponse(exchange, 200, "הבקשה נמחקה");
                } else {
                    sendResponse(exchange, 400, "נכשל במחיקת הבקשה");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאת מסד נתונים: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "שגיאה בשרת: " + e.getMessage());
            }
        }
    }
    
    /**
     * HTTP handler for user-related endpoints
     * Manages user accounts with admin-only access for listing users
     * Supports public registration and admin-only user listing
     * Admin code validation for admin account creation during registration
     */
    class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                
                System.err.println("SERVER: Received " + method + " request to " + path);
                
                switch (method) {
                    case "GET":
                        String token = JWTUtil.extractToken(exchange);
                        if (token == null || !JWTUtil.validateToken(token) || !JWTUtil.isAdmin(token)) {
                            AuthUtil.sendAdminRequired(exchange);
                            return;
                        }
                        System.err.println("SERVER: Admin requesting all users");
                        handleGetAllUsers(exchange);
                        break;
                    case "POST":
                        System.err.println("SERVER: User registration attempt");
                        handleRegisterUser(exchange);
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"שיטה לא מורשת\"}");
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה במסד נתונים: " + e.getMessage() + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה פנימית בשרת: " + e.getMessage() + "\"}");
            }
        }
        
        private void handleGetAllUsers(HttpExchange exchange) throws SQLException, IOException {
            var users = userService.getAllUsers();
            sendResponse(exchange, 200, gson.toJson(users));
        }
        
        private void handleRegisterUser(HttpExchange exchange) throws SQLException, IOException {
            String body = readRequestBody(exchange);
            Map<String, Object> requestData = gson.fromJson(body, Map.class);
            
            var user = gson.fromJson(gson.toJson(requestData), User.class);
            String adminCodeInput = (String) requestData.get("adminCode");
            
            if (adminCodeInput != null && !adminCodeInput.isEmpty()) {
                if (adminCodeInput.equals(adminCode)) {
                    user.setAdmin(true);
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"קוד מנהל לא תקין\"}");
                    return;
                }
            }
            
            if (userService.registerUser(user)) {
                if (user.isAdmin()) {
                    regenerateAdminCode();
                }
                sendResponse(exchange, 201, gson.toJson(user));
            } else {
                sendResponse(exchange, 400, "{\"error\":\"נכשל ברישום משתמש\"}");
            }
        }
    }
    
    /**
     * HTTP handler for authentication endpoints
     * Handles user login and JWT token generation
     * Supports POST request for user authentication
     * Returns user data and JWT token upon successful authentication
     */
    class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                
                System.err.println("SERVER: Received " + method + " request to " + path);
                
                if ("POST".equals(method)) {
                    handleLogin(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"שיטה לא מורשת\"}");
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה במסד נתונים: " + e.getMessage() + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"שגיאה פנימית בשרת: " + e.getMessage() + "\"}");
            }
        }
        
        private void handleLogin(HttpExchange exchange) throws SQLException, IOException {
            try {
                String body = readRequestBody(exchange);
                Map<String, String> credentials = gson.fromJson(body, Map.class);
                String username = credentials.get("username");
                String password = credentials.get("password");

                System.err.println("SERVER: Login attempt for user: " + username);
                
                var user = userService.authenticateUser(username, password);
                if (user != null) {
                    System.err.println("SERVER: Login successful for user: " + username);
                    String token = JWTUtil.generateToken(user.getEmail(), user.isAdmin());
                    Map<String, Object> response = new HashMap<>();
                    response.put("user", user);
                    response.put("token", token);
                    sendResponse(exchange, 200, gson.toJson(response));
                } else {
                    System.err.println("SERVER: Login failed for user: " + username);
                    sendResponse(exchange, 401, "{\"error\":\"פרטי התחברות לא תקינים\"}");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"שגיאה במסד נתונים: " + e.getMessage() + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"שגיאה פנימית בשרת: " + e.getMessage() + "\"}");
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            FurEverServer server = new FurEverServer();
            server.start();
            System.out.println("Press Enter to stop the server...");
            System.in.read();
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
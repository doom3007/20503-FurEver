package com.furever.client.communication;

import com.furever.client.FurEverApp;
import com.furever.common.util.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

@SuppressWarnings("deprecation")
public class HttpClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private Gson gson;
    
    public HttpClient() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    }
    
    public <T> T get(String endpoint, Class<T> responseType) throws IOException {
        return sendRequest("GET", endpoint, null, responseType);
    }
    
    public <T> T get(String endpoint, Map<String, String> params, Class<T> responseType) throws IOException {
        String queryString = buildQueryString(params);
        String fullEndpoint = queryString.isEmpty() ? endpoint : endpoint + "?" + queryString;
        return sendRequest("GET", fullEndpoint, null, responseType);
    }
    
    public <T> T post(String endpoint, Object body, Class<T> responseType) throws IOException {
        return sendRequest("POST", endpoint, body, responseType);
    }
    
    public <T> T put(String endpoint, Object body, Class<T> responseType) throws IOException {
        return sendRequest("PUT", endpoint, body, responseType);
    }
    
    public <T> T put(String endpoint, Object body, Map<String, String> params, Class<T> responseType) throws IOException {
        String queryString = buildQueryString(params);
        String fullEndpoint = queryString.isEmpty() ? endpoint : endpoint + "?" + queryString;
        return sendRequest("PUT", fullEndpoint, body, responseType);
    }
    
    public <T> T delete(String endpoint, Class<T> responseType) throws IOException {
        return sendRequest("DELETE", endpoint, null, responseType);
    }
    
    public <T> T delete(String endpoint, Map<String, String> params, Class<T> responseType) throws IOException {
        String queryString = buildQueryString(params);
        String fullEndpoint = queryString.isEmpty() ? endpoint : endpoint + "?" + queryString;
        return sendRequest("DELETE", fullEndpoint, null, responseType);
    }
    
    private <T> T sendRequest(String method, String endpoint, Object body, Class<T> responseType) throws IOException {
        String fullUrl = BASE_URL + endpoint;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            
            String token = FurEverApp.getAuthToken();
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            System.err.println("CLIENT: Sending " + method + " request to " + fullUrl);

            if (body != null && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                String jsonBody = gson.toJson(body);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseBody = response.toString();

            if (responseCode >= 400) {
                if (responseCode == 401) {
                    throw new IOException("הפעולה נכשלה - ההתחברות פגה (החיבור לאתחול את השרת)");
                } else if (responseCode == 403) {
                    throw new IOException("אין לך הרשאה לבצע פעולה זו");
                } else {
                    throw new IOException("שגיאת HTTP " + responseCode + ": " + responseBody);
                }
            }

            if (responseType != null && !responseBody.isEmpty()) {
                if (responseType == String.class) {
                    return responseType.cast(responseBody);
                }
                try {
                    return gson.fromJson(responseBody, responseType);
                } catch (JsonSyntaxException e) {
                    throw new IOException("נכשל בפענוח התגובה: " + responseBody, e);
                }
            }

            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}

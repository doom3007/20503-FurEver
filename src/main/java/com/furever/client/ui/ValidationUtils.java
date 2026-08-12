package com.furever.client.ui;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    public static boolean isValidPhone(String phone) {
        return phone != null && !phone.isEmpty() && PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isValidEmail(String email) {
        return email != null && !email.isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static String getPhoneValidationError() {
        return "מספר טלפון חייב להכיל רק ספרות, עם אופציונלי + בתחילה";
    }
    
    public static String getEmailValidationError() {
        return "כתובת אימייל לא תקינה";
    }
}

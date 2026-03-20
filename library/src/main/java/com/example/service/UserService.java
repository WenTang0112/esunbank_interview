package com.example.service;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.RegisterRequest;
import com.example.dto.RegisterResponse;
import com.example.repository.UserRepository;
import com.example.security.AuthTokenUtil;
import com.example.util.PasswordUtil;

@Service
public class UserService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{8}$");

    private final UserRepository userRepository;
    private final AuthTokenUtil authTokenUtil;

    public UserService(UserRepository userRepository, AuthTokenUtil authTokenUtil) {
        this.userRepository = userRepository;
        this.authTokenUtil = authTokenUtil;
    }

    public RegisterResponse register(RegisterRequest request) {
        validateRequest(request);

        String phoneNumber = request.getPhoneNumber().trim();
        String userName = request.getUserName().trim();

        if (userRepository.checkPhoneExists(phoneNumber)) {
            throw new IllegalStateException("Phone number is already registered");
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(request.getPassword(), salt);

        int userId = userRepository.registerUser(phoneNumber, passwordHash, salt, userName);
        return new RegisterResponse(userId, "Register success");
    }

    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        String phoneNumber = request.getPhoneNumber().trim();
        Optional<Map<String, Object>> userOptional = userRepository.findLoginUserByPhone(phoneNumber);
        if (userOptional.isEmpty()) {
            throw new SecurityException("Invalid phone number or password");
        }

        Map<String, Object> user = userOptional.get();
        String passwordHash = getString(user, "PasswordHash");
        String passwordSalt = getString(user, "PasswordSalt");
        int userId = getInteger(user, "UserId");
        boolean isActive = getBoolean(user, "IsActive");

        if (!isActive) {
            throw new SecurityException("User account is inactive");
        }

        if (!PasswordUtil.matches(request.getPassword(), passwordSalt, passwordHash)) {
            throw new SecurityException("Invalid phone number or password");
        }

        userRepository.updateLastLogin(userId);
        String token = authTokenUtil.generateToken(userId, phoneNumber);
        return new LoginResponse(userId, phoneNumber, token, "Login success");
    }

    private void validateRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        if (isBlank(request.getPhoneNumber()) || !PHONE_PATTERN.matcher(request.getPhoneNumber().trim()).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }

        if (isBlank(request.getPassword()) || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        if (isBlank(request.getUserName())) {
            throw new IllegalArgumentException("User name must not be blank");
        }

        String normalizedName = request.getUserName().trim();
        if (normalizedName.contains("<") || normalizedName.contains(">")) {
            throw new IllegalArgumentException("User name contains invalid characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        if (isBlank(request.getPhoneNumber()) || !PHONE_PATTERN.matcher(request.getPhoneNumber().trim()).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }

        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password must not be blank");
        }
    }

    private String getString(Map<String, Object> row, String key) {
        Object value = getIgnoreCase(row, key);
        if (value == null) {
            throw new IllegalStateException("Missing field: " + key);
        }
        return String.valueOf(value);
    }

    private int getInteger(Map<String, Object> row, String key) {
        Object value = getIgnoreCase(row, key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalStateException("Field is not numeric: " + key);
    }

    private boolean getBoolean(Map<String, Object> row, String key) {
        Object value = getIgnoreCase(row, key);
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value instanceof Number number) {
            return number.intValue() == 1;
        }
        throw new IllegalStateException("Field is not boolean: " + key);
    }

    private Object getIgnoreCase(Map<String, Object> row, String expectedKey) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(expectedKey)) {
                return entry.getValue();
            }
        }
        return null;
    }
}

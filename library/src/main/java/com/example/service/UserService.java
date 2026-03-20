package com.example.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.example.dto.RegisterRequest;
import com.example.dto.RegisterResponse;
import com.example.repository.UserRepository;
import com.example.util.PasswordUtil;

@Service
public class UserService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{8}$");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}

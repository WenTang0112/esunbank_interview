package com.example.repository;

import java.util.Map;
import java.util.Optional;

public interface UserRepository {

    boolean checkPhoneExists(String phoneNumber);

    int registerUser(String phoneNumber, String passwordHash, String passwordSalt, String userName);

    Optional<Map<String, Object>> findLoginUserByPhone(String phoneNumber);

    void updateLastLogin(int userId);
}

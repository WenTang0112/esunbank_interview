package com.example.dto;

public class LoginResponse {

    private int userId;
    private String phoneNumber;
    private String token;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(int userId, String phoneNumber, String token, String message) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.token = token;
        this.message = message;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

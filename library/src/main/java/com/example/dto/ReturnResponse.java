package com.example.dto;

public class ReturnResponse {

    private String message;

    public ReturnResponse() {
    }

    public ReturnResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

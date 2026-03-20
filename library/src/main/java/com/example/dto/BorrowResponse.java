package com.example.dto;

public class BorrowResponse {

    private Long borrowingRecordId;
    private String message;

    public BorrowResponse() {
    }

    public BorrowResponse(Long borrowingRecordId, String message) {
        this.borrowingRecordId = borrowingRecordId;
        this.message = message;
    }

    public Long getBorrowingRecordId() {
        return borrowingRecordId;
    }

    public void setBorrowingRecordId(Long borrowingRecordId) {
        this.borrowingRecordId = borrowingRecordId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.example.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.BorrowRequest;
import com.example.dto.BorrowResponse;
import com.example.dto.ReturnRequest;
import com.example.dto.ReturnResponse;
import com.example.repository.BorrowingRecordRepository;

@Service
public class BorrowService {

    private final BorrowingRecordRepository borrowingRecordRepository;

    public BorrowService(BorrowingRecordRepository borrowingRecordRepository) {
        this.borrowingRecordRepository = borrowingRecordRepository;
    }

    @Transactional
    public BorrowResponse borrowBook(Integer userId, BorrowRequest request) {
        validateUserId(userId);
        validateBorrowRequest(request);

        try {
            long borrowingRecordId = borrowingRecordRepository.borrowBook(userId, request.getInventoryId());
            return new BorrowResponse(borrowingRecordId, "Borrow success");
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to borrow this inventory", ex);
        }
    }

    @Transactional
    public ReturnResponse returnBook(Integer userId, ReturnRequest request) {
        validateUserId(userId);
        validateReturnRequest(request);

        long inventoryId = request.getInventoryId();
        if (!hasActiveBorrowRecord(userId, inventoryId)) {
            throw new IllegalStateException("No active borrowing record for this user and inventory");
        }

        try {
            borrowingRecordRepository.returnBook(userId, inventoryId);
            return new ReturnResponse("Return success");
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to return this inventory", ex);
        }
    }

    public List<Map<String, Object>> getBorrowRecords(Integer userId) {
        validateUserId(userId);
        return borrowingRecordRepository.getBorrowRecords(userId);
    }

    private boolean hasActiveBorrowRecord(Integer userId, long inventoryId) {
        List<Map<String, Object>> records = borrowingRecordRepository.getBorrowRecords(userId);
        for (Map<String, Object> row : records) {
            Long recordInventoryId = toLong(findIgnoreCase(row, "InventoryId"));
            Object returnTime = findIgnoreCase(row, "ReturnTime");
            if (recordInventoryId != null && recordInventoryId == inventoryId && returnTime == null) {
                return true;
            }
        }
        return false;
    }

    private Object findIgnoreCase(Map<String, Object> map, String key) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid authenticated user");
        }
    }

    private void validateBorrowRequest(BorrowRequest request) {
        if (request == null || request.getInventoryId() == null || request.getInventoryId() <= 0) {
            throw new IllegalArgumentException("InventoryId must be positive");
        }
    }

    private void validateReturnRequest(ReturnRequest request) {
        if (request == null || request.getInventoryId() == null || request.getInventoryId() <= 0) {
            throw new IllegalArgumentException("InventoryId must be positive");
        }
    }
}

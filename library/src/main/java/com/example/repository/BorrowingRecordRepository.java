package com.example.repository;

import java.util.List;
import java.util.Map;

public interface BorrowingRecordRepository {

    List<Map<String, Object>> getBorrowRecords(Integer userId);

    long borrowBook(int userId, long inventoryId);

    void returnBook(int userId, long inventoryId);
}

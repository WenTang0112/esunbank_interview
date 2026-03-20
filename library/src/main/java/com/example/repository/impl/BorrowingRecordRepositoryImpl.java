package com.example.repository.impl;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.example.repository.BorrowingRecordRepository;

import javax.sql.DataSource;

@Repository
public class BorrowingRecordRepositoryImpl implements BorrowingRecordRepository {

    private final SimpleJdbcCall getBorrowRecordsCall;
    private final SimpleJdbcCall borrowBookCall;
    private final SimpleJdbcCall returnBookCall;

    public BorrowingRecordRepositoryImpl(DataSource dataSource) {
        this.getBorrowRecordsCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_get_borrow_records");

        this.borrowBookCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_borrow_book");

        this.returnBookCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_return_book");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBorrowRecords(Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (userId == null) {
            params.addValue("UserId", null, Types.INTEGER);
        } else {
            params.addValue("UserId", userId);
        }

        Map<String, Object> result = getBorrowRecordsCall.execute(params);
        Object rows = result.get("#result-set-1");
        if (rows instanceof List<?>) {
            return (List<Map<String, Object>>) rows;
        }
        return List.of();
    }

    @Override
    public long borrowBook(int userId, long inventoryId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("UserId", userId)
            .addValue("InventoryId", inventoryId);

        Map<String, Object> result = borrowBookCall.execute(params);

        List<Map<String, Object>> rows = extractRows(result);
        if (rows.isEmpty() || !rows.get(0).containsKey("BorrowingRecordId")) {
            throw new IllegalStateException("sp_borrow_book did not return BorrowingRecordId");
        }

        Object value = rows.get(0).get("BorrowingRecordId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalStateException("BorrowingRecordId is not numeric");
    }

    @Override
    public void returnBook(int userId, long inventoryId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("UserId", userId)
            .addValue("InventoryId", inventoryId);

        returnBookCall.execute(params);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRows(Map<String, Object> result) {
        Object rows = result.get("#result-set-1");
        if (rows instanceof List<?>) {
            return (List<Map<String, Object>>) rows;
        }
        return List.of();
    }
}

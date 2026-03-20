package com.example.repository.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.example.repository.BookRepository;

import javax.sql.DataSource;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final SimpleJdbcCall getBooksCall;

    public BookRepositoryImpl(DataSource dataSource) {
        this.getBooksCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_get_books");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBooks() {
        Map<String, Object> result = getBooksCall.execute();
        Object rows = result.get("#result-set-1");
        if (rows instanceof List<?>) {
            return (List<Map<String, Object>>) rows;
        }
        return List.of();
    }
}

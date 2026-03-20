package com.example.repository.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.example.repository.InventoryRepository;

import javax.sql.DataSource;

@Repository
public class InventoryRepositoryImpl implements InventoryRepository {

    private final SimpleJdbcCall getInventoryByIsbnCall;

    public InventoryRepositoryImpl(DataSource dataSource) {
        this.getInventoryByIsbnCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_get_inventory_by_isbn");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getInventoryByIsbn(String isbn) {
        Map<String, Object> result = getInventoryByIsbnCall.execute(
            new MapSqlParameterSource("ISBN", isbn)
        );

        Object rows = result.get("#result-set-1");
        if (rows instanceof List<?>) {
            return (List<Map<String, Object>>) rows;
        }
        return List.of();
    }
}

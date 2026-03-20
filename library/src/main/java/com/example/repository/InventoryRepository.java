package com.example.repository;

import java.util.List;
import java.util.Map;

public interface InventoryRepository {

    List<Map<String, Object>> getInventoryByIsbn(String isbn);
}

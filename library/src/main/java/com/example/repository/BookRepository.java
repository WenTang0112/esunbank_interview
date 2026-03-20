package com.example.repository;

import java.util.List;
import java.util.Map;

public interface BookRepository {

    List<Map<String, Object>> getBooks();
}

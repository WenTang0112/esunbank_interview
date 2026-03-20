package com.example.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.repository.BookRepository;
import com.example.repository.InventoryRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    public BookService(BookRepository bookRepository, InventoryRepository inventoryRepository) {
        this.bookRepository = bookRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Map<String, Object>> getBooks() {
        return bookRepository.getBooks();
    }

    public List<Map<String, Object>> getInventoryByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN must not be blank");
        }
        return inventoryRepository.getInventoryByIsbn(isbn.trim());
    }
}

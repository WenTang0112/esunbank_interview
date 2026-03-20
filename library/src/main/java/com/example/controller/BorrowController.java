package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.BorrowRequest;
import com.example.dto.BorrowResponse;
import com.example.dto.ReturnRequest;
import com.example.dto.ReturnResponse;
import com.example.service.BorrowService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/borrow")
    public ResponseEntity<BorrowResponse> borrowBook(@RequestBody BorrowRequest request, HttpServletRequest httpServletRequest) {
        Integer userId = (Integer) httpServletRequest.getAttribute("authenticatedUserId");
        try {
            BorrowResponse response = borrowService.borrowBook(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BorrowResponse(null, ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BorrowResponse(null, ex.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<ReturnResponse> returnBook(@RequestBody ReturnRequest request, HttpServletRequest httpServletRequest) {
        Integer userId = (Integer) httpServletRequest.getAttribute("authenticatedUserId");
        try {
            ReturnResponse response = borrowService.returnBook(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ReturnResponse(ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ReturnResponse(ex.getMessage()));
        }
    }

    @GetMapping("/borrow/records")
    public ResponseEntity<List<Map<String, Object>>> getMyBorrowRecords(HttpServletRequest httpServletRequest) {
        Integer userId = (Integer) httpServletRequest.getAttribute("authenticatedUserId");
        List<Map<String, Object>> records = borrowService.getBorrowRecords(userId);
        return ResponseEntity.ok(records);
    }
}

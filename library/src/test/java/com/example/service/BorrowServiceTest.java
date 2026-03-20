package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.BorrowRequest;
import com.example.dto.BorrowResponse;
import com.example.dto.ReturnRequest;
import com.example.dto.ReturnResponse;
import com.example.repository.BorrowingRecordRepository;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @InjectMocks
    private BorrowService borrowService;

    @Test
    @DisplayName("借書成功：可借閱庫存應產生借閱紀錄")
    void shouldBorrowBookSuccessfully() {
        // Given
        BorrowRequest request = new BorrowRequest();
        request.setInventoryId(1001L);
        when(borrowingRecordRepository.borrowBook(1, 1001L)).thenReturn(9001L);

        // When
        BorrowResponse response = borrowService.borrowBook(1, request);

        // Then
        assertEquals(9001L, response.getBorrowingRecordId());
        assertEquals("Borrow success", response.getMessage());
    }

    @Test
    @DisplayName("借書失敗：不可借閱狀態時應拋出業務例外")
    void shouldThrowWhenBorrowUnavailableInventory() {
        // Given
        BorrowRequest request = new BorrowRequest();
        request.setInventoryId(1001L);
        when(borrowingRecordRepository.borrowBook(1, 1001L)).thenThrow(new RuntimeException("Inventory not available"));

        // When
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> borrowService.borrowBook(1, request));

        // Then
        assertEquals("Unable to borrow this inventory", ex.getMessage());
    }

    @Test
    @DisplayName("還書成功：存在有效借閱紀錄時可完成歸還")
    void shouldReturnBookSuccessfully() {
        // Given
        ReturnRequest request = new ReturnRequest();
        request.setInventoryId(1001L);
        Map<String, Object> activeRecord = new HashMap<>();
        activeRecord.put("InventoryId", 1001L);
        activeRecord.put("ReturnTime", null);
        when(borrowingRecordRepository.getBorrowRecords(1)).thenReturn(List.of(activeRecord));

        // When
        ReturnResponse response = borrowService.returnBook(1, request);

        // Then
        verify(borrowingRecordRepository).returnBook(1, 1001L);
        assertEquals("Return success", response.getMessage());
    }

    @Test
    @DisplayName("還書失敗：非本人借閱時應拋出業務例外")
    void shouldThrowWhenReturnBookNotBorrowedByUser() {
        // Given
        ReturnRequest request = new ReturnRequest();
        request.setInventoryId(1001L);
        Map<String, Object> anotherRecord = new HashMap<>();
        anotherRecord.put("InventoryId", 2002L);
        anotherRecord.put("ReturnTime", null);
        when(borrowingRecordRepository.getBorrowRecords(1)).thenReturn(List.of(anotherRecord));

        // When
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> borrowService.returnBook(1, request));

        // Then
        assertEquals("No active borrowing record for this user and inventory", ex.getMessage());
    }

    @Test
    @DisplayName("還書失敗：已歸還紀錄不可重複歸還")
    void shouldThrowWhenReturnAlreadyReturnedBook() {
        // Given
        ReturnRequest request = new ReturnRequest();
        request.setInventoryId(1001L);
        when(borrowingRecordRepository.getBorrowRecords(1)).thenReturn(List.of(
            Map.of(
                "InventoryId", 1001L,
                "ReturnTime", LocalDateTime.now()
            )
        ));

        // When
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> borrowService.returnBook(1, request));

        // Then
        assertEquals("No active borrowing record for this user and inventory", ex.getMessage());
    }
}

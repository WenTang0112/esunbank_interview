package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.RegisterRequest;
import com.example.dto.RegisterResponse;
import com.example.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<String> phoneCaptor;

    @Captor
    private ArgumentCaptor<String> hashCaptor;

    @Captor
    private ArgumentCaptor<String> saltCaptor;

    @Captor
    private ArgumentCaptor<String> userNameCaptor;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setPhoneNumber("0912345678");
        validRequest.setPassword("Password123");
        validRequest.setUserName("Alice");
    }

    @Test
    @DisplayName("註冊成功：合法資料可建立帳號並回傳使用者編號")
    void shouldRegisterSuccessfully() {
        // Given
        when(userRepository.checkPhoneExists("0912345678")).thenReturn(false);
        when(userRepository.registerUser(anyString(), anyString(), anyString(), anyString())).thenReturn(10);

        // When
        RegisterResponse response = userService.register(validRequest);

        // Then
        assertEquals(10, response.getUserId());
        assertEquals("Register success", response.getMessage());
        verify(userRepository).registerUser(phoneCaptor.capture(), hashCaptor.capture(), saltCaptor.capture(), userNameCaptor.capture());
        assertEquals("0912345678", phoneCaptor.getValue());
        assertEquals("Alice", userNameCaptor.getValue());
        assertFalse(hashCaptor.getValue().isBlank());
        assertFalse(saltCaptor.getValue().isBlank());
    }

    @Test
    @DisplayName("註冊失敗：手機已註冊時應拋出業務例外")
    void shouldThrowWhenPhoneAlreadyExists() {
        // Given
        when(userRepository.checkPhoneExists("0912345678")).thenReturn(true);

        // When
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> userService.register(validRequest));

        // Then
        assertEquals("Phone number is already registered", ex.getMessage());
    }

    @Test
    @DisplayName("註冊失敗：手機格式錯誤時應拋出參數例外")
    void shouldThrowWhenPhoneFormatInvalid() {
        // Given
        validRequest.setPhoneNumber("12345");

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.register(validRequest));

        // Then
        assertEquals("Phone number format is invalid", ex.getMessage());
    }

    @Test
    @DisplayName("安全性：儲存密碼必須是雜湊值，不能是明碼")
    void shouldStoreHashedPasswordNotRawPassword() {
        // Given
        when(userRepository.checkPhoneExists("0912345678")).thenReturn(false);
        when(userRepository.registerUser(anyString(), anyString(), anyString(), anyString())).thenReturn(11);

        // When
        userService.register(validRequest);

        // Then
        verify(userRepository).registerUser(phoneCaptor.capture(), hashCaptor.capture(), saltCaptor.capture(), userNameCaptor.capture());
        assertTrue(validRequest.getPassword().length() > 0);
        assertFalse(validRequest.getPassword().equals(hashCaptor.getValue()));
    }
}

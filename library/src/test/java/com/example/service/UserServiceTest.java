package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

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
import com.example.security.AuthTokenUtil;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenUtil authTokenUtil;

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
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setPhoneNumber("0912345678");
        validRequest.setPassword("Password123");
        validRequest.setUserName("Alice");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setPhoneNumber("0912345678");
        validLoginRequest.setPassword("Password123");
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

    @Test
    @DisplayName("登入成功：正確帳密可取得 Token 並更新最後登入時間")
    void shouldLoginSuccessfully() {
        // Given
        String salt = "somesalt";
        String hash = com.example.util.PasswordUtil.hashPassword("Password123", salt);
        Map<String, Object> userRow = Map.of(
            "UserId", 1,
            "PhoneNumber", "0912345678",
            "PasswordHash", hash,
            "PasswordSalt", salt,
            "IsActive", true
        );
        when(userRepository.findLoginUserByPhone("0912345678")).thenReturn(java.util.Optional.of(userRow));
        when(authTokenUtil.generateToken(1, "0912345678")).thenReturn("token-abc");

        // When
        LoginResponse response = userService.login(validLoginRequest);

        // Then
        assertEquals(1, response.getUserId());
        assertEquals("0912345678", response.getPhoneNumber());
        assertEquals("token-abc", response.getToken());
        assertEquals("Login success", response.getMessage());
        verify(userRepository).updateLastLogin(1);
    }

    @Test
    @DisplayName("登入失敗：密碼錯誤時應回傳未授權")
    void shouldThrowWhenPasswordIncorrect() {
        // Given
        String salt = "somesalt";
        String wrongHash = com.example.util.PasswordUtil.hashPassword("WrongPassword", salt);
        Map<String, Object> userRow = Map.of(
            "UserId", 1,
            "PhoneNumber", "0912345678",
            "PasswordHash", wrongHash,
            "PasswordSalt", salt,
            "IsActive", true
        );
        when(userRepository.findLoginUserByPhone("0912345678")).thenReturn(java.util.Optional.of(userRow));

        // When
        SecurityException ex = assertThrows(SecurityException.class, () -> userService.login(validLoginRequest));

        // Then
        assertEquals("Invalid phone number or password", ex.getMessage());
    }

    @Test
    @DisplayName("登入失敗：手機不存在時應回傳未授權")
    void shouldThrowWhenPhoneNotFound() {
        // Given
        when(userRepository.findLoginUserByPhone("0912345678")).thenReturn(java.util.Optional.empty());

        // When
        SecurityException ex = assertThrows(SecurityException.class, () -> userService.login(validLoginRequest));

        // Then
        assertEquals("Invalid phone number or password", ex.getMessage());
    }
}

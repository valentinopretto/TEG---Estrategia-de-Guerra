package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.auth.Credential;
import ar.edu.utn.frc.tup.piii.dtos.auth.EmailIdentity;
import ar.edu.utn.frc.tup.piii.dtos.auth.IdentityType;
import ar.edu.utn.frc.tup.piii.dtos.auth.UsernameIdentity;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.exceptions.EmailAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exceptions.UserAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exceptions.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegisterDto registerDto;
    private Credential usernameCredential;
    private Credential emailCredential;
    private User user;

    @BeforeEach
    void setUp() {
        registerDto = new UserRegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("Password123!");
        registerDto.setEmail("test@email.com");
        registerDto.setAvatarUrl("http://example.com/avatar.jpg");

        // Username credential
        UsernameIdentity usernameIdentity = new UsernameIdentity();
        usernameIdentity.setType(IdentityType.USERNAME);
        usernameIdentity.setUserName("testuser");

        usernameCredential = new Credential();
        usernameCredential.setIdentity(usernameIdentity);
        usernameCredential.setPassword("Password123!");

        // Email credential
        EmailIdentity emailIdentity = new EmailIdentity();
        emailIdentity.setType(IdentityType.EMAIL);
        emailIdentity.setEmail("test@email.com");

        emailCredential = new Credential();
        emailCredential.setIdentity(emailIdentity);
        emailCredential.setPassword("Password123!");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .passwordHash("$2a$10$hashedPassword")
                .lastLogin(LocalDateTime.now())
                .build();
    }

    @Test
    void register_WithValidData_ShouldReturnUser() {
        // Given
        when(userService.existsByUserName(registerDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(encoder.encode(registerDto.getPassword())).thenReturn("$2a$10$hashedPassword");
        doNothing().when(userService).save(any(User.class));

        // When
        User result = authService.register(registerDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(registerDto.getUsername());
        assertThat(result.getEmail()).isEqualTo(registerDto.getEmail());
        assertThat(result.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
        verify(encoder).encode(registerDto.getPassword());
        verify(userService).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Given
        when(userService.existsByUserName(registerDto.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already exists: testuser");

        verify(encoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userService.existsByUserName(registerDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(registerDto.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerDto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already registered: test@email.com");

        verify(encoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void login_WithValidUsernameCredentials_ShouldReturnUser() {
        // Given
        when(userService.getUserByUserName("testuser")).thenReturn(user);
        when(encoder.matches("Password123!", user.getPasswordHash())).thenReturn(true);
        doNothing().when(userService).save(any(User.class));

        // When
        User result = authService.login(usernameCredential);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getLastLogin()).isNotNull();
        assertThat(result.getLastLogin()).isAfter(user.getLastLogin()); // Verificar que se actualizó
        verify(encoder).matches("Password123!", user.getPasswordHash());
        verify(userService).save(any(User.class));
    }

    @Test
    void login_WithValidEmailCredentials_ShouldReturnUser() {
        // Given
        when(userService.getUserByEmailAndPasswordHash("test@email.com", "Password123!")).thenReturn(user);
        when(encoder.matches("Password123!", user.getPasswordHash())).thenReturn(true);
        doNothing().when(userService).save(any(User.class));

        // When
        User result = authService.login(emailCredential);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getLastLogin()).isNotNull();
        assertThat(result.getLastLogin()).isAfter(user.getLastLogin()); // Verificar que se actualizó
        verify(encoder).matches("Password123!", user.getPasswordHash());
        verify(userService).save(any(User.class));
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userService.getUserByUserName("testuser")).thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        assertThatThrownBy(() -> authService.login(usernameCredential))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(encoder, never()).matches(anyString(), anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Given
        when(userService.getUserByUserName("testuser")).thenReturn(user);
        when(encoder.matches("Password123!", user.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(usernameCredential))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid user or password");

        verify(encoder).matches("Password123!", user.getPasswordHash());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void register_ShouldSetUserFieldsCorrectly() {
        // Given
        when(userService.existsByUserName(registerDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(encoder.encode(registerDto.getPassword())).thenReturn("$2a$10$hashedPassword");
        doNothing().when(userService).save(any(User.class));

        // When
        User result = authService.register(registerDto);

        // Then
        assertThat(result.getUsername()).isEqualTo(registerDto.getUsername());
        assertThat(result.getEmail()).isEqualTo(registerDto.getEmail());
        assertThat(result.getAvatarUrl()).isEqualTo(registerDto.getAvatarUrl());
        assertThat(result.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
        verify(userService).save(argThat(user ->
                user.getUsername().equals(registerDto.getUsername()) &&
                        user.getEmail().equals(registerDto.getEmail()) &&
                        user.getAvatarUrl().equals(registerDto.getAvatarUrl()) &&
                        user.getPasswordHash().equals("$2a$10$hashedPassword")
        ));
    }

    @Test
    void register_WithNullAvatarUrl_ShouldNotThrowException() {
        // Given
        registerDto.setAvatarUrl(null);
        when(userService.existsByUserName(registerDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(encoder.encode(registerDto.getPassword())).thenReturn("$2a$10$hashedPassword");
        doNothing().when(userService).save(any(User.class));

        // When
        User result = authService.register(registerDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvatarUrl()).isNull();
    }
}
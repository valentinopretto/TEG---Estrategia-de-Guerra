package ar.edu.utn.frc.tup.piii.controllers;

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
import ar.edu.utn.frc.tup.piii.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegisterDto registerDto;
    private Credential usernameCredential;
    private Credential emailCredential;
    private User mockUser;

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

        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .passwordHash("hashedPassword")
                .lastLogin(LocalDateTime.now())
                .build();
    }

    @Test
    void register_WithValidData_ShouldReturnUser() throws Exception {
        when(authService.register(any(UserRegisterDto.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void register_WithInvalidDataUsername_ShouldReturnBadRequest() throws Exception {
        registerDto.setUsername(""); // Invalid username

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithInvalidDataPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShortPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("1234");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithoutUpperCasePassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("password1#");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithoutNumberPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("Password#");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithoutEspecialCharPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("Password1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithExistingUser_ShouldReturnConflict() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void register_WithExistingEmail_ShouldReturnConflict() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void register_WithGenericRuntimeException_ShouldReturnBadRequest() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new RuntimeException("Generic error occurred"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Generic error occurred"));
    }

    @Test
    void register_WithInternalError_ShouldReturnInternalServerError() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new Error("System error"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_WithValidUsernameCredentials_ShouldReturnUser() throws Exception {
        when(authService.login(any(Credential.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernameCredential)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void login_WithValidEmailCredentials_ShouldReturnUser() throws Exception {
        when(authService.login(any(Credential.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailCredential)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception{
        when(authService.login(any(Credential.class)))
                .thenThrow(new InvalidCredentialsException("Invalid Password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernameCredential)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        when(authService.login(any(Credential.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernameCredential)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void login_WithNullCredential_ShouldReturnBadRequest() throws Exception {
        // Opción 1: JSON malformado que no puede ser parseado
        String malformedJson = "{\"identity\":,\"password\":null}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
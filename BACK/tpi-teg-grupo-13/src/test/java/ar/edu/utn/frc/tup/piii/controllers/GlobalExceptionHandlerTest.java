package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void handleUserAlreadyExists_ShouldReturn409() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void handleEmailAlreadyExists_ShouldReturn409() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void handleUserNotFound_ShouldReturn404() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void handleInvalidCredentials_ShouldReturn401() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void handleRuntimeException_ShouldReturn400() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new RuntimeException("Generic runtime error"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void handleGameNotFound_ShouldReturn404() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void handleGenericException_ShouldReturn500() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new Error("System error")); // Error en lugar de Exception

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
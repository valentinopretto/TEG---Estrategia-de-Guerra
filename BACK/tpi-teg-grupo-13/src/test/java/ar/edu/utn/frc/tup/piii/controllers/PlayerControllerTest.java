package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.player.PlayerRequestDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private UserService userService;

    @MockBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    private Player humanPlayer;
    private Player botPlayer;
    private User testUser;
    private Game testGame;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testGame = Game.builder()
                .id(1L)
                .gameCode("GAME123")
                .maxPlayers(6)
                .build();

        humanPlayer = Player.builder()
                .id(1L)
                .username("testuser")
                .displayName("Test User")
                .isBot(false)
                .status(PlayerStatus.ACTIVE)
                .color(PlayerColor.RED)
                .seatOrder(1)
                .joinedAt(LocalDateTime.now())
                .build();

        botPlayer = Player.builder()
                .id(2L)
                .username(null)
                .displayName("Bot Player")
                .isBot(true)
                .botLevel(BotLevel.BALANCED)
                .status(PlayerStatus.ACTIVE)
                .color(PlayerColor.BLUE)
                .seatOrder(2)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllPlayers_Success() throws Exception {
        List<Player> players = Arrays.asList(humanPlayer, botPlayer);
        when(playerService.findAll()).thenReturn(players);

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].isBot").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].isBot").value(true));
    }

    @Test
    void getAllPlayers_Exception() throws Exception {
        when(playerService.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getPlayerById_Success() throws Exception {
        when(playerService.findById(1L)).thenReturn(Optional.of(humanPlayer));

        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.isBot").value(false));
    }

    @Test
    void getPlayerById_NotFound() throws Exception {
        when(playerService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/players/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPlayerById_Exception() throws Exception {
        when(playerService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createHumanPlayer_Success() throws Exception {
        PlayerRequestDto requestDto = new PlayerRequestDto();
        requestDto.setUserId(1L);
        requestDto.setGameId(1L);
        requestDto.setIsBot(false);
        requestDto.setSeatOrder(1);

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(gameService.findById(1L)).thenReturn(testGame);
        when(playerService.createHumanPlayer(eq(testUser), eq(testGame), eq(1))).thenReturn(humanPlayer);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.isBot").value(false));
    }

    @Test
    void createBotPlayer_Success() throws Exception {
        PlayerRequestDto requestDto = new PlayerRequestDto();
        requestDto.setGameId(1L);
        requestDto.setIsBot(true);
        requestDto.setBotLevel("BALANCED");

        when(gameService.findById(1L)).thenReturn(testGame);
        when(playerService.createBotPlayer(eq(BotLevel.BALANCED), eq(testGame))).thenReturn(botPlayer);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.isBot").value(true))
                .andExpect(jsonPath("$.botLevel").value("BALANCED"));
    }

    @Test
    void createPlayer_UserServiceException() throws Exception {
        PlayerRequestDto requestDto = new PlayerRequestDto();
        requestDto.setUserId(1L);
        requestDto.setGameId(1L);
        requestDto.setIsBot(false);

        when(userService.getUserById(1L)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlayer_GameServiceException() throws Exception {
        PlayerRequestDto requestDto = new PlayerRequestDto();
        requestDto.setUserId(1L);
        requestDto.setGameId(1L);
        requestDto.setIsBot(false);

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(gameService.findById(1L)).thenThrow(new RuntimeException("Game not found"));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlayer_InvalidBotLevel() throws Exception {
        PlayerRequestDto requestDto = new PlayerRequestDto();
        requestDto.setGameId(1L);
        requestDto.setIsBot(true);
        requestDto.setBotLevel("INVALID_LEVEL");

        when(gameService.findById(1L)).thenReturn(testGame);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlayer_PlayerServiceException() throws Exception {
        PlayerRequestDto requestDto = new PlayerRequestDto();
        requestDto.setUserId(1L);
        requestDto.setGameId(1L);
        requestDto.setIsBot(false);
        requestDto.setSeatOrder(1);

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(gameService.findById(1L)).thenReturn(testGame);
        when(playerService.createHumanPlayer(eq(testUser), eq(testGame), eq(1)))
                .thenThrow(new IllegalArgumentException("Player already exists"));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePlayer_Success() throws Exception {
        mockMvc.perform(delete("/api/players/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePlayer_Exception() throws Exception {
        // El método deleteById no retorna nada, pero puede lanzar excepción
        // Necesitamos usar doThrow en lugar de when().thenThrow()
        doThrow(new RuntimeException("Cannot delete")).when(playerService).deleteById(1L);

        mockMvc.perform(delete("/api/players/1"))
                .andExpect(status().isBadRequest());
    }
}
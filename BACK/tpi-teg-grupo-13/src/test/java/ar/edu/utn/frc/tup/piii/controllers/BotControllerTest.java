package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.service.interfaces.BotService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BotController.class)
class BotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BotService botService;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private GameStateService gameStateService;

    @MockBean
    private GameMapper gameMapper;

    @MockBean
    private PlayerMapper playerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Game testGame;
    private Player botPlayer;
    private Player humanPlayer;
    private PlayerEntity botPlayerEntity;
    private GameEntity gameEntity;
    private BotProfileEntity botProfile;
    private GameResponseDto gameResponseDto;

    @BeforeEach
    void setUp() {
        // Setup bot profile
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setBotName("TestBot");
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);

        // Setup bot player entity
        botPlayerEntity = new PlayerEntity();
        botPlayerEntity.setId(1L);
        botPlayerEntity.setBotProfile(botProfile);
        botPlayerEntity.setStatus(PlayerStatus.ACTIVE);
        botPlayerEntity.setArmiesToPlace(5);
        botPlayerEntity.setTerritories(List.of(new GameTerritoryEntity()));

        // Setup bot player model
        botPlayer = new Player();
        botPlayer.setId(1L);
        botPlayer.setIsBot(true);
        botPlayer.setDisplayName("TestBot");
        botPlayer.setBotLevel(BotLevel.BALANCED);
        botPlayer.setStatus(PlayerStatus.ACTIVE);
        botPlayer.setArmiesToPlace(5);
        botPlayer.setTerritoryIds(List.of(1L, 2L, 3L));

        // Setup human player
        humanPlayer = new Player();
        humanPlayer.setId(2L);
        humanPlayer.setIsBot(false);
        humanPlayer.setDisplayName("Human Player");

        // Setup game
        testGame = new Game();
        testGame.setGameCode("TEST-GAME");
        testGame.setState(GameState.NORMAL_PLAY);
        testGame.setCurrentPlayerIndex(0);
        testGame.setPlayers(Arrays.asList(botPlayer, humanPlayer));

        // Setup game entity
        gameEntity = new GameEntity();
        gameEntity.setGameCode("TEST-GAME");

        // Setup response DTO
        gameResponseDto = new GameResponseDto();
        gameResponseDto.setGameCode("TEST-GAME");
        gameResponseDto.setState(GameState.NORMAL_PLAY);
    }

    // ===============================
    // TESTS PARA ENDPOINTS DE PRUEBA
    // ===============================

    @Test
    void testEndpoint_Success() throws Exception {
        mockMvc.perform(get("/api/bots/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bot Controller funcionando correctamente"));
    }

    @Test
    void testEndpointWithParams_Success() throws Exception {
        mockMvc.perform(get("/api/bots/test/TEST-GAME/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Parámetros recibidos: TEST-GAME, 1"));
    }

    // ===============================
    // TESTS PARA executeBotTurn
    // ===============================

    @Test
    void testExecuteBotTurn_Success() throws Exception {
        // IMPORTANTE: Usar el método correcto del servicio
        when(botService.executeBotTurnComplete("TEST-GAME", 1L))
                .thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/bots/games/TEST-GAME/1/execute-turn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gameCode").value("TEST-GAME"));

        verify(botService).executeBotTurnComplete("TEST-GAME", 1L);
    }

    @Test
    void testExecuteBotTurn_InvalidGameCode() throws Exception {
        // Test con un gameCode que el controller considere inválido
        // Asumiendo que el controller valida gameCode vacío o "null"
        when(botService.executeBotTurnComplete(eq(""), any()))
                .thenThrow(new IllegalArgumentException("GameCode inválido"));

        mockMvc.perform(post("/api/bots/games//1/execute-turn") // URL con gameCode vacío
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 porque la URL no matchea
    }

    @Test
    void testExecuteBotTurn_EmptyGameCode() throws Exception {
        mockMvc.perform(post("/api/bots/games/ /1/execute-turn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("GameCode inválido"));

        verify(botService, never()).executeBotTurnComplete(any(), any());
    }

    @Test
    void testExecuteBotTurn_IllegalArgumentException() throws Exception {
        when(botService.executeBotTurnComplete("TEST-GAME", 1L))
                .thenThrow(new IllegalArgumentException("Bot no encontrado"));

        mockMvc.perform(post("/api/bots/games/TEST-GAME/1/execute-turn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bot no encontrado"));

        verify(botService).executeBotTurnComplete("TEST-GAME", 1L);
    }

    @Test
    void testExecuteBotTurn_IllegalStateException() throws Exception {
        when(botService.executeBotTurnComplete("TEST-GAME", 1L))
                .thenThrow(new IllegalStateException("No es el turno del bot"));

        mockMvc.perform(post("/api/bots/games/TEST-GAME/1/execute-turn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No es el turno del bot"));

        verify(botService).executeBotTurnComplete("TEST-GAME", 1L);
    }

    @Test
    void testExecuteBotTurn_UnexpectedException() throws Exception {
        when(botService.executeBotTurnComplete("TEST-GAME", 1L))
                .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/api/bots/games/TEST-GAME/1/execute-turn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ocurrió un error inesperado"));

        verify(botService).executeBotTurnComplete("TEST-GAME", 1L);
    }

    // ===============================
    // TESTS PARA getBotStatus
    // ===============================

    @Test
    void testGetBotStatus_Success() throws Exception {
        when(playerService.findById(1L)).thenReturn(Optional.of(botPlayer));
        when(playerMapper.toEntity(botPlayer)).thenReturn(botPlayerEntity);
        when(gameService.findByGameCode("TEST-GAME")).thenReturn(testGame);
        when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(testGame, "bot_turn")).thenReturn(true);

        mockMvc.perform(get("/api/bots/games/TEST-GAME/1/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.botId").value(1))
                .andExpect(jsonPath("$.botName").value("TestBot"))
                .andExpect(jsonPath("$.level").value("BALANCED"))
                .andExpect(jsonPath("$.strategy").value("AGGRESSIVE"))
                .andExpect(jsonPath("$.isCurrentTurn").value(true))
                .andExpect(jsonPath("$.territoriesCount").value(1))
                .andExpect(jsonPath("$.armiesCount").value(5))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.canExecuteTurn").value(true));
    }

    @Test
    void testGetBotStatus_BotNotFound() throws Exception {
        when(playerService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bots/games/TEST-GAME/1/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBotStatus_PlayerIsNotBot() throws Exception {
        when(playerService.findById(2L)).thenReturn(Optional.of(humanPlayer));

        mockMvc.perform(get("/api/bots/games/TEST-GAME/2/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBotStatus_ServiceException() throws Exception {
        when(playerService.findById(1L)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/bots/games/TEST-GAME/1/status"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // TESTS PARA getGameBots
    // ===============================

    @Test
    void testGetGameBots_Success() throws Exception {
        when(gameService.findByGameCode("TEST-GAME")).thenReturn(testGame);
        when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(testGame, "bot_turn")).thenReturn(true);

        mockMvc.perform(get("/api/bots/games/TEST-GAME"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].botId").value(1))
                .andExpect(jsonPath("$[0].botName").value("TestBot"))
                .andExpect(jsonPath("$[0].level").value("BALANCED"));
    }

    @Test
    void testGetGameBots_EmptyList() throws Exception {
        Game gameWithoutBots = new Game();
        gameWithoutBots.setGameCode("TEST-GAME");
        gameWithoutBots.setPlayers(Arrays.asList(humanPlayer));

        when(gameService.findByGameCode("TEST-GAME")).thenReturn(gameWithoutBots);

        mockMvc.perform(get("/api/bots/games/TEST-GAME"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetGameBots_ServiceException() throws Exception {
        when(gameService.findByGameCode("TEST-GAME")).thenThrow(new RuntimeException("Game not found"));

        mockMvc.perform(get("/api/bots/games/TEST-GAME"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // TESTS PARA getBotsByLevel
    // ===============================

    @Test
    void testGetBotsByLevel_Success() throws Exception {
        List<BotProfileEntity> botProfiles = Arrays.asList(botProfile);
        when(botService.findByLevel(BotLevel.BALANCED)).thenReturn(botProfiles);

        mockMvc.perform(get("/api/bots/profiles")
                        .param("level", "BALANCED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].botName").value("TestBot"))
                .andExpect(jsonPath("$[0].level").value("BALANCED"));
    }

    @Test
    void testGetBotsByLevel_EmptyResult() throws Exception {
        when(botService.findByLevel(BotLevel.NOVICE)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/bots/profiles")
                        .param("level", "NOVICE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetBotsByLevel_ServiceException() throws Exception {
        when(botService.findByLevel(any())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/bots/profiles")
                        .param("level", "BALANCED"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // TESTS PARA getAllBotProfiles
    // ===============================

    @Test
    void testGetAllBotProfiles_Success() throws Exception {
        BotProfileEntity botProfile2 = new BotProfileEntity();
        botProfile2.setId(2L);
        botProfile2.setBotName("TestBot2");
        botProfile2.setLevel(BotLevel.EXPERT);

        List<BotProfileEntity> botProfiles = Arrays.asList(botProfile, botProfile2);
        when(botService.findAll()).thenReturn(botProfiles);

        mockMvc.perform(get("/api/bots/profiles/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void testGetAllBotProfiles_EmptyResult() throws Exception {
        when(botService.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/bots/profiles/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetAllBotProfiles_ServiceException() throws Exception {
        when(botService.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/bots/profiles/all"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // TESTS PARA getBotProfile
    // ===============================

    @Test
    void testGetBotProfile_Success() throws Exception {
        when(botService.findById(1L)).thenReturn(Optional.of(botProfile));

        mockMvc.perform(get("/api/bots/profiles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.botName").value("TestBot"))
                .andExpect(jsonPath("$.level").value("BALANCED"))
                .andExpect(jsonPath("$.strategy").value("AGGRESSIVE"));
    }

    @Test
    void testGetBotProfile_NotFound() throws Exception {
        when(botService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bots/profiles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBotProfile_ServiceException() throws Exception {
        when(botService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/bots/profiles/1"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // TESTS ADICIONALES PARA 100% COVERAGE
    // ===============================

    @Test
    void testGetBotStatus_NotCurrentTurn() throws Exception {
        when(playerService.findById(1L)).thenReturn(Optional.of(botPlayer));
        when(playerMapper.toEntity(botPlayer)).thenReturn(botPlayerEntity);
        when(gameService.findByGameCode("TEST-GAME")).thenReturn(testGame);
        when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(false);
        when(gameStateService.canPerformAction(testGame, "bot_turn")).thenReturn(false);

        mockMvc.perform(get("/api/bots/games/TEST-GAME/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCurrentTurn").value(false))
                .andExpect(jsonPath("$.canExecuteTurn").value(false));
    }

    @Test
    void testGetGameBots_MultipleBotsWithDifferentStates() throws Exception {
        // Setup second bot
        Player botPlayer2 = new Player();
        botPlayer2.setId(3L);
        botPlayer2.setIsBot(true);
        botPlayer2.setDisplayName("TestBot2");
        botPlayer2.setBotLevel(BotLevel.EXPERT);
        botPlayer2.setStatus(PlayerStatus.ELIMINATED);

        testGame.setPlayers(Arrays.asList(botPlayer, humanPlayer, botPlayer2));

        when(gameService.findByGameCode("TEST-GAME")).thenReturn(testGame);
        when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(testGame, "bot_turn")).thenReturn(true);
        when(gameStateService.isPlayerTurn(testGame, 3L)).thenReturn(false);

        mockMvc.perform(get("/api/bots/games/TEST-GAME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].isActive").value(false));
    }

    // ===============================
    // TESTS PARA CUBRIR LOGS Y BRANCHES
    // ===============================

    @Test
    void testExecuteBotTurn_InvalidBotId() throws Exception {
        // Test con un botId que cause problemas de conversión
        // Spring Boot lanzará NumberFormatException al convertir "abc" a Long
        mockMvc.perform(post("/api/bots/games/TEST-GAME/abc/execute-turn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // 400 por error de conversión de tipo

        verify(botService, never()).executeBotTurnComplete(any(), any());
    }

    @Test
    void testExecuteBotTurn_ValidationSteps() throws Exception {
        // Test que cubra todos los System.out.println del controller
        when(botService.executeBotTurnComplete("TEST-GAME", 1L))
                .thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/bots/games/TEST-GAME/1/execute-turn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(botService).executeBotTurnComplete("TEST-GAME", 1L);
    }
}
package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementRequestDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.ReinforcementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class ReinforcementControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReinforcementService reinforcementService;

    @Mock
    private GameService gameService;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private ReinforcementController reinforcementController;

    private static final String GAME_CODE = "ABC123";
    private static final Long PLAYER_ID = 1L;
    private static final String BASE_URL = "/api/games/{gameCode}/reinforcement";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reinforcementController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should place reinforcement armies successfully")
    void placeReinforcementArmies_Success() throws Exception {
        // Arrange
        Map<Long, Integer> armiesByCountry = new HashMap<>();
        armiesByCountry.put(1L, 2);
        armiesByCountry.put(2L, 3);

        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(armiesByCountry)
                .build();

        Game game = Game.builder()
                .id(1L)
                .gameCode(GAME_CODE)
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.ATTACK)
                .build();

        GameResponseDto responseDto = GameResponseDto.builder()
                .gameCode(GAME_CODE)
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.ATTACK)
                .build();

        doNothing().when(reinforcementService).placeReinforcementArmies(
                eq(GAME_CODE), eq(PLAYER_ID), eq(armiesByCountry));
        when(gameService.findByGameCode(GAME_CODE)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value(GAME_CODE))
                .andExpect(jsonPath("$.state").value("NORMAL_PLAY"))
                .andExpect(jsonPath("$.currentPhase").value("ATTACK"));

        verify(reinforcementService).placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry);
        verify(gameService).findByGameCode(GAME_CODE);
        verify(gameMapper).toResponseDto(game);
    }

    @Test
    @DisplayName("Should return bad request when player ID is null")
    void placeReinforcementArmies_NullPlayerId() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(null)
                .armiesByCountry(Map.of(1L, 2))
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reinforcementService);
    }

    @Test
    @DisplayName("Should return bad request when armies map is empty")
    void placeReinforcementArmies_EmptyArmiesMap() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(new HashMap<>())
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reinforcementService);
    }

    @Test
    @DisplayName("Should return not found when game doesn't exist")
    void placeReinforcementArmies_GameNotFound() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(Map.of(1L, 2))
                .build();

        doThrow(new GameNotFoundException("Game not found"))
                .when(reinforcementService).placeReinforcementArmies(anyString(), anyLong(), any());

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when player doesn't exist")
    void placeReinforcementArmies_PlayerNotFound() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(Map.of(1L, 2))
                .build();

        doThrow(new PlayerNotFoundException("Player not found"))
                .when(reinforcementService).placeReinforcementArmies(anyString(), anyLong(), any());

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return forbidden when game state is invalid")
    void placeReinforcementArmies_InvalidGameState() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(Map.of(1L, 2))
                .build();

        doThrow(new InvalidGameStateException("Invalid game state"))
                .when(reinforcementService).placeReinforcementArmies(anyString(), anyLong(), any());

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get reinforcement status successfully")
    void getReinforcementStatus_Success() throws Exception {
        // Arrange
        ReinforcementStatusDto statusDto = ReinforcementStatusDto.builder()
                .playerId(PLAYER_ID)
                .playerName("Player 1")
                .gameState(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.REINFORCEMENT)
                .armiesToPlace(5)
                .baseArmies(3)
                .continentBonus(2)
                .cardBonus(0)
                .totalArmies(5)
                .isPlayerTurn(true)
                .canReinforce(true)
                .message("Place your reinforcement armies on your territories")
                .controlledContinents(List.of("South America"))
                .build();

        when(reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID))
                .thenReturn(statusDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/status/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(PLAYER_ID))
                .andExpect(jsonPath("$.playerName").value("Player 1"))
                .andExpect(jsonPath("$.armiesToPlace").value(5))
                .andExpect(jsonPath("$.baseArmies").value(3))
                .andExpect(jsonPath("$.continentBonus").value(2))
                .andExpect(jsonPath("$.isPlayerTurn").value(true))
                .andExpect(jsonPath("$.canReinforce").value(true))
                .andExpect(jsonPath("$.controlledContinents[0]").value("South America"));

        verify(reinforcementService).getReinforcementStatus(GAME_CODE, PLAYER_ID);
    }

    @Test
    @DisplayName("Should return not found when getting status for non-existent game")
    void getReinforcementStatus_GameNotFound() throws Exception {
        // Arrange
        when(reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID))
                .thenThrow(new GameNotFoundException("Game not found"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/status/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should calculate reinforcements successfully")
    void calculateReinforcements_Success() throws Exception {
        // Arrange
        Game game = Game.builder()
                .id(1L)
                .gameCode(GAME_CODE)
                .build();

        ReinforcementStatusDto statusDto = ReinforcementStatusDto.builder()
                .playerId(PLAYER_ID)
                .playerName("Player 1")
                .baseArmies(3)
                .continentBonus(2)
                .cardBonus(0)
                .ownedTerritories(List.of())
                .controlledContinents(List.of("South America"))
                .build();

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(game);
        when(reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID))
                .thenReturn(statusDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/calculate/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(PLAYER_ID))
                .andExpect(jsonPath("$.playerName").value("Player 1"))
                .andExpect(jsonPath("$.baseArmies").value(3))
                .andExpect(jsonPath("$.continentBonus").value(2))
                .andExpect(jsonPath("$.totalArmies").value(5))
                .andExpect(jsonPath("$.controlledContinents[0]").value("South America"));

        verify(gameService).findByGameCode(GAME_CODE);
        verify(reinforcementService).getReinforcementStatus(GAME_CODE, PLAYER_ID);
    }

    @Test
    @DisplayName("Should handle internal server error gracefully")
    void placeReinforcementArmies_InternalServerError() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(Map.of(1L, 2))
                .build();

        doThrow(new RuntimeException("Unexpected error"))
                .when(reinforcementService).placeReinforcementArmies(anyString(), anyLong(), any());

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return bad request when placing armies throws IllegalArgumentException")
    void placeReinforcementArmies_IllegalArgument() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(Map.of(1L, 2))
                .build();

        doThrow(new IllegalArgumentException("Invalid placement: not enough armies"))
                .when(reinforcementService).placeReinforcementArmies(anyString(), anyLong(), any());

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(reinforcementService).placeReinforcementArmies(GAME_CODE, PLAYER_ID, requestDto.getArmiesByCountry());
    }

    @Test
    @DisplayName("Should handle null armies map in request")
    void placeReinforcementArmies_NullArmiesMap() throws Exception {
        // Arrange
        ReinforcementRequestDto requestDto = ReinforcementRequestDto.builder()
                .playerId(PLAYER_ID)
                .armiesByCountry(null)
                .build();

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reinforcementService);
        verifyNoInteractions(gameService);
        verifyNoInteractions(gameMapper);
    }

    @Test
    @DisplayName("Should handle PlayerNotFoundException when getting status")
    void getReinforcementStatus_PlayerNotFound() throws Exception {
        // Arrange
        when(reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID))
                .thenThrow(new PlayerNotFoundException("Player not found"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/status/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isNotFound());

        verify(reinforcementService).getReinforcementStatus(GAME_CODE, PLAYER_ID);
    }

    @Test
    @DisplayName("Should handle internal server error when getting status")
    void getReinforcementStatus_InternalServerError() throws Exception {
        // Arrange
        when(reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/status/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isInternalServerError());

        verify(reinforcementService).getReinforcementStatus(GAME_CODE, PLAYER_ID);
    }

    @Test
    @DisplayName("Should handle PlayerNotFoundException when calculating reinforcements")
    void calculateReinforcements_PlayerNotFound() throws Exception {
        // Arrange
        Game game = Game.builder()
                .id(1L)
                .gameCode(GAME_CODE)
                .build();

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(game);
        when(reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID))
                .thenThrow(new PlayerNotFoundException("Player not found"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/calculate/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isNotFound());

        verify(gameService).findByGameCode(GAME_CODE);
        verify(reinforcementService).getReinforcementStatus(GAME_CODE, PLAYER_ID);
    }

    @Test
    @DisplayName("Should handle internal server error when calculating reinforcements")
    void calculateReinforcements_InternalServerError() throws Exception {
        // Arrange
        when(gameService.findByGameCode(GAME_CODE))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/calculate/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isInternalServerError());

        verify(gameService).findByGameCode(GAME_CODE);
    }

    @Test
    @DisplayName("Should calculate reinforcements with empty territories list")
    void calculateReinforcements_EmptyTerritories() throws Exception {
        // Arrange
        Game game = Game.builder()
                .id(1L)
                .gameCode(GAME_CODE)
                .build();

        ReinforcementStatusDto statusDto = ReinforcementStatusDto.builder()
                .playerId(PLAYER_ID)
                .playerName("Player 1")
                .baseArmies(3)
                .continentBonus(0)
                .cardBonus(0)
                .ownedTerritories(List.of()) // Empty list
                .controlledContinents(List.of())
                .build();

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(game);
        when(reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID))
                .thenReturn(statusDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/calculate/{playerId}", GAME_CODE, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(PLAYER_ID))
                .andExpect(jsonPath("$.territoryCount").value(0))
                .andExpect(jsonPath("$.baseArmies").value(3))
                .andExpect(jsonPath("$.totalArmies").value(3));
    }

    @Test
    @DisplayName("Should return validation errors for invalid request")
    void placeReinforcementArmies_ValidationErrors() throws Exception {
        // Arrange - empty request body
        String emptyJson = "{}";

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/place-armies", GAME_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reinforcementService);
    }
}
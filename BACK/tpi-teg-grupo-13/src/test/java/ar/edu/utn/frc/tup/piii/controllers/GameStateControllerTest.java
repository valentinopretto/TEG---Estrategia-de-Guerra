package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameStateResponse;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.impl.GameStateServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameStateController.class)
class GameStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameStateServiceImpl stateService;

    @MockBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = Player.builder()
                .id(1L)
                .username("player1")
                .displayName("Player 1")
                .status(PlayerStatus.ACTIVE)
                .seatOrder(0)
                .joinedAt(LocalDateTime.now())
                .build();

        player2 = Player.builder()
                .id(2L)
                .username("player2")
                .displayName("Player 2")
                .status(PlayerStatus.ACTIVE)
                .seatOrder(1)
                .joinedAt(LocalDateTime.now())
                .build();

        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(TurnPhase.REINFORCEMENT)
                .currentTurn(1)
                .currentPlayerIndex(0)
                .maxPlayers(6)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.now())
                .players(Arrays.asList(player1, player2))
                .build();
    }

    @Test
    void startGame_WhenGameCanStart_ShouldReturnSuccessMessage() throws Exception {
        // Given
        Long gameId = 1L;
        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.startGame(game)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);

        // When & Then
        mockMvc.perform(post("/api/games/{gameId}/state/start", gameId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Game started successfully"));

        verify(stateService).startGame(game);
        verify(gameService).save(game);
    }

    @Test
    void startGame_WhenGameCannotStart_ShouldReturnBadRequest() throws Exception {
        // Given
        Long gameId = 1L;
        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.startGame(game)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/games/{gameId}/state/start", gameId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot start game"));

        verify(stateService).startGame(game);
        verify(gameService, never()).save(any(Game.class));
    }

    @Test
    void pauseGame_WhenGameCanBePaused_ShouldReturnSuccessMessage() throws Exception {
        // Given
        Long gameId = 1L;
        game.setState(GameState.NORMAL_PLAY);
        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.pauseGame(game)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);

        // When & Then
        mockMvc.perform(post("/api/games/{gameId}/state/pause", gameId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Game paused"));

        verify(stateService).pauseGame(game);
        verify(gameService).save(game);
    }

    @Test
    void resumeGame_WhenGameCanBeResumed_ShouldReturnSuccessMessage() throws Exception {
        // Given
        Long gameId = 1L;
        game.setState(GameState.PAUSED);
        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.resumeGame(game)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);

        // When & Then
        mockMvc.perform(post("/api/games/{gameId}/state/resume", gameId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Game resumed"));

        verify(stateService).resumeGame(game);
        verify(gameService).save(game);
    }

    @Test
    void finishGame_WhenGameCanBeFinished_ShouldReturnSuccessMessage() throws Exception {
        // Given
        Long gameId = 1L;
        game.setState(GameState.NORMAL_PLAY);
        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.finishGame(game)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);

        // When & Then
        mockMvc.perform(post("/api/games/{gameId}/state/finish", gameId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Game finished"));

        verify(stateService).finishGame(game);
        verify(gameService).save(game);
    }

    @Test
    void performAction_WithValidAction_ShouldReturnSuccessMessage() throws Exception {
        Long gameId = 1L;
        String action = "next_phase";
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        when(gameService.findById(gameId)).thenReturn(game);
        // El controller llama executeGameAction(gameId, action) - NO canPerformAction
        when(stateService.executeGameAction(gameId, action)).thenReturn("Action performed: next_phase");

        mockMvc.perform(post("/api/games/{gameId}/state/action/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Action performed: next_phase"));
    }

    @Test
    void performAction_WithInvalidAction_ShouldReturnBadRequest() throws Exception {
        Long gameId = 1L;
        String action = "invalid_action";

        when(gameService.findById(gameId)).thenReturn(game);
        // El controller debe estar llamando executeGameAction que lanza excepción
        when(stateService.executeGameAction(gameId, action))
                .thenThrow(new IllegalStateException("Action not allowed in current phase"));

        mockMvc.perform(post("/api/games/{gameId}/state/action/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Action not allowed in current phase"));
    }


    @Test
    void performAction_WithUnknownAction_ShouldReturnBadRequest() throws Exception {
        Long gameId = 1L;
        String action = "unknown_action";

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.executeGameAction(gameId, action))
                .thenThrow(new IllegalArgumentException("Unknown action: unknown_action"));

        mockMvc.perform(post("/api/games/{gameId}/state/action/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unknown action: unknown_action"));
    }

    @Test
    void getCurrentState_ShouldReturnGameStateResponse() throws Exception {
        Long gameId = 1L;
        game.setState(GameState.NORMAL_PLAY); // Cambiar expectativa
        game.setCurrentPhase(TurnPhase.ATTACK);
        game.setCurrentTurn(5);

        String[] availableActions = {"attack", "skip_attack", "next_phase"};
        String phaseDescription = "Attack enemy territories or skip to fortification";

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.getAvailableActions(game)).thenReturn(availableActions);
        when(stateService.getCurrentPhaseDescription(game)).thenReturn(phaseDescription);

        mockMvc.perform(get("/api/games/{gameId}/state/current", gameId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameState").value("NORMAL_PLAY")) // Cambiar expectativa
                .andExpect(jsonPath("$.turnPhase").value("ATTACK"))
                .andExpect(jsonPath("$.currentPlayer").value("Player 1"))
                .andExpect(jsonPath("$.currentTurn").value(5))
                .andExpect(jsonPath("$.availableActions").isArray())
                .andExpect(jsonPath("$.availableActions.length()").value(3))
                .andExpect(jsonPath("$.phaseDescription").value(phaseDescription));
    }

    @Test
    void getCurrentState_WithNoCurrentPlayer_ShouldReturnNoCurrentPlayer() throws Exception {
        Long gameId = 1L;
        game.setPlayers(Arrays.asList()); // Lista vacía en lugar de null
        game.setCurrentPlayerIndex(null);
        game.setState(GameState.WAITING_FOR_PLAYERS);

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.getAvailableActions(game)).thenReturn(new String[0]);
        when(stateService.getCurrentPhaseDescription(game)).thenReturn("Game is not active");

        mockMvc.perform(get("/api/games/{gameId}/state/current", gameId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayer").value("No current player"));
    }

    @Test
    void canPerformAction_WhenActionAllowed_ShouldReturnTrue() throws Exception {
        // Given
        Long gameId = 1L;
        String action = "attack";

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.canPerformAction(game, action)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/games/{gameId}/state/can-perform/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(stateService).canPerformAction(game, action);
    }

    @Test
    void canPerformAction_WhenActionNotAllowed_ShouldReturnFalse() throws Exception {
        // Given
        Long gameId = 1L;
        String action = "attack";

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.canPerformAction(game, action)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/games/{gameId}/state/can-perform/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(stateService).canPerformAction(game, action);
    }

    @Test
    void performNextTurnAction_ShouldAdvanceToNextTurn() throws Exception {
        Long gameId = 1L;
        String action = "next_turn";

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.executeGameAction(gameId, action)).thenReturn("Action performed: next_turn");

        mockMvc.perform(post("/api/games/{gameId}/state/action/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Action performed: next_turn"));
    }

    @Test
    void performSkipAttackAction_ShouldChangePhraseToFortify() throws Exception {
        Long gameId = 1L;
        String action = "skip_attack";
        game.setCurrentPhase(TurnPhase.ATTACK);

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.executeGameAction(gameId, action)).thenReturn("Action performed: skip_attack");

        mockMvc.perform(post("/api/games/{gameId}/state/action/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Action performed: skip_attack"));
    }

    @Test
    void performSkipFortifyAction_ShouldChangePhaseToEndTurn() throws Exception {
        Long gameId = 1L;
        String action = "skip_fortify";
        game.setCurrentPhase(TurnPhase.FORTIFY);

        when(gameService.findById(gameId)).thenReturn(game);
        when(stateService.executeGameAction(gameId, action)).thenReturn("Action performed: skip_fortify");

        mockMvc.perform(post("/api/games/{gameId}/state/action/{action}", gameId, action))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Action performed: skip_fortify"));
    }
}
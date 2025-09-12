package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.TurnActionDto;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TurnManagementController.class)
class TurnManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private GameStateService gameStateService;

    @MockBean
    private GameMapper gameMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Game game;
    private Player player;
    private GameResponseDto gameResponseDto;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1L);
        player.setDisplayName("Player1");

        game = new Game();
        game.setGameCode("GAME123");
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);
        game.setCurrentPlayerIndex(0);
        game.setCurrentTurn(1);
        game.setPlayers(Arrays.asList(player));

        gameResponseDto = new GameResponseDto();
        gameResponseDto.setGameCode("GAME123");
    }

    @Test
    void skipAttackPhase_Success() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-attack/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("GAME123"));
    }

    @Test
    void skipAttackPhase_NotPlayerTurn() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-attack/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skipAttackPhase_WrongPhase() throws Exception {
        game.setCurrentPhase(TurnPhase.FORTIFY);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-attack/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skipAttackPhase_PhaseChangeFails() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY)).thenReturn(false);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-attack/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skipAttackPhase_Exception() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenThrow(new RuntimeException("Game error"));

        mockMvc.perform(post("/api/games/GAME123/turn/skip-attack/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skipFortifyPhase_Success() throws Exception {
        game.setCurrentPhase(TurnPhase.FORTIFY);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.END_TURN)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-fortify/1"))
                .andExpect(status().isOk());
    }

    @Test
    void skipFortifyPhase_WrongPhase() throws Exception {
        game.setCurrentPhase(TurnPhase.ATTACK);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-fortify/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skipFortifyPhase_NotPlayerTurn() throws Exception {
        game.setCurrentPhase(TurnPhase.FORTIFY);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-fortify/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endTurn_Success() throws Exception {
        game.setCurrentPhase(TurnPhase.END_TURN);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/end-turn/1"))
                .andExpect(status().isOk());
    }

    @Test
    void endTurn_WrongPhase() throws Exception {
        game.setCurrentPhase(TurnPhase.ATTACK);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/turn/end-turn/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endTurn_NotPlayerTurn() throws Exception {
        game.setCurrentPhase(TurnPhase.END_TURN);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);

        mockMvc.perform(post("/api/games/GAME123/turn/end-turn/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proceedToFortify_Success() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/proceed-to-fortify/1"))
                .andExpect(status().isOk());
    }

    @Test
    void proceedToFortify_WrongPhase() throws Exception {
        game.setCurrentPhase(TurnPhase.FORTIFY);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/turn/proceed-to-fortify/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proceedToClaimCard_Success() throws Exception {
        game.setCurrentPhase(TurnPhase.FORTIFY);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "claim_card")).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.CLAIM_CARD)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/claim-card/1"))
                .andExpect(status().isOk());
    }

    @Test
    void proceedToClaimCard_CannotClaim() throws Exception {
        game.setCurrentPhase(TurnPhase.FORTIFY);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "claim_card")).thenReturn(false);

        mockMvc.perform(post("/api/games/GAME123/turn/claim-card/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proceedToClaimCard_WrongPhase() throws Exception {
        game.setCurrentPhase(TurnPhase.ATTACK);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/turn/claim-card/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skipClaimCard_Success() throws Exception {
        game.setCurrentPhase(TurnPhase.CLAIM_CARD);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.END_TURN)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-claim-card/1"))
                .andExpect(status().isOk());
    }

    @Test
    void skipClaimCard_WrongPhase() throws Exception {
        game.setCurrentPhase(TurnPhase.ATTACK);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/turn/skip-claim-card/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void performTurnAction_SkipAttack() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("skip_attack");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_SkipFortify() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("skip_fortify");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.END_TURN)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_ProceedToFortify() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("proceed_to_fortify");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_FinishFortify_WithClaimCard() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("finish_fortify");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "claim_card")).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.CLAIM_CARD)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_FinishFortify_WithoutClaimCard() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("finish_fortify");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "claim_card")).thenReturn(false);
        when(gameStateService.changeTurnPhase(game, TurnPhase.END_TURN)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_ClaimCard() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("claim_card");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.CLAIM_CARD)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_SkipClaimCard() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("skip_claim_card");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.changeTurnPhase(game, TurnPhase.END_TURN)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_EndTurn() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("end_turn");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameService.save(game)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void performTurnAction_UnknownAction() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("unknown_action");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void performTurnAction_NotPlayerTurn() throws Exception {
        TurnActionDto actionDto = new TurnActionDto();
        actionDto.setPlayerId(1L);
        actionDto.setGameId(1L);
        actionDto.setAction("skip_attack");

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);

        mockMvc.perform(post("/api/games/GAME123/turn/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTurnStatus_Success() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.getAvailableActions(game)).thenReturn(new String[]{"attack", "fortify"});
        when(gameStateService.getCurrentPhaseDescription(game)).thenReturn("Attack phase");

        mockMvc.perform(get("/api/games/GAME123/turn/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("GAME123"))
                .andExpect(jsonPath("$.currentPhase").value("ATTACK"))
                .andExpect(jsonPath("$.currentTurn").value(1))
                .andExpect(jsonPath("$.currentPlayerIndex").value(0))
                .andExpect(jsonPath("$.totalActivePlayers").value(1))
                .andExpect(jsonPath("$.canSkipPhase").value(true));
    }

    @Test
    void getTurnStatus_NoCurrentPlayer() throws Exception {
        game.setCurrentPlayerIndex(null);
        game.setPlayers(Arrays.asList());

        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.getAvailableActions(game)).thenReturn(new String[0]);
        when(gameStateService.getCurrentPhaseDescription(game)).thenReturn("No active phase");

        mockMvc.perform(get("/api/games/GAME123/turn/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName").isEmpty());
    }

    @Test
    void getTurnStatus_Exception() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenThrow(new RuntimeException("Game error"));

        mockMvc.perform(get("/api/games/GAME123/turn/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void canSkipCurrentPhase_Reinforcement() throws Exception {
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.getAvailableActions(game)).thenReturn(new String[0]);
        when(gameStateService.getCurrentPhaseDescription(game)).thenReturn("Reinforcement phase");

        mockMvc.perform(get("/api/games/GAME123/turn/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canSkipPhase").value(false));
    }

    @Test
    void canSkipCurrentPhase_EndTurn() throws Exception {
        game.setCurrentPhase(TurnPhase.END_TURN);
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.getAvailableActions(game)).thenReturn(new String[0]);
        when(gameStateService.getCurrentPhaseDescription(game)).thenReturn("End turn phase");

        mockMvc.perform(get("/api/games/GAME123/turn/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canSkipPhase").value(false));
    }
}
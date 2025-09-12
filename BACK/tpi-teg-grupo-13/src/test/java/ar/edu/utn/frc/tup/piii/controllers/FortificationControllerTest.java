package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.FortificationResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.service.interfaces.FortificationService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FortificationController.class)
public class FortificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FortificationService fortificationService;

    @MockBean
    private GameService gameService;

    @MockBean
    private GameStateService gameStateService;

    @MockBean
    private GameTerritoryService gameTerritoryService;

    private Game game;
    private FortifyDto fortifyDto;
    private Territory fromTerritory;
    private Territory toTerritory;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1L);
        game.setState(GameState.NORMAL_PLAY);

        fortifyDto = FortifyDto.builder()
                .playerId(1L)
                .fromCountryId(101L)
                .toCountryId(102L)
                .armies(3)
                .build();

        fromTerritory = new Territory();
        fromTerritory.setId(101L);
        fromTerritory.setName("Argentina");
        fromTerritory.setOwnerName("Player 1");
        fromTerritory.setArmies(5);

        toTerritory = new Territory();
        toTerritory.setId(102L);
        toTerritory.setName("Brasil");
        toTerritory.setArmies(2);
    }

    @Test
    void performFortification_ShouldReturnSuccessResponse() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(eq(game), eq(1L))).thenReturn(true);
        when(gameStateService.canPerformAction(eq(game), eq("fortify"))).thenReturn(true);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 101L)).thenReturn(fromTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 102L)).thenReturn(toTerritory);
        when(fortificationService.isValidFortification(eq("GAME123"), any(FortifyDto.class))).thenReturn(true);
        when(fortificationService.performFortification(eq("GAME123"), any(FortifyDto.class))).thenReturn(true);

        mockMvc.perform(post("/api/games/GAME123/fortification/fortify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fortifyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fortification completed successfully"));
    }

    @Test
    void performFortification_Invalid_ShouldReturnBadRequest() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(eq(game), eq(1L))).thenReturn(true);
        when(gameStateService.canPerformAction(eq(game), eq("fortify"))).thenReturn(true);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 101L)).thenReturn(fromTerritory);
        when(fortificationService.isValidFortification(eq("GAME123"), any(FortifyDto.class))).thenReturn(false);

        mockMvc.perform(post("/api/games/GAME123/fortification/fortify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fortifyDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void performFortification_Exception_ShouldReturnBadRequest() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(post("/api/games/GAME123/fortification/fortify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fortifyDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFortifiableTerritories_ShouldReturnList() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(fortificationService.getFortifiableTerritoriesForPlayer("GAME123", 1L)).thenReturn(List.of(fromTerritory));

        mockMvc.perform(get("/api/games/GAME123/fortification/fortifiable-territories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Argentina"));
    }

    @Test
    void getFortifiableTerritories_Exception_ShouldReturnBadRequest() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/games/GAME123/fortification/fortifiable-territories/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFortificationTargets_ShouldReturnList() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(fortificationService.getFortificationTargetsForTerritory("GAME123", 101L, 1L))
                .thenReturn(List.of(toTerritory));

        mockMvc.perform(get("/api/games/GAME123/fortification/fortification-targets/101/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Brasil"));
    }

    @Test
    void getFortificationTargets_Exception_ShouldReturnBadRequest() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/games/GAME123/fortification/fortification-targets/101/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void canPlayerFortify_ShouldReturnTrue() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "fortify")).thenReturn(true);

        mockMvc.perform(get("/api/games/GAME123/fortification/can-fortify/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void canPlayerFortify_Exception_ShouldReturnFalse() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/games/GAME123/fortification/can-fortify/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getMaxMovableArmies_ShouldReturnValue() throws Exception {
        when(fortificationService.getMaxMovableArmies("GAME123", 101L)).thenReturn(3);

        mockMvc.perform(get("/api/games/GAME123/fortification/max-movable-armies/101"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void getMaxMovableArmies_Exception_ShouldReturnZero() throws Exception {
        when(fortificationService.getMaxMovableArmies("GAME123", 101L)).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/games/GAME123/fortification/max-movable-armies/101"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void checkTerritoryConnection_ShouldReturnTrue() throws Exception {
        when(fortificationService.areTerritoriesConnectedByPlayer("GAME123", 101L, 102L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/games/GAME123/fortification/check-connection/101/102/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkTerritoryConnection_Exception_ShouldReturnFalse() throws Exception {
        when(fortificationService.areTerritoriesConnectedByPlayer("GAME123", 101L, 102L, 1L))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/games/GAME123/fortification/check-connection/101/102/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}

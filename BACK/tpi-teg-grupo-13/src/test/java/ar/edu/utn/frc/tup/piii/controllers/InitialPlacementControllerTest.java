package ar.edu.utn.frc.tup.piii.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.utn.frc.tup.piii.dtos.country.TerritoryDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.InitialArmyPlacementDto;
import ar.edu.utn.frc.tup.piii.dtos.game.InitialPlacementSummaryDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerInitialInfoDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerTerritoriesDto;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.CountryMapper;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.service.impl.InitialPlacementServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;

@WebMvcTest(InitialPlacementController.class)
public class InitialPlacementControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InitialPlacementServiceImpl initialPlacementService;

    @MockBean
    private GameTerritoryService gameTerritoryService;

    @MockBean
    private GameService gameService;


    @MockBean
    private GameMapper gameMapper;

    @MockBean
    private CountryMapper countryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Game gameTest;
    private Player playerTest;
    private Territory territoryTest;
    private InitialArmyPlacementDto placementDto;
    private GameResponseDto gameResponseDto;
    private TerritoryDto territoryDto;
    private PlayerTerritoriesDto playerTerritoriesDto;
    private InitialPlacementSummaryDto summaryDto;
    private PlayerInitialInfoDto playerInitialInfoDto;
    private InitialPlacementServiceImpl.InitialPlacementStatus placementStatus;
    private InitialPlacementServiceImpl.PlayerInitialStatus playerStatus;
        
    @BeforeEach
    void setUp(){
        // Setup test data
        gameTest = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.REINFORCEMENT_5)
                .currentPlayerIndex(0)
                .build();

        playerTest = Player.builder()
                .id(1L)
                .displayName("Test Player")
                .seatOrder(1)
                .status(PlayerStatus.ACTIVE)
                .build();

        territoryTest = Territory.builder()
                .id(1L)
                .name("Test Territory")
                .ownerId(1L)
                .armies(0)
                .build();

        territoryDto = TerritoryDto.builder()
                .id(1L)
                .name("Test Territory")
                .continentName("Test Continent")
                .armies(0)
                .build();

        placementDto = InitialArmyPlacementDto.builder()
                .playerId(1L)
                .armiesByCountry(new HashMap<>() {{
                    put(1L, 5);
                }})
                .build();

        gameResponseDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.REINFORCEMENT_5)
                .currentPhase(null)
                .currentTurn(null)
                .currentPlayerIndex(null)
                .maxPlayers(null)
                .turnTimeLimit(null)
                .chatEnabled(null)
                .pactsAllowed(null)
                .createdAt(null)
                .startedAt(null)
                .finishedAt(null)
                .players(null)
                .territories(null)
                .continents(null)
                .recentEvents(null)
                .recentMessages(null)
                .currentPlayerName(null)
                .canStart(null)
                .isGameOver(null)
                .winnerName(null)
                .build();

        playerTerritoriesDto = PlayerTerritoriesDto.builder()
                .playerId(1L)
                .playerName("Test Player")
                .armiesToPlace(5)
                .expectedArmiesThisRound(5)
                .canPlaceArmies(true)
                .isPlayerTurn(true)
                .message("Your turn to place armies")
                .ownedTerritories(Arrays.asList(territoryDto))
                .build();

        playerInitialInfoDto = PlayerInitialInfoDto.builder()
                .playerId(1L)
                .playerName("Test Player")
                .seatOrder(1)
                .armiesToPlace(5)
                .territoryCount(1)
                .isCurrentPlayer(true)
                .territories(Arrays.asList(territoryDto))
                .build();

        summaryDto = InitialPlacementSummaryDto.builder()
                .gameCode("TEST123")
                .currentPhase(GameState.REINFORCEMENT_5)
                .isActive(true)
                .message("Initial placement in progress")
                .currentPlayerId(1L)
                .expectedArmies(5)
                .players(Arrays.asList(playerInitialInfoDto))
                .build();

        placementStatus = new InitialPlacementServiceImpl.InitialPlacementStatus(
                true, "Initial placement in progress", 1L, 5
        );

        playerStatus = new InitialPlacementServiceImpl.PlayerInitialStatus(
                1L, "Test Player", true, 5, GameState.REINFORCEMENT_5, 5, 
                Arrays.asList(1L), true, "Your turn to place armies"
        );

        // Setup game with players
        gameTest.setPlayers(Arrays.asList(playerTest));
    }

    @Test
    void testPlaceInitialArmies_Success() throws Exception {
        doNothing().when(initialPlacementService).placeInitialArmies("TEST123", 1L, placementDto.getArmiesByCountry());
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameMapper.toResponseDto(gameTest)).thenReturn(gameResponseDto);

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placementDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.state").value("REINFORCEMENT_5"));
    }

    @Test
    void testPlaceInitialArmies_NullPlayerId() throws Exception {
        InitialArmyPlacementDto invalidDto = InitialArmyPlacementDto.builder()
                .playerId(null)
                .armiesByCountry(new HashMap<>() {{
                    put(1L, 5);
                }})
                .build();

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlaceInitialArmies_NullArmiesByCountry() throws Exception {
        InitialArmyPlacementDto invalidDto = InitialArmyPlacementDto.builder()
                .playerId(1L)
                .armiesByCountry(null)
                .build();

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())

                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlaceInitialArmies_EmptyArmiesByCountry() throws Exception {
        InitialArmyPlacementDto invalidDto = InitialArmyPlacementDto.builder()
                .playerId(1L)
                .armiesByCountry(new HashMap<>())
                .build();

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlaceInitialArmies_GameNotFound() throws Exception {
        doThrow(new GameNotFoundException("Game not found")).when(initialPlacementService)
                .placeInitialArmies("INVALID", 1L, placementDto.getArmiesByCountry());

        mockMvc.perform(post("/api/games/INVALID/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placementDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testPlaceInitialArmies_PlayerNotFound() throws Exception {
        doThrow(new PlayerNotFoundException("Player not found")).when(initialPlacementService)
                .placeInitialArmies("TEST123", 999L, placementDto.getArmiesByCountry());

        InitialArmyPlacementDto invalidDto = InitialArmyPlacementDto.builder()
                .playerId(999L)
                .armiesByCountry(new HashMap<>() {{
                    put(1L, 5);
                }})
                .build();

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testPlaceInitialArmies_InvalidGameState() throws Exception {
        doThrow(new InvalidGameStateException("Invalid game state")).when(initialPlacementService)
                .placeInitialArmies("TEST123", 1L, placementDto.getArmiesByCountry());

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placementDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlaceInitialArmies_IllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException("Invalid placement")).when(initialPlacementService)
                .placeInitialArmies("TEST123", 1L, placementDto.getArmiesByCountry());

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placementDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlaceInitialArmies_Exception() throws Exception {
        doThrow(new RuntimeException("Unexpected error")).when(initialPlacementService)
                .placeInitialArmies("TEST123", 1L, placementDto.getArmiesByCountry());

        mockMvc.perform(post("/api/games/TEST123/initial-placement/place-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placementDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetPlacementStatus_Success() throws Exception {
        when(initialPlacementService.getPlacementStatus("TEST123")).thenReturn(placementStatus);

        mockMvc.perform(get("/api/games/TEST123/initial-placement/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.message").value("Initial placement in progress"))
                .andExpect(jsonPath("$.currentPlayerId").value(1))
                .andExpect(jsonPath("$.expectedArmies").value(5));
    }

    @Test
    void testGetPlacementStatus_GameNotFound() throws Exception {
        when(initialPlacementService.getPlacementStatus("INVALID"))
                .thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/api/games/INVALID/initial-placement/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPlacementStatus_Exception() throws Exception {
        when(initialPlacementService.getPlacementStatus("TEST123"))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/games/TEST123/initial-placement/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetPlayerStatus_Success() throws Exception {
        when(initialPlacementService.getPlayerStatus("TEST123", 1L)).thenReturn(playerStatus);

        mockMvc.perform(get("/api/games/TEST123/initial-placement/player/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(1))
                .andExpect(jsonPath("$.playerName").value("Test Player"))
                .andExpect(jsonPath("$.playerTurn").value(true))
                .andExpect(jsonPath("$.armiesToPlace").value(5));
    }

    @Test
    void testGetPlayerStatus_GameNotFound() throws Exception {
        when(initialPlacementService.getPlayerStatus("INVALID", 1L))
                .thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/api/games/INVALID/initial-placement/player/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPlayerStatus_PlayerNotFound() throws Exception {
        when(initialPlacementService.getPlayerStatus("TEST123", 999L))
                .thenThrow(new PlayerNotFoundException("Player not found"));

        mockMvc.perform(get("/api/games/TEST123/initial-placement/player/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPlayerStatus_Exception() throws Exception {
        when(initialPlacementService.getPlayerStatus("TEST123", 1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/games/TEST123/initial-placement/player/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetPlayerTerritories_Success() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(Arrays.asList(territoryTest));
        when(initialPlacementService.getPlayerStatus("TEST123", 1L)).thenReturn(playerStatus);
        when(countryMapper.mapTerritoryToDto(territoryTest)).thenReturn(territoryDto);

        mockMvc.perform(get("/api/games/TEST123/initial-placement/player/1/territories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(1))
                .andExpect(jsonPath("$.playerName").value("Test Player"))
                .andExpect(jsonPath("$.armiesToPlace").value(5))
                .andExpect(jsonPath("$.expectedArmiesThisRound").value(5))
                .andExpect(jsonPath("$.canPlaceArmies").value(true))
                .andExpect(jsonPath("$.isPlayerTurn").value(true))
                .andExpect(jsonPath("$.message").value("Your turn to place armies"))
                .andExpect(jsonPath("$.ownedTerritories").isArray())
                .andExpect(jsonPath("$.ownedTerritories.length()").value(1));
    }

    @Test
    void testGetPlayerTerritories_GameNotFound() throws Exception {
        when(gameService.findByGameCode("INVALID")).thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/api/games/INVALID/initial-placement/player/1/territories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())

                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPlayerTerritories_PlayerNotFound() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(initialPlacementService.getPlayerStatus("TEST123", 999L))
                .thenThrow(new PlayerNotFoundException("Player not found"));

        mockMvc.perform(get("/api/games/TEST123/initial-placement/player/999/territories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())

                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPlayerTerritories_Exception() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(Arrays.asList(territoryTest));
        when(initialPlacementService.getPlayerStatus("TEST123", 1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/games/TEST123/initial-placement/player/1/territories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetInitialPlacementSummary_Success() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(initialPlacementService.getPlacementStatus("TEST123")).thenReturn(placementStatus);
        when(initialPlacementService.getPlayerStatus("TEST123", 1L)).thenReturn(playerStatus);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(Arrays.asList(territoryTest));
        when(countryMapper.mapTerritoryToDto(territoryTest)).thenReturn(territoryDto);

        mockMvc.perform(get("/api/games/TEST123/initial-placement/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.currentPhase").value("REINFORCEMENT_5"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.message").value("Initial placement in progress"))
                .andExpect(jsonPath("$.currentPlayerId").value(1))
                .andExpect(jsonPath("$.expectedArmies").value(5))
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players.length()").value(1));
    }

    @Test
    void testGetInitialPlacementSummary_GameNotFound() throws Exception {
        when(gameService.findByGameCode("INVALID")).thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/api/games/INVALID/initial-placement/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetInitialPlacementSummary_Exception() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(initialPlacementService.getPlacementStatus("TEST123"))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/games/TEST123/initial-placement/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
} 


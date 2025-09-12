package ar.edu.utn.frc.tup.piii.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;

@WebMvcTest(CombatController.class)
public class CombatControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CombatService combatService;


    @MockBean
    private GameService gameService;

    @MockBean
    private GameStateService gameStateService;

    @Autowired
    private ObjectMapper objectMapper;

    private Game gameTest;
    private AttackDto attackDto;
    private CombatResultDto combatResultDto;
    private Territory territoryTest1;
    private Territory territoryTest2;
    private List<Territory> territoriesTest;
        
    @BeforeEach
    void setUp(){
        // Setup test data
        gameTest = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.ATTACK)
                .currentPlayerIndex(0)
                .build();

        attackDto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(1L)
                .defenderCountryId(2L)
                .attackingArmies(3)
                .build();

        combatResultDto = CombatResultDto.builder()
                .attackerCountryId(1L)
                .attackerCountryName("Test Attacker")
                .defenderCountryId(2L)
                .defenderCountryName("Test Defender")
                .attackerPlayerName("Player 1")
                .defenderPlayerName("Player 2")
                .attackerDice(Arrays.asList(6, 5, 4))
                .defenderDice(Arrays.asList(3, 2))
                .attackerLosses(0)
                .defenderLosses(2)
                .territoryConquered(true)
                .attackerRemainingArmies(2)
                .defenderRemainingArmies(0)
                .build();

        territoryTest1 = Territory.builder()
                .id(1L)
                .name("Test Territory 1")
                .ownerId(1L)
                .ownerName("Player 1")
                .armies(5)
                .build();

        territoryTest2 = Territory.builder()
                .id(2L)
                .name("Test Territory 2")
                .ownerId(2L)
                .ownerName("Player 2")
                .armies(3)
                .build();

        territoriesTest = Arrays.asList(territoryTest1, territoryTest2);
    }

    @Test
    void testAttack_Success() throws Exception {
        when(combatService.performCombatWithValidation("TEST123", attackDto)).thenReturn(combatResultDto);

        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attackerCountryId").value(1))
                .andExpect(jsonPath("$.attackerCountryName").value("Test Attacker"))
                .andExpect(jsonPath("$.defenderCountryId").value(2))
                .andExpect(jsonPath("$.defenderCountryName").value("Test Defender"))
                .andExpect(jsonPath("$.attackerPlayerName").value("Player 1"))
                .andExpect(jsonPath("$.defenderPlayerName").value("Player 2"))
                .andExpect(jsonPath("$.attackerDice").isArray())
                .andExpect(jsonPath("$.attackerDice.length()").value(3))
                .andExpect(jsonPath("$.defenderDice").isArray())
                .andExpect(jsonPath("$.defenderDice.length()").value(2))
                .andExpect(jsonPath("$.attackerLosses").value(0))
                .andExpect(jsonPath("$.defenderLosses").value(2))
                .andExpect(jsonPath("$.territoryConquered").value(true))
                .andExpect(jsonPath("$.attackerRemainingArmies").value(2))
                .andExpect(jsonPath("$.defenderRemainingArmies").value(0));
    }

    @Test
    void testAttack_IllegalStateException() throws Exception {
        doThrow(new IllegalStateException("Invalid game state")).when(combatService)
                .performCombatWithValidation("TEST123", attackDto);

        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAttackableTerritories_Success() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(combatService.getAttackableTerritoriesForPlayer("TEST123", 1L)).thenReturn(territoriesTest);

        mockMvc.perform(get("/api/games/TEST123/combat/attackable-territories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Territory 1"))
                .andExpect(jsonPath("$[0].ownerId").value(1))
                .andExpect(jsonPath("$[0].ownerName").value("Player 1"))
                .andExpect(jsonPath("$[0].armies").value(5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Test Territory 2"))
                .andExpect(jsonPath("$[1].ownerId").value(2))
                .andExpect(jsonPath("$[1].ownerName").value("Player 2"))
                .andExpect(jsonPath("$[1].armies").value(3));
    }

    @Test
    void testGetAttackableTerritories_Exception() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(combatService.getAttackableTerritoriesForPlayer("TEST123", 1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/games/TEST123/combat/attackable-territories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAttackTargets_Success() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(combatService.getTargetsForTerritory("TEST123", 1L, 1L)).thenReturn(Arrays.asList(territoryTest2));

        mockMvc.perform(get("/api/games/TEST123/combat/attack-targets/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Test Territory 2"))
                .andExpect(jsonPath("$[0].ownerId").value(2))
                .andExpect(jsonPath("$[0].ownerName").value("Player 2"))
                .andExpect(jsonPath("$[0].armies").value(3));
    }

    @Test
    void testGetAttackTargets_Exception() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(combatService.getTargetsForTerritory("TEST123", 1L, 1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/games/TEST123/combat/attack-targets/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCanPlayerAttack_Success_True() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(gameTest, "attack")).thenReturn(true);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCanPlayerAttack_Success_False() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(false);
        when(gameStateService.canPerformAction(gameTest, "attack")).thenReturn(true);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_GameStateNotValidForCombat() throws Exception {
        Game invalidGame = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(TurnPhase.REINFORCEMENT)
                .currentPlayerIndex(0)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(invalidGame);
        when(gameStateService.isPlayerTurn(invalidGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(invalidGame, "attack")).thenReturn(true);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_NotPlayerTurn() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(false);
        when(gameStateService.canPerformAction(gameTest, "attack")).thenReturn(true);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_NotAttackPhase() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(gameTest, "attack")).thenReturn(false);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_Exception() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_HostilityOnlyState() throws Exception {
        Game hostilityGame = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.HOSTILITY_ONLY)
                .currentPhase(TurnPhase.ATTACK)
                .currentPlayerIndex(0)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(hostilityGame);
        when(gameStateService.isPlayerTurn(hostilityGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(hostilityGame, "attack")).thenReturn(true);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCanPlayerAttack_NormalPlayState() throws Exception {
        Game normalGame = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.ATTACK)
                .currentPlayerIndex(0)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(normalGame);
        when(gameStateService.isPlayerTurn(normalGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(normalGame, "attack")).thenReturn(true);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testGetAttackableTerritories_GameStateValidationException() throws Exception {
        Game invalidGame = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(TurnPhase.REINFORCEMENT)
                .currentPlayerIndex(0)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(invalidGame);

        mockMvc.perform(get("/api/games/TEST123/combat/attackable-territories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAttackTargets_GameStateValidationException() throws Exception {
        Game invalidGame = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.REINFORCEMENT_5)
                .currentPhase(TurnPhase.REINFORCEMENT)
                .currentPlayerIndex(0)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(invalidGame);

        mockMvc.perform(get("/api/games/TEST123/combat/attack-targets/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCanPlayerAttack_GameServiceException() throws Exception {
        when(gameService.findByGameCode("TEST123"))
                .thenThrow(new RuntimeException("Game not found"));

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testGetAttackableTerritories_GameServiceException() throws Exception {
        when(gameService.findByGameCode("TEST123"))
                .thenThrow(new RuntimeException("Game not found"));

        mockMvc.perform(get("/api/games/TEST123/combat/attackable-territories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAttackTargets_GameServiceException() throws Exception {
        when(gameService.findByGameCode("TEST123"))
                .thenThrow(new RuntimeException("Game not found"));

        mockMvc.perform(get("/api/games/TEST123/combat/attack-targets/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCanPlayerAttack_GameStateServiceException() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_AllGameStates() throws Exception {
        // Test all possible game states
        GameState[] allStates = GameState.values();
        
        for (GameState state : allStates) {
            Game testGame = Game.builder()
                    .id(1L)
                    .gameCode("TEST123")
                    .state(state)
                    .currentPhase(TurnPhase.ATTACK)
                    .currentPlayerIndex(0)
                    .build();

            when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
            when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(true);
            when(gameStateService.canPerformAction(testGame, "attack")).thenReturn(true);

            mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Test
    void testAttack_OtherException() throws Exception {
        doThrow(new RuntimeException("Unexpected error")).when(combatService)
                .performCombatWithValidation("TEST123", attackDto);

        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCanPlayerAttack_GameStateServiceCanPerformActionException() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(gameTest, "attack"))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_AllConditionsFalse() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(false);
        when(gameStateService.canPerformAction(gameTest, "attack")).thenReturn(false);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_GameStateValidButNotPlayerTurn() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(false);
        when(gameStateService.canPerformAction(gameTest, "attack")).thenReturn(true);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCanPlayerAttack_GameStateValidButNotAttackPhase() throws Exception {
        when(gameService.findByGameCode("TEST123")).thenReturn(gameTest);
        when(gameStateService.isPlayerTurn(gameTest, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(gameTest, "attack")).thenReturn(false);

        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testAttack_NotPlayerTurn() throws Exception {
        // Simula que no es el turno del jugador
        doThrow(new IllegalStateException("It's not player's turn to attack")).when(combatService)
                .performCombatWithValidation("TEST123", attackDto);

        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAttack_NotAttackPhase() throws Exception {
        // Simula que no es la fase de ataque
        doThrow(new IllegalStateException("Cannot attack in current turn phase: REINFORCEMENT")).when(combatService)
                .performCombatWithValidation("TEST123", attackDto);

        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombatServiceImplTest {

    @Mock
    private GameTerritoryService gameTerritoryService;

    @Mock
    private GameService gameService;

    @Mock
    private GameStateService gameStateService;

    @Spy
    @InjectMocks
    private CombatServiceImpl combatService;

    private Game testGame;
    private Territory attackerTerritory;
    private Territory defenderTerritory;
    private AttackDto attackDto;

    @BeforeEach
    void setUp() {
        testGame = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.HOSTILITY_ONLY)
                .currentPhase(TurnPhase.ATTACK)
                .build();

        attackerTerritory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(1L)
                .ownerName("Player1")
                .armies(5)
                .build();

        defenderTerritory = Territory.builder()
                .id(2L)
                .name("Brazil")
                .ownerId(2L)
                .ownerName("Player2")
                .armies(3)
                .build();

        attackDto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(1L)
                .defenderCountryId(2L)
                .attackingArmies(3)
                .build();

        // Lenient stubbing for (id, id) combos used in buildCombatResultDto
        // This avoids PotentialStubbingProblem in tests
        org.mockito.Mockito.lenient().when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(attackerTerritory);
        org.mockito.Mockito.lenient().when(gameTerritoryService.getTerritoryByGameAndCountry(eq(2L), eq(2L))).thenReturn(defenderTerritory);
    }

    @Test
    void testGetAttackableTerritoriesForPlayer_Success() {
        // Arrange
        List<Territory> playerTerritories = Arrays.asList(
                Territory.builder().id(1L).armies(5).build(),
                Territory.builder().id(2L).armies(1).build(), // Should be filtered out
                Territory.builder().id(3L).armies(3).build()
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(playerTerritories);

        // Act
        List<Territory> result = combatService.getAttackableTerritoriesForPlayer("TEST123", 1L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getArmies() > 1));
        verify(gameService).findByGameCode("TEST123");
        verify(gameTerritoryService).getTerritoriesByOwner(1L, 1L);
    }

    @Test
    void testGetTargetsForTerritory_Success() {
        // Arrange
        List<Territory> neighbors = Arrays.asList(
                Territory.builder().id(2L).ownerId(2L).build(),
                Territory.builder().id(3L).ownerId(1L).build(), // Should be filtered out
                Territory.builder().id(4L).ownerId(3L).build()
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getNeighborTerritories(1L, 1L)).thenReturn(neighbors);

        // Act
        List<Territory> result = combatService.getTargetsForTerritory("TEST123", 1L, 1L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(t -> t.getOwnerId().equals(1L)));
        verify(gameService).findByGameCode("TEST123");
        verify(gameTerritoryService).getNeighborTerritories(1L, 1L);
    }

    @Test
    void testPerformCombat_Success() {
        // Arrange
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        // Act
        CombatResultDto result = combatService.performCombat("TEST123", attackDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAttackerCountryId());
        assertEquals(2L, result.getDefenderCountryId());
        assertEquals("Argentina", result.getAttackerCountryName());
        assertEquals("Brazil", result.getDefenderCountryName());
        assertNotNull(result.getAttackerDice());
        assertNotNull(result.getDefenderDice());
        verify(gameService).findByGameCode("TEST123");
        verify(gameTerritoryService, times(2)).getTerritoryByGameAndCountry(1L, 1L);
        verify(gameTerritoryService, times(2)).getTerritoryByGameAndCountry(1L, 2L);
        verify(gameTerritoryService).areTerritoriesNeighbors(1L, 2L);
    }

    @Test
    void testPerformCombat_InvalidGameState() {
        // Arrange
        testGame.setState(GameState.WAITING_FOR_PLAYERS);
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            combatService.performCombat("TEST123", attackDto));
        verify(gameService).findByGameCode("TEST123");
    }

    @Test
    void testPerformCombat_AttackerTerritoryNotFound() {
        // Arrange
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            combatService.performCombat("TEST123", attackDto));
        assertEquals("Attacker territory not found: 1", exception.getMessage());
    }

    @Test
    void testPerformCombat_DefenderTerritoryNotFound() {
        // Arrange
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            combatService.performCombat("TEST123", attackDto));
        assertEquals("Defender territory not found: 2", exception.getMessage());
    }

    @Test
    void testPerformCombat_PlayerDoesNotOwnAttackerTerritory() {
        // Arrange
        attackerTerritory.setOwnerId(999L); // Different owner
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            combatService.performCombat("TEST123", attackDto));
        assertEquals("Player doesn't own the attacking territory", exception.getMessage());
    }

    @Test
    void testPerformCombat_AttackingOwnTerritory() {
        // Arrange
        defenderTerritory.setOwnerId(1L); // Same owner as attacker
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            combatService.performCombat("TEST123", attackDto));
        assertEquals("Cannot attack your own territory", exception.getMessage());
    }

    @Test
    void testPerformCombat_TerritoriesNotNeighbors() {
        // Arrange
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            combatService.performCombat("TEST123", attackDto));
        assertEquals("Territories are not neighbors", exception.getMessage());
    }

    @Test
    void testPerformCombat_InsufficientArmies() {
        // Arrange
        attackerTerritory.setArmies(1); // Only 1 army, can't attack
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            combatService.performCombat("TEST123", attackDto));
        assertEquals("Attacking territory must have more than 1 army", exception.getMessage());
    }

    @Test
    void testPerformCombat_InvalidAttackingArmies() {
        // Arrange
        AttackDto invalidAttackDto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(1L)
                .defenderCountryId(2L)
                .attackingArmies(5) // More than available (4)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            combatService.performCombat("TEST123", invalidAttackDto));
        assertTrue(exception.getMessage().contains("Invalid attacking armies"));
    }

    @Test
    void testPerformCombatWithValidation_Success() {
        // Arrange
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(testGame, "attack")).thenReturn(true);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        // Act
        CombatResultDto result = combatService.performCombatWithValidation("TEST123", attackDto);

        // Assert
        assertNotNull(result);
        verify(gameStateService).isPlayerTurn(testGame, 1L);
        verify(gameStateService).canPerformAction(testGame, "attack");
    }

    @Test
    void testPerformCombatWithValidation_NotPlayerTurn() {
        // Arrange
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            combatService.performCombatWithValidation("TEST123", attackDto));
        assertEquals("It's not player's turn to attack", exception.getMessage());
    }

    @Test
    void testPerformCombatWithValidation_CannotPerformAction() {
        // Arrange
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameStateService.isPlayerTurn(testGame, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(testGame, "attack")).thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            combatService.performCombatWithValidation("TEST123", attackDto));
        assertEquals("Cannot attack in current turn phase: ATTACK", exception.getMessage());
    }

    @Test
    void testPerformCombat_TerritoryConquered() throws Exception {
        // Arrange
        defenderTerritory.setArmies(1); // Will be conquered with one loss
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        
        // Create a defender territory with 0 armies (after losses)
        Territory conqueredDefender = Territory.builder()
                .id(2L)
                .name("Brazil")
                .ownerId(2L)
                .ownerName("Player2")
                .armies(0) // This will trigger conquest
                .build();
        
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L))
            .thenReturn(defenderTerritory) // First call: original territory
            .thenReturn(conqueredDefender); // Second call: after losses, 0 armies
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);
        
        // Mock the Random field to control dice rolls
        Field randomField = CombatServiceImpl.class.getDeclaredField("random");
        randomField.setAccessible(true);
        Random mockRandom = org.mockito.Mockito.mock(Random.class);
        // Make attacker always roll high (6,5,4) and defender always roll low (1)
        // nextInt(6) returns 0-5, so we add 1 to get 1-6
        org.mockito.Mockito.when(mockRandom.nextInt(6)).thenReturn(5, 4, 3, 0, 0, 0); // 6,5,4,1,1,1
        randomField.set(combatService, mockRandom);

        // Act
        CombatResultDto result = combatService.performCombat("TEST123", attackDto);

        // Assert
        assertNotNull(result);
        // Debug: print the actual result
        System.out.println("Territory conquered: " + result.getTerritoryConquered());
        System.out.println("Attacker dice: " + result.getAttackerDice());
        System.out.println("Defender dice: " + result.getDefenderDice());
        System.out.println("Attacker losses: " + result.getAttackerLosses());
        System.out.println("Defender losses: " + result.getDefenderLosses());
        assertTrue(result.getTerritoryConquered());
        verify(gameTerritoryService).transferTerritoryOwnership(eq(1L), eq(2L), eq(1L), anyInt());
    }

    @Test
    void testPerformCombat_NormalPlayState() {
        // Arrange
        testGame.setState(GameState.NORMAL_PLAY);
        when(gameService.findByGameCode("TEST123")).thenReturn(testGame);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        // Act
        CombatResultDto result = combatService.performCombat("TEST123", attackDto);

        // Assert
        assertNotNull(result);
        verify(gameService).findByGameCode("TEST123");
    }
}

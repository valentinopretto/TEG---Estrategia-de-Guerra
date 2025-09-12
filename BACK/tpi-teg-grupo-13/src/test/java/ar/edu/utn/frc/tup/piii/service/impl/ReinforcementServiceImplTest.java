package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.CountryMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReinforcementServiceImplTest {

    @Mock
    private GameService gameService;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameTerritoryService gameTerritoryService;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private CountryMapper countryMapper;

    @InjectMocks
    private ReinforcementServiceImpl reinforcementService;

    private Game testGame;
    private Player testPlayer;
    private static final String GAME_CODE = "TEST123";
    private static final Long PLAYER_ID = 1L;
    private static final Long GAME_ID = 100L;

    @BeforeEach
    void setUp() {
        // Setup test game
        testGame = Game.builder()
                .id(GAME_ID)
                .gameCode(GAME_CODE)
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.REINFORCEMENT)
                .currentPlayerIndex(0)
                .territories(new HashMap<>())
                .continents(new ArrayList<>())
                .build();

        // Setup test player
        testPlayer = Player.builder()
                .id(PLAYER_ID)
                .displayName("Test Player")
                .status(PlayerStatus.ACTIVE)
                .territoryIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L))
                .build();
    }

    @Test
    @DisplayName("Should place reinforcement armies successfully")
    void placeReinforcementArmies_Success() {
        // Arrange
        Map<Long, Integer> armiesByCountry = new HashMap<>();
        armiesByCountry.put(1L, 2);
        armiesByCountry.put(2L, 1);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(3);

        Territory territory1 = Territory.builder().id(1L).ownerId(PLAYER_ID).build();
        Territory territory2 = Territory.builder().id(2L).ownerId(PLAYER_ID).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 1L)).thenReturn(territory1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 2L)).thenReturn(territory2);

        // Act
        assertDoesNotThrow(() ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );

        // Assert
        verify(gameTerritoryService).addArmiesToTerritory(GAME_ID, 1L, 2);
        verify(gameTerritoryService).addArmiesToTerritory(GAME_ID, 2L, 1);
        verify(playerService).removeArmiesToPlace(PLAYER_ID, 3);
        verify(gameStateService).changeTurnPhase(testGame, TurnPhase.ATTACK);
        verify(gameService).save(testGame);
    }

    @Test
    @DisplayName("Should throw exception when not player's turn")
    void placeReinforcementArmies_NotPlayerTurn() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(1L, 2);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidGameStateException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );

        verify(gameTerritoryService, never()).addArmiesToTerritory(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should throw exception when game not in normal play")
    void placeReinforcementArmies_InvalidGameState() {
        // Arrange
        testGame.setState(GameState.WAITING_FOR_PLAYERS);
        Map<Long, Integer> armiesByCountry = Map.of(1L, 2);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));

        // Act & Assert
        assertThrows(InvalidGameStateException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should throw exception when player doesn't own territory")
    void placeReinforcementArmies_NotOwnerOfTerritory() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(1L, 2);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(3);

        Territory territory = Territory.builder().id(1L).ownerId(999L).build(); // Different owner
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 1L)).thenReturn(territory);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should calculate base armies correctly")
    void calculateBaseArmies_VariousCases() {
        // Test minimum (3 armies)
        assertEquals(3, reinforcementService.calculateBaseArmies(2));
        assertEquals(3, reinforcementService.calculateBaseArmies(5));

        // Test normal calculation
        assertEquals(3, reinforcementService.calculateBaseArmies(6));
        assertEquals(3, reinforcementService.calculateBaseArmies(7));
        assertEquals(4, reinforcementService.calculateBaseArmies(8));
        assertEquals(4, reinforcementService.calculateBaseArmies(9));
        assertEquals(5, reinforcementService.calculateBaseArmies(10));
        assertEquals(10, reinforcementService.calculateBaseArmies(20));
        assertEquals(21, reinforcementService.calculateBaseArmies(42));
    }

    @Test
    @DisplayName("Should calculate continent bonus correctly")
    void calculateContinentBonus_WithControlledContinents() {
        // Arrange
        Territory t1 = Territory.builder().id(1L).ownerId(PLAYER_ID).build();
        Territory t2 = Territory.builder().id(2L).ownerId(PLAYER_ID).build();
        Territory t3 = Territory.builder().id(3L).ownerId(PLAYER_ID).build();

        testGame.getTerritories().put(1L, t1);
        testGame.getTerritories().put(2L, t2);
        testGame.getTerritories().put(3L, t3);

        Continent southAmerica = Continent.builder()
                .id(1L)
                .name("South America")
                .bonusArmies(2)
                .countryIds(Arrays.asList(1L, 2L))
                .build();

        Continent africa = Continent.builder()
                .id(2L)
                .name("Africa")
                .bonusArmies(3)
                .countryIds(Arrays.asList(3L))
                .build();

        testGame.getContinents().add(southAmerica);
        testGame.getContinents().add(africa);

        // Act
        int bonus = reinforcementService.calculateContinentBonus(testGame, testPlayer);

        // Assert
        assertEquals(5, bonus); // 2 + 3
    }

    @Test
    @DisplayName("Should calculate zero continent bonus when no continents controlled")
    void calculateContinentBonus_NoControlledContinents() {
        // Arrange
        Territory t1 = Territory.builder().id(1L).ownerId(PLAYER_ID).build();
        Territory t2 = Territory.builder().id(2L).ownerId(999L).build(); // Different owner

        testGame.getTerritories().put(1L, t1);
        testGame.getTerritories().put(2L, t2);

        Continent continent = Continent.builder()
                .id(1L)
                .name("South America")
                .bonusArmies(2)
                .countryIds(Arrays.asList(1L, 2L))
                .build();

        testGame.getContinents().add(continent);

        // Act
        int bonus = reinforcementService.calculateContinentBonus(testGame, testPlayer);

        // Assert
        assertEquals(0, bonus);
    }

    @Test
    @DisplayName("Should get reinforcement status for current player")
    void getReinforcementStatus_CurrentPlayer() {
        // Arrange
        testGame.setCurrentPlayerIndex(0);
        List<Territory> territories = Arrays.asList(
                Territory.builder().id(1L).name("Argentina").ownerId(PLAYER_ID).build(),
                Territory.builder().id(2L).name("Brazil").ownerId(PLAYER_ID).build(),
                Territory.builder().id(3L).name("Peru").ownerId(PLAYER_ID).build(),
                Territory.builder().id(4L).name("Chile").ownerId(PLAYER_ID).build(),
                Territory.builder().id(5L).name("Uruguay").ownerId(PLAYER_ID).build(),
                Territory.builder().id(6L).name("Venezuela").ownerId(PLAYER_ID).build()
        );

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(gameTerritoryService.getTerritoriesByOwner(GAME_ID, PLAYER_ID)).thenReturn(territories);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(0);

        // Act
        ReinforcementStatusDto status = reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID);

        // Assert
        assertNotNull(status);
        assertEquals(PLAYER_ID, status.getPlayerId());
        assertEquals("Test Player", status.getPlayerName());
        assertEquals(GameState.NORMAL_PLAY, status.getGameState());
        assertEquals(TurnPhase.REINFORCEMENT, status.getCurrentPhase());
        assertTrue(status.getIsPlayerTurn());
        assertTrue(status.getCanReinforce());
        assertEquals(3, status.getArmiesToPlace()); // Should be assigned
        assertEquals(3, status.getBaseArmies());
        assertEquals(0, status.getContinentBonus());
        assertEquals(3, status.getTotalArmies());

        verify(playerService).addArmiesToPlace(PLAYER_ID, 3);
    }

    @Test
    @DisplayName("Should handle reinforcement status when not player's turn")
    void getReinforcementStatus_NotPlayerTurn() {
        // Arrange
        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(false);
        when(gameTerritoryService.getTerritoriesByOwner(GAME_ID, PLAYER_ID)).thenReturn(new ArrayList<>());

        // Act
        ReinforcementStatusDto status = reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID);

        // Assert
        assertNotNull(status);
        assertFalse(status.getIsPlayerTurn());
        assertFalse(status.getCanReinforce());
        assertEquals(0, status.getArmiesToPlace());
        assertEquals("Waiting for other player's turn", status.getMessage());
    }

    @Test
    @DisplayName("Should calculate total reinforcement armies correctly")
    void calculateReinforcementArmies_CompleteCalculation() {
        // Arrange
        testPlayer.setTerritoryIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)); // 10 territories

        Territory t1 = Territory.builder().id(1L).ownerId(PLAYER_ID).build();
        Territory t2 = Territory.builder().id(2L).ownerId(PLAYER_ID).build();
        testGame.getTerritories().put(1L, t1);
        testGame.getTerritories().put(2L, t2);

        Continent continent = Continent.builder()
                .id(1L)
                .name("Test Continent")
                .bonusArmies(5)
                .countryIds(Arrays.asList(1L, 2L))
                .build();
        testGame.getContinents().add(continent);

        // Act
        int totalArmies = reinforcementService.calculateReinforcementArmies(testGame, testPlayer);

        // Assert
        assertEquals(10, totalArmies); // 5 base (10/2) + 5 continent bonus
    }

    @Test
    @DisplayName("Should validate reinforcement can be performed")
    void canPerformReinforcement_AllConditionsMet() {
        // Arrange
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);

        // Act
        boolean canReinforce = reinforcementService.canPerformReinforcement(testGame, testPlayer);

        // Assert
        assertTrue(canReinforce);
    }

    @Test
    @DisplayName("Should not allow reinforcement in wrong game state")
    void canPerformReinforcement_WrongGameState() {
        // Arrange
        testGame.setState(GameState.REINFORCEMENT_5);

        // Act
        boolean canReinforce = reinforcementService.canPerformReinforcement(testGame, testPlayer);

        // Assert
        assertFalse(canReinforce);
    }

    @Test
    @DisplayName("Should not allow reinforcement in wrong phase")
    void canPerformReinforcement_WrongPhase() {
        // Arrange
        testGame.setCurrentPhase(TurnPhase.ATTACK);

        // Act
        boolean canReinforce = reinforcementService.canPerformReinforcement(testGame, testPlayer);

        // Assert
        assertFalse(canReinforce);
    }

    @Test
    @DisplayName("Should not allow reinforcement when not player's turn")
    void canPerformReinforcement_NotPlayerTurn() {
        // Arrange
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(false);

        // Act
        boolean canReinforce = reinforcementService.canPerformReinforcement(testGame, testPlayer);

        // Assert
        assertFalse(canReinforce);
    }

    @Test
    @DisplayName("Should not allow reinforcement when player has no territories")
    void canPerformReinforcement_NoTerritories() {
        // Arrange
        testPlayer.setTerritoryIds(new ArrayList<>());
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);

        // Act
        boolean canReinforce = reinforcementService.canPerformReinforcement(testGame, testPlayer);

        // Assert
        assertFalse(canReinforce);
    }

    @Test
    @DisplayName("Should throw exception when trying to place more armies than available")
    void placeReinforcementArmies_TooManyArmies() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(1L, 5); // Trying to place 5

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(3); // Only has 3

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should throw exception when player has no armies to place")
    void placeReinforcementArmies_NoArmiesToPlace() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(1L, 1);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(0); // No armies

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should throw exception when territory not found")
    void placeReinforcementArmies_TerritoryNotFound() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(999L, 1); // Non-existent territory

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(3);
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 999L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should not advance phase when player still has armies to place")
    void placeReinforcementArmies_PartialPlacement() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(1L, 1); // Placing only 1

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID))
                .thenReturn(3)  // Initial
                .thenReturn(2); // After placing 1

        Territory territory = Territory.builder().id(1L).ownerId(PLAYER_ID).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 1L)).thenReturn(territory);

        // Act
        reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry);

        // Assert
        verify(playerService).removeArmiesToPlace(PLAYER_ID, 1);
        verify(gameStateService, never()).changeTurnPhase(any(), any());
        verify(gameService, never()).save(any());
    }

    @Test
    @DisplayName("Should handle multiple territory placements in single request")
    void placeReinforcementArmies_MultipleTerritories() {
        // Arrange
        Map<Long, Integer> armiesByCountry = new HashMap<>();
        armiesByCountry.put(1L, 2);
        armiesByCountry.put(2L, 1);
        armiesByCountry.put(3L, 2);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(5);

        Territory territory1 = Territory.builder().id(1L).ownerId(PLAYER_ID).build();
        Territory territory2 = Territory.builder().id(2L).ownerId(PLAYER_ID).build();
        Territory territory3 = Territory.builder().id(3L).ownerId(PLAYER_ID).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 1L)).thenReturn(territory1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 2L)).thenReturn(territory2);
        when(gameTerritoryService.getTerritoryByGameAndCountry(GAME_ID, 3L)).thenReturn(territory3);

        // Act
        reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry);

        // Assert
        verify(gameTerritoryService).addArmiesToTerritory(GAME_ID, 1L, 2);
        verify(gameTerritoryService).addArmiesToTerritory(GAME_ID, 2L, 1);
        verify(gameTerritoryService).addArmiesToTerritory(GAME_ID, 3L, 2);
        verify(playerService).removeArmiesToPlace(PLAYER_ID, 5);
    }

    @Test
    @DisplayName("Should throw exception when placing negative armies")
    void placeReinforcementArmies_NegativeArmies() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(1L, -1);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(3);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should throw exception when placing zero armies")
    void placeReinforcementArmies_ZeroArmies() {
        // Arrange
        Map<Long, Integer> armiesByCountry = new HashMap<>();
        armiesByCountry.put(1L, 0);
        armiesByCountry.put(2L, 0);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(3);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should handle player not found exception")
    void placeReinforcementArmies_PlayerNotFound() {
        // Arrange
        Map<Long, Integer> armiesByCountry = Map.of(1L, 1);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PlayerNotFoundException.class, () ->
                reinforcementService.placeReinforcementArmies(GAME_CODE, PLAYER_ID, armiesByCountry)
        );
    }

    @Test
    @DisplayName("Should get status with controlled continents")
    void getReinforcementStatus_WithControlledContinents() {
        // Arrange
        List<Territory> territories = Arrays.asList(
                Territory.builder().id(1L).name("Argentina").ownerId(PLAYER_ID).build(),
                Territory.builder().id(2L).name("Brazil").ownerId(PLAYER_ID).build()
        );

        Territory t1 = Territory.builder().id(1L).ownerId(PLAYER_ID).build();
        Territory t2 = Territory.builder().id(2L).ownerId(PLAYER_ID).build();
        testGame.getTerritories().put(1L, t1);
        testGame.getTerritories().put(2L, t2);

        Continent continent = Continent.builder()
                .id(1L)
                .name("South America")
                .bonusArmies(2)
                .countryIds(Arrays.asList(1L, 2L))
                .build();
        testGame.getContinents().add(continent);

        when(gameService.findByGameCode(GAME_CODE)).thenReturn(testGame);
        when(playerService.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
        when(gameStateService.isPlayerTurn(testGame, PLAYER_ID)).thenReturn(true);
        when(gameTerritoryService.getTerritoriesByOwner(GAME_ID, PLAYER_ID)).thenReturn(territories);
        when(playerService.getArmiesToPlace(PLAYER_ID)).thenReturn(0);

        // Act
        ReinforcementStatusDto status = reinforcementService.getReinforcementStatus(GAME_CODE, PLAYER_ID);

        // Assert
        assertNotNull(status);
        assertEquals(1, status.getControlledContinents().size());
        assertEquals("South America", status.getControlledContinents().get(0));
        assertEquals(2, status.getContinentBonus());
    }
}
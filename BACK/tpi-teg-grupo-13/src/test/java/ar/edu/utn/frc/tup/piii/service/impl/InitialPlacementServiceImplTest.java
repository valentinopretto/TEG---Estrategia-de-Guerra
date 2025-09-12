package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialPlacementServiceImplTest {

    @Mock
    private GameService gameService;
    @Mock
    private PlayerService playerService;
    @Mock
    private GameTerritoryService gameTerritoryService;
    @Mock
    private GameStateService gameStateService;

    @InjectMocks
    private InitialPlacementServiceImpl initialPlacementService;

    private Game game;
    private Player player1;
    private Player player2;
    private Territory territory1;
    private Territory territory2;
    private Map<Long, Integer> armiesByCountry;

    @BeforeEach
    void setUp() {
        territory1 = Territory.builder()
                .id(1L)
                .name("Territory 1")
                .ownerId(1L)
                .armies(1)
                .build();

        territory2 = Territory.builder()
                .id(2L)
                .name("Territory 2")
                .ownerId(2L)
                .armies(1)
                .build();

        player1 = Player.builder()
                .id(1L)
                .username("player1")
                .status(PlayerStatus.ACTIVE)
                .seatOrder(0)
                .armiesToPlace(5)
                .territoryIds(Arrays.asList(1L))
                .build();

        player2 = Player.builder()
                .id(2L)
                .username("player2")
                .status(PlayerStatus.ACTIVE)
                .seatOrder(1)
                .armiesToPlace(5)
                .territoryIds(Arrays.asList(2L))
                .build();

        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.REINFORCEMENT_5)
                .currentPlayerIndex(0)
                .currentTurn(1)
                .players(Arrays.asList(player1, player2))
                .build();

        armiesByCountry = new HashMap<>();
        armiesByCountry.put(1L, 5);
    }

    @Test
    void placeInitialArmies_WhenGameNotInInitialPhase_ShouldThrowException() {
        game.setState(GameState.NORMAL_PLAY);
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));

        assertThatThrownBy(() -> initialPlacementService.placeInitialArmies("TEST123", 1L, armiesByCountry))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("Game is not in initial placement phase");
    }

    @Test
    void placeInitialArmies_WhenPlayerNotFound_ShouldThrowException() {
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> initialPlacementService.placeInitialArmies("TEST123", 999L, armiesByCountry))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("Player not found with id: 999");
    }

    @Test
    void placeInitialArmies_WhenNotPlayerTurn_ShouldThrowException() {
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);

        assertThatThrownBy(() -> initialPlacementService.placeInitialArmies("TEST123", 1L, armiesByCountry))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("It's not player's turn");
    }

    @Test
    void placeInitialArmies_WhenWrongArmiesCount_ShouldThrowException() {
        Map<Long, Integer> wrongArmies = new HashMap<>();
        wrongArmies.put(1L, 3);

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(playerService.getArmiesToPlace(1L)).thenReturn(5);

        assertThatThrownBy(() -> initialPlacementService.placeInitialArmies("TEST123", 1L, wrongArmies))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Must place exactly 5 armies, got 3");
    }

    @Test
    void placeInitialArmies_WhenPlayerDoesntOwnTerritory_ShouldThrowException() {
        Territory notOwnedTerritory = Territory.builder()
                .id(1L)
                .ownerId(999L)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(notOwnedTerritory);
        when(playerService.getArmiesToPlace(1L)).thenReturn(5);

        assertThatThrownBy(() -> initialPlacementService.placeInitialArmies("TEST123", 1L, armiesByCountry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player doesn't own territory: 1");
    }

    @Test
    void placeInitialArmies_WhenTerritoryNotFound_ShouldThrowException() {
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(null);
        when(playerService.getArmiesToPlace(1L)).thenReturn(5);

        assertThatThrownBy(() -> initialPlacementService.placeInitialArmies("TEST123", 1L, armiesByCountry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Territory not found: 1");
    }


    @Test
    void placeInitialArmies_WhenWrongArmiesForReinforcement3_ShouldThrowException() {
        game.setState(GameState.REINFORCEMENT_3);
        Map<Long, Integer> wrongArmies = new HashMap<>();
        wrongArmies.put(1L, 5);

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(playerService.getArmiesToPlace(1L)).thenReturn(3);

        assertThatThrownBy(() -> initialPlacementService.placeInitialArmies("TEST123", 1L, wrongArmies))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Must place exactly 3 armies, got 5");
    }

    @Test
    void placeInitialArmies_WhenNotAllPlayersCompleted_ShouldAdvanceToNextPlayer() {
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(territory1);
        when(playerService.getArmiesToPlace(1L)).thenReturn(5).thenReturn(0);
        when(playerService.getArmiesToPlace(2L)).thenReturn(3);
        doNothing().when(gameTerritoryService).addArmiesToTerritory(1L, 1L, 5);
        doNothing().when(playerService).removeArmiesToPlace(1L, 5);
        when(gameService.save(game)).thenReturn(game);

        initialPlacementService.placeInitialArmies("TEST123", 1L, armiesByCountry);

        assertThat(game.getCurrentPlayerIndex()).isEqualTo(1);
        verify(gameService).save(game);
    }

    @Test
    void getPlacementStatus_WhenInInitialPhase_ShouldReturnActiveStatus() {
        when(gameService.findByGameCode("TEST123")).thenReturn(game);

        InitialPlacementServiceImpl.InitialPlacementStatus status =
                initialPlacementService.getPlacementStatus("TEST123");

        assertThat(status.isActive()).isTrue();
        assertThat(status.getMessage()).isEqualTo("Initial placement in progress");
        assertThat(status.getCurrentPlayerId()).isEqualTo(1L);
        assertThat(status.getExpectedArmies()).isEqualTo(5);
    }

    @Test
    void getPlacementStatus_WhenNotInInitialPhase_ShouldReturnInactiveStatus() {
        game.setState(GameState.NORMAL_PLAY);
        when(gameService.findByGameCode("TEST123")).thenReturn(game);

        InitialPlacementServiceImpl.InitialPlacementStatus status =
                initialPlacementService.getPlacementStatus("TEST123");

        assertThat(status.isActive()).isFalse();
        assertThat(status.getMessage()).isEqualTo("Game is not in initial placement phase");
        assertThat(status.getCurrentPlayerId()).isNull();
        assertThat(status.getExpectedArmies()).isEqualTo(0);
    }

    @Test
    void getPlacementStatus_WhenNoCurrentPlayer_ShouldReturnErrorStatus() {
        game.setCurrentPlayerIndex(999);
        when(gameService.findByGameCode("TEST123")).thenReturn(game);

        InitialPlacementServiceImpl.InitialPlacementStatus status =
                initialPlacementService.getPlacementStatus("TEST123");

        assertThat(status.isActive()).isFalse();
        assertThat(status.getMessage()).isEqualTo("No current player found");
        assertThat(status.getCurrentPlayerId()).isNull();
    }

    @Test
    void getPlayerStatus_WhenIsPlayerTurn_ShouldReturnCorrectStatus() {
        List<Territory> playerTerritories = Arrays.asList(territory1);

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(playerService.getArmiesToPlace(1L)).thenReturn(5);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(playerTerritories);

        InitialPlacementServiceImpl.PlayerInitialStatus status =
                initialPlacementService.getPlayerStatus("TEST123", 1L);

        assertThat(status.getPlayerId()).isEqualTo(1L);
        assertThat(status.isPlayerTurn()).isTrue();
        assertThat(status.getArmiesToPlace()).isEqualTo(5);
        assertThat(status.getCurrentPhase()).isEqualTo(GameState.REINFORCEMENT_5);
        assertThat(status.getExpectedArmiesThisRound()).isEqualTo(5);
        assertThat(status.getOwnedTerritoryIds()).containsExactly(1L);
        assertThat(status.isCanPlaceArmies()).isTrue();
        assertThat(status.getMessage()).isEqualTo("Your turn to place armies");
    }

    @Test
    void getPlayerStatus_WhenNotPlayerTurn_ShouldReturnWaitingStatus() {
        List<Territory> playerTerritories = Arrays.asList(territory1);

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);
        when(playerService.getArmiesToPlace(1L)).thenReturn(3);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(playerTerritories);

        InitialPlacementServiceImpl.PlayerInitialStatus status =
                initialPlacementService.getPlayerStatus("TEST123", 1L);

        assertThat(status.isPlayerTurn()).isFalse();
        assertThat(status.isCanPlaceArmies()).isFalse();
        assertThat(status.getMessage()).isEqualTo("Waiting for other players");
    }

    @Test
    void getPlayerStatus_WhenPlayerNotFound_ShouldThrowException() {
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> initialPlacementService.getPlayerStatus("TEST123", 999L))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("Player not found");
    }

    @Test
    void getPlayerStatus_InReinforcement3Phase_ShouldReturnCorrectExpectedArmies() {
        game.setState(GameState.REINFORCEMENT_3);
        List<Territory> playerTerritories = Arrays.asList(territory1);

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(playerService.getArmiesToPlace(1L)).thenReturn(3);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(playerTerritories);

        InitialPlacementServiceImpl.PlayerInitialStatus status =
                initialPlacementService.getPlayerStatus("TEST123", 1L);

        assertThat(status.getCurrentPhase()).isEqualTo(GameState.REINFORCEMENT_3);
        assertThat(status.getExpectedArmiesThisRound()).isEqualTo(3);
    }

    @Test
    void getPlayerStatus_WhenNotInInitialPhase_ShouldReturnZeroExpectedArmies() {
        game.setState(GameState.NORMAL_PLAY);
        List<Territory> playerTerritories = Arrays.asList(territory1);

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player1));
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(playerService.getArmiesToPlace(1L)).thenReturn(0);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(playerTerritories);

        InitialPlacementServiceImpl.PlayerInitialStatus status =
                initialPlacementService.getPlayerStatus("TEST123", 1L);

        assertThat(status.getCurrentPhase()).isEqualTo(GameState.NORMAL_PLAY);
        assertThat(status.getExpectedArmiesThisRound()).isEqualTo(0);
    }
}
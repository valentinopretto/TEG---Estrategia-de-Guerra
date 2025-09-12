package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import ar.edu.utn.frc.tup.piii.service.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameInitializationServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private ObjectiveService objectiveService;

    @Mock
    private GameTerritoryService gameTerritoryService;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameInitializationServiceImpl gameInitializationService;

    private GameEntity gameEntity;
    private Game game;
    private List<PlayerEntity> playerEntities;
    private List<Player> players;
    private List<Territory> territories;
    private List<Objective> objectives;

    @BeforeEach
    void setUp() {
        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setStatus(GameState.WAITING_FOR_PLAYERS);

        PlayerEntity player1 = new PlayerEntity();
        player1.setId(1L);
        player1.setStatus(PlayerStatus.WAITING);
        player1.setColor(PlayerColor.RED);

        PlayerEntity player2 = new PlayerEntity();
        player2.setId(2L);
        player2.setStatus(PlayerStatus.WAITING);
        player2.setColor(PlayerColor.BLUE);

        PlayerEntity player3 = new PlayerEntity();
        player3.setId(3L);
        player3.setStatus(PlayerStatus.WAITING);
        player3.setColor(PlayerColor.GREEN);

        playerEntities = new ArrayList<>(Arrays.asList(player1, player2, player3));
        gameEntity.setPlayers(playerEntities);

        game = new Game();
        game.setId(1L);
        game.setState(GameState.WAITING_FOR_PLAYERS);

        Player modelPlayer1 = Player.builder()
                .id(1L)
                .status(PlayerStatus.WAITING)
                .color(PlayerColor.RED)
                .seatOrder(0)
                .build();

        Player modelPlayer2 = Player.builder()
                .id(2L)
                .status(PlayerStatus.WAITING)
                .color(PlayerColor.BLUE)
                .seatOrder(1)
                .build();

        Player modelPlayer3 = Player.builder()
                .id(3L)
                .status(PlayerStatus.WAITING)
                .color(PlayerColor.GREEN)
                .seatOrder(2)
                .build();

        players = new ArrayList<>(Arrays.asList(modelPlayer1, modelPlayer2, modelPlayer3));
        game.setPlayers(players);

        Territory territory1 = Territory.builder().id(1L).name("Argentina").build();
        Territory territory2 = Territory.builder().id(2L).name("Brazil").build();
        Territory territory3 = Territory.builder().id(3L).name("Chile").build();
        Territory territory4 = Territory.builder().id(4L).name("Uruguay").build();
        Territory territory5 = Territory.builder().id(5L).name("Paraguay").build();
        Territory territory6 = Territory.builder().id(6L).name("Bolivia").build();

        territories = new ArrayList<>(Arrays.asList(territory1, territory2, territory3, territory4, territory5, territory6));

        Objective occupationObjective1 = Objective.builder()
                .id(1L)
                .type(ObjectiveType.OCCUPATION)
                .description("Occupy 18 territories")
                .targetData("NORTH_AMERICA,AFRICA")
                .build();

        Objective occupationObjective2 = Objective.builder()
                .id(2L)
                .type(ObjectiveType.OCCUPATION)
                .description("Occupy Europe and Asia")
                .targetData("EUROPE,ASIA")
                .build();

        Objective destructionObjective = Objective.builder()
                .id(3L)
                .type(ObjectiveType.DESTRUCTION)
                .description("Destroy enemy player")
                .targetData(String.valueOf(PlayerColor.YELLOW))
                .build();

        objectives = new ArrayList<>(Arrays.asList(occupationObjective1, occupationObjective2, destructionObjective));
    }

    @Test
    void initializeGame_WithValidGameState_ShouldInitializeSuccessfully() {
        // Given
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(any(GameEntity.class))).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(gameMapper).toModel(gameEntity);
        verify(playerRepository, times(2)).saveAll(any());
        verify(gameTerritoryService).getAllAvailableTerritories();
        verify(objectiveService).findByType(ObjectiveType.OCCUPATION);
        verify(objectiveService).findByType(ObjectiveType.DESTRUCTION);
        verify(playerService, times(3)).assignObjective(anyLong(), any(Objective.class));
        verify(playerService, times(3)).addArmiesToPlace(anyLong(), eq(5));
        verify(gameRepository).save(gameEntity);

        assertThat(gameEntity.getStatus()).isEqualTo(GameState.REINFORCEMENT_5);
        assertThat(gameEntity.getCurrentPhase()).isEqualTo(TurnPhase.REINFORCEMENT);
        assertThat(gameEntity.getCurrentTurn()).isEqualTo(1);
        assertThat(gameEntity.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(gameEntity.getStartedAt()).isNotNull();
    }

    @Test
    void initializeGame_WithInvalidGameState_ShouldThrowException() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When & Then
        assertThatThrownBy(() -> gameInitializationService.initializeGame(gameEntity))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("Cannot start game. Current state: NORMAL_PLAY");

        verify(playerRepository, never()).saveAll(any());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void initializeGame_WithInsufficientPlayers_ShouldThrowException() {

        players.forEach(p -> p.setStatus(PlayerStatus.ACTIVE));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        assertThatThrownBy(() -> gameInitializationService.initializeGame(gameEntity))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessageContaining("Minimum 2 players required to start");

        verify(playerRepository, never()).saveAll(any());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void assignSeatOrderFixed_ShouldAssignSeatOrdersAndSetPlayersActive() {
        // Given
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(any(GameEntity.class))).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerRepository, atLeast(1)).saveAll(argThat(playerList -> {
            List<PlayerEntity> players = (List<PlayerEntity>) playerList;
            boolean allHaveSeatOrder = players.stream().allMatch(p -> p.getSeatOrder() != null);
            boolean allAreActive = players.stream().allMatch(p -> p.getStatus() == PlayerStatus.ACTIVE);
            boolean allHaveGame = players.stream().allMatch(p -> p.getGame() != null);
            return allHaveSeatOrder && allAreActive && allHaveGame;
        }));
    }

    @Test
    void assignSeatOrderFixed_WithNullArmiesToPlace_ShouldSetToZero() {
        // Given
        playerEntities.get(0).setArmiesToPlace(null);
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerRepository, atLeast(1)).saveAll(argThat(playerList -> {
            List<PlayerEntity> players = (List<PlayerEntity>) playerList;
            return players.stream().allMatch(p -> p.getArmiesToPlace() != null && p.getArmiesToPlace() >= 0);
        }));
    }

    @Test
    void assignSeatOrderFixed_WithNullJoinedAt_ShouldSetCurrentTime() {
        // Given
        playerEntities.get(0).setJoinedAt(null);
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerRepository, atLeast(1)).saveAll(argThat(playerList -> {
            List<PlayerEntity> players = (List<PlayerEntity>) playerList;
            return players.stream().allMatch(p -> p.getJoinedAt() != null);
        }));
    }

    @Test
    void distributeCountries_ShouldAssignTerritoriesEvenly() {
        // Given
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(gameTerritoryService, times(territories.size()))
                .assignTerritoryToPlayer(eq(1L), anyLong(), anyLong(), eq(1));
    }

    @Test
    void assignObjectives_ShouldAssignObjectiveToEachPlayer() {
        // Given
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerService, times(3)).assignObjective(anyLong(), any(Objective.class));
    }

    @Test
    void assignObjectives_WithDestructionObjectiveTargetingSameColor_ShouldAssignRightPlayerColor() {
        // Given
        Objective destructionObjective = Objective.builder()
                .id(3L)
                .type(ObjectiveType.DESTRUCTION)
                .description("Destroy red player")
                .targetData(String.valueOf(PlayerColor.RED))
                .build();

        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(Arrays.asList(destructionObjective));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerService, times(3)).assignObjective(anyLong(), any(Objective.class));
    }

    @Test
    void assignObjectives_WithDestructionObjectiveTargetingNonExistentColor_ShouldAssignRightPlayerColor() {
        // Given
        Objective destructionObjective = Objective.builder()
                .id(3L)
                .type(ObjectiveType.DESTRUCTION)
                .description("Destroy yellow player")
                .targetData(String.valueOf(PlayerColor.YELLOW)) // Color not in game
                .build();

        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(Arrays.asList(destructionObjective));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerService, times(3)).assignObjective(anyLong(), any(Objective.class));
    }

    @Test
    void prepareInitialPlacement_ShouldAddFiveArmiesToEachPlayer() {
        // Given
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerService, times(3)).addArmiesToPlace(anyLong(), eq(5));
    }

    @Test
    void setAllPlayersActive_ShouldSetAllNonEliminatedPlayersToActive() {
        // Given
        PlayerEntity eliminatedPlayer = new PlayerEntity();
        eliminatedPlayer.setId(4L);
        eliminatedPlayer.setStatus(PlayerStatus.ELIMINATED);

        List<PlayerEntity> allPlayers = new ArrayList<>(playerEntities);
        allPlayers.add(eliminatedPlayer);
        gameEntity.setPlayers(allPlayers);


        Player eliminatedModelPlayer = Player.builder()
                .id(4L)
                .status(PlayerStatus.ELIMINATED)
                .color(PlayerColor.YELLOW)
                .build();
        List<Player> allModelPlayers = new ArrayList<>(players);
        allModelPlayers.add(eliminatedModelPlayer);
        game.setPlayers(allModelPlayers);

        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(allPlayers);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(playerRepository, atLeast(1)).saveAll(argThat(playerList -> {
            List<PlayerEntity> players = (List<PlayerEntity>) playerList;
            long activeCount = players.stream()
                    .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                    .count();
            long eliminatedCount = players.stream()
                    .filter(p -> p.getStatus() == PlayerStatus.ELIMINATED)
                    .count();
            return activeCount >= 3 && eliminatedCount <= 1;
        }));
    }

    @Test
    void setupGameState_ShouldSetCorrectInitialGameState() {
        // Given
        List<Objective> occupationObjectives = new ArrayList<>(objectives.subList(0, 2));
        List<Objective> destructionObjectives = new ArrayList<>(objectives.subList(2, 3));

        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(territories);
        when(objectiveService.findByType(ObjectiveType.OCCUPATION)).thenReturn(occupationObjectives);
        when(objectiveService.findByType(ObjectiveType.DESTRUCTION)).thenReturn(destructionObjectives);
        when(playerRepository.saveAll(any())).thenReturn(playerEntities);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        // When
        gameInitializationService.initializeGame(gameEntity);

        // Then
        verify(gameRepository).save(argThat(game ->
                game.getStatus() == GameState.REINFORCEMENT_5 &&
                        game.getCurrentPhase() == TurnPhase.REINFORCEMENT &&
                        game.getCurrentTurn() == 1 &&
                        game.getCurrentPlayerIndex() == 0 &&
                        game.getStartedAt() != null
        ));
    }


}
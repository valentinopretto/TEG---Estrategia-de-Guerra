package ar.edu.utn.frc.tup.piii.FactoryBots;

import ar.edu.utn.frc.tup.piii.FactoryBots.NoviceStrategies.NoviceAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
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
class NoviceAggressiveExecutorTest {

    @Mock private CombatService combatService;
    @Mock private FortificationService fortificationService;
    @Mock private GameTerritoryService gameTerritoryService;
    @Mock private GameStateService gameStateService;
    @Mock private InitialPlacementService initialPlacementService;
    @Mock private PlayerService playerService;
    @Mock private IGameEventService gameEventService;
    @Mock private GameMapper gameMapper;
    @Mock private GameService gameService;
    @Mock private ReinforcementService reinforcementService;

    @InjectMocks
    private NoviceAggressiveExecutor executor;

    private PlayerEntity botPlayer;
    private GameEntity game;
    private BotProfileEntity botProfile;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Setup bot profile
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setBotName("TestBot");
        botProfile.setLevel(BotLevel.NOVICE);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);

        // Setup user
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("TestUser");

        // Setup bot player
        botPlayer = new PlayerEntity();
        botPlayer.setId(1L);
        botPlayer.setBotProfile(botProfile);
        botPlayer.setUser(userEntity);

        // Setup game
        game = new GameEntity();
        game.setId(1L);
        game.setGameCode("TEST_GAME");
        game.setStatus(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        game.setCurrentTurn(1);
        game.setCurrentPlayerIndex(0);
    }

    @Test
    void testGetLevel() {
        assertEquals(BotLevel.NOVICE, executor.getLevel());
    }

    @Test
    void testGetStrategy() {
        assertEquals(BotStrategy.AGGRESSIVE, executor.getStrategy());
    }

    @Test
    void testExecuteTurn_REINFORCEMENT_5() {
        // Arrange
        game.setStatus(GameState.REINFORCEMENT_5);
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameEventService).recordTurnStart(game.getId(), botPlayer.getId(), game.getCurrentTurn());
        verify(gameEventService).recordTurnEnd(game.getId(), botPlayer.getId(), game.getCurrentTurn());
        verify(initialPlacementService).placeInitialArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testExecuteTurn_REINFORCEMENT_3() {
        // Arrange
        game.setStatus(GameState.REINFORCEMENT_3);
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(initialPlacementService).placeInitialArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.ATTACK));
    }

    @Test
    void testExecuteTurn_HOSTILITY_ONLY() {
        // Arrange
        game.setStatus(GameState.HOSTILITY_ONLY);
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameStateService, times(2)).changeTurnPhase(any(Game.class), any(TurnPhase.class));
    }

    @Test
    void testExecuteTurn_NORMAL_PLAY() {
        // Arrange
        game.setStatus(GameState.NORMAL_PLAY);
        Player player = createMockPlayer();
        Game gameModel = createMockGame();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(true);
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong()))
                .thenReturn(createMockReinforcementStatus());
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameStateService, times(3)).changeTurnPhase(any(Game.class), any(TurnPhase.class));
    }

    @Test
    void testExecuteTurn_WithException() {
        // Arrange
        game.setStatus(GameState.NORMAL_PLAY);
        when(playerService.findById(botPlayer.getId())).thenThrow(new RuntimeException("Test exception"));

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameEventService).recordEvent(eq(game.getId()), eq(botPlayer.getId()),
                eq(EventType.TURN_ENDED), eq(game.getCurrentTurn()), anyString());
    }

    @Test
    void testExecuteTurn_UnhandledState() {
        // Arrange
        game.setStatus(GameState.FINISHED);

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameEventService).recordTurnStart(game.getId(), botPlayer.getId(), game.getCurrentTurn());
        verify(gameEventService).recordTurnEnd(game.getId(), botPlayer.getId(), game.getCurrentTurn());
    }

    @Test
    void testPerformInitialPlacement_WithTerritories() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong())).thenReturn(territories);

        // Act - usando reflection para acceder al método privado
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("performInitialPlacement",
                    PlayerEntity.class, GameEntity.class, int.class);
            method.setAccessible(true);
            method.invoke(executor, botPlayer, game, 5);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }

        // Assert
        verify(initialPlacementService).placeInitialArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testPerformInitialPlacement_EmptyTerritories() {
        // Arrange
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("performInitialPlacement",
                    PlayerEntity.class, GameEntity.class, int.class);
            method.setAccessible(true);
            method.invoke(executor, botPlayer, game, 5);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }

        // Assert
        verify(initialPlacementService, never()).placeInitialArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformInitialPlacement_WithException() {
        // Arrange
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("performInitialPlacement",
                    PlayerEntity.class, GameEntity.class, int.class);
            method.setAccessible(true);
            method.invoke(executor, botPlayer, game, 5);
        } catch (Exception e) {
            // Expected exception from reflection
        }

        // Assert
        verify(initialPlacementService, never()).placeInitialArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformBotReinforcement_CannotPerform() {
        // Arrange
        Player player = createMockPlayer();
        Game gameModel = createMockGame();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(false);

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService, never()).getReinforcementStatus(anyString(), anyLong());
    }

    @Test
    void testPerformBotReinforcement_NoReinforcements() {
        // Arrange
        Player player = createMockPlayer();
        Game gameModel = createMockGame();
        ReinforcementStatusDto status = ReinforcementStatusDto.builder().armiesToPlace(0).build();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(true);
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong())).thenReturn(status);

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService, never()).placeReinforcementArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformBotReinforcement_EmptyTerritories() {
        // Arrange
        Player player = createMockPlayer();
        Game gameModel = createMockGame();
        ReinforcementStatusDto status = createMockReinforcementStatus();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(true);
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong())).thenReturn(status);
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService, never()).placeReinforcementArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformBotReinforcement_WithBorderTerritories() {
        // Arrange
        Player player = createMockPlayer();
        Game gameModel = createMockGame();
        ReinforcementStatusDto status = createMockReinforcementStatus();
        List<Territory> territories = createMockTerritories();
        List<Territory> enemyNeighbors = createEnemyTerritories();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(true);
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong())).thenReturn(status);
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong())).thenReturn(territories);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(enemyNeighbors);

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService).placeReinforcementArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
        verify(gameEventService).recordReinforcementsPlaced(anyLong(), anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    void testPerformBotReinforcement_NoBorderTerritories() {
        // Arrange
        Player player = createMockPlayer();
        Game gameModel = createMockGame();
        ReinforcementStatusDto status = createMockReinforcementStatus();
        List<Territory> territories = createMockTerritories();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(true);
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong())).thenReturn(status);
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong())).thenReturn(territories);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(territories); // All neighbors owned by same player

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService).placeReinforcementArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testPerformBotReinforcement_WithException() {
        // Arrange
        when(playerService.findById(botPlayer.getId())).thenThrow(new RuntimeException("Test exception"));

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert - Should not throw exception
        verify(reinforcementService, never()).placeReinforcementArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformBotAttack_NoAttackableTerritories() {
        // Arrange
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).getAttackableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
        verify(combatService, never()).getTargetsForTerritory(anyString(), anyLong(), anyLong());
    }

    @Test
    void testPerformBotAttack_NoTargets() {
        // Arrange
        List<Territory> attackers = createMockTerritories();
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService, never()).performCombat(anyString(), any(AttackDto.class));
    }

    @Test
    void testPerformBotAttack_SuccessfulAttack() {
        // Arrange
        List<Territory> attackers = createMockTerritories();
        List<Territory> targets = createEnemyTerritories();
        CombatResultDto result = createSuccessfulCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);
        when(playerService.findById(targets.get(0).getOwnerId())).thenReturn(Optional.of(createMockPlayer()));

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).performCombat(eq(game.getGameCode()), any(AttackDto.class));
        verify(gameEventService).recordAttack(anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(gameEventService).recordTerritoryConquest(anyLong(), anyLong(), anyString(), anyString(), anyInt());
    }

    @Test
    void testPerformBotAttack_FailedAttack() {
        // Arrange
        List<Territory> attackers = createMockTerritories();
        List<Territory> targets = createEnemyTerritories();
        CombatResultDto result = createFailedCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(gameEventService).recordAttack(anyLong(), anyLong(), anyString(), anyString(), anyInt(), eq(false));
        verify(gameEventService, never()).recordTerritoryConquest(anyLong(), anyLong(), anyString(), anyString(), anyInt());
    }

    @Test
    void testPerformBotAttack_MaxAttacksReached() {
        // Arrange
        List<Territory> attackers = createManyTerritories(5);
        List<Territory> targets = createEnemyTerritories();
        CombatResultDto result = createSuccessfulCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);
        when(playerService.findById(targets.get(0).getOwnerId())).thenReturn(Optional.of(createMockPlayer()));

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - Should only attack 3 times (max for NOVICE)
        verify(combatService, times(3)).performCombat(eq(game.getGameCode()), any(AttackDto.class));
    }

    @Test
    void testPerformBotAttack_InsufficientArmies() {
        // Arrange
        List<Territory> attackers = createWeakTerritories();
        List<Territory> targets = createEnemyTerritories();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - Should not attack with insufficient armies
        verify(combatService, never()).performCombat(anyString(), any(AttackDto.class));
    }

    @Test
    void testPerformBotAttack_WithException() {
        // Arrange
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - Should not throw exception
        verify(combatService, never()).performCombat(anyString(), any(AttackDto.class));
    }

    @Test
    void testPerformBotFortify_NoFortifiableTerritories() {
        // Arrange
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService, never()).getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong());
    }

    @Test
    void testPerformBotFortify_NoSafeTerritories() {
        // Arrange
        List<Territory> fortifiable = createMockTerritories();
        List<Territory> enemyNeighbors = createEnemyTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(enemyNeighbors);

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService, never()).getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong());
    }

    @Test
    void testPerformBotFortify_NoTargets() {
        // Arrange
        List<Territory> fortifiable = createSafeTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService, never()).getMaxMovableArmies(anyString(), anyLong());
    }

    @Test
    void testPerformBotFortify_NoMovableArmies() {
        // Arrange
        List<Territory> fortifiable = createSafeTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong())).thenReturn(0);

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService, never()).performFortification(anyString(), any(FortifyDto.class));
    }

    @Test
    void testPerformBotFortify_SuccessfulFortification() {
        // Arrange
        List<Territory> fortifiable = createSafeTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong())).thenReturn(5);
        when(fortificationService.performFortification(eq(game.getGameCode()), any(FortifyDto.class))).thenReturn(true);

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).performFortification(eq(game.getGameCode()), any(FortifyDto.class));
        verify(gameEventService).recordFortification(anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void testPerformBotFortify_FailedFortification() {
        // Arrange
        List<Territory> fortifiable = createSafeTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong())).thenReturn(5);
        when(fortificationService.performFortification(eq(game.getGameCode()), any(FortifyDto.class))).thenReturn(false);

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).performFortification(eq(game.getGameCode()), any(FortifyDto.class));
        verify(gameEventService, never()).recordFortification(anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void testPerformBotFortify_WithException() {
        // Arrange
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert - Should not throw exception
        verify(fortificationService, never()).performFortification(anyString(), any(FortifyDto.class));
    }

    @Test
    void testGetPlayerName_WithUser() {
        // Arrange
        Player player = createMockPlayerWithUser();
        when(playerService.findById(anyLong())).thenReturn(Optional.of(player));

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("getPlayerName", Long.class);
            method.setAccessible(true);
            String result = (String) method.invoke(executor, 1L);

            // Assert
            assertEquals("TestUser", result);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }
    }

    @Test
    void testGetPlayerName_WithBot() {
        // Arrange
        Player player = createMockPlayerWithBot();
        when(playerService.findById(anyLong())).thenReturn(Optional.of(player));

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("getPlayerName", Long.class);
            method.setAccessible(true);
            String result = (String) method.invoke(executor, 1L);

            // Assert
            assertEquals("TestBot", result);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }
    }

    @Test
    void testGetPlayerName_PlayerNotFound() {
        // Arrange
        when(playerService.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("getPlayerName", Long.class);
            method.setAccessible(true);
            String result = (String) method.invoke(executor, 1L);

            // Assert
            assertEquals("Jugador Desconocido", result);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }
    }

    @Test
    void testGetPlayerName_WithException() {
        // Arrange
        when(playerService.findById(anyLong())).thenThrow(new RuntimeException("Test exception"));

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("getPlayerName", Long.class);
            method.setAccessible(true);
            String result = (String) method.invoke(executor, 1L);

            // Assert
            assertEquals("Jugador Desconocido", result);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }
    }

    @Test
    void testAdvanceToNextPhase_AllPhases() {
        // Test REINFORCEMENT to ATTACK
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        Game gameModel = createMockGame();
        when(gameMapper.toModel(game)).thenReturn(gameModel);

        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("advanceToNextPhase", GameEntity.class);
            method.setAccessible(true);
            method.invoke(executor, game);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }

        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.ATTACK));

        // Test ATTACK to FORTIFY
        game.setCurrentPhase(TurnPhase.ATTACK);
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("advanceToNextPhase", GameEntity.class);
            method.setAccessible(true);
            method.invoke(executor, game);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }

        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.FORTIFY));

        // Test FORTIFY to END_TURN
        game.setCurrentPhase(TurnPhase.FORTIFY);
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("advanceToNextPhase", GameEntity.class);
            method.setAccessible(true);
            method.invoke(executor, game);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }

        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.END_TURN));

        // Test END_TURN to REINFORCEMENT
        game.setCurrentPhase(TurnPhase.END_TURN);
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("advanceToNextPhase", GameEntity.class);
            method.setAccessible(true);
            method.invoke(executor, game);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }

        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.REINFORCEMENT));
    }


    @Test
    void testAdvanceToNextPhase_WithException() {
        // Arrange
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        when(gameMapper.toModel(game)).thenThrow(new RuntimeException("Test exception"));

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("advanceToNextPhase", GameEntity.class);
            method.setAccessible(true);
            method.invoke(executor, game);
        } catch (Exception e) {
            // Expected exception from reflection or internal error
        }

        // Assert - Should handle exception gracefully
    }

    @Test
    void testEvaluateAttackProbability() {
        // Test all scenarios
        assertEquals(0, executor.evaluateAttackProbability(botPlayer, 1, 5));
        assertEquals(0, executor.evaluateAttackProbability(botPlayer, 0, 5));
        assertEquals(0, executor.evaluateAttackProbability(botPlayer, 2, 3));
    }

    @Test
    void testGetBestAttackTargets() {
        List<CountryEntity> result = executor.getBestAttackTargets(botPlayer, game);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetBestDefensePositions() {
        List<CountryEntity> result = executor.getBestDefensePositions(botPlayer, game);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDistributeInitialArmies_BorderTerritories() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        List<Territory> enemyNeighbors = createEnemyTerritories();

        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(enemyNeighbors);

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("distributeInitialArmies",
                    List.class, GameEntity.class, Long.class, int.class);
            method.setAccessible(true);
            Map<Long, Integer> result = (Map<Long, Integer>) method.invoke(executor, territories, game, botPlayer.getId(), 5);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }
    }

    @Test
    void testDistributeInitialArmies_NoBorderTerritories() {
        // Arrange
        List<Territory> territories = createMockTerritories();

        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(territories);

        // Act
        try {
            var method = NoviceAggressiveExecutor.class.getDeclaredMethod("distributeInitialArmies",
                    List.class, GameEntity.class, Long.class, int.class);
            method.setAccessible(true);
            Map<Long, Integer> result = (Map<Long, Integer>) method.invoke(executor, territories, game, botPlayer.getId(), 5);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }
    }

    // Helper methods for creating test data
    private List<Territory> createMockTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory t1 = new Territory();
        t1.setId(1L);
        t1.setName("Territory1");
        t1.setOwnerId(botPlayer.getId());
        t1.setArmies(3);
        t1.setNeighborIds(Set.of(2L, 3L));

        Territory t2 = new Territory();
        t2.setId(2L);
        t2.setName("Territory2");
        t2.setOwnerId(botPlayer.getId());
        t2.setArmies(5);
        t2.setNeighborIds(Set.of(1L, 3L));

        territories.add(t1);
        territories.add(t2);

        return territories;
    }

    private List<Territory> createEnemyTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory enemy1 = new Territory();
        enemy1.setId(10L);
        enemy1.setName("EnemyTerritory1");
        enemy1.setOwnerId(2L);
        enemy1.setArmies(2);
        enemy1.setNeighborIds(Set.of(1L));

        Territory enemy2 = new Territory();
        enemy2.setId(11L);
        enemy2.setName("EnemyTerritory2");
        enemy2.setOwnerId(3L);
        enemy2.setArmies(1);
        enemy2.setNeighborIds(Set.of(2L));

        territories.add(enemy1);
        territories.add(enemy2);

        return territories;
    }

    private List<Territory> createManyTerritories(int count) {
        List<Territory> territories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Territory t = new Territory();
            t.setId((long) i + 1);
            t.setName("Territory" + (i + 1));
            t.setOwnerId(botPlayer.getId());
            t.setArmies(5);
            t.setNeighborIds(Set.of((long) i + 10));
            territories.add(t);
        }
        return territories;
    }

    private List<Territory> createWeakTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory weak = new Territory();
        weak.setId(1L);
        weak.setName("WeakTerritory");
        weak.setOwnerId(botPlayer.getId());
        weak.setArmies(1); // Only 1 army, cannot attack
        weak.setNeighborIds(Set.of(2L));

        territories.add(weak);
        return territories;
    }

    private List<Territory> createSafeTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory safe = new Territory();
        safe.setId(1L);
        safe.setName("SafeTerritory");
        safe.setOwnerId(botPlayer.getId());
        safe.setArmies(10);
        safe.setNeighborIds(Set.of(2L));

        territories.add(safe);
        return territories;
    }

    private Player createMockPlayer() {
        Player player = new Player();
        player.setId(botPlayer.getId());
        player.setDisplayName("TestPlayer");
        player.setIsBot(true);
        player.setBotProfile(botProfile);
        return player;
    }

    private Player createMockPlayerWithUser() {
        Player player = new Player();
        player.setId(1L);
        player.setUsername("TestUser");
        player.setDisplayName("TestUser");
        player.setIsBot(false);
        return player;
    }

    private Player createMockPlayerWithBot() {
        Player player = new Player();
        player.setId(1L);
        player.setDisplayName("TestBot");
        player.setIsBot(true);
        player.setBotProfile(botProfile);
        return player;
    }

    private Game createMockGame() {
        Game game = new Game();
        game.setId(this.game.getId());
        game.setGameCode(this.game.getGameCode());
        game.setState(this.game.getStatus());
        game.setCurrentPhase(this.game.getCurrentPhase());
        game.setCurrentTurn(this.game.getCurrentTurn());
        game.setCurrentPlayerIndex(this.game.getCurrentPlayerIndex());
        return game;
    }

    private ReinforcementStatusDto createMockReinforcementStatus() {
        return ReinforcementStatusDto.builder()
                .playerId(botPlayer.getId())
                .armiesToPlace(5)
                .baseArmies(3)
                .continentBonus(2)
                .totalArmies(5)
                .isPlayerTurn(true)
                .canReinforce(true)
                .build();
    }

    private CombatResultDto createSuccessfulCombatResult() {
        CombatResultDto result = new CombatResultDto();
        result.setAttackerCountryId(1L);
        result.setDefenderCountryId(10L);
        result.setAttackerCountryName("Territory1");
        result.setDefenderCountryName("EnemyTerritory1");
        result.setTerritoryConquered(true);
        result.setAttackerLosses(1);
        result.setDefenderLosses(2);
        return result;
    }

    private CombatResultDto createFailedCombatResult() {
        CombatResultDto result = new CombatResultDto();
        result.setAttackerCountryId(1L);
        result.setDefenderCountryId(10L);
        result.setAttackerCountryName("Territory1");
        result.setDefenderCountryName("EnemyTerritory1");
        result.setTerritoryConquered(false);
        result.setAttackerLosses(2);
        result.setDefenderLosses(1);
        return result;
    }
}
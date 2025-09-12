package ar.edu.utn.frc.tup.piii.FactoryBots;

import ar.edu.utn.frc.tup.piii.FactoryBots.BalancedStrategies.BalancedAggressiveExecutor;
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

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalancedAggressiveExecutorTest {

    @Mock private CombatService combatService;
    @Mock private FortificationService fortificationService;
    @Mock private ReinforcementService reinforcementService;
    @Mock private GameTerritoryService gameTerritoryService;
    @Mock private GameStateService gameStateService;
    @Mock private InitialPlacementService initialPlacementService;
    @Mock private PlayerService playerService;
    @Mock private GameMapper gameMapper;
    @Mock private GameService gameService;
    @Mock private IGameEventService gameEventService;

    @InjectMocks
    private BalancedAggressiveExecutor executor;

    private PlayerEntity botPlayer;
    private GameEntity game;
    private BotProfileEntity botProfile;

    @BeforeEach
    void setUp() {
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setBotName("BalancedBot");
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);

        botPlayer = new PlayerEntity();
        botPlayer.setId(1L);
        botPlayer.setBotProfile(botProfile);

        game = new GameEntity();
        game.setId(1L);
        game.setGameCode("TEST_GAME");
        game.setStatus(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        game.setCurrentTurn(1);
    }

    @Test
    void testGetLevel() {
        assertEquals(BotLevel.BALANCED, executor.getLevel());
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
        verify(initialPlacementService).placeInitialArmies(
                eq(game.getGameCode()),
                eq(botPlayer.getId()),
                any() // En lugar de any(Map.class)
        );
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
        // Verificar las llamadas específicas en lugar de times(2)
        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.REINFORCEMENT));
        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.END_TURN));

        // O si no sabes exactamente qué fases se cambian:
        // verify(gameStateService, atLeast(1)).changeTurnPhase(any(Game.class), any(TurnPhase.class));
    }

    @Test
    void testExecuteTurn_NORMAL_PLAY() {
        // Arrange
        game.setStatus(GameState.NORMAL_PLAY);
        setupMocksForNormalPlay();

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameStateService, times(3)).changeTurnPhase(any(Game.class), any(TurnPhase.class));
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

        // Act
        callPrivateMethod("performInitialPlacement", botPlayer, game, 5);

        // Assert
        verify(initialPlacementService).placeInitialArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testPerformInitialPlacement_EmptyTerritories() {
        // Arrange
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        callPrivateMethod("performInitialPlacement", botPlayer, game, 5);

        // Assert
        verify(initialPlacementService, never()).placeInitialArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformInitialPlacement_WithException() {
        // Arrange
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        assertDoesNotThrow(() -> callPrivateMethod("performInitialPlacement", botPlayer, game, 5));
    }

    @Test
    void testDistributeInitialArmiesBalanced_FewTerritories() {
        // Test with 2 or fewer territories
        List<Territory> territories = createMockTerritories().subList(0, 2);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeInitialArmiesBalanced",
                territories, game, botPlayer.getId(), 5);

        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeInitialArmiesBalanced_ManyTerritories() {
        // Test with more than 2 territories
        List<Territory> territories = createManyTerritories(5);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeInitialArmiesBalanced",
                territories, game, botPlayer.getId(), 6);

        assertNotNull(result);
        assertEquals(6, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeInitialArmiesBalanced_NoBorderTerritories() {
        // Test when no border territories exist
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(territories); // All neighbors owned by same player

        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeInitialArmiesBalanced",
                territories, game, botPlayer.getId(), 5);

        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
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
        setupMocksForReinforcement();
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong()))
                .thenReturn(createReinforcementStatus(0));

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService, never()).placeReinforcementArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformBotReinforcement_EmptyTerritories() {
        // Arrange
        setupMocksForReinforcement();
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong()))
                .thenReturn(createReinforcementStatus(5));
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService, never()).placeReinforcementArmies(anyString(), anyLong(), any(Map.class));
    }

    @Test
    void testPerformBotReinforcement_Success() {
        // Arrange
        setupMocksForReinforcement();
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong()))
                .thenReturn(createReinforcementStatus(5));
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService).placeReinforcementArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
        verify(gameEventService, atLeastOnce()).recordReinforcementsPlaced(anyLong(), anyLong(), anyString(), anyInt(), anyInt());
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
    void testDistributeReinforcementsBalanced_NoBorderTerritories() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(territories); // All neighbors owned by same player

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeReinforcementsBalanced",
                territories, game, botPlayer.getId(), 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeReinforcementsBalanced_FewReinforcements() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeReinforcementsBalanced",
                territories, game, botPlayer.getId(), 3);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeReinforcementsBalanced_ManyReinforcements() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeReinforcementsBalanced",
                territories, game, botPlayer.getId(), 10);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeReinforcementsBalanced_NoSecondaryTargets() {
        // Arrange
        List<Territory> territories = createStrongTerritories(); // All territories strong (> 3 armies)
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeReinforcementsBalanced",
                territories, game, botPlayer.getId(), 10);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.values().stream().mapToInt(Integer::intValue).sum());
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
    void testPerformBotAttack_LowProbabilityTargets() {
        // Arrange
        List<Territory> attackers = createWeakAttackers();
        List<Territory> targets = createStrongDefenders();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - Should not attack due to low probability
        verify(combatService, never()).performCombat(anyString(), any(AttackDto.class));
    }

    @Test
    void testPerformBotAttack_SuccessfulAttack() {
        // Arrange
        List<Territory> attackers = createStrongAttackers();
        List<Territory> targets = createWeakDefenders();
        CombatResultDto result = createSuccessfulCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).performCombat(eq(game.getGameCode()), any(AttackDto.class));
        verify(gameEventService).recordAttack(anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(gameEventService).recordTerritoryConquest(anyLong(), anyLong(), anyString(), anyString(), anyInt());
    }

    @Test
    void testPerformBotAttack_MaxAttacksReached() {
        // Arrange
        List<Territory> attackers = createManyAttackers(10);
        List<Territory> targets = createWeakDefenders();
        CombatResultDto result = createSuccessfulCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - Should only attack 5 times (max for BALANCED)
        verify(combatService, atMost(5)).performCombat(eq(game.getGameCode()), any(AttackDto.class));
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
    void testPerformBotFortify_NoBestMove() {
        // Arrange
        List<Territory> fortifiable = createMockTerritories();
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
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
        List<Territory> fortifiable = createMockTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong())).thenReturn(0);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService, never()).performFortification(anyString(), any(FortifyDto.class));
    }

    @Test
    void testPerformBotFortify_SuccessfulFortification() {
        // Arrange
        List<Territory> fortifiable = createMockTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong())).thenReturn(5);
        when(fortificationService.performFortification(eq(game.getGameCode()), any(FortifyDto.class))).thenReturn(true);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).performFortification(eq(game.getGameCode()), any(FortifyDto.class));
        verify(gameEventService).recordFortification(anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void testPerformBotFortify_FailedFortification() {
        // Arrange
        List<Territory> fortifiable = createMockTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong())).thenReturn(5);
        when(fortificationService.performFortification(eq(game.getGameCode()), any(FortifyDto.class))).thenReturn(false);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

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
    void testAdvanceToNextPhase_AllPhases() {
        // Test all phase transitions
        Game gameModel = createMockGame();
        when(gameMapper.toModel(game)).thenReturn(gameModel);

        // Test each phase transition
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        callPrivateMethod("advanceToNextPhase", game);
        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.ATTACK));

        game.setCurrentPhase(TurnPhase.ATTACK);
        callPrivateMethod("advanceToNextPhase", game);
        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.FORTIFY));

        game.setCurrentPhase(TurnPhase.FORTIFY);
        callPrivateMethod("advanceToNextPhase", game);
        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.END_TURN));

        game.setCurrentPhase(TurnPhase.END_TURN);
        callPrivateMethod("advanceToNextPhase", game);
        verify(gameStateService).changeTurnPhase(any(Game.class), eq(TurnPhase.REINFORCEMENT));
    }

    @Test
    void testAdvanceToNextPhase_WithException() {
        // Arrange
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        when(gameMapper.toModel(game)).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        assertDoesNotThrow(() -> callPrivateMethod("advanceToNextPhase", game));
    }

    @Test
    void testEvaluateAttackProbability_NoAttackingArmies() {
        double result = executor.evaluateAttackProbability(botPlayer, 1, 5);
        assertEquals(0.0, result);
    }

    @Test
    void testEvaluateAttackProbability_HighRatio() {
        double result = executor.evaluateAttackProbability(botPlayer, 15, 5);
        assertEquals(0.9, result);
    }

    @Test
    void testEvaluateAttackProbability_ModerateRatio() {
        double result = executor.evaluateAttackProbability(botPlayer, 10, 5);
        assertEquals(0.75, result);
    }

    @Test
    void testEvaluateAttackProbability_LowRatio() {
        double result = executor.evaluateAttackProbability(botPlayer, 6, 4);
        assertEquals(0.6, result);
    }

    @Test
    void testEvaluateAttackProbability_VeryLowRatio() {
        double result = executor.evaluateAttackProbability(botPlayer, 5, 4);
        assertEquals(0.4, result);
    }

    @Test
    void testEvaluateAttackProbability_PoorRatio() {
        double result = executor.evaluateAttackProbability(botPlayer, 4, 4);
        assertEquals(0.2, result);
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
    void testCalculateTerritoryPriority() {
        // Test territory priority calculation
        Territory territory = createMockTerritories().get(0);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        int result = callPrivateMethodWithReturn("calculateTerritoryPriority",
                territory, game, botPlayer.getId());

        assertTrue(result >= 0); // Should return a valid priority score
    }

    @Test
    void testCalculateOptimalAttackForce_WeakDefender() {
        int result = callPrivateMethodWithReturn("calculateOptimalAttackForce", 5, 2);
        assertEquals(2, result);
    }

    @Test
    void testCalculateOptimalAttackForce_ModerateDefender() {
        int result = callPrivateMethodWithReturn("calculateOptimalAttackForce", 6, 3);
        assertEquals(3, result);
    }

    @Test
    void testCalculateOptimalAttackForce_StrongDefender() {
        int result = callPrivateMethodWithReturn("calculateOptimalAttackForce", 8, 6);
        assertEquals(5, result);
    }

    @Test
    void testCalculateFortificationScore() {
        // Test fortification score calculation
        Territory source = createMockTerritories().get(0);
        Territory target = createMockTerritories().get(1);

        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        int result = callPrivateMethodWithReturn("calculateFortificationScore",
                source, target, game, botPlayer.getId());

        assertTrue(result != 0); // Should return a meaningful score
    }

    @Test
    void testCalculateOptimalFortificationAmount_WeakTarget() {
        int result = callPrivateMethodWithReturn("calculateOptimalFortificationAmount",
                6, 2, 4);
        assertEquals(2, result); // Should move 2 armies to reach ideal strength of 4
    }

    @Test
    void testCalculateOptimalFortificationAmount_StrongTarget() {
        int result = callPrivateMethodWithReturn("calculateOptimalFortificationAmount",
                6, 5, 4);
        assertEquals(1, result); // Target already strong, move minimal
    }

    @Test
    void testCalculateOptimalFortificationAmount_WeakSource() {
        int result = callPrivateMethodWithReturn("calculateOptimalFortificationAmount",
                3, 1, 2);
        assertEquals(1, result); // Can't leave source too weak
    }

    // Helper methods for creating test data and calling private methods
    private List<Territory> createMockTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory t1 = new Territory();
        t1.setId(1L);
        t1.setName("Territory1");
        t1.setOwnerId(botPlayer.getId());
        t1.setArmies(3);

        Territory t2 = new Territory();
        t2.setId(2L);
        t2.setName("Territory2");
        t2.setOwnerId(botPlayer.getId());
        t2.setArmies(5);

        territories.add(t1);
        territories.add(t2);
        return territories;
    }

    private List<Territory> createEnemyTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory enemy = new Territory();
        enemy.setId(10L);
        enemy.setName("EnemyTerritory");
        enemy.setOwnerId(2L);
        enemy.setArmies(2);

        territories.add(enemy);
        return territories;
    }

    private List<Territory> createManyTerritories(int count) {
        List<Territory> territories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Territory t = new Territory();
            t.setId((long) i + 1);
            t.setName("Territory" + (i + 1));
            t.setOwnerId(botPlayer.getId());
            t.setArmies(3);
            territories.add(t);
        }
        return territories;
    }

    private List<Territory> createStrongTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory strong = new Territory();
        strong.setId(1L);
        strong.setName("StrongTerritory");
        strong.setOwnerId(botPlayer.getId());
        strong.setArmies(10);

        territories.add(strong);
        return territories;
    }

    private List<Territory> createWeakAttackers() {
        List<Territory> territories = new ArrayList<>();

        Territory weak = new Territory();
        weak.setId(1L);
        weak.setName("WeakAttacker");
        weak.setOwnerId(botPlayer.getId());
        weak.setArmies(2);

        territories.add(weak);
        return territories;
    }

    private List<Territory> createStrongDefenders() {
        List<Territory> territories = new ArrayList<>();

        Territory strong = new Territory();
        strong.setId(10L);
        strong.setName("StrongDefender");
        strong.setOwnerId(2L);
        strong.setArmies(10);

        territories.add(strong);
        return territories;
    }

    private List<Territory> createStrongAttackers() {
        List<Territory> territories = new ArrayList<>();

        Territory strong = new Territory();
        strong.setId(1L);
        strong.setName("StrongAttacker");
        strong.setOwnerId(botPlayer.getId());
        strong.setArmies(10);

        territories.add(strong);
        return territories;
    }

    private List<Territory> createWeakDefenders() {
        List<Territory> territories = new ArrayList<>();

        Territory weak = new Territory();
        weak.setId(10L);
        weak.setName("WeakDefender");
        weak.setOwnerId(2L);
        weak.setArmies(1);

        territories.add(weak);
        return territories;
    }

    private List<Territory> createManyAttackers(int count) {
        List<Territory> territories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Territory t = new Territory();
            t.setId((long) i + 1);
            t.setName("Attacker" + (i + 1));
            t.setOwnerId(botPlayer.getId());
            t.setArmies(5);
            territories.add(t);
        }
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

    private Game createMockGame() {
        Game game = new Game();
        game.setId(this.game.getId());
        game.setGameCode(this.game.getGameCode());
        game.setState(this.game.getStatus());
        game.setCurrentPhase(this.game.getCurrentPhase());
        game.setCurrentTurn(this.game.getCurrentTurn());
        return game;
    }

    private ReinforcementStatusDto createReinforcementStatus(int armies) {
        return ReinforcementStatusDto.builder()
                .playerId(botPlayer.getId())
                .armiesToPlace(armies)
                .baseArmies(3)
                .continentBonus(2)
                .totalArmies(armies)
                .isPlayerTurn(true)
                .canReinforce(true)
                .build();
    }

    private CombatResultDto createSuccessfulCombatResult() {
        CombatResultDto result = new CombatResultDto();
        result.setAttackerCountryId(1L);
        result.setDefenderCountryId(10L);
        result.setAttackerCountryName("Territory1");
        result.setDefenderCountryName("EnemyTerritory");
        result.setTerritoryConquered(true);
        result.setAttackerLosses(1);
        result.setDefenderLosses(2);
        return result;
    }

    private void setupMocksForNormalPlay() {
        Player player = createMockPlayer();
        Game gameModel = createMockGame();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(true);
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong()))
                .thenReturn(createReinforcementStatus(5));
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());
    }

    private void setupMocksForReinforcement() {
        Player player = createMockPlayer();
        Game gameModel = createMockGame();

        when(playerService.findById(botPlayer.getId())).thenReturn(Optional.of(player));
        when(gameMapper.toModel(game)).thenReturn(gameModel);
        when(reinforcementService.canPerformReinforcement(any(Game.class), any(Player.class))).thenReturn(true);
    }

    private void callPrivateMethod(String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
                // Handle primitive types
                if (paramTypes[i] == Integer.class) paramTypes[i] = int.class;
                if (paramTypes[i] == Long.class) paramTypes[i] = long.class;
            }

            var method = BalancedAggressiveExecutor.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            method.invoke(executor, args);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T callPrivateMethodWithReturn(String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                Class<?> argClass = args[i].getClass();

                // Handle primitive types and common conversions
                if (argClass == Integer.class) {
                    paramTypes[i] = int.class;
                } else if (argClass == Long.class) {
                    paramTypes[i] = long.class;
                } else if (argClass == Boolean.class) {
                    paramTypes[i] = boolean.class;
                } else if (argClass == Double.class) {
                    paramTypes[i] = double.class;
                } else if (argClass == Float.class) {
                    paramTypes[i] = float.class;
                } else if (argClass == ArrayList.class) {
                    // Para listas, usar la interfaz List
                    paramTypes[i] = List.class;
                } else if (argClass == HashMap.class) {
                    // Para mapas, usar la interfaz Map
                    paramTypes[i] = Map.class;
                } else {
                    paramTypes[i] = argClass;
                }
            }

            Method method = null;

            // Primero intentar con los tipos exactos
            try {
                method = BalancedAggressiveExecutor.class.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                // Si no encuentra el método, intentar con todas las combinaciones posibles
                Method[] methods = BalancedAggressiveExecutor.class.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                        Class<?>[] methodParamTypes = m.getParameterTypes();
                        boolean matches = true;

                        for (int i = 0; i < args.length; i++) {
                            if (!isAssignable(methodParamTypes[i], args[i])) {
                                matches = false;
                                break;
                            }
                        }

                        if (matches) {
                            method = m;
                            break;
                        }
                    }
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("No suitable method found: " + methodName);
            }

            method.setAccessible(true);
            return (T) method.invoke(executor, args);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage() + " - " + e.getClass().getSimpleName());
            return null;
        }
    }

    // Método auxiliar para verificar si un tipo es asignable
    private boolean isAssignable(Class<?> paramType, Object arg) {
        if (arg == null) {
            return !paramType.isPrimitive();
        }

        Class<?> argType = arg.getClass();

        // Verificar asignación directa
        if (paramType.isAssignableFrom(argType)) {
            return true;
        }

        // Verificar primitivos y sus wrappers
        if (paramType == int.class && argType == Integer.class) return true;
        if (paramType == long.class && argType == Long.class) return true;
        if (paramType == boolean.class && argType == Boolean.class) return true;
        if (paramType == double.class && argType == Double.class) return true;
        if (paramType == float.class && argType == Float.class) return true;

        // Verificar interfaces comunes
        if (paramType == List.class && List.class.isAssignableFrom(argType)) return true;
        if (paramType == Map.class && Map.class.isAssignableFrom(argType)) return true;

        return false;
    }
}
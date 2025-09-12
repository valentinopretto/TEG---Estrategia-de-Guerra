package ar.edu.utn.frc.tup.piii.FactoryBots;

import ar.edu.utn.frc.tup.piii.FactoryBots.ExpertStrategies.ExpertAggressiveExecutor;
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
class ExpertAggressiveExecutorTest {

    @Mock
    private CombatService combatService;
    @Mock
    private FortificationService fortificationService;
    @Mock
    private ReinforcementService reinforcementService;
    @Mock
    private GameTerritoryService gameTerritoryService;
    @Mock
    private GameStateService gameStateService;
    @Mock
    private InitialPlacementService initialPlacementService;
    @Mock
    private PlayerService playerService;
    @Mock
    private IGameEventService gameEventService;
    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private ExpertAggressiveExecutor executor;

    private PlayerEntity botPlayer;
    private GameEntity game;
    private BotProfileEntity botProfile;
    private ObjectiveEntity objective;

    @BeforeEach
    void setUp() {
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setBotName("ExpertBot");
        botProfile.setLevel(BotLevel.EXPERT);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);

        objective = new ObjectiveEntity();
        objective.setId(1L);
        objective.setType(ObjectiveType.OCCUPATION);
        objective.setDescription("Ocupar Asia y Europa");
        objective.setTargetData("Asia,Europa");
        objective.setIsCommon(false);

        botPlayer = new PlayerEntity();
        botPlayer.setId(1L);
        botPlayer.setBotProfile(botProfile);
        botPlayer.setObjective(objective);

        game = new GameEntity();
        game.setId(1L);
        game.setGameCode("TEST_GAME");
        game.setStatus(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        game.setCurrentTurn(1);
    }

    @Test
    void testGetLevel() {
        assertEquals(BotLevel.EXPERT, executor.getLevel());
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
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenReturn(createAllTerritories());

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
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenReturn(createAllTerritories());

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
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenReturn(createAllTerritories());

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameStateService, times(2)).changeTurnPhase(any(Game.class), any(TurnPhase.class));
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
    void testPerformInitialPlacement_WithOccupationObjective() {
        // Arrange
        objective.setType(ObjectiveType.OCCUPATION);
        objective.setTargetData("Asia,Europa");
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenReturn(createAllTerritories());

        // Act
        callPrivateMethod("performInitialPlacement", botPlayer, game, 5);

        // Assert
        verify(initialPlacementService).placeInitialArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testPerformInitialPlacement_WithDestructionObjective() {
        // Arrange
        objective.setType(ObjectiveType.DESTRUCTION);
        objective.setTargetData("2");
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenReturn(createAllTerritories());

        // Act
        callPrivateMethod("performInitialPlacement", botPlayer, game, 5);

        // Assert
        verify(initialPlacementService).placeInitialArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testPerformInitialPlacement_WithCommonObjective() {
        // Arrange
        objective.setType(ObjectiveType.COMMON);
        objective.setTargetData("18");
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenReturn(createAllTerritories());

        // Act
        callPrivateMethod("performInitialPlacement", botPlayer, game, 5);

        // Assert
        verify(initialPlacementService).placeInitialArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testPerformInitialPlacement_WithNullObjective() {
        // Arrange
        botPlayer.setObjective(null);
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenReturn(createAllTerritories());

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
    void testDistributeInitialArmiesExpert_DestructionObjective() {
        // Arrange
        objective.setType(ObjectiveType.DESTRUCTION);
        objective.setTargetData("2");
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeInitialArmiesExpert",
                territories, game, botPlayer, 5, "DESTRUCTION");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeInitialArmiesExpert_OccupationObjective() {
        // Arrange
        objective.setType(ObjectiveType.OCCUPATION);
        objective.setTargetData("Asia,Europa");
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeInitialArmiesExpert",
                territories, game, botPlayer, 5, "OCCUPATION");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeInitialArmiesExpert_GeneralObjective() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("distributeInitialArmiesExpert",
                territories, game, botPlayer, 5, "GENERAL");

        // Assert
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
    void testPerformBotReinforcement_WithDestructionObjective() {
        // Arrange
        objective.setType(ObjectiveType.DESTRUCTION);
        objective.setTargetData("2");
        setupMocksForReinforcement();
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong()))
                .thenReturn(createReinforcementStatus(5));
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(reinforcementService).placeReinforcementArmies(eq(game.getGameCode()), eq(botPlayer.getId()), any(Map.class));
    }

    @Test
    void testPerformBotReinforcement_WithOccupationObjective() {
        // Arrange
        objective.setType(ObjectiveType.OCCUPATION);
        objective.setTargetData("Asia");
        setupMocksForReinforcement();
        when(reinforcementService.getReinforcementStatus(anyString(), anyLong()))
                .thenReturn(createReinforcementStatus(5));
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

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
    void testPerformBotAttack_WithDestructionObjective() {
        // Arrange
        objective.setType(ObjectiveType.DESTRUCTION);
        objective.setTargetData("2");
        List<Territory> attackers = createStrongAttackers();
        List<Territory> targets = createWeakDefenders();
        CombatResultDto result = createSuccessfulCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).performCombat(eq(game.getGameCode()), any(AttackDto.class));
        verify(gameEventService).recordAttack(anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    void testPerformBotAttack_WithOccupationObjective() {
        // Arrange
        objective.setType(ObjectiveType.OCCUPATION);
        objective.setTargetData("Asia");
        List<Territory> attackers = createStrongAttackers();
        List<Territory> targets = createWeakDefenders();
        CombatResultDto result = createSuccessfulCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).performCombat(eq(game.getGameCode()), any(AttackDto.class));
    }

    @Test
    void testPerformBotAttack_MaxAttacksReached() {
        // Arrange
        objective.setType(ObjectiveType.DESTRUCTION); // Max 10 attacks
        List<Territory> attackers = createManyAttackers(15);
        List<Territory> targets = createWeakDefenders();
        CombatResultDto result = createSuccessfulCombatResult();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class))).thenReturn(result);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - Should only attack up to max (10 for destruction)
        verify(combatService, atMost(10)).performCombat(eq(game.getGameCode()), any(AttackDto.class));
    }

    @Test
    void testPerformBotAttack_LowProbabilityTargets() {
        // Arrange
        objective.setType(ObjectiveType.OCCUPATION); // Min probability 0.50
        List<Territory> attackers = createWeakAttackers();
        List<Territory> targets = createStrongDefenders();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(attackers);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - Should not attack due to low probability
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
    void testPerformBotFortify_WithOccupationObjective() {
        // Arrange
        objective.setType(ObjectiveType.OCCUPATION);
        List<Territory> fortifiable = createMockTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong())).thenReturn(fortifiable);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong())).thenReturn(targets);
        when(fortificationService.performFortification(eq(game.getGameCode()), any(FortifyDto.class))).thenReturn(true);
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).performFortification(eq(game.getGameCode()), any(FortifyDto.class));
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
        double result = executor.evaluateAttackProbability(botPlayer, 0, 5);
        assertEquals(0.0, result);
    }

    @Test
    void testEvaluateAttackProbability_HighAdvantage() {
        double result = executor.evaluateAttackProbability(botPlayer, 6, 2);
        assertEquals(0.85, result); // attackerArmies >= 3, defenderArmies == 1
    }

    @Test
    void testEvaluateAttackProbability_ModerateAdvantage() {
        double result = executor.evaluateAttackProbability(botPlayer, 4, 2);
        assertEquals(0.70, result);
    }

    @Test
    void testEvaluateAttackProbability_LowAdvantage() {
        double result = executor.evaluateAttackProbability(botPlayer, 3, 2);
        assertEquals(0.55, result);
    }

    @Test
    void testEvaluateAttackProbability_PoorAdvantage() {
        double result = executor.evaluateAttackProbability(botPlayer, 2, 3);
        assertEquals(0.25, result);
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
    void testIdentifyStrategicTargets_WithOccupationObjective() {
        // Arrange
        objective.setType(ObjectiveType.OCCUPATION);
        objective.setTargetData("Asia,Europa");
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyStrategicTargets", game, botPlayer, "OCCUPATION");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testIdentifyStrategicTargets_WithDestructionObjective() {
        // Arrange
        objective.setType(ObjectiveType.DESTRUCTION);
        objective.setTargetData("2");
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyStrategicTargets", game, botPlayer, "DESTRUCTION");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testIdentifyStrategicTargets_WithGeneralObjective() {
        // Arrange
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyStrategicTargets", game, botPlayer, "GENERAL");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testIdentifyStrategicTargets_WithException() {
        // Arrange
        when(gameTerritoryService.getAllAvailableTerritories())
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyStrategicTargets", game, botPlayer, "OCCUPATION");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIdentifyOccupationTargets_WithValidContinents() {
        // Arrange
        List<Territory> enemyTerritories = createAllTerritories();
        objective.setTargetData("Asia,Europa");

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyOccupationTargets", botPlayer, enemyTerritories);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testIdentifyOccupationTargets_WithNullTargetData() {
        // Arrange
        List<Territory> enemyTerritories = createAllTerritories();
        objective.setTargetData(null);

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyOccupationTargets", botPlayer, enemyTerritories);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIdentifyDestructionTargets_WithValidPlayerId() {
        // Arrange
        List<Territory> enemyTerritories = createAllTerritories();
        objective.setTargetData("2");

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyDestructionTargets", botPlayer, enemyTerritories);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testIdentifyDestructionTargets_WithInvalidPlayerId() {
        // Arrange
        List<Territory> enemyTerritories = createAllTerritories();
        objective.setTargetData("invalid_id");

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyDestructionTargets", botPlayer, enemyTerritories);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIdentifyDestructionTargets_WithNullTargetData() {
        // Arrange
        List<Territory> enemyTerritories = createAllTerritories();
        objective.setTargetData(null);

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyDestructionTargets", botPlayer, enemyTerritories);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIdentifyGeneralTargets() {
        // Arrange
        List<Territory> enemyTerritories = createWeakDefenders();

        // Act
        List<?> result = callPrivateMethodWithReturn("identifyGeneralTargets", enemyTerritories);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testPlanExpertReinforcements_DestructionObjective() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        List<?> strategicTargets = new ArrayList<>();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("planExpertReinforcements",
                territories, strategicTargets, 5, "DESTRUCTION", game, botPlayer.getId());

        // Assert
        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testPlanExpertReinforcements_OccupationObjective() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        List<?> strategicTargets = new ArrayList<>();

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("planExpertReinforcements",
                territories, strategicTargets, 5, "OCCUPATION", game, botPlayer.getId());

        // Assert
        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testPlanExpertReinforcements_DefaultObjective() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        List<?> strategicTargets = new ArrayList<>();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

        // Act
        Map<Long, Integer> result = callPrivateMethodWithReturn("planExpertReinforcements",
                territories, strategicTargets, 5, "GENERAL", game, botPlayer.getId());

        // Assert
        assertNotNull(result);
        assertEquals(5, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testFindBorderTerritories() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

        // Act
        List<Territory> result = callPrivateMethodWithReturn("findBorderTerritories", territories, game, botPlayer.getId());

        // Assert
        assertNotNull(result);
    }

    @Test
    void testFindStrongestBorderTerritory() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong())).thenReturn(createEnemyTerritories());

        // Act
        Territory result = callPrivateMethodWithReturn("findStrongestBorderTerritory", territories, game, botPlayer.getId());

        // Assert
        assertNotNull(result);
    }

    @Test
    void testDistributeForOccupation() {
        // Arrange
        Map<Long, Integer> plan = new HashMap<>();
        List<Territory> territories = createMockTerritories();

        // Act
        callPrivateMethod("distributeForOccupation", plan, territories, 5);

        // Assert
        assertEquals(5, plan.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeForOccupation_EmptyTerritories() {
        // Arrange
        Map<Long, Integer> plan = new HashMap<>();
        List<Territory> territories = Collections.emptyList();

        // Act
        callPrivateMethod("distributeForOccupation", plan, territories, 5);

        // Assert
        assertTrue(plan.isEmpty());
    }

    @Test
    void testDistributeForOccupation_SingleTerritory() {
        // Arrange
        Map<Long, Integer> plan = new HashMap<>();
        List<Territory> territories = createMockTerritories().subList(0, 1);

        // Act
        callPrivateMethod("distributeForOccupation", plan, territories, 5);

        // Assert
        assertEquals(5, plan.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void testDistributeBalanced_EmptyTerritories() {
        // Arrange
        Map<Long, Integer> plan = new HashMap<>();
        List<Territory> territories = Collections.emptyList();

        // Act
        callPrivateMethod("distributeBalanced", plan, territories, 5);

        // Assert
        assertTrue(plan.isEmpty());
    }

    @Test
    void testDistributeBalanced_ManyTerritories() {
        // Arrange
        Map<Long, Integer> plan = new HashMap<>();
        List<Territory> territories = createManyTerritories(5);

        // Act
        callPrivateMethod("distributeBalanced", plan, territories, 6);

        // Assert
        assertEquals(6, plan.values().stream().mapToInt(Integer::intValue).sum());
        assertTrue(plan.size() <= 3); // Should distribute to max 3 territories
    }

    @Test
    void testDistributeBalanced_FewTerritories() {
        // Arrange
        Map<Long, Integer> plan = new HashMap<>();
        List<Territory> territories = createMockTerritories().subList(0, 2);

        // Act
        callPrivateMethod("distributeBalanced", plan, territories, 5);

        // Assert
        assertEquals(5, plan.values().stream().mapToInt(Integer::intValue).sum());
    }

//    @Test
//    void testIsStrategicallyRelevant_True() {
//        // Arrange
//        Territory territory = createMockTerritories().get(0);
//        Object strategicTarget = createMockStrategicTarget(territory.getId());
//        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
//                .thenReturn(Arrays.asList(territory));
//
//        // Act
//        boolean result = callPrivateMethodWithReturn("isStrategicallyRelevant",
//                territory, strategicTarget, game, botPlayer.getId());
//
//        // Assert
//        assertTrue(result);
//    }
//
//    @Test
//    void testIsStrategicallyRelevant_False() {
//        // Arrange
//        Territory territory = createMockTerritories().get(0);
//        Object strategicTarget = createMockStrategicTarget(999L); // Different ID
//        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
//                .thenReturn(Collections.emptyList());
//
//        // Act
//        boolean result = callPrivateMethodWithReturn("isStrategicallyRelevant",
//                territory, strategicTarget, game, botPlayer.getId());
//
//        // Assert
//        assertFalse(result);
//    }

    @Test
    void testGetMaxAttacksForObjective_Destruction() {
        int result = callPrivateMethodWithReturn("getMaxAttacksForObjective", "DESTRUCTION");
        assertEquals(10, result);
    }

    @Test
    void testGetMaxAttacksForObjective_Occupation() {
        int result = callPrivateMethodWithReturn("getMaxAttacksForObjective", "OCCUPATION");
        assertEquals(8, result);
    }

    @Test
    void testGetMaxAttacksForObjective_Default() {
        int result = callPrivateMethodWithReturn("getMaxAttacksForObjective", "GENERAL");
        assertEquals(6, result);
    }

    @Test
    void testPrioritizeAttackers() {
        // Arrange
        List<Territory> attackers = createMockTerritories();
        List<?> targets = new ArrayList<>();

        // Act
        List<Territory> result = callPrivateMethodWithReturn("prioritizeAttackers", attackers, targets);

        // Assert
        assertNotNull(result);
        assertEquals(attackers.size(), result.size());
    }

    @Test
    void testSelectBestTarget() {
        // Arrange
        List<Territory> targets = createMockTerritories();
        List<?> strategicTargets = new ArrayList<>();

        // Act
        Territory result = callPrivateMethodWithReturn("selectBestTarget", targets, strategicTargets, "OCCUPATION");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSelectBestTarget_EmptyTargets() {
        // Arrange
        List<Territory> targets = Collections.emptyList();
        List<?> strategicTargets = new ArrayList<>();

        // Act
        Territory result = callPrivateMethodWithReturn("selectBestTarget", targets, strategicTargets, "OCCUPATION");

        // Assert
        assertNull(result);
    }

    @Test
    void testShouldAttack_True() {
        // Arrange
        Territory attacker = createStrongAttackers().get(0);
        Territory target = createWeakDefenders().get(0);

        // Act
        boolean result = callPrivateMethodWithReturn("shouldAttack", attacker, target, "DESTRUCTION");

        // Assert
        assertTrue(result);
    }

    @Test
    void testShouldAttack_False() {
        // Arrange
        Territory attacker = createWeakAttackers().get(0);
        Territory target = createStrongDefenders().get(0);

        // Act
        boolean result = callPrivateMethodWithReturn("shouldAttack", attacker, target, "OCCUPATION");

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetMinAttackProbability_Destruction() {
        double result = callPrivateMethodWithReturn("getMinAttackProbability", "DESTRUCTION");
        assertEquals(0.40, result);
    }

    @Test
    void testGetMinAttackProbability_Occupation() {
        double result = callPrivateMethodWithReturn("getMinAttackProbability", "OCCUPATION");
        assertEquals(0.50, result);
    }

    @Test
    void testGetMinAttackProbability_Default() {
        double result = callPrivateMethodWithReturn("getMinAttackProbability", "GENERAL");
        assertEquals(0.55, result);
    }

    @Test
    void testCalculateOptimalAttackForce_Destruction() {
        int result = callPrivateMethodWithReturn("calculateOptimalAttackForce", 6, 3, "DESTRUCTION");
        assertEquals(5, result); // defenderArmies + 2 = 3 + 2 = 5
    }

    @Test
    void testCalculateOptimalAttackForce_Default() {
        int result = callPrivateMethodWithReturn("calculateOptimalAttackForce", 6, 3, "OCCUPATION");
        assertEquals(3, result); // Math.max(1, baseForce)
    }

    @Test
    void testFindBestExpertFortificationMove_Found() {
        // Arrange
        List<Territory> fortifiable = createMockTerritories();
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(createMockTerritories());
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        Optional<?> result = callPrivateMethodWithReturn("findBestExpertFortificationMove",
                fortifiable, game, botPlayer.getId(), "DESTRUCTION");

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void testFindBestExpertFortificationMove_NotFound() {
        // Arrange
        List<Territory> fortifiable = createWeakTerritories(); // <= 2 armies
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        Optional<?> result = callPrivateMethodWithReturn("findBestExpertFortificationMove",
                fortifiable, game, botPlayer.getId(), "DESTRUCTION");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testShouldFortifyExpert_Destruction_True() {
        // Arrange
        Territory source = createMockTerritories().get(0);
        Territory target = createStrongTerritories().get(0);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        boolean result = callPrivateMethodWithReturn("shouldFortifyExpert",
                source, target, "DESTRUCTION", game, botPlayer.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    void testShouldFortifyExpert_Default_True() {
        // Arrange
        Territory source = createStrongTerritories().get(0);
        Territory target = createWeakTerritories().get(0);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(Arrays.asList(createMockTerritories().get(0)), createEnemyTerritories()); // source safe, target border

        // Act
        boolean result = callPrivateMethodWithReturn("shouldFortifyExpert",
                source, target, "OCCUPATION", game, botPlayer.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    void testShouldFortifyExpert_False() {
        // Arrange
        Territory source = createMockTerritories().get(0);
        Territory target = createMockTerritories().get(1);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createMockTerritories()); // Both territories not border

        // Act
        boolean result = callPrivateMethodWithReturn("shouldFortifyExpert",
                source, target, "OCCUPATION", game, botPlayer.getId());

        // Assert
        assertFalse(result);
    }

    @Test
    void testCalculateExpertFortificationArmies_Destruction() {
        int result = callPrivateMethodWithReturn("calculateExpertFortificationArmies", 10, "DESTRUCTION");
        assertEquals(7, result); // 10 * 80 / 100 - 1 = 7
    }

    @Test
    void testCalculateExpertFortificationArmies_Default() {
        int result = callPrivateMethodWithReturn("calculateExpertFortificationArmies", 10, "OCCUPATION");
        assertEquals(5, result); // 10 / 2 = 5
    }

    @Test
    void testCalculateExpertFortificationArmies_MinimumOne() {
        int result = callPrivateMethodWithReturn("calculateExpertFortificationArmies", 2, "DESTRUCTION");
        assertEquals(1, result); // Should return at least 1
    }

    @Test
    void testGetPlayerName_WithUser() {
        // Arrange
        Player playerWithUser = createMockPlayerWithUser();
        when(playerService.findById(anyLong())).thenReturn(Optional.of(playerWithUser));

        // Act
        String result = callPrivateMethodWithReturn("getPlayerName", 1L);

        // Assert
        assertEquals("TestUser", result);
    }

    @Test
    void testGetPlayerName_WithBot() {
        // Arrange
        Player playerWithBot = createMockPlayerWithBot();
        when(playerService.findById(anyLong())).thenReturn(Optional.of(playerWithBot));

        // Act
        String result = callPrivateMethodWithReturn("getPlayerName", 1L);

        // Assert
        assertEquals("TestBot", result);
    }

    @Test
    void testGetPlayerName_PlayerNotFound() {
        // Arrange
        when(playerService.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        String result = callPrivateMethodWithReturn("getPlayerName", 1L);

        // Assert
        assertEquals("Jugador Desconocido", result);
    }

    @Test
    void testGetPlayerName_WithException() {
        // Arrange
        when(playerService.findById(anyLong())).thenThrow(new RuntimeException("Test exception"));

        // Act
        String result = callPrivateMethodWithReturn("getPlayerName", 1L);

        // Assert
        assertEquals("Jugador Desconocido", result);
    }

    // Helper methods for creating test data and calling private methods
    private List<Territory> createMockTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory t1 = new Territory();
        t1.setId(1L);
        t1.setName("Territory1");
        t1.setOwnerId(botPlayer.getId());
        t1.setArmies(3);
        t1.setContinentName("America");

        Territory t2 = new Territory();
        t2.setId(2L);
        t2.setName("Territory2");
        t2.setOwnerId(botPlayer.getId());
        t2.setArmies(5);
        t2.setContinentName("Europe");

        territories.add(t1);
        territories.add(t2);
        return territories;
    }

    private List<Territory> createAllTerritories() {
        List<Territory> territories = new ArrayList<>();

        // Player territories
        territories.addAll(createMockTerritories());

        // Enemy territories in Asia
        Territory asia1 = new Territory();
        asia1.setId(10L);
        asia1.setName("China");
        asia1.setOwnerId(2L);
        asia1.setArmies(3);
        asia1.setContinentName("Asia");

        Territory asia2 = new Territory();
        asia2.setId(11L);
        asia2.setName("India");
        asia2.setOwnerId(2L);
        asia2.setArmies(2);
        asia2.setContinentName("Asia");

        // Enemy territories in Europa
        Territory europa1 = new Territory();
        europa1.setId(12L);
        europa1.setName("France");
        europa1.setOwnerId(3L);
        europa1.setArmies(4);
        europa1.setContinentName("Europa");

        territories.addAll(Arrays.asList(asia1, asia2, europa1));
        return territories;
    }

    private List<Territory> createEnemyTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory enemy = new Territory();
        enemy.setId(10L);
        enemy.setName("EnemyTerritory");
        enemy.setOwnerId(2L);
        enemy.setArmies(2);
        enemy.setContinentName("Enemy Land");

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
            t.setContinentName("Continent" + (i % 3));
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
        strong.setContinentName("Strong Land");

        territories.add(strong);
        return territories;
    }

    private List<Territory> createWeakTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory weak = new Territory();
        weak.setId(1L);
        weak.setName("WeakTerritory");
        weak.setOwnerId(botPlayer.getId());
        weak.setArmies(2);
        weak.setContinentName("Weak Land");

        territories.add(weak);
        return territories;
    }

    private List<Territory> createStrongAttackers() {
        List<Territory> territories = new ArrayList<>();

        Territory strong = new Territory();
        strong.setId(1L);
        strong.setName("StrongAttacker");
        strong.setOwnerId(botPlayer.getId());
        strong.setArmies(10);
        strong.setContinentName("Attack Land");

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
        weak.setContinentName("Weak Attack");

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
        strong.setContinentName("Defense Land");

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
        weak.setContinentName("Weak Defense");

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
        when(gameTerritoryService.getAllAvailableTerritories()).thenReturn(createAllTerritories());
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
                // Handle interfaces and common types
                if (args[i] instanceof List) paramTypes[i] = List.class;
                if (args[i] instanceof Map) paramTypes[i] = Map.class;
            }

            var method = ExpertAggressiveExecutor.class.getDeclaredMethod(methodName, paramTypes);
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
                paramTypes[i] = args[i].getClass();
                // Handle primitive types
                if (paramTypes[i] == Integer.class) paramTypes[i] = int.class;
                if (paramTypes[i] == Long.class) paramTypes[i] = long.class;
                // Handle interfaces and common types
                if (args[i] instanceof List) paramTypes[i] = List.class;
                if (args[i] instanceof Map) paramTypes[i] = Map.class;
            }

            var method = ExpertAggressiveExecutor.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (T) method.invoke(executor, args);
        } catch (Exception e) {
            fail("Method invocation failed: " + e.getMessage());
            return null;
        }
    }
}

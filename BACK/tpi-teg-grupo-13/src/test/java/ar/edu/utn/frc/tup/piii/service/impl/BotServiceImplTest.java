package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyExecutor;
import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyFactory;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.repository.BotProfileRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotServiceImplTest {

    @Mock
    private BotStrategyFactory botStrategyFactory;

    @Mock
    private BotStrategyExecutor botStrategyExecutor;

    @Mock
    private BotProfileRepository botProfileRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameService gameService;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private BotServiceImpl botService;

    private PlayerEntity botPlayer;
    private PlayerEntity humanPlayer;
    private GameEntity game;
    private BotProfileEntity botProfile;
    private Player botPlayerModel;
    private Game gameModel;
    private GameResponseDto gameResponseDto;

    @BeforeEach
    void setUp() {
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setBotName("TestBot");
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);

        botPlayer = new PlayerEntity();
        botPlayer.setId(1L);
        botPlayer.setBotProfile(botProfile);

        humanPlayer = new PlayerEntity();
        humanPlayer.setId(2L);
        humanPlayer.setBotProfile(null);

        game = new GameEntity();
        game.setId(1L);
        game.setGameCode("TEST-GAME");
        game.setStatus(GameState.NORMAL_PLAY);
        game.setCurrentPlayerIndex(0);

        botPlayerModel = new Player();
        botPlayerModel.setId(1L);
        botPlayerModel.setIsBot(true);

        gameModel = new Game();
        gameModel.setId(1L);
        gameModel.setGameCode("TEST-GAME");
        gameModel.setState(GameState.NORMAL_PLAY);
        gameModel.setCurrentPlayerIndex(0);

        gameResponseDto = new GameResponseDto();
        gameResponseDto.setId(1L);
        gameResponseDto.setGameCode("TEST-GAME");
    }

    @Test
    void testExecuteBotTurn() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        botService.executeBotTurn(botPlayer, game);

        verify(botStrategyExecutor).executeTurn(botPlayer, game);
    }

    @Test
    void testEvaluateAttackProbability() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.evaluateAttackProbability(botPlayer, 5, 3)).thenReturn(0.75);

        double result = botService.evaluateAttackProbability(botPlayer, 5, 3);

        assertEquals(0.75, result);
    }

    @Test
    void testGetBestAttackTargets() {
        List<CountryEntity> mockTargets = Arrays.asList(new CountryEntity(), new CountryEntity());

        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestAttackTargets(botPlayer, game)).thenReturn(mockTargets);

        List<CountryEntity> result = botService.getBestAttackTargets(botPlayer, game);

        assertEquals(2, result.size());
    }

    @Test
    void testSaveBotProfile() {
        BotProfileEntity botProfile = new BotProfileEntity();
        when(botProfileRepository.save(botProfile)).thenReturn(botProfile);

        BotProfileEntity result = botService.save(botProfile);

        assertEquals(botProfile, result);
        verify(botProfileRepository, times(1)).save(botProfile);
    }

    @Test
    void testFindById() {
        BotProfileEntity botProfile = new BotProfileEntity();
        when(botProfileRepository.findById(1L)).thenReturn(Optional.of(botProfile));

        Optional<BotProfileEntity> result = botService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(botProfile, result.get());
    }

    @Test
    void testDeleteById() {
        botService.deleteById(10L);
        verify(botProfileRepository).deleteById(10L);
    }

    @Test
    void testFindByLevel() {
        BotLevel level = BotLevel.BALANCED;
        List<BotProfileEntity> expectedList = List.of(new BotProfileEntity());
        when(botProfileRepository.findByLevel(level)).thenReturn(expectedList);

        List<BotProfileEntity> result = botService.findByLevel(level);

        assertEquals(expectedList, result);
    }

    @Test
    void testExecuteBotTurn_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.executeBotTurn(humanPlayer, game));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
        verify(botStrategyFactory, never()).getExecutor(any());
    }

    @Test
    void testEvaluateAttackProbability_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.evaluateAttackProbability(humanPlayer, 5, 3));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
    }

    @Test
    void testGetBestAttackTargets_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.getBestAttackTargets(humanPlayer, game));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
    }

    @Test
    void testGetBestDefensePositions_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.getBestDefensePositions(humanPlayer, game));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
    }

    @Test
    void testGetBestDefensePositions() {
        List<CountryEntity> mockDefensePositions = Arrays.asList(new CountryEntity(), new CountryEntity());

        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestDefensePositions(botPlayer, game)).thenReturn(mockDefensePositions);

        List<CountryEntity> result = botService.getBestDefensePositions(botPlayer, game);

        assertEquals(2, result.size());
        verify(botStrategyExecutor).getBestDefensePositions(botPlayer, game);
    }

    @Test
    void testShouldBotAttack_WithTargets() {
        List<CountryEntity> mockTargets = Arrays.asList(new CountryEntity());

        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestAttackTargets(botPlayer, game)).thenReturn(mockTargets);

        boolean result = botService.shouldBotAttack(botPlayer, game);

        assertTrue(result);
    }

    @Test
    void testShouldBotAttack_WithoutTargets() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestAttackTargets(botPlayer, game)).thenReturn(Collections.emptyList());

        boolean result = botService.shouldBotAttack(botPlayer, game);

        assertFalse(result);
    }

    @Test
    void testShouldBotAttack_WithNonBotPlayer() {
        boolean result = botService.shouldBotAttack(humanPlayer, game);

        assertFalse(result);
    }

    @Test
    void testShouldBotFortify_WithBotPlayer() {
        boolean result = botService.shouldBotFortify(botPlayer, game);

        assertTrue(result);
    }

    @Test
    void testShouldBotFortify_WithNonBotPlayer() {
        boolean result = botService.shouldBotFortify(humanPlayer, game);

        assertFalse(result);
    }

    @Test
    void testFindAll() {
        List<BotProfileEntity> expectedList = Arrays.asList(new BotProfileEntity(), new BotProfileEntity());
        when(botProfileRepository.findAll()).thenReturn(expectedList);

        List<BotProfileEntity> result = botService.findAll();

        assertEquals(2, result.size());
        verify(botProfileRepository).findAll();
    }

    @Test
    void testFindById_NotFound() {
        when(botProfileRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<BotProfileEntity> result = botService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testIsValidBot_ValidBot() {
        boolean result = botService.isValidBot(botPlayer);

        assertTrue(result);
    }

    @Test
    void testIsValidBot_NullPlayer() {
        boolean result = botService.isValidBot(null);

        assertFalse(result);
    }

    @Test
    void testIsValidBot_NullBotProfile() {
        boolean result = botService.isValidBot(humanPlayer);

        assertFalse(result);
    }

    @Test
    void testIsValidBot_NullBotName() {
        botProfile.setBotName(null);
        boolean result = botService.isValidBot(botPlayer);

        assertFalse(result);
    }

    @Test
    void testGetBotDisplayName_ValidBot() {
        String result = botService.getBotDisplayName(botPlayer);

        String expected = String.format("%s (%s - %s)",
                botProfile.getBotName(),
                botProfile.getLevel(),
                botProfile.getStrategy());
        assertEquals(expected, result);
    }

    @Test
    void testGetBotDisplayName_InvalidBot() {
        String result = botService.getBotDisplayName(humanPlayer);

        assertEquals("Unknown Bot", result);
    }

    @Test
    void testGetBotDisplayName_NullPlayer() {
        String result = botService.getBotDisplayName(null);

        assertEquals("Unknown Bot", result);
    }

    @Test
    void testExecuteBotTurn_HandlesExecutorException() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        doThrow(new RuntimeException("Strategy execution failed")).when(botStrategyExecutor).executeTurn(botPlayer, game);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> botService.executeBotTurn(botPlayer, game));

        assertEquals("Failed to execute bot turn", exception.getMessage());
        assertEquals("Strategy execution failed", exception.getCause().getMessage());
    }

    @Test
    void testEvaluateAttackProbability_EdgeCases() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        when(botStrategyExecutor.evaluateAttackProbability(botPlayer, 0, 5)).thenReturn(0.0);
        double result1 = botService.evaluateAttackProbability(botPlayer, 0, 5);
        assertEquals(0.0, result1);

        when(botStrategyExecutor.evaluateAttackProbability(botPlayer, 100, 1)).thenReturn(0.99);
        double result2 = botService.evaluateAttackProbability(botPlayer, 100, 1);
        assertEquals(0.99, result2);
    }

    @Test
    void testFactoryInteractions() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        botService.executeBotTurn(botPlayer, game);
        botService.evaluateAttackProbability(botPlayer, 5, 3);
        botService.getBestAttackTargets(botPlayer, game);
        botService.getBestDefensePositions(botPlayer, game);

        verify(botStrategyFactory, times(4)).getExecutor(botProfile);
    }

    @Test
    void testExecuteBotTurn_LoggingBehavior() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        botService.executeBotTurn(botPlayer, game);

        verify(botStrategyExecutor).executeTurn(botPlayer, game);
    }

    @Test
    void testExecuteBotTurnComplete_WithNonBotPlayer_ShouldThrowException() {
        String gameCode = "TEST-GAME";
        Long playerId = 1L;

        Player humanPlayerModel = new Player();
        humanPlayerModel.setId(playerId);
        humanPlayerModel.setIsBot(false);

        when(playerService.findById(playerId)).thenReturn(Optional.of(humanPlayerModel));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> botService.executeBotTurnComplete(gameCode, playerId));

        assertEquals("Bot not found or not a bot with ID: " + playerId, exception.getMessage());

        verify(gameService, never()).findByGameCode(anyString());
        verify(gameStateService, never()).isPlayerTurn(any(), any());
    }

    @Test
    void testExecuteBotTurnComplete_WithPlayerNotFound_ShouldThrowException() {
        String gameCode = "TEST-GAME";
        Long playerId = 999L;

        when(playerService.findById(playerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> botService.executeBotTurnComplete(gameCode, playerId));

        assertEquals("Bot not found or not a bot with ID: " + playerId, exception.getMessage());

        verify(gameService, never()).findByGameCode(anyString());
        verify(gameStateService, never()).isPlayerTurn(any(), any());
    }

    @Test
    void testExecuteBotTurnComplete_WithNotPlayerTurn_ShouldThrowException() {
        String gameCode = "TEST-GAME";
        Long botId = 1L;

        // Crear un Player bot válido
        Player validBotPlayer = new Player();
        validBotPlayer.setId(botId);
        validBotPlayer.setIsBot(true);

        when(playerService.findById(botId)).thenReturn(Optional.of(validBotPlayer));
        when(gameService.findByGameCode(gameCode)).thenReturn(gameModel);
        when(gameStateService.isPlayerTurn(gameModel, botId)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.executeBotTurnComplete(gameCode, botId));

        assertTrue(exception.getMessage().contains("It's not bot's turn"));

        verify(gameService, never()).save(any());
        verify(gameStateService, never()).nextTurn(any());
    }

    @Test
    void testExecuteBotTurnComplete_SuccessfulExecution_ShouldReturnGameResponseDto() {
        String gameCode = "TEST-GAME";
        Long botId = 1L;

        // Crear un Player bot válido
        Player validBotPlayer = new Player();
        validBotPlayer.setId(botId);
        validBotPlayer.setIsBot(true);

        // Configurar todos los mocks necesarios
        when(playerService.findById(botId)).thenReturn(Optional.of(validBotPlayer));
        when(gameService.findByGameCode(gameCode)).thenReturn(gameModel);
        when(gameStateService.isPlayerTurn(gameModel, botId)).thenReturn(true);
        when(playerMapper.toEntity(validBotPlayer)).thenReturn(botPlayer);
        when(gameMapper.toEntity(gameModel)).thenReturn(game);
        when(botStrategyFactory.getExecutor(any())).thenReturn(botStrategyExecutor);
        when(gameService.save(gameModel)).thenReturn(gameModel);
        when(gameMapper.toResponseDto(gameModel)).thenReturn(gameResponseDto);

        // Mock del executor - no debe lanzar excepciones
        doNothing().when(botStrategyExecutor).executeTurn(any(), any());

        // Ejecutar el método
        GameResponseDto result = botService.executeBotTurnComplete(gameCode, botId);

        // Verificaciones básicas
        assertNotNull(result);
        assertEquals(gameResponseDto, result);

        // Verificar que se llamaron los métodos clave
        verify(playerService, times(1)).findById(botId);
        verify(gameService, times(1)).findByGameCode(gameCode);
        verify(gameStateService, times(1)).isPlayerTurn(gameModel, botId);
        verify(gameStateService, times(1)).nextTurn(gameModel);
        verify(gameService, times(1)).save(gameModel);
    }
}
package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import ar.edu.utn.frc.tup.piii.repository.GameEventRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEventServiceImplTest {

    @Mock
    private GameEventRepository gameEventRepository;

    @Mock
    private GameService gameService;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private GameEventServiceImpl gameEventService;

    private Game testGame;
    private GameEntity testGameEntity;
    private Player testPlayer;
    private PlayerEntity testPlayerEntity;
    private UserEntity testUser;
    private BotProfileEntity testBotProfile;
    private GameEventEntity testEventEntity;

    @BeforeEach
    void setUp() {
        // Setup test data
        testGame = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .currentTurn(1)
                .build();

        testGameEntity = new GameEntity();
        testGameEntity.setId(1L);
        testGameEntity.setGameCode("TEST123");

        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testBotProfile = new BotProfileEntity();
        testBotProfile.setId(1L);
        testBotProfile.setBotName("TestBot");

        testPlayer = Player.builder()
                .id(1L)
                .username("testUser")
                .displayName("testUser")
                .build();

        testPlayerEntity = new PlayerEntity();
        testPlayerEntity.setId(1L);
        testPlayerEntity.setUser(testUser);

        testEventEntity = new GameEventEntity();
        testEventEntity.setId(1L);
        testEventEntity.setGame(testGameEntity);
        testEventEntity.setActor(testPlayerEntity);
        testEventEntity.setType(EventType.GAME_STARTED);
        testEventEntity.setTimestamp(LocalDateTime.now());
        testEventEntity.setTurnNumber(1);
        testEventEntity.setData("test data");
    }

    @Test
    void recordEvent_ShouldCreateAndSaveEvent() {
        // Arrange
        Long gameId = 1L;
        Long actorId = 1L;
        EventType eventType = EventType.GAME_STARTED;
        Integer turnNumber = 1;
        String eventData = "test data";

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(actorId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordEvent(gameId, actorId, eventType, turnNumber, eventData);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
        verify(gameService).findById(gameId);
        verify(playerService).findById(actorId);
    }

    @Test
    void recordEvent_WithNullActor_ShouldCreateEventWithoutActor() {
        // Arrange
        Long gameId = 1L;
        Long actorId = null;
        EventType eventType = EventType.GAME_STARTED;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordEvent(gameId, actorId, eventType, null, null);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
        verify(playerService, never()).findById(any());
    }

    @Test
    void recordEvent_WithNonExistentPlayer_ShouldCreateEventWithoutActor() {
        // Arrange
        Long gameId = 1L;
        Long actorId = 999L;
        EventType eventType = EventType.GAME_STARTED;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(actorId)).thenReturn(Optional.empty());
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordEvent(gameId, actorId, eventType, null, null);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordTerritoryConquest_ShouldCreateConquestEvent() {
        // Arrange
        Long gameId = 1L;
        Long conquererPlayerId = 1L;
        String conqueredTerritory = "Argentina";
        String fromPlayer = "enemy";
        Integer turnNumber = 1;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(conquererPlayerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordTerritoryConquest(
                gameId, conquererPlayerId, conqueredTerritory, fromPlayer, turnNumber);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordAttack_ShouldCreateAttackEvent() {
        // Arrange
        Long gameId = 1L;
        Long attackerPlayerId = 1L;
        String fromTerritory = "Brazil";
        String toTerritory = "Argentina";
        Integer turnNumber = 1;
        boolean successful = true;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(attackerPlayerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordAttack(
                gameId, attackerPlayerId, fromTerritory, toTerritory, turnNumber, successful);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordTurnStart_ShouldCreateTurnStartEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        Integer turnNumber = 1;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordTurnStart(gameId, playerId, turnNumber);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordTurnEnd_ShouldCreateTurnEndEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        Integer turnNumber = 1;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordTurnEnd(gameId, playerId, turnNumber);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordGameStart_ShouldCreateGameStartEvent() {
        // Arrange
        Long gameId = 1L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordGameStart(gameId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordGameFinish_WithWinner_ShouldCreateGameFinishEvent() {
        // Arrange
        Long gameId = 1L;
        Long winnerPlayerId = 1L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(winnerPlayerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordGameFinish(gameId, winnerPlayerId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordGameFinish_WithoutWinner_ShouldCreateGameFinishEvent() {
        // Arrange
        Long gameId = 1L;
        Long winnerPlayerId = null;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordGameFinish(gameId, winnerPlayerId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordPlayerJoined_ShouldCreatePlayerJoinedEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordPlayerJoined(gameId, playerId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordPlayerLeft_ShouldCreatePlayerLeftEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordPlayerLeft(gameId, playerId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordPlayerEliminated_WithEliminator_ShouldCreatePlayerEliminatedEvent() {
        // Arrange
        Long gameId = 1L;
        Long eliminatedPlayerId = 1L;
        Long eliminatorPlayerId = 2L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(eliminatedPlayerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordPlayerEliminated(gameId, eliminatedPlayerId, eliminatorPlayerId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordPlayerEliminated_WithoutEliminator_ShouldCreatePlayerEliminatedEvent() {
        // Arrange
        Long gameId = 1L;
        Long eliminatedPlayerId = 1L;
        Long eliminatorPlayerId = null;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(eliminatedPlayerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordPlayerEliminated(gameId, eliminatedPlayerId, eliminatorPlayerId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordCardsTraded_ShouldCreateCardsTradeEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        Integer turnNumber = 1;
        String cardsData = "card data";

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordCardsTraded(gameId, playerId, turnNumber, cardsData);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordReinforcementsPlaced_ShouldCreateReinforcementEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        String territory = "Argentina";
        Integer reinforcements = 5;
        Integer turnNumber = 1;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordReinforcementsPlaced(
                gameId, playerId, territory, reinforcements, turnNumber);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordFortification_ShouldCreateFortificationEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        String fromTerritory = "Brazil";
        String toTerritory = "Argentina";
        Integer armies = 3;
        Integer turnNumber = 1;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordFortification(
                gameId, playerId, fromTerritory, toTerritory, armies, turnNumber);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void recordObjectiveCompleted_ShouldCreateObjectiveCompletedEvent() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        String objectiveData = "objective data";

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.save(any(GameEventEntity.class))).thenReturn(testEventEntity);

        // Act
        GameEventEntity result = gameEventService.recordObjectiveCompleted(gameId, playerId, objectiveData);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).save(any(GameEventEntity.class));
    }

    @Test
    void getGameHistory_ShouldReturnOrderedEvents() {
        // Arrange
        Long gameId = 1L;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventEntity> result = gameEventService.getGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameEventRepository).findByGameOrderByTimestampDesc(testGameEntity);
    }

    @Test
    void getPlayerEventsInGame_WithExistingPlayer_ShouldReturnPlayerEvents() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(gameEventRepository.findByGame(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventEntity> result = gameEventService.getPlayerEventsInGame(gameId, playerId);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).findByGame(testGameEntity);
    }

    @Test
    void getPlayerEventsInGame_WithNonExistingPlayer_ShouldReturnEmptyList() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 999L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerService.findById(playerId)).thenReturn(Optional.empty());

        // Act
        List<GameEventEntity> result = gameEventService.getPlayerEventsInGame(gameId, playerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRecentGameEvents_ShouldReturnRecentEvents() {
        // Arrange
        Long gameId = 1L;
        int hoursBack = 24;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findRecentEventsByGame(eq(testGameEntity), any(LocalDateTime.class)))
                .thenReturn(events);

        // Act
        List<GameEventEntity> result = gameEventService.getRecentGameEvents(gameId, hoursBack);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameEventRepository).findRecentEventsByGame(eq(testGameEntity), any(LocalDateTime.class));
    }

    @Test
    void getGameEventStats_ShouldReturnEventStatistics() {
        // Arrange
        Long gameId = 1L;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.countAttacksByGame(testGameEntity)).thenReturn(5L);
        when(gameEventRepository.findByGame(testGameEntity)).thenReturn(events);

        // Act
        Map<String, Object> result = gameEventService.getGameEventStats(gameId);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalAttacks"));
        assertTrue(result.containsKey("conquestsByPlayer"));
        assertTrue(result.containsKey("totalEvents"));
        verify(gameEventRepository).countAttacksByGame(testGameEntity);
    }

    @Test
    void getFormattedGameHistory_ShouldReturnFormattedEvents() {
        // Arrange
        Long gameId = 1L;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void getEventsByType_ShouldReturnFilteredEvents() {
        // Arrange
        Long gameId = 1L;
        EventType eventType = EventType.ATTACK_PERFORMED;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGame(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventEntity> result = gameEventService.getEventsByType(gameId, eventType);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).findByGame(testGameEntity);
    }

    @Test
    void getEventsByTurn_ShouldReturnTurnEvents() {
        // Arrange
        Long gameId = 1L;
        Integer turnNumber = 1;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameAndTurnNumber(testGameEntity, turnNumber)).thenReturn(events);

        // Act
        List<GameEventEntity> result = gameEventService.getEventsByTurn(gameId, turnNumber);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameEventRepository).findByGameAndTurnNumber(testGameEntity, turnNumber);
    }

    @Test
    void getLastEventByType_WithoutEvents_ShouldReturnEmpty() {
        // Arrange
        Long gameId = 1L;
        EventType eventType = EventType.ATTACK_PERFORMED;
        List<GameEventEntity> events = Collections.emptyList();

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGame(testGameEntity)).thenReturn(events);

        // Act
        Optional<GameEventEntity> result = gameEventService.getLastEventByType(gameId, eventType);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void countEventsByType_ShouldReturnCount() {
        // Arrange
        Long gameId = 1L;
        EventType eventType = EventType.ATTACK_PERFORMED;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGame(testGameEntity)).thenReturn(events);

        // Act
        Long result = gameEventService.countEventsByType(gameId, eventType);

        // Assert
        assertNotNull(result);
        verify(gameEventRepository).findByGame(testGameEntity);
    }

    @Test
    void getPlayerAttacks_WithExistingPlayer_ShouldReturnAttacks() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.findByActorAndType(testPlayerEntity, EventType.ATTACK_PERFORMED))
                .thenReturn(events);

        // Act
        List<GameEventEntity> result = gameEventService.getPlayerAttacks(gameId, playerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameEventRepository).findByActorAndType(testPlayerEntity, EventType.ATTACK_PERFORMED);
    }

    @Test
    void getPlayerAttacks_WithNonExistingPlayer_ShouldReturnEmptyList() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 999L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerService.findById(playerId)).thenReturn(Optional.empty());

        // Act
        List<GameEventEntity> result = gameEventService.getPlayerAttacks(gameId, playerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPlayerConquests_WithExistingPlayer_ShouldReturnConquests() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 1L;
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerService.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(playerMapper.toEntity(testPlayer)).thenReturn(testPlayerEntity);
        when(gameEventRepository.findByActorAndType(testPlayerEntity, EventType.TERRITORY_CONQUERED))
                .thenReturn(events);

        // Act
        List<GameEventEntity> result = gameEventService.getPlayerConquests(gameId, playerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gameEventRepository).findByActorAndType(testPlayerEntity, EventType.TERRITORY_CONQUERED);
    }

    @Test
    void getPlayerConquests_WithNonExistingPlayer_ShouldReturnEmptyList() {
        // Arrange
        Long gameId = 1L;
        Long playerId = 999L;

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(playerService.findById(playerId)).thenReturn(Optional.empty());

        // Act
        List<GameEventEntity> result = gameEventService.getPlayerConquests(gameId, playerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests for formatEventForDisplay method (private method testing through getFormattedGameHistory)

    @Test
    void formatEventForDisplay_TerritoryConquered_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.TERRITORY_CONQUERED);
        testEventEntity.setData("{\"territory\":\"Argentina\", \"fromPlayer\":\"Enemy\"}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertNotNull(dto.getDescription());
        assertTrue(dto.getDescription().contains("conquistó"));
    }

    @Test
    void formatEventForDisplay_AttackPerformed_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.ATTACK_PERFORMED);
        testEventEntity.setData("{\"from\":\"Brazil\", \"to\":\"Argentina\", \"successful\":true}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertNotNull(dto.getDescription());
        assertTrue(dto.getDescription().contains("atacó"));
    }

    @Test
    void formatEventForDisplay_AttackPerformed_Failed_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.ATTACK_PERFORMED);
        testEventEntity.setData("{\"from\":\"Brazil\", \"to\":\"Argentina\", \"successful\":false}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertNotNull(dto.getDescription());
        assertTrue(dto.getDescription().contains("fallido"));
    }

    @Test
    void formatEventForDisplay_TurnStarted_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.TURN_STARTED);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("testUser comenzó su turno", dto.getDescription());
    }

    @Test
    void formatEventForDisplay_TurnEnded_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.TURN_ENDED);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("testUser terminó su turno", dto.getDescription());
    }

    @Test
    void formatEventForDisplay_GameStarted_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.GAME_STARTED);
        testEventEntity.setActor(null); // Sistema event
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("La partida ha comenzado", dto.getDescription());
    }

    @Test
    void formatEventForDisplay_GameFinished_WithWinner_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.GAME_FINISHED);
        testEventEntity.setData("{\"winnerId\":1}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("ganó la partida"));
    }

    @Test
    void formatEventForDisplay_PlayerJoined_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.PLAYER_JOINED);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("testUser se unió a la partida", dto.getDescription());
    }

    @Test
    void formatEventForDisplay_PlayerLeft_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.PLAYER_LEFT);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("testUser abandonó la partida", dto.getDescription());
    }

    @Test
    void formatEventForDisplay_PlayerEliminated_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.PLAYER_ELIMINATED);
        testEventEntity.setData("{\"eliminatorId\":2}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("fue eliminado"));
    }

    @Test
    void formatEventForDisplay_CardsTraded_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.CARDS_TRADED);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("testUser intercambió cartas", dto.getDescription());
    }

    @Test
    void formatEventForDisplay_ReinforcementsPlaced_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.REINFORCEMENTS_PLACED);
        testEventEntity.setData("{\"territory\":\"Argentina\", \"reinforcements\":5}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("colocó"));
        assertTrue(dto.getDescription().contains("refuerzos"));
    }

    @Test
    void formatEventForDisplay_FortificationPerformed_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.FORTIFICATION_PERFORMED);
        testEventEntity.setData("{\"from\":\"Brazil\", \"to\":\"Argentina\", \"armies\":3}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("movió"));
        assertTrue(dto.getDescription().contains("ejércitos"));
    }

    @Test
    void formatEventForDisplay_ObjectiveCompleted_ShouldFormatCorrectly() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.OBJECTIVE_COMPLETED);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("testUser completó un objetivo", dto.getDescription());
    }

    // Tests for getActorName method with different actor types

    @Test
    void getActorName_WithBotProfile_ShouldReturnBotName() {
        // Arrange
        Long gameId = 1L;
        PlayerEntity botPlayerEntity = new PlayerEntity();
        botPlayerEntity.setId(2L);
        botPlayerEntity.setBotProfile(testBotProfile);
        botPlayerEntity.setUser(null);

        testEventEntity.setActor(botPlayerEntity);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("TestBot", dto.getActorName());
    }

    @Test
    void getActorName_WithNullActor_ShouldReturnSistema() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setActor(null);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("Sistema", dto.getActorName());
    }

    @Test
    void getActorName_WithInvalidActor_ShouldReturnUnknown() {
        // Arrange
        Long gameId = 1L;
        PlayerEntity invalidPlayerEntity = new PlayerEntity();
        invalidPlayerEntity.setId(3L);
        invalidPlayerEntity.setUser(null);
        invalidPlayerEntity.setBotProfile(null);

        testEventEntity.setActor(invalidPlayerEntity);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("Jugador Desconocido", dto.getActorName());
    }

    // Tests for parsing methods with error handling

    @Test
    void parseConquestMessage_WithInvalidData_ShouldReturnFallback() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.TERRITORY_CONQUERED);
        testEventEntity.setData("invalid json");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("conquistó un territorio"));
    }

    @Test
    void parseAttackMessage_WithInvalidData_ShouldReturnFallback() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.ATTACK_PERFORMED);
        testEventEntity.setData("invalid json");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("realizó un ataque"));
    }

    @Test
    void parseReinforcementsMessage_WithInvalidData_ShouldReturnFallback() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.REINFORCEMENTS_PLACED);
        testEventEntity.setData("invalid json");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("colocó refuerzos"));
    }

    @Test
    void parseFortificationMessage_WithInvalidData_ShouldReturnFallback() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.FORTIFICATION_PERFORMED);
        testEventEntity.setData("invalid json");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("realizó una fortificación"));
    }

    // Test extractJsonValue method indirectly

    @Test
    void extractJsonValue_ShouldExtractCorrectValues() {
        // Arrange
        Long gameId = 1L;
        testEventEntity.setType(EventType.TERRITORY_CONQUERED);
        testEventEntity.setData("{\"territory\":\"Argentina\", \"fromPlayer\":\"Enemy\", \"successful\":true, \"count\":5}");
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertTrue(dto.getDescription().contains("Argentina"));
        assertTrue(dto.getDescription().contains("Enemy"));
    }

    // Edge case tests

    @Test
    void getGameEventStats_WithEmptyConquests_ShouldHandleCorrectly() {
        // Arrange
        Long gameId = 1L;
        List<GameEventEntity> events = Collections.emptyList();

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.countAttacksByGame(testGameEntity)).thenReturn(0L);
        when(gameEventRepository.findByGame(testGameEntity)).thenReturn(events);

        // Act
        Map<String, Object> result = gameEventService.getGameEventStats(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.get("totalAttacks"));
        assertTrue(((Map<?, ?>) result.get("conquestsByPlayer")).isEmpty());
        assertEquals(0, result.get("totalEvents"));
    }

    @Test
    void getActorName_WithException_ShouldReturnUnknown() {
        // Arrange
        Long gameId = 1L;
        PlayerEntity problematicPlayerEntity = mock(PlayerEntity.class);
        when(problematicPlayerEntity.getUser()).thenThrow(new RuntimeException("Test exception"));

        testEventEntity.setActor(problematicPlayerEntity);
        List<GameEventEntity> events = Arrays.asList(testEventEntity);

        when(gameService.findById(gameId)).thenReturn(testGame);
        when(gameMapper.toEntity(testGame)).thenReturn(testGameEntity);
        when(gameEventRepository.findByGameOrderByTimestampDesc(testGameEntity)).thenReturn(events);

        // Act
        List<GameEventDto> result = gameEventService.getFormattedGameHistory(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        GameEventDto dto = result.get(0);
        assertEquals("Jugador Desconocido", dto.getActorName());
    }
}
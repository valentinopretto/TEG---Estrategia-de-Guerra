package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameMapperTest {

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private GameEventMapper gameEventMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private GameMapper gameMapper;

    private GameEntity gameEntity;
    private Game game;
    private UserEntity userEntity;
    private PlayerEntity playerEntity;
    private GameTerritoryEntity territoryEntity;
    private CountryEntity countryEntity;
    private ContinentEntity continentEntity;
    private CardEntity cardEntity;
    private GameEventEntity gameEventEntity;
    private ChatMessageEntity chatMessageEntity;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        // Setup UserEntity
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");

        // Setup Continent
        continentEntity = new ContinentEntity();
        continentEntity.setId(1L);
        continentEntity.setName("Test Continent");
        continentEntity.setBonusArmies(3);

        // Setup Country
        countryEntity = new CountryEntity();
        countryEntity.setId(1L);
        countryEntity.setName("Test Country");
        countryEntity.setContinent(continentEntity);
        countryEntity.setNeighbors(new HashSet<>());

        // Setup Player
        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setUser(userEntity);
        playerEntity.setStatus(PlayerStatus.ACTIVE);
        playerEntity.setColor(PlayerColor.RED);

        // Setup Territory
        territoryEntity = new GameTerritoryEntity();
        territoryEntity.setId(1L);
        territoryEntity.setCountry(countryEntity);
        territoryEntity.setOwner(playerEntity);
        territoryEntity.setArmies(5);

        // Setup Card
        cardEntity = new CardEntity();
        cardEntity.setId(1L);
        cardEntity.setType(CardType.INFANTRY);
        cardEntity.setCountry(countryEntity);

        // Setup Game Event
        gameEventEntity = new GameEventEntity();
        gameEventEntity.setId(1L);
        gameEventEntity.setType(EventType.GAME_STARTED);
        gameEventEntity.setTurnNumber(1);
        gameEventEntity.setTimestamp(testTime);

        // Setup Chat Message
        chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setId(1L);
        chatMessageEntity.setContent("Test message");
        chatMessageEntity.setSentAt(testTime);
        chatMessageEntity.setIsSystemMessage(false);

        // Setup GameEntity
        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameCode("TEST123");
        gameEntity.setCreatedBy(userEntity);
        gameEntity.setStatus(GameState.NORMAL_PLAY);
        gameEntity.setCurrentPhase(TurnPhase.ATTACK);
        gameEntity.setCurrentTurn(5);
        gameEntity.setCurrentPlayerIndex(0);
        gameEntity.setMaxPlayers(6);
        gameEntity.setTurnTimeLimit(600);
        gameEntity.setChatEnabled(true);
        gameEntity.setPactsAllowed(false);
        gameEntity.setCreatedAt(testTime);
        gameEntity.setStartedAt(testTime.minusHours(1));
        gameEntity.setFinishedAt(null);
        gameEntity.setLastModified(testTime);
        gameEntity.setPlayers(Arrays.asList(playerEntity));
        gameEntity.setTerritories(Arrays.asList(territoryEntity));
        gameEntity.setDeck(Arrays.asList(cardEntity));
        gameEntity.setEvents(Arrays.asList(gameEventEntity));
        gameEntity.setChatMessages(Arrays.asList(chatMessageEntity));

        // Setup Game model
        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .createdByUsername("testuser")
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.ATTACK)
                .currentTurn(5)
                .currentPlayerIndex(0)
                .maxPlayers(6)
                .turnTimeLimit(600)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(testTime)
                .startedAt(testTime.minusHours(1))
                .finishedAt(null)
                .lastModified(testTime)
                .build();
    }

    @Test
    void toModel_WithNullEntity_ShouldReturnNull() {
        // When
        Game result = gameMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }


    @Test
    void toEntity_WithNullGame_ShouldReturnNull() {
        // When
        GameEntity result = gameMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithEmptyCollections_ShouldHandleCorrectly() {
        // Given
        gameEntity.setPlayers(Arrays.asList());
        gameEntity.setTerritories(Arrays.asList());
        gameEntity.setDeck(Arrays.asList());
        gameEntity.setEvents(Arrays.asList());
        gameEntity.setChatMessages(Arrays.asList());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPlayers()).isEmpty();
        assertThat(result.getTerritories()).isEmpty();
        assertThat(result.getDeck()).isEmpty();
        assertThat(result.getEvents()).isEmpty();
        assertThat(result.getChatMessages()).isEmpty();
    }

}
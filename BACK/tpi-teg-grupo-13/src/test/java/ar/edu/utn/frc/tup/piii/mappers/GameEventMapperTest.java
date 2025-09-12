package ar.edu.utn.frc.tup.piii.mappers;


import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEventEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.GameEvent;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GameEventMapperTest {

    @InjectMocks
    private GameEventMapper gameEventMapper;

    private GameEventEntity gameEventEntity;
    private GameEvent gameEvent;
    private PlayerEntity humanPlayer;
    private PlayerEntity botPlayer;
    private UserEntity userEntity;
    private BotProfileEntity botProfile;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        // Setup UserEntity
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");

        // Setup BotProfile
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);
        botProfile.setBotName("Test Bot");

        // Setup Human Player
        humanPlayer = new PlayerEntity();
        humanPlayer.setId(1L);
        humanPlayer.setUser(userEntity);
        humanPlayer.setBotProfile(null);

        // Setup Bot Player
        botPlayer = new PlayerEntity();
        botPlayer.setId(2L);
        botPlayer.setUser(null);
        botPlayer.setBotProfile(botProfile);

        // Setup GameEventEntity
        gameEventEntity = new GameEventEntity();
        gameEventEntity.setId(1L);
        gameEventEntity.setTurnNumber(1);
        gameEventEntity.setActor(humanPlayer);
        gameEventEntity.setType(EventType.GAME_STARTED);
        gameEventEntity.setData("{\"key\":\"value\"}");
        gameEventEntity.setTimestamp(testTime);

        // Setup GameEvent model
        gameEvent = GameEvent.builder()
                .id(1L)
                .turnNumber(1)
                .actorName("testuser")
                .type(EventType.GAME_STARTED)
                .description("El juego ha comenzado")
                .data("{\"key\":\"value\"}")
                .timestamp(testTime)
                .build();
    }

    @Test
    void toModel_WithHumanPlayerActor_ShouldMapCorrectly() {
        // When
        GameEvent result = gameEventMapper.toModel(gameEventEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTurnNumber()).isEqualTo(1);
        assertThat(result.getActorName()).isEqualTo("testuser");
        assertThat(result.getType()).isEqualTo(EventType.GAME_STARTED);
        assertThat(result.getDescription()).isEqualTo("El juego ha comenzado");
        assertThat(result.getData()).isEqualTo("{\"key\":\"value\"}");
        assertThat(result.getTimestamp()).isEqualTo(testTime);
    }

    @Test
    void toModel_WithBotPlayerActor_ShouldMapCorrectly() {
        // Given
        gameEventEntity.setActor(botPlayer);
        gameEventEntity.setType(EventType.PLAYER_JOINED);

        // When
        GameEvent result = gameEventMapper.toModel(gameEventEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getActorName()).isEqualTo("Test Bot");
        assertThat(result.getDescription()).isEqualTo("Test Bot se unió al juego");
    }

    @Test
    void toModel_WithNullActor_ShouldUseSystemActor() {
        // Given
        gameEventEntity.setActor(null);

        // When
        GameEvent result = gameEventMapper.toModel(gameEventEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getActorName()).isEqualTo("Sistema");
        assertThat(result.getDescription()).isEqualTo("El juego ha comenzado");
    }

    @Test
    void toModel_WithNullEntity_ShouldReturnNull() {
        // When
        GameEvent result = gameEventMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithAllEventTypes_ShouldGenerateCorrectDescriptions() {
        // Test ATTACK_PERFORMED
        gameEventEntity.setType(EventType.ATTACK_PERFORMED);
        GameEvent result = gameEventMapper.toModel(gameEventEntity);
        assertThat(result.getDescription()).isEqualTo("testuser realizó un ataque");

        // Test TERRITORY_CONQUERED
        gameEventEntity.setType(EventType.TERRITORY_CONQUERED);
        result = gameEventMapper.toModel(gameEventEntity);
        assertThat(result.getDescription()).isEqualTo("testuser conquistó un territorio");

        // Test TURN_STARTED
        gameEventEntity.setType(EventType.TURN_STARTED);
        result = gameEventMapper.toModel(gameEventEntity);
        assertThat(result.getDescription()).isEqualTo("Turno de testuser");

        // Test TURN_ENDED
        gameEventEntity.setType(EventType.TURN_ENDED);
        result = gameEventMapper.toModel(gameEventEntity);
        assertThat(result.getDescription()).isEqualTo("testuser terminó su turno");

        // Test PLAYER_ELIMINATED
        gameEventEntity.setType(EventType.PLAYER_ELIMINATED);
        result = gameEventMapper.toModel(gameEventEntity);
        assertThat(result.getDescription()).isEqualTo("testuser fue eliminado");

        // Test GAME_FINISHED
        gameEventEntity.setType(EventType.GAME_FINISHED);
        result = gameEventMapper.toModel(gameEventEntity);
        assertThat(result.getDescription()).isEqualTo("El juego ha terminado");
    }

    @Test
    void toEntity_WithValidModel_ShouldMapCorrectly() {
        // When
        GameEventEntity result = gameEventMapper.toEntity(gameEvent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTurnNumber()).isEqualTo(1);
        assertThat(result.getType()).isEqualTo(EventType.GAME_STARTED);
        assertThat(result.getData()).isEqualTo("{\"key\":\"value\"}");
        assertThat(result.getTimestamp()).isEqualTo(testTime);
    }

    @Test
    void toEntity_WithNullModel_ShouldReturnNull() {
        // When
        GameEventEntity result = gameEventMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDto_WithValidModel_ShouldMapCorrectly() {
        // When
        GameEventDto result = gameEventMapper.toDto(gameEvent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(EventType.GAME_STARTED);
        assertThat(result.getDescription()).isEqualTo("El juego ha comenzado");
        assertThat(result.getTimestamp()).isEqualTo(testTime);
    }

    @Test
    void toDto_WithNullModel_ShouldReturnNull() {
        // When
        GameEventDto result = gameEventMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getPlayerDisplayName_WithUnknownPlayer_ShouldReturnUnknown() {
        // Given
        PlayerEntity unknownPlayer = new PlayerEntity();
        unknownPlayer.setUser(null);
        unknownPlayer.setBotProfile(null);
        gameEventEntity.setActor(unknownPlayer);

        // When
        GameEvent result = gameEventMapper.toModel(gameEventEntity);

        // Then
        assertThat(result.getActorName()).isEqualTo("Unknown");
    }
}
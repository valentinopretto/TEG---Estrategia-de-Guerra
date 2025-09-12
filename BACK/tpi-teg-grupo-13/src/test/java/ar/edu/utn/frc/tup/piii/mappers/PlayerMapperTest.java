package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.Objective;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerMapperTest {

    @Mock
    private CardMapper cardMapper;

    @Mock
    private ObjectiveMapper objectiveMapper;

    @InjectMocks
    private PlayerMapper playerMapper;

    private PlayerEntity playerEntity;
    private Player player;
    private UserEntity userEntity;
    private BotProfileEntity botProfile;
    private ObjectiveEntity objectiveEntity;
    private CardEntity cardEntity;
    private GameTerritoryEntity territoryEntity;
    private CountryEntity countryEntity;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        // Setup UserEntity
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@email.com");

        // Setup BotProfile
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);
        botProfile.setBotName("Test Bot");

        // Setup Objective
        objectiveEntity = new ObjectiveEntity();
        objectiveEntity.setId(1L);
        objectiveEntity.setType(ObjectiveType.COMMON);
        objectiveEntity.setDescription("Test objective");

        // Setup Country and Territory
        countryEntity = new CountryEntity();
        countryEntity.setId(1L);
        countryEntity.setName("Test Country");

        territoryEntity = new GameTerritoryEntity();
        territoryEntity.setId(1L);
        territoryEntity.setCountry(countryEntity);
        territoryEntity.setArmies(5);

        // Setup Card
        cardEntity = new CardEntity();
        cardEntity.setId(1L);
        cardEntity.setType(CardType.INFANTRY);
        cardEntity.setCountry(countryEntity);

        // Setup PlayerEntity (Human Player)
        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setUser(userEntity);
        playerEntity.setBotProfile(null); // Human player
        playerEntity.setObjective(objectiveEntity);
        playerEntity.setStatus(PlayerStatus.ACTIVE);
        playerEntity.setColor(PlayerColor.RED);
        playerEntity.setArmiesToPlace(10);
        playerEntity.setSeatOrder(0);
        playerEntity.setJoinedAt(testTime);
        playerEntity.setEliminatedAt(null);
        playerEntity.setHand(Arrays.asList(cardEntity));
        playerEntity.setTerritories(Arrays.asList(territoryEntity));

        // Setup Player model
        player = Player.builder()
                .id(1L)
                .username("testuser")
                .displayName("testuser")
                .isBot(false)
                .botLevel(null)
                .status(PlayerStatus.ACTIVE)
                .color(PlayerColor.RED)
                .armiesToPlace(10)
                .seatOrder(0)
                .joinedAt(testTime)
                .eliminatedAt(null)
                .objective(Objective.builder().id(1L).build())
                .hand(Arrays.asList(Card.builder().id(1L).build()))
                .territoryIds(Arrays.asList(1L))
                .build();
    }

    @Test
    void toModel_WithHumanPlayer_ShouldMapCorrectly() {
        // Given
        when(objectiveMapper.toModel(any(ObjectiveEntity.class)))
                .thenReturn(Objective.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getDisplayName()).isEqualTo("testuser");
        assertThat(result.getIsBot()).isFalse();
        assertThat(result.getBotLevel()).isNull();
        assertThat(result.getStatus()).isEqualTo(PlayerStatus.ACTIVE);
        assertThat(result.getColor()).isEqualTo(PlayerColor.RED);
        assertThat(result.getArmiesToPlace()).isEqualTo(10);
        assertThat(result.getSeatOrder()).isEqualTo(0);
        assertThat(result.getJoinedAt()).isEqualTo(testTime);
        assertThat(result.getEliminatedAt()).isNull();
        assertThat(result.getHand()).hasSize(1);
        assertThat(result.getTerritoryIds()).hasSize(1);
        assertThat(result.getTerritoryIds().get(0)).isEqualTo(1L);
    }

    @Test
    void toModel_WithBotPlayer_ShouldMapCorrectly() {
        // Given
        playerEntity.setUser(null);
        playerEntity.setBotProfile(botProfile);

        when(objectiveMapper.toModel(any(ObjectiveEntity.class)))
                .thenReturn(Objective.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isNull();
        assertThat(result.getDisplayName()).isEqualTo("Test Bot");
        assertThat(result.getIsBot()).isTrue();
        assertThat(result.getBotLevel()).isEqualTo(BotLevel.BALANCED);
    }

    @Test
    void toModel_WithNullEntity_ShouldReturnNull() {
        // When
        Player result = playerMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithEmptyCollections_ShouldHandleCorrectly() {
        // Given
        playerEntity.setHand(Arrays.asList());
        playerEntity.setTerritories(Arrays.asList());

        when(objectiveMapper.toModel(any(ObjectiveEntity.class)))
                .thenReturn(Objective.builder().id(1L).build());

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHand()).isEmpty();
        assertThat(result.getTerritoryIds()).isEmpty();
    }

    @Test
    void toEntity_WithValidPlayer_ShouldMapCorrectly() {
        // When
        PlayerEntity result = playerMapper.toEntity(player);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PlayerStatus.ACTIVE);
        assertThat(result.getColor()).isEqualTo(PlayerColor.RED);
        assertThat(result.getArmiesToPlace()).isEqualTo(10);
        assertThat(result.getSeatOrder()).isEqualTo(0);
        assertThat(result.getJoinedAt()).isEqualTo(testTime);
        assertThat(result.getEliminatedAt()).isNull();
    }

    @Test
    void toEntity_WithNullPlayer_ShouldReturnNull() {
        // When
        PlayerEntity result = playerMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getDisplayName_WithUser_ShouldReturnUsername() {
        // Given
        playerEntity.setUser(userEntity);
        playerEntity.setBotProfile(null);

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result.getDisplayName()).isEqualTo("testuser");
    }

    @Test
    void getDisplayName_WithBot_ShouldReturnBotName() {
        // Given
        playerEntity.setUser(null);
        playerEntity.setBotProfile(botProfile);

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result.getDisplayName()).isEqualTo("Test Bot");
    }

    @Test
    void getDisplayName_WithNeitherUserNorBot_ShouldReturnUnknown() {
        // Given
        playerEntity.setUser(null);
        playerEntity.setBotProfile(null);

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result.getDisplayName()).isEqualTo("Unknown Player");
    }

    @Test
    void toModel_WithEliminatedPlayer_ShouldMapEliminationTime() {
        // Given
        LocalDateTime eliminationTime = testTime.plusHours(2);
        playerEntity.setEliminatedAt(eliminationTime);
        playerEntity.setStatus(PlayerStatus.ELIMINATED);

        when(objectiveMapper.toModel(any(ObjectiveEntity.class)))
                .thenReturn(Objective.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result.getEliminatedAt()).isEqualTo(eliminationTime);
        assertThat(result.getStatus()).isEqualTo(PlayerStatus.ELIMINATED);
    }

    @Test
    void toModel_WithNullObjective_ShouldHandleCorrectly() {
        // Given
        playerEntity.setObjective(null);

        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());

        // When
        Player result = playerMapper.toModel(playerEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getObjective()).isNull();
    }
}
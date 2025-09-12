package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.ObjectiveMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ObjectiveMapper objectiveMapper;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private Player player;
    private PlayerEntity playerEntity;
    private Game game;
    private GameEntity gameEntity;
    private User user;
    private UserEntity userEntity;
    private Objective objective;
    private ObjectiveEntity objectiveEntity;

    @BeforeEach
    void setUp() {
        // Setup Player
        player = Player.builder()
                .id(1L)
                .username("testPlayer")
                .displayName("Test Player")
                .isBot(false)
                .status(PlayerStatus.ACTIVE)
                .color(PlayerColor.RED)
                .armiesToPlace(5)
                .seatOrder(0)
                .tradeCount(0)
                .joinedAt(LocalDateTime.now())
                .build();

        // Setup PlayerEntity
        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setStatus(PlayerStatus.ACTIVE);
        playerEntity.setColor(PlayerColor.RED);
        playerEntity.setArmiesToPlace(5);
        playerEntity.setSeatOrder(0);
        playerEntity.setTradeCount(0);
        playerEntity.setJoinedAt(LocalDateTime.now());

        // Setup User
        user = User.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .build();

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setEmail("test@example.com");

        // Setup Game
        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.WAITING_FOR_PLAYERS)
                .build();

        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameCode("TEST123");

        // Setup Objective
        objective = Objective.builder()
                .id(1L)
                .type(ObjectiveType.COMMON)
                .description("Test objective")
                .build();

        objectiveEntity = new ObjectiveEntity();
        objectiveEntity.setId(1L);
        objectiveEntity.setType(ObjectiveType.COMMON);
    }

    @Test
    void save_WhenValidPlayer_ShouldReturnSavedPlayer() {
        // Given
        when(playerMapper.toEntity(player)).thenReturn(playerEntity);
        when(playerRepository.save(playerEntity)).thenReturn(playerEntity);
        when(playerMapper.toModel(playerEntity)).thenReturn(player);

        // When
        Player result = playerService.save(player);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(playerRepository).save(playerEntity);
    }

    @Test
    void findById_WhenPlayerExists_ShouldReturnPlayer() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(playerMapper.toModel(playerEntity)).thenReturn(player);

        // When
        Optional<Player> result = playerService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findById_WhenPlayerNotExists_ShouldReturnEmpty() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Player> result = playerService.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_WhenPlayersExist_ShouldReturnList() {
        // Given
        PlayerEntity player2Entity = new PlayerEntity();
        player2Entity.setId(2L);

        Player player2 = Player.builder().id(2L).build();

        List<PlayerEntity> entities = Arrays.asList(playerEntity, player2Entity);

        when(playerRepository.findAll()).thenReturn(entities);
        when(playerMapper.toModel(playerEntity)).thenReturn(player);
        when(playerMapper.toModel(player2Entity)).thenReturn(player2);

        // When
        List<Player> result = playerService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void findAll_WhenNoPlayersExist_ShouldThrowException() {
        // Given
        when(playerRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> playerService.findAll())
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("No players found");
    }

    @Test
    void findByGame_WhenPlayersExist_ShouldReturnList() {
        // Given
        List<PlayerEntity> entities = Arrays.asList(playerEntity);
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerRepository.findByGame(gameEntity)).thenReturn(entities);
        when(playerMapper.toModel(playerEntity)).thenReturn(player);

        // When
        List<Player> result = playerService.findByGame(game);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void findByGame_WhenNoPlayersExist_ShouldThrowException() {
        // Given
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerRepository.findByGame(gameEntity)).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> playerService.findByGame(game))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("No players found");
    }

    @Test
    void findActivePlayersByGame_WhenPlayersExist_ShouldReturnList() {
        // Given
        List<PlayerEntity> entities = Arrays.asList(playerEntity);
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerRepository.findActivePlayersByGame(gameEntity)).thenReturn(entities);
        when(playerMapper.toModel(playerEntity)).thenReturn(player);

        // When
        List<Player> result = playerService.findActivePlayersByGame(game);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void findActivePlayersByGame_WhenNoPlayersExist_ShouldThrowException() {
        // Given
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerRepository.findActivePlayersByGame(gameEntity)).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> playerService.findActivePlayersByGame(game))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("No players found");
    }

    @Test
    void deleteById_ShouldCallRepository() {
        // When
        playerService.deleteById(1L);

        // Then
        verify(playerRepository).deleteById(1L);
    }

    @Test
    void createHumanPlayer_WhenValidData_ShouldReturnPlayer() {
        // Given
        when(userMapper.toEntity(user)).thenReturn(userEntity);
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(playerEntity);
        when(playerMapper.toModel(playerEntity)).thenReturn(player);

        // When
        Player result = playerService.createHumanPlayer(user, game, 1);

        // Then
        assertThat(result).isNotNull();
        verify(playerRepository).save(any(PlayerEntity.class));
    }

    @Test
    void createHumanPlayer_WhenNoColorsAvailable_ShouldThrowException() {
        // Given
        List<Player> existingPlayers = Arrays.asList(
                Player.builder().color(PlayerColor.RED).build(),
                Player.builder().color(PlayerColor.BLUE).build(),
                Player.builder().color(PlayerColor.GREEN).build(),
                Player.builder().color(PlayerColor.YELLOW).build(),
                Player.builder().color(PlayerColor.BLACK).build(),
                Player.builder().color(PlayerColor.PURPLE).build()
        );

        Game fullGame = Game.builder().players(existingPlayers).build();

        when(userMapper.toEntity(user)).thenReturn(userEntity);
        when(gameMapper.toEntity(fullGame)).thenReturn(gameEntity);

        // When & Then
        assertThatThrownBy(() -> playerService.createHumanPlayer(user, fullGame, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No available colors left for players");
    }

    @Test
    void createBotPlayer_ShouldReturnNull() {
        // When
        Player result = playerService.createBotPlayer(BotLevel.BALANCED, game);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void eliminatePlayer_ShouldUpdatePlayerStatus() {
        // Given
        PlayerEntity playerToUpdate = new PlayerEntity();
        playerToUpdate.setId(1L);
        playerToUpdate.setStatus(PlayerStatus.ACTIVE);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerToUpdate));

        // When
        playerService.eliminatePlayer(1L);

        // Then
        verify(playerRepository).findById(1L);
        verify(playerRepository).save(playerToUpdate);
        assertThat(playerToUpdate.getStatus()).isEqualTo(PlayerStatus.ELIMINATED);
    }

    @Test
    void activatePlayer_ShouldUpdatePlayerStatus() {
        // Given
        PlayerEntity playerToUpdate = new PlayerEntity();
        playerToUpdate.setId(1L);
        playerToUpdate.setStatus(PlayerStatus.WAITING);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerToUpdate));

        // When
        playerService.activatePlayer(1L);

        // Then
        verify(playerRepository).findById(1L);
        verify(playerRepository).save(playerToUpdate);
        assertThat(playerToUpdate.getStatus()).isEqualTo(PlayerStatus.ACTIVE);
    }

    @Test
    void updateStatus_WhenPlayerExists_ShouldUpdateStatus() {
        // Given
        PlayerEntity playerToUpdate = new PlayerEntity();
        playerToUpdate.setId(1L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerToUpdate));

        // When
        playerService.updateStatus(1L, PlayerStatus.DISCONNECTED);

        // Then
        verify(playerRepository).save(playerToUpdate);
        assertThat(playerToUpdate.getStatus()).isEqualTo(PlayerStatus.DISCONNECTED);
    }

    @Test
    void updateStatus_WhenPlayerNotExists_ShouldNotThrow() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then - should not throw exception
        playerService.updateStatus(999L, PlayerStatus.ACTIVE);

        verify(playerRepository, never()).save(any());
    }

    @Test
    void isEliminated_WhenPlayerIsEliminated_ShouldReturnTrue() {
        // Given
        PlayerEntity eliminatedPlayer = new PlayerEntity();
        eliminatedPlayer.setStatus(PlayerStatus.ELIMINATED);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(eliminatedPlayer));

        // When
        boolean result = playerService.isEliminated(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isEliminated_WhenPlayerIsNotEliminated_ShouldReturnFalse() {
        // Given
        PlayerEntity activePlayer = new PlayerEntity();
        activePlayer.setStatus(PlayerStatus.ACTIVE);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(activePlayer));

        // When
        boolean result = playerService.isEliminated(1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isEliminated_WhenPlayerNotExists_ShouldReturnFalse() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = playerService.isEliminated(999L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isActive_WhenPlayerIsActive_ShouldReturnTrue() {
        // Given
        PlayerEntity activePlayer = new PlayerEntity();
        activePlayer.setStatus(PlayerStatus.ACTIVE);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(activePlayer));

        // When
        boolean result = playerService.isActive(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_WhenPlayerIsNotActive_ShouldReturnFalse() {
        // Given
        PlayerEntity eliminatedPlayer = new PlayerEntity();
        eliminatedPlayer.setStatus(PlayerStatus.ELIMINATED);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(eliminatedPlayer));

        // When
        boolean result = playerService.isActive(1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isActive_WhenPlayerNotExists_ShouldReturnFalse() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = playerService.isActive(999L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void assignObjective_WhenPlayerExists_ShouldAssignObjective() {
        // Given
        PlayerEntity playerToUpdate = new PlayerEntity();
        playerToUpdate.setId(1L);

        when(objectiveMapper.toEntity(objective)).thenReturn(objectiveEntity);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerToUpdate));

        // When
        playerService.assignObjective(1L, objective);

        // Then
        verify(playerRepository).save(playerToUpdate);
        assertThat(playerToUpdate.getObjective()).isEqualTo(objectiveEntity);
    }

    @Test
    void assignObjective_WhenPlayerNotExists_ShouldNotThrow() {
        // Given
        when(objectiveMapper.toEntity(objective)).thenReturn(objectiveEntity);
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then - should not throw exception
        playerService.assignObjective(999L, objective);

        verify(playerRepository, never()).save(any());
    }

    @Test
    void hasWon_ShouldReturnFalse() {
        // When
        boolean result = playerService.hasWon(1L, game);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAchievedObjective_ShouldReturnFalse() {
        // When
        boolean result = playerService.hasAchievedObjective(1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void addArmiesToPlace_ShouldCallRepository() {
        // When
        playerService.addArmiesToPlace(1L, 5);

        // Then
        verify(playerRepository).addArmiesToPlace(1L, 5);
    }

    @Test
    void removeArmiesToPlace_ShouldCallRepository() {
        // When
        playerService.removeArmiesToPlace(1L, 3);

        // Then
        verify(playerRepository).removeArmiesToPlace(1L, 3);
    }

    @Test
    void getArmiesToPlace_WhenPlayerExists_ShouldReturnArmies() {
        // Given
        PlayerEntity playerWithArmies = new PlayerEntity();
        playerWithArmies.setArmiesToPlace(7);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerWithArmies));
        when(playerMapper.toModel(playerWithArmies)).thenReturn(
                Player.builder().armiesToPlace(7).build()
        );

        // When
        int result = playerService.getArmiesToPlace(1L);

        // Then
        assertThat(result).isEqualTo(7);
    }

    @Test
    void getArmiesToPlace_WhenPlayerNotExists_ShouldReturnZero() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        int result = playerService.getArmiesToPlace(999L);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void canPerformAction_WhenPlayerIsActiveAndBelongsToGame_ShouldReturnTrue() {
        // Given
        PlayerEntity activePlayer = new PlayerEntity();
        activePlayer.setStatus(PlayerStatus.ACTIVE);
        activePlayer.setGame(gameEntity);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(activePlayer));

        // When
        boolean result = playerService.canPerformAction(1L, game);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_WhenPlayerIsNotActive_ShouldReturnFalse() {
        // Given
        PlayerEntity eliminatedPlayer = new PlayerEntity();
        eliminatedPlayer.setStatus(PlayerStatus.ELIMINATED);
        eliminatedPlayer.setGame(gameEntity);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(eliminatedPlayer));

        // When
        boolean result = playerService.canPerformAction(1L, game);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPlayerTurn_WhenIsPlayersTurn_ShouldReturnTrue() {
        // Given
        Player currentPlayer = Player.builder().id(1L).build();
        List<Player> players = Arrays.asList(currentPlayer);
        Game gameWithPlayers = Game.builder()
                .players(players)
                .currentPlayerIndex(0)
                .build();

        // When
        boolean result = playerService.isPlayerTurn(1L, gameWithPlayers);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void belongsToGame_WhenPlayerBelongsToGame_ShouldReturnTrue() {
        // Given
        GameEntity playerGame = new GameEntity();
        playerGame.setId(1L);

        PlayerEntity playerInGame = new PlayerEntity();
        playerInGame.setGame(playerGame);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerInGame));

        // When
        boolean result = playerService.belongsToGame(1L, 1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void belongsToGame_WhenPlayerNotBelongsToGame_ShouldReturnFalse() {
        // Given
        GameEntity playerGame = new GameEntity();
        playerGame.setId(2L);

        PlayerEntity playerInDifferentGame = new PlayerEntity();
        playerInDifferentGame.setGame(playerGame);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerInDifferentGame));

        // When
        boolean result = playerService.belongsToGame(1L, 1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void belongsToGame_WhenPlayerNotExists_ShouldReturnFalse() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = playerService.belongsToGame(999L, 1L);

        // Then
        assertThat(result).isFalse();
    }
}
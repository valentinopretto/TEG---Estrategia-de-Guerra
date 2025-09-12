package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    private UserEntity user1;
    private UserEntity user2;
    private GameEntity game1;
    private GameEntity game2;
    private PlayerEntity activePlayer1;
    private PlayerEntity activePlayer2;
    private PlayerEntity eliminatedPlayer;

    @BeforeEach
    void setUp() {
        // Crear usuarios
        user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        user1.setPasswordHash("hash1");
        user1.setIsActive(true);
        user1 = entityManager.persistAndFlush(user1);

        user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");
        user2.setPasswordHash("hash2");
        user2.setIsActive(true);
        user2 = entityManager.persistAndFlush(user2);

        // Crear juegos
        game1 = new GameEntity();
        game1.setGameCode("GAME001");
        game1.setCreatedBy(user1);
        game1.setStatus(GameState.REINFORCEMENT_5);
        game1.setMaxPlayers(4);
        game1.setCreatedAt(LocalDateTime.now());
        game1.setLastModified(LocalDateTime.now());
        game1 = entityManager.persistAndFlush(game1);

        game2 = new GameEntity();
        game2.setGameCode("GAME002");
        game2.setCreatedBy(user2);
        game2.setStatus(GameState.WAITING_FOR_PLAYERS);
        game2.setMaxPlayers(6);
        game2.setCreatedAt(LocalDateTime.now());
        game2.setLastModified(LocalDateTime.now());
        game2 = entityManager.persistAndFlush(game2);

        // Crear jugadores
        activePlayer1 = createPlayer(game1, user1, PlayerColor.RED, PlayerStatus.ACTIVE, 1);
        activePlayer2 = createPlayer(game1, user2, PlayerColor.BLUE, PlayerStatus.ACTIVE, 2);
        eliminatedPlayer = createPlayer(game1, user1, PlayerColor.GREEN, PlayerStatus.ELIMINATED, 3);

        // Jugador en segundo juego
        createPlayer(game2, user2, PlayerColor.YELLOW, PlayerStatus.WAITING, 1);

        entityManager.flush();
    }

    private PlayerEntity createPlayer(GameEntity game, UserEntity user, PlayerColor color,
                                      PlayerStatus status, int seatOrder) {
        PlayerEntity player = new PlayerEntity();
        player.setGame(game);
        player.setUser(user);
        player.setColor(color);
        player.setStatus(status);
        player.setSeatOrder(seatOrder);
        player.setArmiesToPlace(5);
        player.setJoinedAt(LocalDateTime.now());
        if (status == PlayerStatus.ELIMINATED) {
            player.setEliminatedAt(LocalDateTime.now());
        }
        return entityManager.persistAndFlush(player);
    }

    @Test
    void findActivePlayersByGame_ShouldReturnOnlyNonEliminatedPlayersInOrder() {
        List<PlayerEntity> activePlayers = playerRepository.findActivePlayersByGame(game1);

        assertThat(activePlayers).hasSize(2);
        assertThat(activePlayers.get(0).getSeatOrder()).isEqualTo(1);
        assertThat(activePlayers.get(1).getSeatOrder()).isEqualTo(2);
        assertThat(activePlayers).allMatch(p -> p.getStatus() != PlayerStatus.ELIMINATED);
    }


    @Test
    void findAllHumanActivePlayers_ShouldReturnOnlyHumanActivePlayers() {
        List<PlayerEntity> humanPlayers = playerRepository.findAllHumanActivePlayers();

        assertThat(humanPlayers).hasSize(2); // activePlayer1 y activePlayer2
        assertThat(humanPlayers).allMatch(p -> p.getUser() != null);
        assertThat(humanPlayers).allMatch(p -> p.getStatus() == PlayerStatus.ACTIVE);
    }

    @Test
    void countActiveGamesByUser_ShouldReturnCountOfNonEliminatedGames() {
        long activeGamesCount = playerRepository.countActiveGamesByUser(user1);

        assertThat(activeGamesCount).isEqualTo(1); // Solo activePlayer1, eliminatedPlayer no cuenta
    }

    @Test
    @Transactional
    void addArmiesToPlace_ShouldIncreaseArmiesCount() {
        int initialArmies = activePlayer1.getArmiesToPlace();

        playerRepository.addArmiesToPlace(activePlayer1.getId(), 3);
        entityManager.flush();
        entityManager.clear();

        PlayerEntity updated = playerRepository.findById(activePlayer1.getId()).orElseThrow();
        assertThat(updated.getArmiesToPlace()).isEqualTo(initialArmies + 3);
    }

    @Test
    @Transactional
    void removeArmiesToPlace_ShouldDecreaseArmiesCount() {
        int initialArmies = activePlayer1.getArmiesToPlace();

        playerRepository.removeArmiesToPlace(activePlayer1.getId(), 2);
        entityManager.flush();
        entityManager.clear();

        PlayerEntity updated = playerRepository.findById(activePlayer1.getId()).orElseThrow();
        assertThat(updated.getArmiesToPlace()).isEqualTo(initialArmies - 2);
    }

    @Test
    @Transactional
    void removeArmiesToPlace_WhenMoreThanAvailable_ShouldNotGoBelowZero() {
        playerRepository.removeArmiesToPlace(activePlayer1.getId(), 100);
        entityManager.flush();
        entityManager.clear();

        PlayerEntity updated = playerRepository.findById(activePlayer1.getId()).orElseThrow();
        assertThat(updated.getArmiesToPlace()).isEqualTo(0);
    }


    @Test
    void findByGameAndColor_ShouldReturnPlayerWithColor() {
        Optional<PlayerEntity> found = playerRepository.findByGameAndColor(game1, PlayerColor.BLUE);

        assertThat(found).isPresent();
        assertThat(found.get().getColor()).isEqualTo(PlayerColor.BLUE);
        assertThat(found.get().getUser().getId()).isEqualTo(user2.getId());
    }
}
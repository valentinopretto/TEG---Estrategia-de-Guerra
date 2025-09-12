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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class GameRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameRepository gameRepository;

    private UserEntity user1;
    private UserEntity user2;
    private GameEntity waitingGame;
    private GameEntity activeGame;
    private GameEntity pausedGame;
    private GameEntity finishedGame;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        gameRepository.deleteAll();

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

        // Crear juegos con diferentes estados
        waitingGame = new GameEntity();
        waitingGame.setGameCode("WAIT001");
        waitingGame.setCreatedBy(user1);
        waitingGame.setStatus(GameState.WAITING_FOR_PLAYERS);
        waitingGame.setMaxPlayers(4);
        waitingGame.setCreatedAt(LocalDateTime.now());
        waitingGame.setLastModified(LocalDateTime.now());
        waitingGame = entityManager.persistAndFlush(waitingGame);

        activeGame = new GameEntity();
        activeGame.setGameCode("ACTV001");
        activeGame.setCreatedBy(user2);
        activeGame.setStatus(GameState.REINFORCEMENT_5);
        activeGame.setMaxPlayers(6);
        activeGame.setCreatedAt(LocalDateTime.now());
        activeGame.setLastModified(LocalDateTime.now());
        activeGame = entityManager.persistAndFlush(activeGame);

        pausedGame = new GameEntity();
        pausedGame.setGameCode("PAUS001");
        pausedGame.setCreatedBy(user1);
        pausedGame.setStatus(GameState.PAUSED);
        pausedGame.setMaxPlayers(4);
        pausedGame.setCreatedAt(LocalDateTime.now());
        pausedGame.setLastModified(LocalDateTime.now().minusHours(2));
        pausedGame = entityManager.persistAndFlush(pausedGame);

        finishedGame = new GameEntity();
        finishedGame.setGameCode("FINN001");
        finishedGame.setCreatedBy(user2);
        finishedGame.setStatus(GameState.FINISHED);
        finishedGame.setMaxPlayers(6);
        finishedGame.setCreatedAt(LocalDateTime.now());
        finishedGame.setLastModified(LocalDateTime.now());
        finishedGame = entityManager.persistAndFlush(finishedGame);

        // Crear jugadores para algunos juegos
        createPlayersForGame(waitingGame, 2); // 2 de 4 jugadores
        createPlayersForGame(activeGame, 6);  // Juego lleno
    }

    private void createPlayersForGame(GameEntity game, int playerCount) {
        PlayerColor[] colors = {PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN,
                PlayerColor.YELLOW, PlayerColor.BLACK, PlayerColor.PURPLE};

        for (int i = 0; i < playerCount; i++) {
            PlayerEntity player = new PlayerEntity();
            player.setGame(game);
            player.setUser(i % 2 == 0 ? user1 : user2);
            player.setColor(colors[i]);
            player.setStatus(PlayerStatus.WAITING);
            player.setSeatOrder(i);
            player.setJoinedAt(LocalDateTime.now());
            entityManager.persistAndFlush(player);
        }
    }

    @Test
    void findByStatusIn_ShouldReturnGamesWithSpecifiedStatuses() {
        List<GameState> statuses = Arrays.asList(GameState.WAITING_FOR_PLAYERS, GameState.REINFORCEMENT_5);

        List<GameEntity> games = gameRepository.findByStatusIn(statuses);

        assertThat(games).hasSize(2);
        assertThat(games).extracting(GameEntity::getStatus)
                .containsExactlyInAnyOrder(GameState.WAITING_FOR_PLAYERS, GameState.REINFORCEMENT_5);
    }

    @Test
    void findAvailableGames_ShouldReturnOnlyWaitingGamesWithSpace() {
        List<GameEntity> availableGames = gameRepository.findAvailableGames();

        assertThat(availableGames).hasSize(1);
        assertThat(availableGames.get(0).getGameCode()).isEqualTo("WAIT001");
        assertThat(availableGames.get(0).getStatus()).isEqualTo(GameState.WAITING_FOR_PLAYERS);
    }

    @Test
    void findGamesByUser_ShouldReturnGamesWhereUserIsPlayer() {
        List<GameEntity> user1Games = gameRepository.findGamesByUser(user1);

        assertThat(user1Games).hasSize(2); // user1 participa en waitingGame y activeGame
        assertThat(user1Games).extracting(GameEntity::getGameCode)
                .containsExactlyInAnyOrder("WAIT001", "ACTV001");
    }


    @Test
    void existsByGameCode_WhenGameExists_ShouldReturnTrue() {
        boolean exists = gameRepository.existsByGameCode("WAIT001");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByGameCode_WhenGameNotExists_ShouldReturnFalse() {
        boolean exists = gameRepository.existsByGameCode("NOEXIST");

        assertThat(exists).isFalse();
    }


    @Test
    void findByCreatedByIdOrderByCreatedAtDesc_ShouldReturnUserGamesInOrder() {
        List<GameEntity> user1Games = gameRepository.findByCreatedByIdOrderByCreatedAtDesc(user1.getId());

        assertThat(user1Games).hasSize(2);
        assertThat(user1Games).extracting(GameEntity::getGameCode)
                .containsExactly("PAUS001", "WAIT001"); // Orden descendente por fecha
    }
}
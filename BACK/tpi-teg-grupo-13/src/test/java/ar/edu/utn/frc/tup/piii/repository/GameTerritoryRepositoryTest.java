package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.*;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class GameTerritoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameTerritoryRepository territoryRepository;

    private UserEntity user1;
    private UserEntity user2;
    private GameEntity game;
    private PlayerEntity player1;
    private PlayerEntity player2;
    private ContinentEntity continent1;
    private ContinentEntity continent2;
    private CountryEntity country1;
    private CountryEntity country2;
    private CountryEntity country3;
    private GameTerritoryEntity territory1;
    private GameTerritoryEntity territory2;
    private GameTerritoryEntity territory3;

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

        // Crear juego
        game = new GameEntity();
        game.setGameCode("GAME001");
        game.setCreatedBy(user1);
        game.setStatus(GameState.REINFORCEMENT_5);
        game.setMaxPlayers(4);
        game.setCreatedAt(LocalDateTime.now());
        game.setLastModified(LocalDateTime.now());
        game = entityManager.persistAndFlush(game);

        // Crear continentes
        continent1 = new ContinentEntity();
        continent1.setName("América del Sur");
        continent1.setBonusArmies(3);
        continent1 = entityManager.persistAndFlush(continent1);

        continent2 = new ContinentEntity();
        continent2.setName("Europa");
        continent2.setBonusArmies(5);
        continent2 = entityManager.persistAndFlush(continent2);

        // Crear países
        country1 = new CountryEntity();
        country1.setName("Argentina");
        country1.setContinent(continent1);
        country1 = entityManager.persistAndFlush(country1);

        country2 = new CountryEntity();
        country2.setName("Brasil");
        country2.setContinent(continent1);
        country2 = entityManager.persistAndFlush(country2);

        country3 = new CountryEntity();
        country3.setName("España");
        country3.setContinent(continent2);
        country3 = entityManager.persistAndFlush(country3);

        // Crear jugadores
        player1 = new PlayerEntity();
        player1.setGame(game);
        player1.setUser(user1);
        player1.setColor(PlayerColor.RED);
        player1.setStatus(PlayerStatus.ACTIVE);
        player1.setSeatOrder(1);
        player1.setJoinedAt(LocalDateTime.now());
        player1 = entityManager.persistAndFlush(player1);

        player2 = new PlayerEntity();
        player2.setGame(game);
        player2.setUser(user2);
        player2.setColor(PlayerColor.BLUE);
        player2.setStatus(PlayerStatus.ACTIVE);
        player2.setSeatOrder(2);
        player2.setJoinedAt(LocalDateTime.now());
        player2 = entityManager.persistAndFlush(player2);

        // Crear territorios del juego
        territory1 = new GameTerritoryEntity();
        territory1.setGame(game);
        territory1.setCountry(country1);
        territory1.setOwner(player1);
        territory1.setArmies(5); // Puede atacar
        territory1 = entityManager.persistAndFlush(territory1);

        territory2 = new GameTerritoryEntity();
        territory2.setGame(game);
        territory2.setCountry(country2);
        territory2.setOwner(player1);
        territory2.setArmies(1); // No puede atacar
        territory2 = entityManager.persistAndFlush(territory2);

        territory3 = new GameTerritoryEntity();
        territory3.setGame(game);
        territory3.setCountry(country3);
        territory3.setOwner(player2);
        territory3.setArmies(3); // Puede atacar
        territory3 = entityManager.persistAndFlush(territory3);

        entityManager.flush();
    }

    @Test
    void findTerritoriesCanAttack_ShouldReturnTerritoriesWithMoreThanOneArmy() {
        List<GameTerritoryEntity> canAttack = territoryRepository.findTerritoriesCanAttack(game);

        assertThat(canAttack).hasSize(2);
        assertThat(canAttack).extracting(GameTerritoryEntity::getArmies)
                .allMatch(armies -> armies > 1);
        assertThat(canAttack).extracting(t -> t.getCountry().getName())
                .containsExactlyInAnyOrder("Argentina", "España");
    }

    @Test
    void findPlayerTerritoriesCanAttack_ShouldReturnPlayerTerritoriesWithMoreThanOneArmy() {
        List<GameTerritoryEntity> player1CanAttack = territoryRepository.findPlayerTerritoriesCanAttack(player1);

        assertThat(player1CanAttack).hasSize(1);
        assertThat(player1CanAttack.get(0).getCountry().getName()).isEqualTo("Argentina");
        assertThat(player1CanAttack.get(0).getArmies()).isEqualTo(5);
    }

    @Test
    void getTotalArmiesByPlayer_ShouldReturnSumOfAllPlayerArmies() {
        Integer totalArmies = territoryRepository.getTotalArmiesByPlayer(player1);

        assertThat(totalArmies).isEqualTo(6); // 5 + 1
    }

    @Test
    void countTerritoriesByPlayer_ShouldReturnNumberOfTerritoriesOwnedByPlayer() {
        Long territoryCount = territoryRepository.countTerritoriesByPlayer(player1);

        assertThat(territoryCount).isEqualTo(2);
    }

    @Test
    void findTerritoriesByContinentName_ShouldReturnTerritoriesInSpecifiedContinent() {
        List<GameTerritoryEntity> southAmericaTerritories =
                territoryRepository.findTerritoriesByContinentName(game, "América del Sur");

        assertThat(southAmericaTerritories).hasSize(2);
        assertThat(southAmericaTerritories).extracting(t -> t.getCountry().getName())
                .containsExactlyInAnyOrder("Argentina", "Brasil");
        assertThat(southAmericaTerritories).allMatch(t ->
                t.getCountry().getContinent().getName().equals("América del Sur"));
    }

    @Test
    void findByGameAndCountry_ShouldReturnSpecificTerritory() {
        Optional<GameTerritoryEntity> found = territoryRepository.findByGameAndCountry(game, country1);

        assertThat(found).isPresent();
        assertThat(found.get().getCountry().getName()).isEqualTo("Argentina");
        assertThat(found.get().getOwner().getId()).isEqualTo(player1.getId());
        assertThat(found.get().getArmies()).isEqualTo(5);
    }

    @Test
    void findByGameAndCountry_WhenNotExists_ShouldReturnEmpty() {
        CountryEntity nonExistentCountry = new CountryEntity();
        nonExistentCountry.setName("NoExiste");
        nonExistentCountry.setContinent(continent1);
        nonExistentCountry = entityManager.persistAndFlush(nonExistentCountry);

        Optional<GameTerritoryEntity> found = territoryRepository.findByGameAndCountry(game, nonExistentCountry);

        assertThat(found).isEmpty();
    }

    @Test
    void findByGameAndOwner_ShouldReturnAllTerritoriesOwnedByPlayerInGame() {
        List<GameTerritoryEntity> player1Territories = territoryRepository.findByGameAndOwner(game, player1);

        assertThat(player1Territories).hasSize(2);
        assertThat(player1Territories).extracting(t -> t.getCountry().getName())
                .containsExactlyInAnyOrder("Argentina", "Brasil");
        assertThat(player1Territories).allMatch(t -> t.getOwner().getId().equals(player1.getId()));
    }
}
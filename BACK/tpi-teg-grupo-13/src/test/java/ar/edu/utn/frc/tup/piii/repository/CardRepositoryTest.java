package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    private UserEntity user1;
    private UserEntity user2;
    private GameEntity game;
    private PlayerEntity player1;
    private PlayerEntity player2;
    private CountryEntity country1;
    private CountryEntity country2;
    private CardEntity cardInDeck;
    private CardEntity cardInHand;
    private CardEntity wildcardInDeck;
    private CardEntity wildcardInHand;

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

        // Crear continente
        ContinentEntity continent = new ContinentEntity();
        continent.setName("América del Sur");
        continent.setBonusArmies(3);
        continent = entityManager.persistAndFlush(continent);

        // Crear países
        country1 = new CountryEntity();
        country1.setName("Argentina");
        country1.setContinent(continent);
        country1 = entityManager.persistAndFlush(country1);

        country2 = new CountryEntity();
        country2.setName("Brasil");
        country2.setContinent(continent);
        country2 = entityManager.persistAndFlush(country2);

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

        // Crear cartas
        cardInDeck = new CardEntity();
        cardInDeck.setGame(game);
        cardInDeck.setCountry(country1);
        cardInDeck.setType(CardType.INFANTRY);
        cardInDeck.setIsInDeck(true);
        cardInDeck.setOwner(null);
        cardInDeck = entityManager.persistAndFlush(cardInDeck);

        cardInHand = new CardEntity();
        cardInHand.setGame(game);
        cardInHand.setCountry(country2);
        cardInHand.setType(CardType.CAVALRY);
        cardInHand.setIsInDeck(false);
        cardInHand.setOwner(player1);
        cardInHand = entityManager.persistAndFlush(cardInHand);

        wildcardInDeck = new CardEntity();
        wildcardInDeck.setGame(game);
        wildcardInDeck.setCountry(null);
        wildcardInDeck.setType(CardType.WILDCARD);
        wildcardInDeck.setIsInDeck(true);
        wildcardInDeck.setOwner(null);
        wildcardInDeck = entityManager.persistAndFlush(wildcardInDeck);

        wildcardInHand = new CardEntity();
        wildcardInHand.setGame(game);
        wildcardInHand.setCountry(null);
        wildcardInHand.setType(CardType.WILDCARD);
        wildcardInHand.setIsInDeck(false);
        wildcardInHand.setOwner(player2);
        wildcardInHand = entityManager.persistAndFlush(wildcardInHand);

        // Crear más cartas para testing
        CardEntity cannonCard = new CardEntity();
        cannonCard.setGame(game);
        cannonCard.setCountry(country1);
        cannonCard.setType(CardType.CANNON);
        cannonCard.setIsInDeck(false);
        cannonCard.setOwner(player1);
        entityManager.persistAndFlush(cannonCard);

        entityManager.flush();
    }

    @Test
    void findByGameAndIsInDeckTrue_ShouldReturnOnlyCardsInDeck() {
        List<CardEntity> cardsInDeck = cardRepository.findByGameAndIsInDeckTrue(game);

        assertThat(cardsInDeck).hasSize(2);
        assertThat(cardsInDeck).allMatch(CardEntity::getIsInDeck);
        assertThat(cardsInDeck).allMatch(card -> card.getOwner() == null);
    }

    @Test
    void findByGameAndOwnerIsNull_ShouldReturnCardsWithoutOwner() {
        List<CardEntity> unownedCards = cardRepository.findByGameAndOwnerIsNull(game);

        assertThat(unownedCards).hasSize(2);
        assertThat(unownedCards).allMatch(card -> card.getOwner() == null);
        assertThat(unownedCards).allMatch(card -> card.getIsInDeck());
    }

    @Test
    void findByOwnerAndType_ShouldReturnPlayerCardsOfSpecificType() {
        List<CardEntity> player1InfantryCards = cardRepository.findByOwnerAndType(player1, CardType.CAVALRY);

        assertThat(player1InfantryCards).hasSize(1);
        assertThat(player1InfantryCards.get(0).getType()).isEqualTo(CardType.CAVALRY);
        assertThat(player1InfantryCards.get(0).getOwner().getId()).isEqualTo(player1.getId());
    }

    @Test
    void findAvailableCardsRandomOrder_ShouldReturnShuffledDeckCards() {
        List<CardEntity> availableCards = cardRepository.findAvailableCardsRandomOrder(game);

        assertThat(availableCards).hasSize(2);
        assertThat(availableCards).allMatch(CardEntity::getIsInDeck);
        assertThat(availableCards).allMatch(card -> card.getGame().getId().equals(game.getId()));
    }

    @Test
    void countCardsByPlayer_ShouldReturnNumberOfCardsOwnedByPlayer() {
        Long player1CardCount = cardRepository.countCardsByPlayer(player1);

        assertThat(player1CardCount).isEqualTo(2); // cardInHand + cannonCard
    }

    @Test
    void findPlayerCardsByType_ShouldReturnPlayerCardsOfSpecificType() {
        List<CardEntity> player1CavalryCards = cardRepository.findPlayerCardsByType(player1, CardType.CAVALRY);

        assertThat(player1CavalryCards).hasSize(1);
        assertThat(player1CavalryCards.get(0).getType()).isEqualTo(CardType.CAVALRY);
        assertThat(player1CavalryCards.get(0).getCountry().getName()).isEqualTo("Brasil");
    }

    @Test
    void countCardsByTypeForPlayer_ShouldReturnCardCountGroupedByType() {
        List<Object[]> cardCounts = cardRepository.countCardsByTypeForPlayer(player1);

        assertThat(cardCounts).hasSize(2); // CAVALRY y CANNON

        // Verificar que los resultados contienen los tipos esperados
        boolean hasCavalry = false;
        boolean hasCannon = false;

        for (Object[] result : cardCounts) {
            CardType type = (CardType) result[0];
            Long count = (Long) result[1];

            if (type == CardType.CAVALRY) {
                hasCavalry = true;
                assertThat(count).isEqualTo(1L);
            } else if (type == CardType.CANNON) {
                hasCannon = true;
                assertThat(count).isEqualTo(1L);
            }
        }

        assertThat(hasCavalry).isTrue();
        assertThat(hasCannon).isTrue();
    }

    @Test
    void findByOwner_ShouldReturnAllCardsOwnedByPlayer() {
        List<CardEntity> player2Cards = cardRepository.findByOwner(player2);

        assertThat(player2Cards).hasSize(1);
        assertThat(player2Cards.get(0).getType()).isEqualTo(CardType.WILDCARD);
        assertThat(player2Cards.get(0).getCountry()).isNull();
    }

    @Test
    void findByGame_ShouldReturnAllCardsInGame() {
        List<CardEntity> allGameCards = cardRepository.findByGame(game);

        assertThat(allGameCards).hasSize(5); // Todas las cartas creadas para este juego
        assertThat(allGameCards).allMatch(card -> card.getGame().getId().equals(game.getId()));
    }
}
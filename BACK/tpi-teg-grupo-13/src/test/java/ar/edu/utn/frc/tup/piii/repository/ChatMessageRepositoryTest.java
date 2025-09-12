package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.ChatMessageEntity;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ChatMessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private UserEntity user1;
    private UserEntity user2;
    private GameEntity game1;
    private GameEntity game2;
    private PlayerEntity player1;
    private PlayerEntity player2;
    private ChatMessageEntity userMessage1;
    private ChatMessageEntity userMessage2;
    private ChatMessageEntity systemMessage;
    private ChatMessageEntity oldMessage;

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
        player1 = new PlayerEntity();
        player1.setGame(game1);
        player1.setUser(user1);
        player1.setColor(PlayerColor.RED);
        player1.setStatus(PlayerStatus.ACTIVE);
        player1.setSeatOrder(1);
        player1.setJoinedAt(LocalDateTime.now());
        player1 = entityManager.persistAndFlush(player1);

        player2 = new PlayerEntity();
        player2.setGame(game1);
        player2.setUser(user2);
        player2.setColor(PlayerColor.BLUE);
        player2.setStatus(PlayerStatus.ACTIVE);
        player2.setSeatOrder(2);
        player2.setJoinedAt(LocalDateTime.now());
        player2 = entityManager.persistAndFlush(player2);

        // Crear mensajes de chat
        LocalDateTime now = LocalDateTime.now();

        userMessage1 = new ChatMessageEntity();
        userMessage1.setSender(player1);
        userMessage1.setGame(game1);
        userMessage1.setContent("Hola a todos!");
        userMessage1.setSentAt(now.minusMinutes(10));
        userMessage1.setIsSystemMessage(false);
        userMessage1 = entityManager.persistAndFlush(userMessage1);

        userMessage2 = new ChatMessageEntity();
        userMessage2.setSender(player2);
        userMessage2.setGame(game1);
        userMessage2.setContent("¡Buena suerte!");
        userMessage2.setSentAt(now.minusMinutes(5));
        userMessage2.setIsSystemMessage(false);
        userMessage2 = entityManager.persistAndFlush(userMessage2);

        systemMessage = new ChatMessageEntity();
        systemMessage.setSender(player1);
        systemMessage.setGame(game1);
        systemMessage.setContent("El jugador user1 ha conquistado Argentina");
        systemMessage.setSentAt(now.minusMinutes(2));
        systemMessage.setIsSystemMessage(true);
        systemMessage = entityManager.persistAndFlush(systemMessage);

        oldMessage = new ChatMessageEntity();
        oldMessage.setSender(player1);
        oldMessage.setGame(game1);
        oldMessage.setContent("Mensaje antiguo");
        oldMessage.setSentAt(now.minusHours(2));
        oldMessage.setIsSystemMessage(false);
        oldMessage = entityManager.persistAndFlush(oldMessage);

        // Mensaje en otro juego
        PlayerEntity player3 = new PlayerEntity();
        player3.setGame(game2);
        player3.setUser(user1);
        player3.setColor(PlayerColor.GREEN);
        player3.setStatus(PlayerStatus.WAITING);
        player3.setSeatOrder(1);
        player3.setJoinedAt(LocalDateTime.now());
        player3 = entityManager.persistAndFlush(player3);

        ChatMessageEntity messageGame2 = new ChatMessageEntity();
        messageGame2.setSender(player3);
        messageGame2.setGame(game2);
        messageGame2.setContent("Esperando más jugadores");
        messageGame2.setSentAt(now.minusMinutes(1));
        messageGame2.setIsSystemMessage(false);
        entityManager.persistAndFlush(messageGame2);

        entityManager.flush();
    }

    @Test
    void findByGameAndIsSystemMessageTrue_ShouldReturnOnlySystemMessages() {
        List<ChatMessageEntity> systemMessages = chatMessageRepository.findByGameAndIsSystemMessageTrue(game1);

        assertThat(systemMessages).hasSize(1);
        assertThat(systemMessages.get(0).getContent()).isEqualTo("El jugador user1 ha conquistado Argentina");
        assertThat(systemMessages.get(0).getIsSystemMessage()).isTrue();
    }

    @Test
    void findByGameAndIsSystemMessageFalse_ShouldReturnOnlyUserMessages() {
        List<ChatMessageEntity> userMessages = chatMessageRepository.findByGameAndIsSystemMessageFalse(game1);

        assertThat(userMessages).hasSize(3); // userMessage1, userMessage2, oldMessage
        assertThat(userMessages).allMatch(msg -> !msg.getIsSystemMessage());
        assertThat(userMessages).extracting(ChatMessageEntity::getContent)
                .containsExactlyInAnyOrder("Hola a todos!", "¡Buena suerte!", "Mensaje antiguo");
    }

    @Test
    void findByGameIdOrderBySentAtAsc_ShouldReturnMessagesInChronologicalOrder() {
        List<ChatMessageEntity> orderedMessages = chatMessageRepository.findByGameIdOrderBySentAtAsc(game1.getId());

        assertThat(orderedMessages).hasSize(4);
        assertThat(orderedMessages.get(0).getContent()).isEqualTo("Mensaje antiguo"); // Más antiguo
        assertThat(orderedMessages.get(1).getContent()).isEqualTo("Hola a todos!");
        assertThat(orderedMessages.get(2).getContent()).isEqualTo("¡Buena suerte!");
        assertThat(orderedMessages.get(3).getContent()).isEqualTo("El jugador user1 ha conquistado Argentina"); // Más reciente
    }


    @Test
    void findByGameOrderBySentAtDesc_ShouldReturnMessagesInReverseChronologicalOrder() {
        List<ChatMessageEntity> descendingMessages = chatMessageRepository.findByGameOrderBySentAtDesc(game1);

        assertThat(descendingMessages).hasSize(4);
        assertThat(descendingMessages.get(0).getContent()).isEqualTo("El jugador user1 ha conquistado Argentina"); // Más reciente
        assertThat(descendingMessages.get(3).getContent()).isEqualTo("Mensaje antiguo"); // Más antiguo
    }

    @Test
    void findRecentMessagesByGame_ShouldReturnMessagesAfterSpecifiedTime() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(7);

        List<ChatMessageEntity> recentMessages = chatMessageRepository.findRecentMessagesByGame(game1, since);

        assertThat(recentMessages).hasSize(2); // userMessage2 y systemMessage
        assertThat(recentMessages).extracting(ChatMessageEntity::getContent)
                .containsExactly("¡Buena suerte!", "El jugador user1 ha conquistado Argentina");
    }

    @Test
    void findLastMessagesByGame_ShouldReturnLimitedNumberOfRecentMessages() {
        List<ChatMessageEntity> lastMessages = chatMessageRepository.findLastMessagesByGame(game1, 2);

        assertThat(lastMessages).hasSize(2);
        assertThat(lastMessages.get(0).getContent()).isEqualTo("El jugador user1 ha conquistado Argentina");
        assertThat(lastMessages.get(1).getContent()).isEqualTo("¡Buena suerte!");
    }

    @Test
    void countMessagesBySender_ShouldReturnNumberOfMessagesSentByPlayer() {
        Long messageCount = chatMessageRepository.countMessagesBySender(player1);

        assertThat(messageCount).isEqualTo(3); // userMessage1, systemMessage, oldMessage
    }

    @Test
    void findBySender_ShouldReturnAllMessagesFromSpecificPlayer() {
        List<ChatMessageEntity> player1Messages = chatMessageRepository.findBySender(player1);

        assertThat(player1Messages).hasSize(3);
        assertThat(player1Messages).allMatch(msg -> msg.getSender().getId().equals(player1.getId()));
    }

    @Test
    void findByGame_ShouldReturnAllMessagesInGame() {
        List<ChatMessageEntity> game1Messages = chatMessageRepository.findByGame(game1);

        assertThat(game1Messages).hasSize(4);
        assertThat(game1Messages).allMatch(msg -> msg.getGame().getId().equals(game1.getId()));
    }
}
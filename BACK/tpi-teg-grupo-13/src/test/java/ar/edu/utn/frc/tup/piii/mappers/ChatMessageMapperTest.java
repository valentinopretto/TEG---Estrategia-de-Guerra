package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.ChatMessageEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.ChatMessage;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageMapperTest {

    private ChatMessageMapper chatMessageMapper;
    private ChatMessageEntity chatMessageEntity;
    private ChatMessage chatMessage;
    private PlayerEntity playerEntity;
    private UserEntity userEntity;
    private BotProfileEntity botProfile;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        chatMessageMapper = new ChatMessageMapper();
        testTime = LocalDateTime.now();

        // Setup User
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");

        // Setup Bot Profile
        botProfile = new BotProfileEntity();
        botProfile.setId(1L);
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);
        botProfile.setBotName("Test Bot");

        // Setup Player (Human)
        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setUser(userEntity);
        playerEntity.setBotProfile(null);

        // Setup ChatMessageEntity
        chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setId(1L);
        chatMessageEntity.setSender(playerEntity);
        chatMessageEntity.setContent("Hello, world!");
        chatMessageEntity.setSentAt(testTime);
        chatMessageEntity.setIsSystemMessage(false);

        // Setup ChatMessage model
        chatMessage = ChatMessage.builder()
                .id(1L)
                .senderName("testuser")
                .content("Hello, world!")
                .sentAt(testTime)
                .isSystemMessage(false)
                .build();
    }

    @Test
    void toModel_WithHumanPlayer_ShouldMapCorrectly() {
        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSenderName()).isEqualTo("testuser");
        assertThat(result.getContent()).isEqualTo("Hello, world!");
        assertThat(result.getSentAt()).isEqualTo(testTime);
        assertThat(result.getIsSystemMessage()).isFalse();
    }

    @Test
    void toModel_WithBotPlayer_ShouldMapBotName() {
        // Given
        playerEntity.setUser(null);
        playerEntity.setBotProfile(botProfile);

        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("Test Bot");
        assertThat(result.getContent()).isEqualTo("Hello, world!");
        assertThat(result.getIsSystemMessage()).isFalse();
    }

    @Test
    void toModel_WithSystemMessage_ShouldMapSystemSender() {
        // Given
        chatMessageEntity.setIsSystemMessage(true);

        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("Sistema");
        assertThat(result.getIsSystemMessage()).isTrue();
    }

    @Test
    void toModel_WithNullSender_ShouldMapSystemSender() {
        // Given
        chatMessageEntity.setSender(null);
        chatMessageEntity.setIsSystemMessage(false);

        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("Sistema");
    }

    @Test
    void toModel_WithNullEntity_ShouldReturnNull() {
        // When
        ChatMessage result = chatMessageMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithSenderWithoutUserOrBot_ShouldMapSystemSender() {
        // Given
        playerEntity.setUser(null);
        playerEntity.setBotProfile(null);

        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("Sistema");
    }

    @Test
    void toEntity_WithValidChatMessage_ShouldMapCorrectly() {
        // When
        ChatMessageEntity result = chatMessageMapper.toEntity(chatMessage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Hello, world!");
        assertThat(result.getSentAt()).isEqualTo(testTime);
        assertThat(result.getIsSystemMessage()).isFalse();
        // Note: Sender is not mapped in toEntity method
        assertThat(result.getSender()).isNull();
    }

    @Test
    void toEntity_WithNullChatMessage_ShouldReturnNull() {
        // When
        ChatMessageEntity result = chatMessageMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithSystemMessage_ShouldMapCorrectly() {
        // Given
        ChatMessage systemMessage = ChatMessage.builder()
                .id(2L)
                .senderName("Sistema")
                .content("Game started")
                .sentAt(testTime)
                .isSystemMessage(true)
                .build();

        // When
        ChatMessageEntity result = chatMessageMapper.toEntity(systemMessage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getContent()).isEqualTo("Game started");
        assertThat(result.getSentAt()).isEqualTo(testTime);
        assertThat(result.getIsSystemMessage()).isTrue();
    }

    @Test
    void createSystemMessage_ShouldCreateCorrectly() {
        // When
        ChatMessage result = ChatMessage.createSystemMessage("Game has started!");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Game has started!");
        assertThat(result.getSenderName()).isEqualTo("Sistema");
        assertThat(result.getIsSystemMessage()).isTrue();
        assertThat(result.getSentAt()).isNotNull();
        assertThat(result.getId()).isNull(); // Not set in factory method
    }

    @Test
    void createPlayerMessage_ShouldCreateCorrectly() {
        // When
        ChatMessage result = ChatMessage.createPlayerMessage("testuser", "Hello everyone!");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello everyone!");
        assertThat(result.getSenderName()).isEqualTo("testuser");
        assertThat(result.getIsSystemMessage()).isFalse();
        assertThat(result.getSentAt()).isNotNull();
        assertThat(result.getId()).isNull(); // Not set in factory method
    }

    @Test
    void mappingRoundTrip_ShouldPreserveBasicData() {
        // When
        ChatMessage mappedMessage = chatMessageMapper.toModel(chatMessageEntity);
        ChatMessageEntity mappedBackEntity = chatMessageMapper.toEntity(mappedMessage);

        // Then
        assertThat(mappedBackEntity.getId()).isEqualTo(chatMessageEntity.getId());
        assertThat(mappedBackEntity.getContent()).isEqualTo(chatMessageEntity.getContent());
        assertThat(mappedBackEntity.getSentAt()).isEqualTo(chatMessageEntity.getSentAt());
        assertThat(mappedBackEntity.getIsSystemMessage()).isEqualTo(chatMessageEntity.getIsSystemMessage());
        // Note: Sender relationship is not preserved in round trip
        // because toEntity doesn't map sender relationship
    }

    @Test
    void toModel_WithLongContent_ShouldMapCorrectly() {
        // Given
        String longContent = "This is a very long message that contains a lot of text to test " +
                "how the mapper handles longer content. It should map correctly without any issues.";
        chatMessageEntity.setContent(longContent);

        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(longContent);
    }

    @Test
    void toModel_WithEmptyContent_ShouldMapCorrectly() {
        // Given
        chatMessageEntity.setContent("");

        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("");
    }

    @Test
    void toModel_WithNullContent_ShouldMapNull() {
        // Given
        chatMessageEntity.setContent(null);

        // When
        ChatMessage result = chatMessageMapper.toModel(chatMessageEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNull();
    }
}
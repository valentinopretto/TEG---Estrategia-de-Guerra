package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageDto;
import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.entities.ChatMessageEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.mappers.ChatMessageMapper;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.ChatMessage;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.repository.ChatMessageRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private PlayerServiceImpl playerService;

    @Mock
    private GameService gameService;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatServiceIml chatService;

    private String gameCode;
    private ChatMessageDto messageDto;
    private Game game;
    private GameEntity gameEntity;
    private Player player;
    private PlayerEntity playerEntity;
    private User user;
    private UserEntity userEntity;
    private ChatMessage chatMessage;
    private ChatMessageEntity chatMessageEntity;
    private ChatMessageResponseDto responseDto;

    @BeforeEach
    void setUp() {
        gameCode = "GAME123";

        messageDto = new ChatMessageDto();
        messageDto.setSenderId(1L);
        messageDto.setContent("Test message");

        game = new Game();
        game.setId(1L);
        game.setGameCode(gameCode);

        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameCode(gameCode);
        gameEntity.setChatMessages(new ArrayList<>());

        user = new User();
        user.setId(1L);
        user.setEmail("as@gmail.com");
        user.setUsername("Test Player");
        user.setPasswordHash("password");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername(user.getUsername());
        userEntity.setEmail(user.getEmail());
        userEntity.setPasswordHash(user.getPasswordHash());

        player = new Player();
        player.setId(1L);
        player.setUsername("TestPlayer");

        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setUser(userEntity);
        playerEntity.getUser().setUsername("TestPlayer");

        chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setContent("Test message");

        chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setId(1L);
        chatMessageEntity.setContent("Test message");
        chatMessageEntity.setSender(playerEntity);
        chatMessageEntity.setGame(gameEntity);
        chatMessageEntity.setIsSystemMessage(false);
        chatMessageEntity.setSentAt(LocalDateTime.now());

        responseDto = new ChatMessageResponseDto();
        responseDto.setId(1L);
        responseDto.setContent("Test message");
        responseDto.setSenderName("TestPlayer");
        responseDto.setIsSystemMessage(false);
        responseDto.setSentAt(LocalDateTime.now());
    }

    @Test
    void sendMessage_Success() {
        // Given
        when(gameService.findByGameCode(gameCode)).thenReturn(game);
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerService.findById(1L)).thenReturn(Optional.of(player));
        when(playerMapper.toEntity(player)).thenReturn(playerEntity);
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class))).thenReturn(chatMessage);
        when(chatMessageMapper.toResponseDto(chatMessage)).thenReturn(responseDto);
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenReturn(chatMessageEntity);

        // When
        ChatMessageResponseDto result = chatService.sendMessage(gameCode, messageDto);

        // Then
        assertNotNull(result);
        assertEquals("Test message", result.getContent());
        verify(chatMessageRepository).save(any(ChatMessageEntity.class));
        verify(gameService).findByGameCode(gameCode);
        verify(playerService).findById(1L);
    }

    @Test
    void sendMessage_GameNotFound_ThrowsException() {
        // Given
        when(gameService.findByGameCode(gameCode)).thenReturn(game);
        when(gameMapper.toEntity(game)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.sendMessage(gameCode, messageDto)
        );
        assertEquals("Game not found with code: " + gameCode, exception.getMessage());
    }

    @Test
    void sendMessage_PlayerNotFound_ThrowsException() {
        // Given
        when(gameService.findByGameCode(gameCode)).thenReturn(game);
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerService.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.sendMessage(gameCode, messageDto)
        );
        assertEquals("Sender not found with id: " + messageDto.getSenderId(), exception.getMessage());
    }

    @Test
    void getNewMessages_FirstCall_ReturnsAllMessages() {
        // Given
        List<ChatMessageEntity> entities = Arrays.asList(chatMessageEntity);
        when(gameService.findByGameCode(gameCode)).thenReturn(game);
        when(chatMessageRepository.findByGameIdOrderBySentAtAsc(game.getId())).thenReturn(entities);
        when(chatMessageMapper.toModel(chatMessageEntity)).thenReturn(chatMessage);
        when(chatMessageMapper.toResponseDto(chatMessage)).thenReturn(responseDto);

        // When
        List<ChatMessageResponseDto> result = chatService.getNewMessages(gameCode, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getContent());
        verify(chatMessageRepository).findByGameIdOrderBySentAtAsc(game.getId());
    }

    @Test
    void getNewMessages_WithSinceId_ReturnsNewMessages() {
        // Given
        String sinceId = "5";
        List<ChatMessageEntity> entities = Arrays.asList(chatMessageEntity);
        when(gameService.findByGameCode(gameCode)).thenReturn(game);
        when(chatMessageRepository.findByGameIdAndIdGreaterThanOrderBySentAtAsc(game.getId(), 5L))
                .thenReturn(entities);
        when(chatMessageMapper.toModel(chatMessageEntity)).thenReturn(chatMessage);
        when(chatMessageMapper.toResponseDto(chatMessage)).thenReturn(responseDto);

        // When
        List<ChatMessageResponseDto> result = chatService.getNewMessages(gameCode, sinceId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chatMessageRepository).findByGameIdAndIdGreaterThanOrderBySentAtAsc(game.getId(), 5L);
    }

    @Test
    void getNewMessages_GameNotFound_ThrowsException() {
        // Given
        when(gameService.findByGameCode(gameCode)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.getNewMessages(gameCode, null)
        );
        assertEquals("Game not found with code: " + gameCode, exception.getMessage());
    }

    @Test
    void getNewMessages_EmptyResult_ReturnsEmptyList() {
        // Given
        when(gameService.findByGameCode(gameCode)).thenReturn(game);
        when(chatMessageRepository.findByGameIdOrderBySentAtAsc(game.getId()))
                .thenReturn(new ArrayList<>());

        // When
        List<ChatMessageResponseDto> result = chatService.getNewMessages(gameCode, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}


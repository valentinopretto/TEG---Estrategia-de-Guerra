package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import ar.edu.utn.frc.tup.piii.utils.CodeGenerator;
import ar.edu.utn.frc.tup.piii.utils.ColorManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private BotProfileRepository botProfileRepository;
    @Mock
    private GameMapper gameMapper;
    @Mock
    private CodeGenerator codeGenerator;
    @Mock
    private ColorManager colorManager;
    @Mock
    private GameInitializationServiceImpl gameInitializationService;
    @Mock
    private UserService userService;

    @InjectMocks
    private GameServiceImpl gameService;

    private GameEntity gameEntity;
    private Game game;
    private UserEntity userEntity;
    private PlayerEntity playerEntity;

    @BeforeEach
    void setUp() {
        // Setup GameEntity
        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameCode("TEST123");
        gameEntity.setStatus(GameState.WAITING_FOR_PLAYERS);
        gameEntity.setMaxPlayers(6);
        gameEntity.setTurnTimeLimit(120);
        gameEntity.setChatEnabled(true);
        gameEntity.setPactsAllowed(false);
        gameEntity.setCreatedAt(LocalDateTime.now());

        // Setup Game
        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.WAITING_FOR_PLAYERS)
                .maxPlayers(6)
                .players(new ArrayList<>())
                .build();

        // Setup UserEntity
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setEmail("test@example.com");

        // Setup PlayerEntity
        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setUser(userEntity);
        playerEntity.setGame(gameEntity);
        playerEntity.setColor(PlayerColor.RED);
        playerEntity.setStatus(PlayerStatus.WAITING);
        playerEntity.setSeatOrder(0);

        gameEntity.setCreatedBy(userEntity);
        gameEntity.setPlayers(new ArrayList<>());
    }

    @Test
    void findById_WhenGameExists_ShouldReturnGame() {
        // Given
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When
        Game result = gameService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(gameRepository).findById(1L);
    }

    @Test
    void findById_WhenGameNotExists_ShouldThrowException() {
        // Given
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.findById(999L))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game not found with id: 999");
    }

    @Test
    void findByIdOptional_WhenGameExists_ShouldReturnOptionalWithGame() {
        // Given
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When
        Optional<Game> result = gameService.findByIdOptional(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findByIdOptional_WhenGameNotExists_ShouldReturnEmpty() {
        // Given
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Game> result = gameService.findByIdOptional(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByGameCode_WhenGameExists_ShouldReturnGame() {
        // Given
        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When
        Game result = gameService.findByGameCode("TEST123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGameCode()).isEqualTo("TEST123");
    }

    @Test
    void findByGameCode_WhenGameNotExists_ShouldThrowException() {
        // Given
        when(gameRepository.findByGameCode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.findByGameCode("INVALID"))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game not found with code: INVALID");
    }

    @Test
    void save_ShouldReturnSavedGame() {
        // Given
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When
        Game result = gameService.save(game);

        // Then
        assertThat(result).isNotNull();
        verify(gameRepository).save(gameEntity);
    }

    @Test
    void existsById_WhenGameExists_ShouldReturnTrue() {
        // Given
        when(gameRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = gameService.existsById(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsById_WhenGameNotExists_ShouldReturnFalse() {
        // Given
        when(gameRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = gameService.existsById(999L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void createLobbyWithDefaults_ShouldCreateGameSuccessfully() {
        // Given
        Long hostUserId = 1L;
        when(userService.getUserById(hostUserId)).thenReturn(null); // void method
        when(userRepository.findById(hostUserId)).thenReturn(Optional.of(userEntity));
        when(codeGenerator.generateUniqueCode()).thenReturn("UNIQUE123");
        when(gameRepository.existsByGameCode("UNIQUE123")).thenReturn(false);
        when(gameRepository.save(any(GameEntity.class))).thenReturn(gameEntity);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(playerEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When
        Game result = gameService.createLobbyWithDefaults(hostUserId);

        // Then
        assertThat(result).isNotNull();
        verify(gameRepository).save(any(GameEntity.class));
        verify(playerRepository).save(any(PlayerEntity.class));
    }

    @Test
    void createLobbyWithDefaults_WhenUserNotFound_ShouldThrowException() {
        // Given
        Long hostUserId = 999L;
        when(userRepository.findById(hostUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.createLobbyWithDefaults(hostUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuario no encontrado con id: 999");
    }

    @Test
    void getGameByCode_WhenGameExists_ShouldReturnResponseDto() {
        // Given
        GameResponseDto responseDto = new GameResponseDto();
        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toResponseDto(gameEntity)).thenReturn(responseDto);

        // When
        GameResponseDto result = gameService.getGameByCode("TEST123");

        // Then
        assertThat(result).isNotNull();
        verify(gameRepository).findByGameCode("TEST123");
    }

    @Test
    void getGameByCode_WhenGameNotExists_ShouldThrowException() {
        // Given
        when(gameRepository.findByGameCode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.getGameByCode("INVALID"))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game not found with code: INVALID");
    }

    @Test
    void findGamesByHost_ShouldReturnGamesList() {
        // Given
        Long userId = 1L;
        List<GameEntity> gameEntities = Arrays.asList(gameEntity);
        List<Game> games = Arrays.asList(game);

        when(gameRepository.findByCreatedByIdOrderByCreatedAtDesc(userId)).thenReturn(gameEntities);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When
        List<Game> result = gameService.findGamesByHost(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(game);
    }

    @Test
    void joinGame_WhenValidRequest_ShouldAddPlayerToGame() {
        // Given
        JoinGameDto joinDto = new JoinGameDto();
        joinDto.setGameCode("TEST123");
        joinDto.setUserId(2L);

        UserEntity newUser = new UserEntity();
        newUser.setId(2L);
        newUser.setUsername("newUser");

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(userService.getUserById(2L)).thenReturn(null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(colorManager.getAvailableRandomColor(gameEntity)).thenReturn(PlayerColor.BLUE);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(new PlayerEntity());
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        // When
        Game result = gameService.joinGame(joinDto);

        // Then
        assertThat(result).isNotNull();
        verify(playerRepository).save(any(PlayerEntity.class));
    }

    @Test
    void joinGame_WhenGameNotFound_ShouldThrowException() {
        // Given
        JoinGameDto joinDto = new JoinGameDto();
        joinDto.setGameCode("INVALID");
        joinDto.setUserId(2L);

        when(gameRepository.findByGameCode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.joinGame(joinDto))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game not found with code: INVALID");
    }

    @Test
    void joinGame_WhenGameIsFull_ShouldThrowException() {
        JoinGameDto joinDto = new JoinGameDto();
        joinDto.setGameCode("TEST123");
        joinDto.setUserId(2L);

        gameEntity.setMaxPlayers(1);
        List<PlayerEntity> fullPlayersList = new ArrayList<>();
        fullPlayersList.add(playerEntity);
        gameEntity.setPlayers(fullPlayersList);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.joinGame(joinDto))
                .isInstanceOf(GameFullException.class)
                .hasMessage("Game is full. Max players: 1");
    }

    @Test
    void addBotsToGame_WhenValidRequest_ShouldAddBots() {
        AddBotsDto addBotsDto = new AddBotsDto();
        addBotsDto.setGameCode("TEST123");
        addBotsDto.setNumberOfBots(2);
        addBotsDto.setBotLevel(BotLevel.BALANCED);
        addBotsDto.setBotStrategy(BotStrategy.AGGRESSIVE);

        BotProfileEntity botProfile = new BotProfileEntity();
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);

        gameEntity.setPlayers(new ArrayList<>());
        gameEntity.setMaxPlayers(6);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(botProfileRepository.findByLevelAndStrategy(BotLevel.BALANCED, BotStrategy.AGGRESSIVE))
                .thenReturn(Optional.of(botProfile));
        when(gameRepository.findById(gameEntity.getId())).thenReturn(Optional.of(gameEntity));
        when(colorManager.getAvailableRandomColor(any())).thenReturn(PlayerColor.BLUE, PlayerColor.GREEN);
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> {
            PlayerEntity saved = invocation.getArgument(0);
            saved.setId(System.currentTimeMillis());
            return saved;
        });
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.addBotsToGame(addBotsDto);

        assertThat(result).isNotNull();
        verify(playerRepository, atLeast(2)).save(any(PlayerEntity.class));
    }

    @Test
    void addBotsToGame_WhenBotProfileNotFound_ShouldThrowException() {
        AddBotsDto addBotsDto = new AddBotsDto();
        addBotsDto.setGameCode("TEST123");
        addBotsDto.setNumberOfBots(1);
        addBotsDto.setBotLevel(BotLevel.EXPERT);
        addBotsDto.setBotStrategy(BotStrategy.DEFENSIVE);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(botProfileRepository.findByLevelAndStrategy(BotLevel.EXPERT, BotStrategy.DEFENSIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.addBotsToGame(addBotsDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No bot profile found");
    }

    @Test
    void addBotsToGame_WhenTooManyBots_ShouldThrowException() {
        AddBotsDto addBotsDto = new AddBotsDto();
        addBotsDto.setGameCode("TEST123");
        addBotsDto.setNumberOfBots(5);
        addBotsDto.setBotLevel(BotLevel.BALANCED);
        addBotsDto.setBotStrategy(BotStrategy.AGGRESSIVE);

        gameEntity.setMaxPlayers(3);
        List<PlayerEntity> existingPlayers = new ArrayList<>();
        existingPlayers.add(playerEntity);
        gameEntity.setPlayers(existingPlayers);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.addBotsToGame(addBotsDto))
                .isInstanceOf(GameFullException.class)
                .hasMessageContaining("Not enough space");
    }

    @Test
    void addBotsToGame_WhenGameNotInWaitingState_ShouldThrowException() {
        AddBotsDto addBotsDto = new AddBotsDto();
        addBotsDto.setGameCode("TEST123");
        addBotsDto.setNumberOfBots(1);
        addBotsDto.setBotLevel(BotLevel.BALANCED);
        addBotsDto.setBotStrategy(BotStrategy.AGGRESSIVE);

        gameEntity.setStatus(GameState.NORMAL_PLAY);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.addBotsToGame(addBotsDto))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("Cannot add bots. Game state: NORMAL_PLAY");
    }

    @Test
    void startGame_WhenValidGame_ShouldInitializeGame() {
        String gameCode = "TEST123";
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        doNothing().when(gameInitializationService).initializeGame(gameEntity);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.startGame(gameCode);

        assertThat(result).isNotNull();
        verify(gameInitializationService).initializeGame(gameEntity);
        verify(gameRepository).save(gameEntity);
    }

    @Test
    void startGameByHost_WhenValidHost_ShouldStartGame() {
        String gameCode = "TEST123";
        Long hostUserId = 1L;

        game.setCreatedByUserId(hostUserId);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);
        doNothing().when(gameInitializationService).initializeGame(gameEntity);
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);

        Game result = gameService.startGameByHost(gameCode, hostUserId);

        assertThat(result).isNotNull();
        verify(gameInitializationService).initializeGame(gameEntity);
    }

    @Test
    void startGameByHost_WhenNotHost_ShouldThrowException() {
        String gameCode = "TEST123";
        Long notHostUserId = 999L;

        game.setCreatedByUserId(1L);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        assertThatThrownBy(() -> gameService.startGameByHost(gameCode, notHostUserId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Solo el anfitrión puede iniciar la partida");
    }

    @Test
    void updateGameSettings_WhenValidRequest_ShouldUpdateSettings() {
        String gameCode = "TEST123";
        UpdateGameSettingsDto updateDto = new UpdateGameSettingsDto();
        updateDto.setRequesterId(1L);
        updateDto.setMaxPlayers(4);
        updateDto.setTurnTimeLimit(180);
        updateDto.setChatEnabled(false);

        gameEntity.setCreatedBy(userEntity);

        when(gameRepository.findForSettings(gameCode)).thenReturn(Optional.of(gameEntity));
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.updateGameSettings(gameCode, updateDto);

        assertThat(result).isNotNull();
        assertThat(gameEntity.getMaxPlayers()).isEqualTo(4);
        assertThat(gameEntity.getTurnTimeLimit()).isEqualTo(180);
        assertThat(gameEntity.getChatEnabled()).isFalse();
        verify(gameRepository).save(gameEntity);
    }

    @Test
    void updateGameSettings_WhenNotHost_ShouldThrowException() {
        String gameCode = "TEST123";
        UpdateGameSettingsDto updateDto = new UpdateGameSettingsDto();
        updateDto.setRequesterId(999L);
        updateDto.setMaxPlayers(4);

        gameEntity.setCreatedBy(userEntity);

        when(gameRepository.findForSettings(gameCode)).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.updateGameSettings(gameCode, updateDto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the host can modify game settings");
    }

    @Test
    void updateGameSettings_WhenGameAlreadyStarted_ShouldThrowException() {
        String gameCode = "TEST123";
        UpdateGameSettingsDto updateDto = new UpdateGameSettingsDto();
        updateDto.setRequesterId(1L);
        updateDto.setMaxPlayers(4);

        gameEntity.setStatus(GameState.NORMAL_PLAY);
        gameEntity.setCreatedBy(userEntity);

        when(gameRepository.findForSettings(gameCode)).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.updateGameSettings(gameCode, updateDto))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("Cannot modify settings once game has started");
    }

    @Test
    void updateGameSettings_WhenMaxPlayersTooLow_ShouldThrowException() {
        String gameCode = "TEST123";
        UpdateGameSettingsDto updateDto = new UpdateGameSettingsDto();
        updateDto.setRequesterId(1L);
        updateDto.setMaxPlayers(1);

        gameEntity.setCreatedBy(userEntity);

        when(gameRepository.findForSettings(gameCode)).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.updateGameSettings(gameCode, updateDto))
                .isInstanceOf(InvalidGameConfigurationException.class)
                .hasMessage("Max players must be between 2 and 6");
    }

    @Test
    void updateGameSettings_WhenMaxPlayersBelowCurrentCount_ShouldThrowException() {
        String gameCode = "TEST123";
        UpdateGameSettingsDto updateDto = new UpdateGameSettingsDto();
        updateDto.setRequesterId(1L);
        updateDto.setMaxPlayers(1);

        gameEntity.setCreatedBy(userEntity);
        List<PlayerEntity> players = Arrays.asList(playerEntity, new PlayerEntity());
        gameEntity.setPlayers(players);

        when(gameRepository.findForSettings(gameCode)).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.updateGameSettings(gameCode, updateDto))
                .isInstanceOf(InvalidGameConfigurationException.class)
                .hasMessageContaining("Cannot set max players below current player count");
    }

    @Test
    void kickPlayer_WhenValidRequest_ShouldRemovePlayer() {
        KickPlayerDto kickDto = new KickPlayerDto();
        kickDto.setGameCode("TEST123");
        kickDto.setPlayerId(2L);

        PlayerEntity playerToKick = new PlayerEntity();
        playerToKick.setId(2L);
        UserEntity playerUser = new UserEntity();
        playerUser.setId(2L);
        playerToKick.setUser(playerUser);
        playerToKick.setGame(gameEntity);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(playerToKick));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.kickPlayer(kickDto);

        assertThat(result).isNotNull();
        assertThat(playerToKick.getStatus()).isEqualTo(PlayerStatus.ELIMINATED);
        verify(playerRepository).save(playerToKick);
    }

    @Test
    void kickPlayer_WhenTryingToKickHost_ShouldThrowException() {
        KickPlayerDto kickDto = new KickPlayerDto();
        kickDto.setGameCode("TEST123");
        kickDto.setPlayerId(1L);

        playerEntity.setUser(userEntity);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));

        assertThatThrownBy(() -> gameService.kickPlayer(kickDto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot kick the host of the game");
    }

    @Test
    void kickPlayer_WhenGameNotInLobby_ShouldThrowException() {
        KickPlayerDto kickDto = new KickPlayerDto();
        kickDto.setGameCode("TEST123");
        kickDto.setPlayerId(2L);

        gameEntity.setStatus(GameState.NORMAL_PLAY);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.kickPlayer(kickDto))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessageContaining("Cannot kick player. Game is not in WAITING_FOR_PLAYERS state");
    }

    @Test
    void kickPlayer_WhenPlayerNotFound_ShouldThrowException() {
        KickPlayerDto kickDto = new KickPlayerDto();
        kickDto.setGameCode("TEST123");
        kickDto.setPlayerId(999L);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.kickPlayer(kickDto))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void kickPlayer_WhenPlayerNotInGame_ShouldThrowException() {
        KickPlayerDto kickDto = new KickPlayerDto();
        kickDto.setGameCode("TEST123");
        kickDto.setPlayerId(2L);

        PlayerEntity otherPlayer = new PlayerEntity();
        otherPlayer.setId(2L);
        GameEntity otherGame = new GameEntity();
        otherGame.setGameCode("OTHER123");
        otherPlayer.setGame(otherGame);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(otherPlayer));

        assertThatThrownBy(() -> gameService.kickPlayer(kickDto))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("does not belong to game");
    }

    @Test
    void leaveGame_WhenValidRequest_ShouldEliminatePlayer() {
        LeaveGameDto leaveDto = new LeaveGameDto();
        leaveDto.setGameCode("TEST123");
        leaveDto.setUserId(2L);

        PlayerEntity leavingPlayer = new PlayerEntity();
        leavingPlayer.setId(2L);
        UserEntity playerUser = new UserEntity();
        playerUser.setId(2L);
        leavingPlayer.setUser(playerUser);
        gameEntity.getPlayers().add(leavingPlayer);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.leaveGame(leaveDto);

        assertThat(result).isNotNull();
        assertThat(leavingPlayer.getStatus()).isEqualTo(PlayerStatus.ELIMINATED);
        verify(playerRepository).save(leavingPlayer);
    }

    @Test
    void leaveGame_WhenHostTriesToLeave_ShouldThrowException() {
        LeaveGameDto leaveDto = new LeaveGameDto();
        leaveDto.setGameCode("TEST123");
        leaveDto.setUserId(1L);

        gameEntity.getPlayers().add(playerEntity);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.leaveGame(leaveDto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Host cannot leave the game.");
    }

    @Test
    void leaveGame_WhenGameAlreadyStarted_ShouldThrowException() {
        LeaveGameDto leaveDto = new LeaveGameDto();
        leaveDto.setGameCode("TEST123");
        leaveDto.setUserId(2L);

        gameEntity.setStatus(GameState.NORMAL_PLAY);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.leaveGame(leaveDto))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("Cannot leave. Game already started.");
    }

    @Test
    void leaveGame_WhenPlayerNotFound_ShouldThrowException() {
        LeaveGameDto leaveDto = new LeaveGameDto();
        leaveDto.setGameCode("TEST123");
        leaveDto.setUserId(999L);

        when(gameRepository.findByGameCode("TEST123")).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.leaveGame(leaveDto))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("Player not found in game.");
    }

    @Test
    void cancelGameByUsername_WhenValidHost_ShouldDeleteGame() {
        String gameCode = "TEST123";
        String hostUsername = "testUser";

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));

        gameService.cancelGameByUsername(gameCode, hostUsername);

        verify(gameRepository).delete(gameEntity);
    }

    @Test
    void cancelGameByUsername_WhenNotHost_ShouldThrowException() {
        String gameCode = "TEST123";
        String notHostUsername = "otherUser";

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.cancelGameByUsername(gameCode, notHostUsername))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Solo el host puede cancelar la partida");
    }

    @Test
    void cancelGameByUsername_WhenGameNotFound_ShouldThrowException() {
        String gameCode = "INVALID";
        String username = "testUser";

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.cancelGameByUsername(gameCode, username))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void prepareInitialPlacementPhase_ShouldThrowUnsupportedOperationException() {
        String gameCode = "TEST123";
        Long playerId = 1L;
        Map<Long, Integer> armiesByCountry = new HashMap<>();

        assertThatThrownBy(() -> gameService.prepareInitialPlacementPhase(gameCode, playerId, armiesByCountry))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Use InitialPlacementController.placeInitialArmies()");
    }

    @Test
    void joinGameLobby_WhenValidRequest_ShouldChangePlayerStatus() {
        String gameCode = "TEST123";
        Long playerId = 1L;

        gameEntity.setStatus(GameState.PAUSED);
        playerEntity.setStatus(PlayerStatus.DISCONNECTED);
        gameEntity.getPlayers().add(playerEntity);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(playerRepository.save(playerEntity)).thenReturn(playerEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.joinGameLobby(gameCode, playerId);

        assertThat(result).isNotNull();
        assertThat(playerEntity.getStatus()).isEqualTo(PlayerStatus.WAITING);
        verify(playerRepository).save(playerEntity);
    }

    @Test
    void joinGameLobby_WhenGameNotPaused_ShouldThrowException() {
        String gameCode = "TEST123";
        Long playerId = 1L;

        gameEntity.setStatus(GameState.NORMAL_PLAY);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.joinGameLobby(gameCode, playerId))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("Game is not in PAUSED state. Current: NORMAL_PLAY");
    }

    @Test
    void joinGameLobby_WhenPlayerNotDisconnected_ShouldThrowException() {
        String gameCode = "TEST123";
        Long playerId = 1L;

        gameEntity.setStatus(GameState.PAUSED);
        playerEntity.setStatus(PlayerStatus.ACTIVE);
        gameEntity.getPlayers().add(playerEntity);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));

        assertThatThrownBy(() -> gameService.joinGameLobby(gameCode, playerId))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessage("Player is not in DISCONNECTED state. Current: ACTIVE");
    }

    @Test
    void togglePlayerReady_WhenValidRequest_ShouldToggleStatus() {
        String gameCode = "TEST123";
        Long playerId = 1L;

        playerEntity.setStatus(PlayerStatus.DISCONNECTED);
        gameEntity.getPlayers().add(playerEntity);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(playerRepository.save(playerEntity)).thenReturn(playerEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.togglePlayerReady(gameCode, playerId);

        assertThat(result).isNotNull();
        assertThat(playerEntity.getStatus()).isEqualTo(PlayerStatus.WAITING);
        verify(playerRepository).save(playerEntity);
    }

    @Test
    void getGameLobbyStatus_WhenValidRequest_ShouldReturnGame() {
        String gameCode = "TEST123";
        Long playerId = 1L;

        gameEntity.getPlayers().add(playerEntity);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.getGameLobbyStatus(gameCode, playerId);

        assertThat(result).isNotNull();
    }

    @Test
    void resumeGame_WhenValidRequest_ShouldChangeGameState() {
        String gameCode = "TEST123";
        gameEntity.setStatus(GameState.WAITING_FOR_PLAYERS);

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(gameRepository.save(gameEntity)).thenReturn(gameEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.resumeGame(gameCode);

        assertThat(result).isNotNull();
        assertThat(gameEntity.getStatus()).isEqualTo(GameState.NORMAL_PLAY);
        verify(gameRepository).save(gameEntity);
    }


    @Test
    void createLobbyWithDefaults_WhenCodeAlreadyExists_ShouldGenerateNewCode() {
        Long hostUserId = 1L;
        when(userService.getUserById(hostUserId)).thenReturn(null);
        when(userRepository.findById(hostUserId)).thenReturn(Optional.of(userEntity));
        when(codeGenerator.generateUniqueCode()).thenReturn("USED123", "UNIQUE123");
        when(gameRepository.existsByGameCode("USED123")).thenReturn(true);
        when(gameRepository.existsByGameCode("UNIQUE123")).thenReturn(false);
        when(gameRepository.save(any(GameEntity.class))).thenReturn(gameEntity);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(playerEntity);
        when(gameMapper.toModel(gameEntity)).thenReturn(game);

        Game result = gameService.createLobbyWithDefaults(hostUserId);

        assertThat(result).isNotNull();
        verify(codeGenerator, times(2)).generateUniqueCode();
        verify(gameRepository).existsByGameCode("USED123");
        verify(gameRepository).existsByGameCode("UNIQUE123");
    }


}
package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import ar.edu.utn.frc.tup.piii.utils.CodeGenerator;
import ar.edu.utn.frc.tup.piii.utils.ColorManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private BotProfileRepository botProfileRepository;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private CodeGenerator codeGenerator;
    @Autowired
    private ColorManager colorManager;

    @Autowired
    private GameInitializationServiceImpl gameInitializationService;

    // Nota: No inyectamos InitialPlacementService aca para evitar dependencia CIRCULAR
    // El InitialPlacementService se maneja a traves del InitialPlacementController

    @Autowired
    private UserService userService;
    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Override
    public Game findById(Long gameId) {
        return gameRepository.findById(gameId)
                .map(gameMapper::toModel)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));
    }

    @Override
    public Optional<Game> findByIdOptional(Long gameId) {
        return gameRepository.findById(gameId)
                .map(gameMapper::toModel);
    }

    @Override
    public Game findByGameCode(String gameCode) {
        return gameRepository.findByGameCode(gameCode)
                .map(gameMapper::toModel)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));
    }

    @Override
    public Game save(Game game) {
        GameEntity entity = gameMapper.toEntity(game);
        GameEntity savedEntity = gameRepository.save(entity);
        return gameMapper.toModel(savedEntity);
    }

    @Override
    public boolean existsById(Long gameId) {
        return gameRepository.existsById(gameId);
    }

    @Override
    @Transactional
    public Game createLobbyWithDefaults(Long hostUserId) {
        // usar UserService en lugar del repositorio directamente
        userService.getUserById(hostUserId);
        UserEntity host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + hostUserId));

        String gameCode = generateUniqueGameCode();

        GameEntity gameEntity = createGameEntity(gameCode, host);
        GameEntity savedGame = gameRepository.save(gameEntity);

        PlayerEntity hostPlayer = createHostPlayer(host, savedGame);
        playerRepository.save(hostPlayer);

        return gameMapper.toModel(savedGame);
    }

    @Override
    @Transactional(readOnly = true)
    public GameResponseDto getGameByCode(String gameCode) {
        GameEntity gameEntity = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));
        return gameMapper.toResponseDto(gameEntity);
    }

    @Override
    public List<Game> findGamesByHost(Long userId) {
        try {
            // Buscar por el campo createdByUserId en GameEntity
            List<GameEntity> gameEntities = gameRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);

            return gameEntities.stream()
                    .map(gameMapper::toModel)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving hosted games", e);
        }
    }

    @Override
    @Transactional
    public Game joinGame(JoinGameDto dto) {
        GameEntity gameEntity = findGameEntityByCode(dto.getGameCode());

        if (gameEntity.getStatus() == GameState.WAITING_FOR_PLAYERS) {
            validateGameCanAcceptPlayers(gameEntity);

            userService.getUserById(dto.getUserId());
            UserEntity user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Optional<PlayerEntity> existingPlayer = findExistingPlayer(gameEntity, dto.getUserId());
            if (existingPlayer.isPresent()) {
                return handleExistingPlayer(existingPlayer.get(), gameEntity);
            }

            validateGameCapacity(gameEntity);

            PlayerEntity newPlayer = createNewPlayer(user, gameEntity);
            playerRepository.save(newPlayer);
            gameEntity.getPlayers().add(newPlayer);
            return gameMapper.toModel(gameEntity);

        } else if (gameEntity.getStatus() == GameState.PAUSED) {
            Optional<PlayerEntity> existingPlayer = findExistingPlayer(gameEntity, dto.getUserId());
            if (existingPlayer.isPresent() && existingPlayer.get().getStatus() == PlayerStatus.DISCONNECTED) {
                existingPlayer.get().setStatus(PlayerStatus.WAITING);
                playerRepository.save(existingPlayer.get());
                return gameMapper.toModel(gameEntity);
            } else {
                throw new InvalidGameStateException("Only disconnected players can rejoin a paused game.");
            }
        } else {
            throw new InvalidGameStateException("Game is not accepting new players. Current state: " + gameEntity.getStatus());
        }
    }

    @Override
    @Transactional
    public void cancelGameByUsername(String gameCode, String requesterUsername) {
        GameEntity game = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found: " + gameCode));

        String hostUsername = game.getCreatedBy().getUsername();
        if (!hostUsername.equals(requesterUsername)) {
            throw new ForbiddenException("Solo el host puede cancelar la partida");
        }

        gameRepository.delete(game);

    }


    @Override
    @Transactional
    public Game addBotsToGame(AddBotsDto dto) {
        GameEntity gameEntity = findGameEntityByCode(dto.getGameCode());

        validateGameCanAcceptBots(gameEntity);
        validateBotRequest(dto, gameEntity);

        BotProfileEntity botProfile = findBotProfile(dto.getBotLevel(), dto.getBotStrategy());
        addBotsToGame(gameEntity, dto.getNumberOfBots(), botProfile);
        return gameMapper.toModel(gameEntity);
    }

    @Override
    @Transactional
    public Game startGame(String gameCode) {
        GameEntity gameEntity = findGameEntityByCode(gameCode);
        gameInitializationService.initializeGame(gameEntity);
        GameEntity savedGame = gameRepository.save(gameEntity);
        return gameMapper.toModel(savedGame);
    }

    @Override
    @Transactional
    public Game startGameByHost(String gameCode, Long hostUserId) {
        Game existing = findByGameCode(gameCode);

        if (!Objects.equals(existing.getCreatedByUserId(), hostUserId)) {
            throw new ForbiddenException("Solo el anfitrión puede iniciar la partida");
        }

        return startGame(gameCode);
    }

    @Override
    @Transactional
    public Game updateGameSettings(String gameCode, UpdateGameSettingsDto dto) {
        GameEntity gameEntity = gameRepository.findForSettings(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));

        validateSettingsUpdate(gameEntity, dto);

        updateGameEntitySettings(gameEntity, dto);

        GameEntity savedGame = gameRepository.save(gameEntity);
        return gameMapper.toModel(savedGame);
    }

    @Override
    @Transactional
    public Game kickPlayer(KickPlayerDto dto) {
        GameEntity gameEntity = findGameEntityByCode(dto.getGameCode());

        validateKickAction(gameEntity);

        PlayerEntity playerToKick = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
        validatePlayerBelongsToGame(playerToKick, gameEntity);
        validateCanKickPlayer(playerToKick, gameEntity);

        removeOrEliminatePlayer(playerToKick, gameEntity);

        return gameMapper.toModel(gameEntity);
    }

    @Override
    @Transactional
    public Game leaveGame(LeaveGameDto dto) {
        GameEntity gameEntity = findGameEntityByCode(dto.getGameCode());

        validateGameInLobby(gameEntity);

        PlayerEntity player = findPlayerInGame(gameEntity, dto.getUserId());
        validateCanLeaveGame(player, gameEntity);
        eliminatePlayer(player);
        return gameMapper.toModel(gameEntity);
    }

    //este esta para que compile por si alguien lo llama, pero que le avise que no lo tiene que usar por lom de la
    //dependencia ciruclar
    @Override
    @Transactional
    public void prepareInitialPlacementPhase(String gameCode, Long playerId, Map<Long, Integer> armiesByCountry) {
        throw new UnsupportedOperationException(
                "Use InitialPlacementController.placeInitialArmies() instead to avoid circular dependencies"
        );
    }


    private String generateUniqueGameCode() {
        String gameCode;
        do {
            gameCode = codeGenerator.generateUniqueCode();
        } while (gameRepository.existsByGameCode(gameCode));
        return gameCode;
    }

    private GameEntity createGameEntity(String gameCode, UserEntity host) {
        GameEntity gameEntity = new GameEntity();
        gameEntity.setGameCode(gameCode);
        gameEntity.setCreatedBy(host);
        gameEntity.setStatus(GameState.WAITING_FOR_PLAYERS);
        gameEntity.setMaxPlayers(6);
        gameEntity.setTurnTimeLimit(120);
        gameEntity.setChatEnabled(true);
        gameEntity.setPactsAllowed(false);
        gameEntity.setCurrentPhase(TurnPhase.REINFORCEMENT);
        return gameEntity;
    }

    private PlayerEntity createHostPlayer(UserEntity host, GameEntity gameEntity) {
        PlayerEntity hostPlayer = new PlayerEntity();
        hostPlayer.setUser(host);
        hostPlayer.setGame(gameEntity);
        hostPlayer.setColor(PlayerColor.RED);
        hostPlayer.setStatus(PlayerStatus.WAITING);
        hostPlayer.setSeatOrder(0);
        return hostPlayer;
    }

    private GameEntity findGameEntityByCode(String gameCode) {
        return gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));
    }

    private PlayerEntity findPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));
    }

    //choclo de valicaciones

    private void validateGameCanAcceptPlayers(GameEntity gameEntity) {
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException(
                    "Game is not accepting new players. Current state: " + gameEntity.getStatus());
        }
    }

    private void validateGameCanAcceptBots(GameEntity gameEntity) {
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot add bots. Game state: " + gameEntity.getStatus());
        }
    }

    private void validateGameCapacity(GameEntity gameEntity) {
        long activePlayers = getActivePlayerCount(gameEntity);
        if (activePlayers >= gameEntity.getMaxPlayers()) {
            throw new GameFullException("Game is full. Max players: " + gameEntity.getMaxPlayers());
        }
    }

    private void validateBotRequest(AddBotsDto dto, GameEntity gameEntity) {
        long activePlayers = getActivePlayerCount(gameEntity);
        if (activePlayers + dto.getNumberOfBots() > gameEntity.getMaxPlayers()) {
            throw new GameFullException("Not enough space. Current active: " + activePlayers +
                    ", Requested bots: " + dto.getNumberOfBots() + ", Max: " + gameEntity.getMaxPlayers());
        }
    }

    private void validateSettingsUpdate(GameEntity gameEntity, UpdateGameSettingsDto dto) {
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot modify settings once game has started");
        }

        if (!gameEntity.getCreatedBy().getId().equals(dto.getRequesterId())) {
            throw new ForbiddenException("Only the host can modify game settings");
        }

        if (dto.getMaxPlayers() != null) {
            validateMaxPlayersUpdate(gameEntity, dto.getMaxPlayers());
        }
    }

    private void validateMaxPlayersUpdate(GameEntity gameEntity, Integer newMaxPlayers) {
        if (newMaxPlayers < 2 || newMaxPlayers > 6) {
            throw new InvalidGameConfigurationException("Max players must be between 2 and 6");
        }

        int currentPlayerCount = gameEntity.getPlayers().size();
        if (newMaxPlayers < currentPlayerCount) {
            throw new InvalidGameConfigurationException(
                    "Cannot set max players below current player count: " + currentPlayerCount);
        }
    }

    private void validateKickAction(GameEntity gameEntity) {
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException(
                    "Cannot kick player. Game is not in WAITING_FOR_PLAYERS state. Current: " + gameEntity.getStatus());
        }
    }

    private void validatePlayerBelongsToGame(PlayerEntity player, GameEntity gameEntity) {
        if (!player.getGame().getGameCode().equals(gameEntity.getGameCode())) {
            throw new PlayerNotFoundException(
                    "Player id " + player.getId() + " does not belong to game " + gameEntity.getGameCode());
        }
    }

    private void validateCanKickPlayer(PlayerEntity player, GameEntity gameEntity) {
        Long hostId = gameEntity.getCreatedBy().getId();
        Long userIdOfPlayer = player.getUser() != null ? player.getUser().getId() : null;

        if (hostId.equals(userIdOfPlayer)) {
            throw new ForbiddenException("Cannot kick the host of the game");
        }
    }

    private void validateGameInLobby(GameEntity gameEntity) {
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot leave. Game already started.");
        }
    }

    private void validateCanLeaveGame(PlayerEntity player, GameEntity gameEntity) {
        if (gameEntity.getCreatedBy().getId().equals(player.getUser().getId())) {
            throw new ForbiddenException("Host cannot leave the game.");
        }
    }

    private Optional<PlayerEntity> findExistingPlayer(GameEntity gameEntity, Long userId) {
        return gameEntity.getPlayers().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .findFirst();
    }

    private Game handleExistingPlayer(PlayerEntity existingPlayer, GameEntity gameEntity) {
        if (existingPlayer.getStatus() == PlayerStatus.ELIMINATED) {
            existingPlayer.setStatus(PlayerStatus.WAITING);
            existingPlayer.setEliminatedAt(null);
            playerRepository.save(existingPlayer);
            return gameMapper.toModel(gameEntity);
        } else {
            throw new InvalidGameStateException("User is already in this game.");
        }
    }

    private PlayerEntity createNewPlayer(UserEntity user, GameEntity gameEntity) {
        PlayerColor availableColor = colorManager.getAvailableRandomColor(gameEntity);
        if (availableColor == null) {
            throw new ColorNotAvailableException("No colors available");
        }

        int nextSeatOrder = gameEntity.getPlayers().stream()
                .mapToInt(PlayerEntity::getSeatOrder)
                .max()
                .orElse(-1) + 1;

        PlayerEntity newPlayer = new PlayerEntity();
        newPlayer.setUser(user);
        newPlayer.setGame(gameEntity);
        newPlayer.setColor(availableColor);
        newPlayer.setStatus(PlayerStatus.WAITING);
        newPlayer.setSeatOrder(nextSeatOrder);

        return newPlayer;
    }

    private BotProfileEntity findBotProfile(BotLevel level, BotStrategy strategy) {
        return botProfileRepository.findByLevelAndStrategy(level, strategy)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No bot profile found for level=" + level + " and strategy=" + strategy));
    }

    private void addBotsToGame(GameEntity gameEntity, int numberOfBots, BotProfileEntity botProfile) {
        int botsAdded = 0;
        for (int i = 0; i < numberOfBots; i++) {
            gameEntity = gameRepository.findById(gameEntity.getId())
                    .orElseThrow(() -> new GameNotFoundException("Game not found"));
            PlayerColor availableColor = colorManager.getAvailableRandomColor(gameEntity);
            if (availableColor == null) break;

            PlayerEntity botPlayer = createBotPlayer(gameEntity, botProfile, availableColor);
            playerRepository.save(botPlayer);

            gameEntity.getPlayers().add(botPlayer);

            // Establecer la relación bidireccional
            botPlayer.setGame(gameEntity);

            // Guardar en base de datos
            PlayerEntity savedBot = playerRepository.save(botPlayer);

            // Actualizar la referencia en la lista con el bot guardado (con ID)
            gameEntity.getPlayers().remove(botPlayer); // Quitar el temporal
            gameEntity.getPlayers().add(savedBot);     // Agregar el persistido

            botsAdded++;
        }
    }

    private PlayerEntity createBotPlayer(GameEntity gameEntity, BotProfileEntity botProfile, PlayerColor color) {
        int nextSeatOrder = gameEntity.getPlayers().stream()
                .mapToInt(PlayerEntity::getSeatOrder)
                .max()
                .orElse(-1) + 1;


        PlayerEntity botPlayer = new PlayerEntity();
        botPlayer.setBotProfile(botProfile);
        botPlayer.setGame(gameEntity);
        botPlayer.setColor(color);
        botPlayer.setStatus(PlayerStatus.WAITING);
        botPlayer.setSeatOrder(nextSeatOrder);

        return botPlayer;
    }

    private void updateGameEntitySettings(GameEntity gameEntity, UpdateGameSettingsDto dto) {
        if (dto.getMaxPlayers() != null) {
            gameEntity.setMaxPlayers(dto.getMaxPlayers());
        }
        if (dto.getTurnTimeLimit() != null) {
            gameEntity.setTurnTimeLimit(dto.getTurnTimeLimit());
        }
        if (dto.getChatEnabled() != null) {
            gameEntity.setChatEnabled(dto.getChatEnabled());
        }
        if (dto.getPactsAllowed() != null) {
            gameEntity.setPactsAllowed(dto.getPactsAllowed());
        }
    }

    private void removeOrEliminatePlayer(PlayerEntity player, GameEntity gameEntity) {
        if (player.getBotProfile() != null) {
            gameEntity.getPlayers().remove(player);
            playerRepository.delete(player);
        } else {
            eliminatePlayer(player);
        }
    }

    private void eliminatePlayer(PlayerEntity player) {
        player.setStatus(PlayerStatus.ELIMINATED);
        player.setEliminatedAt(LocalDateTime.now());
        playerRepository.save(player);
    }

    private PlayerEntity findPlayerInGame(GameEntity gameEntity, Long userId) {
        return gameEntity.getPlayers().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player not found in game."));
    }

    private long getActivePlayerCount(GameEntity gameEntity) {
        return gameEntity.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .count();
    }

    private void validateCanJoinGame(PlayerEntity player, GameEntity game) {
        if (player.getStatus() != PlayerStatus.WAITING) {
            throw new InvalidGameStateException("Player is not in WAITING state. Current: " + player.getStatus());
        }
    }

    //Servicios de waiting-room para reanudar partida
    @Override
    public Game joinGameLobby(String gameCode, Long playerId) {
        GameEntity gameEntity = findGameEntityByCode(gameCode);
        validateGamePaused(gameEntity);
        PlayerEntity player = findPlayerInGame(gameEntity, playerId);
        validatePlayerBelongsToGame(player, gameEntity);
        validateCanJoinPausedGame(player, gameEntity);
        player.setStatus(PlayerStatus.WAITING);
        playerRepository.save(player);
        return gameMapper.toModel(gameEntity);
    }

    private void validateCanJoinPausedGame(PlayerEntity player, GameEntity gameEntity) {
        if (player.getStatus() != PlayerStatus.DISCONNECTED) {
            throw new InvalidGameStateException("Player is not in DISCONNECTED state. Current: " + player.getStatus());
        }
    }

    private void validateGamePaused(GameEntity gameEntity) {
        if (gameEntity.getStatus() != GameState.PAUSED) {
            throw new InvalidGameStateException("Game is not in PAUSED state. Current: " + gameEntity.getStatus());
        }
    }

    @Override
    public Game togglePlayerReady(String gameId, Long playerId) {
        GameEntity gameEntity = findGameEntityByCode(gameId);
        PlayerEntity player = findPlayerInGame(gameEntity, playerId);
        validatePlayerBelongsToGame(player, gameEntity);
        validateCanToggleReady(player);
        player.setStatus(player.getStatus() == PlayerStatus.DISCONNECTED ? PlayerStatus.WAITING : PlayerStatus.DISCONNECTED);
        playerRepository.save(player);
        return gameMapper.toModel(gameEntity);
    }

    private void validateCanToggleReady(PlayerEntity player) {
        if (player.getStatus() != PlayerStatus.DISCONNECTED) {
            throw new InvalidGameStateException("Player is not in DISCONNECTED state. Current: " + player.getStatus());
        }
    }

    @Override
    public Game getGameLobbyStatus(String gameId, Long playerId) {
        GameEntity gameEntity = findGameEntityByCode(gameId);
        PlayerEntity player = findPlayerInGame(gameEntity, playerId);
        validatePlayerBelongsToGame(player, gameEntity);
        return gameMapper.toModel(gameEntity);
    }

    @Override
    public Game resumeGame(String gameId) {
        GameEntity gameEntity = findGameEntityByCode(gameId);
        validateGameInLobby(gameEntity);
        gameEntity.setStatus(GameState.NORMAL_PLAY);
        gameRepository.save(gameEntity);
        return gameMapper.toModel(gameEntity);
    }

    @Override
    public Game disconnectFromLobby(String gameId, Long playerId) {
        GameEntity gameEntity = findGameEntityByCode(gameId);
        PlayerEntity player = findPlayerInGame(gameEntity, playerId);
        validatePlayerBelongsToGame(player, gameEntity);
        validateCanLeaveGame(player, gameEntity);
        player.setStatus(PlayerStatus.DISCONNECTED);
        playerRepository.save(player);
        return gameMapper.toModel(gameEntity);
    }


}
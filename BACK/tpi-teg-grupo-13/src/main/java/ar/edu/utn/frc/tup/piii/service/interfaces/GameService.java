package ar.edu.utn.frc.tup.piii.service.interfaces;
import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.model.Game;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal service interface for Game operations needed by GameStateController.
 * Provides basic CRUD operations for Game entities.
 */
public interface GameService {

    /**
     * Finds a game by its ID.
     *
     * @param gameId the ID of the game to find
     * @return the game if found
     * @throws GameNotFoundException if game is not found
     */
    Game findById(Long gameId);

    /**
     * Finds a game by its ID, returning Optional.
     *
     * @param gameId the ID of the game to find
     * @return Optional containing the game if found, empty otherwise
     */
    Optional<Game> findByIdOptional(Long gameId);

    /**
     * Finds a game by its game code.
     *
     * @param gameCode the game code to search for
     * @return the game if found
     * @throws GameNotFoundException if game is not found
     */
    Game findByGameCode(String gameCode);

    /**
     * Saves or updates a game.
     *
     * @param game the game to save
     * @return the saved game
     */
    Game save(Game game);

    /**
     * Checks if a game exists by ID.
     *
     * @param gameId the ID to check
     * @return true if game exists, false otherwise
     */
    boolean existsById(Long gameId);

    /**
     *
     * @param hostUserId
     * @return
     */
    public Game createLobbyWithDefaults(Long hostUserId);

    GameResponseDto getGameByCode(String gameCode);

    /**
     * Allows a user to join an existing game.
     *
     * @param dto join game data transfer object
     * @return the updated game
     * @throws GameNotFoundException if game is not found
     * @throws GameFullException if game is already full
     * @throws InvalidGameStateException if game is not in WAITING_FOR_PLAYERS state
     * @throws UserNotFoundException if user is not found
     * @throws ColorNotAvailableException if no colors are available
     */
    @Transactional
    Game joinGame(JoinGameDto dto);

    /**
     * Adds bot players to an existing game.
     *
     * @param dto add bots data transfer object
     * @return the updated game
     * @throws GameNotFoundException if game is not found
     * @throws GameFullException if adding bots would exceed capacity
     * @throws InvalidGameStateException if game is not in WAITING_FOR_PLAYERS state
     * @throws BotProfileNotFoundException if specified bot profile is not found
     */
    @Transactional
    Game addBotsToGame(AddBotsDto dto);

    /**
     * Starts a game, changing its state from WAITING_FOR_PLAYERS to IN_PROGRESS.
     *
     * @param gameCode the code of the game to start
     * @return the updated game
     * @throws GameNotFoundException if game is not found
     * @throws InvalidGameStateException if game is not in WAITING_FOR_PLAYERS state
     */
    @Transactional
    Game startGame(String gameCode);



    Game updateGameSettings(String gameCode, UpdateGameSettingsDto dto);

    Game kickPlayer(KickPlayerDto dto)
            throws GameNotFoundException, PlayerNotFoundException, InvalidGameStateException, ForbiddenException;

    void prepareInitialPlacementPhase(String gameCode, Long playerId, Map<Long, Integer> armiesByCountry);

    /**
     * Allows a player to voluntarily leave the game while it's in the lobby state.
     *
     * <p>
     * This method is only valid if the game is in the {@code WAITING_FOR_PLAYERS} state.
     * The player will be removed from the list of active players.
     * </p>
     *
     * @param dto DTO containing the {@code gameCode} and the {@code playerId} of the player who wants to leave.
     * @return An updated {@link GameResponseDto} without the player who left.
     * @throws GameNotFoundException if the game does not exist.
     * @throws PlayerNotFoundException if the player is not part of the game.
     * @throws InvalidGameStateException if the game has already started.
     */
    Game leaveGame(LeaveGameDto dto);

    /**
     * Cancela o elimina la partida identificada por gameCode si userId es el host.
     * @throws ForbiddenException si quien llama no es el host.
     * @throws GameNotFoundException si no existe la partida.
     */
    void cancelGameByUsername(String gameCode, String requesterUsername);

    // Metodos para reanudar partida
    Game joinGameLobby(String gameCode, Long playerId);
    Game togglePlayerReady(String gameId, Long playerId);
    Game getGameLobbyStatus(String gameId, Long playerId); // playerId can be optional
    Game resumeGame(String gameId);
    Game disconnectFromLobby(String gameId, Long playerId);

    List<Game> findGamesByHost(Long userId);

    Game startGameByHost(String gameCode, Long userId);
}


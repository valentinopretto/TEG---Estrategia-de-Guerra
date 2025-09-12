package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.Game;

/**
 * Service interface for managing game state transitions and turn phases.
 * Provides methods to control game flow, validate state changes, and manage player turns.
 */
public interface GameStateService {

    /**
     * Changes the current game state to a new state.
     * Validates if the transition is allowed before applying the change.
     *
     * @param game the game to modify
     * @param newState the target state to transition to
     * @return true if the state change was successful, false if the transition is invalid
     */
    boolean changeGameState(Game game, GameState newState);

    /**
     * Changes the current turn phase to a new phase.
     * Only works if the game is in progress and the phase transition is valid.
     *
     * @param game the game to modify
     * @param newPhase the target phase to transition to
     * @return true if the phase change was successful, false if the transition is invalid
     */
    boolean changeTurnPhase(Game game, TurnPhase newPhase);

    /**
     * Starts a game if it's waiting for players and meets start conditions.
     * Sets the game state to IN_PROGRESS and initializes the first turn phase.
     *
     * @param game the game to start
     * @return true if the game was started successfully, false otherwise
     */
    boolean startGame(Game game);

    /**
     * Pauses an active game.
     * Only works if the game is currently in progress.
     *
     * @param game the game to pause
     * @return true if the game was paused successfully, false otherwise
     */
    boolean pauseGame(Game game);

    /**
     * Resumes a paused game.
     * Restores the game to its previous in-progress state.
     *
     * @param game the game to resume
     * @return true if the game was resumed successfully, false otherwise
     */
    boolean resumeGame(Game game);

    /**
     * Finishes a game and sets its state to FINISHED.
     * This is typically called when a player wins or the game ends.
     *
     * @param game the game to finish
     * @return true if the game was finished successfully, false otherwise
     */
    boolean finishGame(Game game);

    /**
     * Advances to the next turn by moving to the next player and resetting the turn phase.
     * Increments the turn counter and sets phase to REINFORCEMENT.
     *
     * @param game the game to advance
     */
    void nextTurn(Game game);

    /**
     * Checks if a specific action can be performed in the current game state and phase.
     * Used to validate player actions before processing them.
     *
     * @param game the game to check
     * @param action the action to validate (e.g., "attack", "reinforce", "fortify")
     * @return true if the action is allowed in the current context, false otherwise
     */
    boolean canPerformAction(Game game, String action);

    /**
     * Gets all available actions that can be performed in the current game state and phase.
     * Useful for UI to show only valid options to players.
     *
     * @param game the game to analyze
     * @return array of action names that are currently valid
     */
    String[] getAvailableActions(Game game);

    /**
     * Validates if the current game state allows for gameplay actions.
     * Checks if the game is in progress and not paused or finished.
     *
     * @param game the game to validate
     * @return true if the game is active and playable, false otherwise
     */
    boolean isGameActive(Game game);

    /**
     * Checks if it's a specific player's turn to act.
     * Compares the given player with the current active player.
     *
     * @param game the game to check
     * @param playerId the ID of the player to validate
     * @return true if it's the specified player's turn, false otherwise
     */
    boolean isPlayerTurn(Game game, Long playerId);

    /**
     * Gets the current phase description for display purposes.
     * Provides human-readable description of what should happen in the current phase.
     *
     * @param game the game to analyze
     * @return string description of the current phase
     */
    String getCurrentPhaseDescription(Game game);

    /**
     * Validates if a turn phase transition is allowed from the current phase.
     * Used internally to ensure proper game flow.
     *
     * @param currentPhase the current turn phase
     * @param targetPhase the desired target phase
     * @return true if the transition is valid, false otherwise
     */
    boolean isValidPhaseTransition(TurnPhase currentPhase, TurnPhase targetPhase);

    /**
     * Validates if a game state transition is allowed from the current state.
     * Used internally to ensure proper game lifecycle management.
     *
     * @param currentState the current game state
     * @param targetState the desired target state
     * @return true if the transition is valid, false otherwise
     */
    boolean isValidStateTransition(GameState currentState, GameState targetState);

    String executeGameAction(Long gameId, String action);
}

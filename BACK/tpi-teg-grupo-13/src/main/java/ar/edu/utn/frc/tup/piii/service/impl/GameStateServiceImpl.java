package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GameStateServiceImpl implements GameStateService {

    List<GameState> noGameList = List.of(GameState.WAITING_FOR_PLAYERS, GameState.PAUSED, GameState.FINISHED);

    private static final Map<Long, Set<Long>> conquestTracker = new HashMap<>();

    @Autowired
    private GameService gameService;

    //registrar que jugador conquisto
    //esta limitado al turno actual  porque lo limpio en nextplayer
    public static void registerConquest(Long gameId, Long playerId) {
        conquestTracker.computeIfAbsent(gameId, k -> new HashSet<>()).add(playerId);
    }

    public static boolean hasPlayerConqueredThisTurn(Long gameId, Long playerId) {
        return conquestTracker.containsKey(gameId) && conquestTracker.get(gameId).contains(playerId);
    }

    // Cambiar estado del juego
    @Override
    public boolean changeGameState(Game game, GameState newState) {
        GameState currentState = game.getState();

        switch (currentState) {
            case WAITING_FOR_PLAYERS:
                if (newState == GameState.REINFORCEMENT_5 && game.canStart()) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.REINFORCEMENT);
                    log.info("Game state changed to REINFORCEMENT_5");
                    return true;
                }
                break;

            case REINFORCEMENT_5: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues
                                  // de la colacion de
                if (newState == GameState.REINFORCEMENT_3) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.REINFORCEMENT);
                    log.info("Game state changed to REINFORCEMENT_3");
                    return true;
                }
                break;

            case REINFORCEMENT_3: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues
                                  // de la colacion de
                if (newState == GameState.HOSTILITY_ONLY) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.ATTACK);
                    log.info("Game state changed to HOSTILITY_ONLY (attack only phase)");
                    return true;
                }
                break;

            case HOSTILITY_ONLY: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues
                                 // de la colacion de
                if (newState == GameState.NORMAL_PLAY) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.REINFORCEMENT);
                    log.info("Game state changed to NORMAL_PLAY");
                    return true;
                }
                break;
            // 4 tipos de rondas // reinforcment 5 // reinforcment 3 // sin reinforcment //
            // ostilidades

            case NORMAL_PLAY: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues
                              // de la colacion de
                if (newState == GameState.PAUSED ||
                        newState == GameState.FINISHED) {
                    game.setState(newState);
                    log.info("Game state changed to {}", newState);
                    return true;
                }
                break;

            case PAUSED:
                if (newState == GameState.NORMAL_PLAY) {
                    game.setState(newState);
                    log.info("Game resumed to NORMAL_PLAY");
                    return true;
                }
                break;

            case FINISHED:
                // No se puede cambiar desde FINISHED
                log.warn("Game state is already FINISHED");
                return false;
        }

        log.warn("Invalid state transition from {} to {}", currentState, newState);
        return false; // Transicion no válida
    }

    // Cambiar fase del turno
    @Override
    public boolean changeTurnPhase(Game game, TurnPhase newPhase) {
        if (noGameList.contains(game.getState())) {
            log.warn("Cannot change turn phase in game state: {}", game.getState());
            return false;
        }

        TurnPhase currentPhase = game.getCurrentPhase();
        log.debug("Attempting to change turn phase from {} to {} in game state {}",
                currentPhase, newPhase, game.getState());

        switch (currentPhase) {
            case REINFORCEMENT:
                return handleReinforcementPhaseTransition(game, newPhase);
            case ATTACK:
                return handleAttackPhaseTransition(game, newPhase);
            case FORTIFY:
                return handleFortifyPhaseTransition(game, newPhase);
            case CLAIM_CARD:
                return handleClaimCardPhaseTransition(game, newPhase);
            case END_TURN:
                return handleEndTurnPhaseTransition(game, newPhase);
        }

        log.warn("Unknown current phase: {}", currentPhase);
        return false;
    }

    private boolean handleReinforcementPhaseTransition(Game game, TurnPhase newPhase) {
        switch (game.getState()) {
            case REINFORCEMENT_5:
            case REINFORCEMENT_3:
                // En fases iniciales, solo se puede pasar al final del turno
                if (newPhase == TurnPhase.END_TURN) {
                    game.setCurrentPhase(newPhase);
                    log.info("Changed to END_TURN from REINFORCEMENT in initial phase");
                    return true;
                }
                break;
            case HOSTILITY_ONLY:
                // En hostilidad, después de refuerzo se va directo a ataque
                if (newPhase == TurnPhase.ATTACK) {
                    game.setCurrentPhase(newPhase);
                    log.info("Changed to ATTACK from REINFORCEMENT in hostility phase");
                    return true;
                }
                break;
            case NORMAL_PLAY:
                // En juego normal, se puede ir a ataque
                if (newPhase == TurnPhase.ATTACK) {
                    game.setCurrentPhase(newPhase);
                    log.info("Changed to ATTACK from REINFORCEMENT in normal play");
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean handleAttackPhaseTransition(Game game, TurnPhase newPhase) {
        // Desde ataque se puede ir a fortificación o directamente al fin del turno
        // En HOSTILITY_ONLY también se permite fortificación según las reglas TEG
        if (newPhase == TurnPhase.FORTIFY) {
            game.setCurrentPhase(newPhase);
            log.info("Changed to FORTIFY from ATTACK");
            return true;
        }
        if (newPhase == TurnPhase.END_TURN) {
            game.setCurrentPhase(newPhase);
            log.info("Changed to END_TURN from ATTACK (skipped fortify)");
            return true;
        }
        return false;
    }

    // Manejo de transiciones de fase de jugador
    private boolean handleFortifyPhaseTransition(Game game, TurnPhase newPhase) {
        // Desde fortificación se puede ir a:
        // 1. CLAIM_CARD - si conquistó al menos un territorio en este turno
        // 2. END_TURN - directamente para finalizar turno

        if (newPhase == TurnPhase.CLAIM_CARD) {
            // TODO: Validar que efectivamente conquistó territorios en este turno
            // Por ahora permitimos la transición, la validación se hará en el servicio de
            // cartas
            game.setCurrentPhase(newPhase);
            log.info("Changed to CLAIM_CARD from FORTIFY");
            return true;
        }

        if (newPhase == TurnPhase.END_TURN) {
            game.setCurrentPhase(newPhase);
            log.info("Changed to END_TURN from FORTIFY");
            return true;
        }

        return false;
    }

    private boolean handleClaimCardPhaseTransition(Game game, TurnPhase newPhase) {
        // Desde reclamar carta se va al fin del turno
        if (newPhase == TurnPhase.END_TURN) {
            game.setCurrentPhase(newPhase);
            log.info("Changed to END_TURN from CLAIM_CARD");
            return true;
        }
        return false;
    }

    private boolean handleEndTurnPhaseTransition(Game game, TurnPhase newPhase) {
        // Desde fin del turno se pasa al refuerzo del siguiente jugador
        if (newPhase == TurnPhase.REINFORCEMENT) {
            nextPlayer(game);
            game.setCurrentPhase(newPhase);
            log.info("Changed to REINFORCEMENT from END_TURN, advanced to next player");
            return true;
        }
        return false;
    }

    // Iniciar juego
    @Override
    public boolean startGame(Game game) {
        if (game.getState() == GameState.WAITING_FOR_PLAYERS && game.canStart()) {
            return changeGameState(game, GameState.REINFORCEMENT_5);
        }
        return false;
    }

    // Pausar juego
    @Override
    public boolean pauseGame(Game game) {
        return changeGameState(game, GameState.PAUSED);
    }

    // Reanudar juego
    @Override
    public boolean resumeGame(Game game) {
        if (game.getState() == GameState.PAUSED) {
            return changeGameState(game, GameState.NORMAL_PLAY);
        }
        return false;
    }

    // Finalizar juego
    @Override
    public boolean finishGame(Game game) {
        return changeGameState(game, GameState.FINISHED);
    }

    // Siguiente turno
    @Override
    public void nextTurn(Game game) {
        int previousPlayerIndex = game.getCurrentPlayerIndex();

        nextPlayer(game);

        // Verificar si completamos una ronda completa (volvimos al primer jugador)
        boolean completedRound = hasCompletedRound(game, previousPlayerIndex);

        // Manejar transiciones de estado automáticas
        if (completedRound) {
            handleRoundCompletion(game);
        }

        // Determinar la fase inicial del siguiente turno según el estado del juego
        switch (game.getState()) {
            case REINFORCEMENT_5:
            case REINFORCEMENT_3:
            case NORMAL_PLAY:
                game.setCurrentPhase(TurnPhase.REINFORCEMENT);
                break;
            case HOSTILITY_ONLY:
                game.setCurrentPhase(TurnPhase.ATTACK);
                break;
            default:
                game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        }

        // Solo incrementar el número de turno si completamos una ronda
        if (completedRound) {
            game.setCurrentTurn(game.getCurrentTurn() + 1);
        }

        log.info("Advanced to turn {} with phase {} for player {}",
                game.getCurrentTurn(), game.getCurrentPhase(), game.getCurrentPlayerIndex());
    }

    /**
     * Verifica si se completó una ronda completa (todos los jugadores jugaron).
     */
    private boolean hasCompletedRound(Game game, int previousPlayerIndex) {
        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .sorted(Comparator.comparing(Player::getSeatOrder))
                .collect(Collectors.toList());

        if (activePlayers.isEmpty()) {
            return false;
        }

        // Si volvimos al primer jugador después de haber pasado por otros
        int firstPlayerSeatOrder = activePlayers.get(0).getSeatOrder();
        int currentPlayerSeatOrder = game.getCurrentPlayerIndex();

        // Completamos ronda si:
        // 1. El jugador actual es el primero Y
        // 2. El jugador anterior no era el primero (significa que pasamos por otros)
        return currentPlayerSeatOrder == firstPlayerSeatOrder && previousPlayerIndex != firstPlayerSeatOrder;
    }

    /**
     * Maneja la finalización de rondas y las transiciones automáticas de estado.
     */
    private void handleRoundCompletion(Game game) {
        log.info("Round completed in game state: {}", game.getState());

        switch (game.getState()) {
            case REINFORCEMENT_5:
                // Después de la primera ronda de refuerzos, pasar a la segunda
                changeGameState(game, GameState.REINFORCEMENT_3);
                log.info("Transitioning from REINFORCEMENT_5 to REINFORCEMENT_3");
                break;

            case REINFORCEMENT_3:
                // Después de la segunda ronda de refuerzos, pasar a hostilidades
                changeGameState(game, GameState.HOSTILITY_ONLY);
                log.info("Transitioning from REINFORCEMENT_3 to HOSTILITY_ONLY");
                break;

            case HOSTILITY_ONLY:
                // Después de la ronda de hostilidades, pasar a juego normal
                changeGameState(game, GameState.NORMAL_PLAY);
                log.info("Transitioning from HOSTILITY_ONLY to NORMAL_PLAY - Game fully started!");
                break;

            case NORMAL_PLAY:
                // En juego normal, no hay transiciones automáticas de estado
                log.debug("Round completed in NORMAL_PLAY - continuing normal gameplay");
                break;

            default:
                log.warn("Unexpected game state during round completion: {}", game.getState());
        }
    }

    // Siguiente jugador
    private void nextPlayer(Game game) {
        if (game.getPlayers() == null || game.getPlayers().isEmpty()) {
            log.warn("No players found in game");
            return;
        }

        // Obtener jugadores activos ordenados por seatOrder
        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .sorted(Comparator.comparing(Player::getSeatOrder))
                .collect(Collectors.toList());

        if (activePlayers.isEmpty()) {
            game.setCurrentPlayerIndex(null);
            log.warn("No active players found");
            return;
        }

        // Obtener el índice actual en la lista de jugadores activos
        int currentIndex = game.getCurrentPlayerIndex() != null ? game.getCurrentPlayerIndex() : -1;
        int nextSeatOrder = -1;

        // Buscar el siguiente seatOrder valido
        boolean foundNext = false;
        for (Player player : activePlayers) {
            if (player.getSeatOrder() > currentIndex) {
                nextSeatOrder = player.getSeatOrder();
                foundNext = true;
                break;
            }
        }

        // Si no encontramos uno mayor, volvemos al primero (wrap around)
        if (!foundNext && !activePlayers.isEmpty()) {
            nextSeatOrder = activePlayers.get(0).getSeatOrder();
            log.debug("Wrapped around to first player");
        }

        // Limpiar jugadores que conquistaron este turno
        conquestTracker.remove(game.getId());


        game.setCurrentPlayerIndex(nextSeatOrder);
        log.debug("Advanced to player with seatOrder: {} (was: {})", nextSeatOrder, currentIndex);
    }

    // Verificar si se puede realizar una acción
    @Override
    public boolean canPerformAction(Game game, String action) {
        if (noGameList.contains(game.getState())) {
            return false;
        }

        TurnPhase currentPhase = game.getCurrentPhase();

        switch (action.toLowerCase()) {
            case "reinforce":
                return currentPhase == TurnPhase.REINFORCEMENT;

            case "attack":
                return currentPhase == TurnPhase.ATTACK && isAttackAllowed(game);

            case "fortify":
                return currentPhase == TurnPhase.FORTIFY && isFortifyAllowed(game);

            case "claim_card":
                return (currentPhase == TurnPhase.FORTIFY || currentPhase == TurnPhase.CLAIM_CARD)
                        && canClaimCard(game);

            case "end_turn":
                return currentPhase == TurnPhase.END_TURN;

            case "skip_attack":
                return currentPhase == TurnPhase.ATTACK;

            case "skip_fortify":
                return currentPhase == TurnPhase.FORTIFY;

            case "skip_claim_card":
                return currentPhase == TurnPhase.CLAIM_CARD;

            case "proceed_to_fortify":
                return currentPhase == TurnPhase.ATTACK;

            case "finish_fortify":
                return currentPhase == TurnPhase.FORTIFY;

            default:
                return false;
        }
    }

    /**
     * Verifica si el jugador puede reclamar una carta en este turno.
     * Según las reglas del TEG, se puede reclamar una carta si se conquistó al
     * menos un territorio.
     */
    private boolean canClaimCard(Game game) {
        // TODO: Implementar lógica para verificar si conquistó territorios en este
        // turno
        // Por ahora devolvemos true para permitir la funcionalidad
        // En una implementación completa, se debería:
        // 1. Llevar registro de territorios conquistados en este turno
        // 2. Verificar que el jugador actual conquistó al menos uno
        // 3. Verificar que no haya reclamado carta ya en este turno

        Long playerId = game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : null;
        if (playerId == null) return false;
        return hasPlayerConqueredThisTurn(game.getId(), playerId);
        // Placeholder: permitir reclamar carta en fases donde es posible
     //   return game.getState() == GameState.HOSTILITY_ONLY || game.getState() == GameState.NORMAL_PLAY;
    }

    private boolean isAttackAllowed(Game game) {
        // Ataque permitido en HOSTILITY_ONLY y NORMAL_PLAY
        return game.getState() == GameState.HOSTILITY_ONLY || game.getState() == GameState.NORMAL_PLAY;
    }

    private boolean isFortifyAllowed(Game game) {
        // Fortificación permitida en HOSTILITY_ONLY y NORMAL_PLAY
        return game.getState() == GameState.HOSTILITY_ONLY || game.getState() == GameState.NORMAL_PLAY;
    }

    // Obtener acciones disponibles
    @Override
    public String[] getAvailableActions(Game game) {
        if (noGameList.contains(game.getState())) {
            return new String[0];
        }

        List<String> actions = new ArrayList<>();

        switch (game.getCurrentPhase()) {
            case REINFORCEMENT:
                actions.add("reinforce");
                if (game.getState() == GameState.REINFORCEMENT_5 || game.getState() == GameState.REINFORCEMENT_3) {
                    actions.add("end_turn");
                } else {
                    actions.add("proceed_to_attack");
                }
                break;

            case ATTACK:
                if (isAttackAllowed(game)) {
                    actions.add("attack");
                }
                actions.add("skip_attack");
                actions.add("proceed_to_fortify");
                break;

            case FORTIFY:
                if (isFortifyAllowed(game)) {
                    actions.add("fortify");
                }
                actions.add("skip_fortify");
                actions.add("finish_fortify");
                // Si conquistó territorios en este turno, puede reclamar carta
                if (canClaimCard(game)) {
                    actions.add("claim_card");
                }
                break;

                case CLAIM_CARD:
                actions.add("claim_card");
                actions.add("skip_claim_card");
                break;

            case END_TURN:
                actions.add("end_turn");
                break;
        }

        return actions.toArray(new String[0]);
    }

    @Override
    public boolean isGameActive(Game game) {
        return !noGameList.contains(game.getState());
    }

    @Override
    public boolean isPlayerTurn(Game game, Long playerId) {
        if (!isGameActive(game)) {
            return false;
        }
        Player currentPlayer = game.getCurrentPlayer();
        return currentPlayer != null && currentPlayer.getId().equals(playerId);
    }

    @Override
    public String getCurrentPhaseDescription(Game game) {
        if (!isGameActive(game)) {
            return "Game is not active";
        }

        switch (game.getCurrentPhase()) {
            case REINFORCEMENT:
                return getReinforcementDescription(game);
            case ATTACK:
                return getAttackDescription(game);
            case FORTIFY:
                return getFortifyDescription(game);
            case CLAIM_CARD:
                return "Claim a country card if you conquered at least one territory";
            case END_TURN:
                return "Confirm to end your turn";
            default:
                return "Unknown phase";
        }
    }

    private String getReinforcementDescription(Game game) {
        switch (game.getState()) {
            case REINFORCEMENT_5:
                return "Place your 5 initial armies on your territories";
            case REINFORCEMENT_3:
                return "Place your 3 initial armies on your territories";
            case NORMAL_PLAY:
                return "Place your reinforcement armies on your territories";
            default:
                return "Place your reinforcement armies";
        }
    }

    private String getAttackDescription(Game game) {
        switch (game.getState()) {
            case HOSTILITY_ONLY:
                return "Attack enemy territories (hostility phase)";
            case NORMAL_PLAY:
                return "Attack enemy territories or proceed to fortification";
            default:
                return "Attack enemy territories";
        }
    }

    private String getFortifyDescription(Game game) {
        switch (game.getState()) {
            case HOSTILITY_ONLY:
                return "Move armies between your adjacent territories (hostility phase)";
            case NORMAL_PLAY:
                return "Move armies between your connected territories or end turn";
            default:
                return "Fortification phase";
        }
    }

    @Override
    public boolean isValidPhaseTransition(TurnPhase currentPhase, TurnPhase targetPhase) {
        switch (currentPhase) {
            case REINFORCEMENT:
                return targetPhase == TurnPhase.ATTACK || targetPhase == TurnPhase.END_TURN;
            case ATTACK:
                return targetPhase == TurnPhase.FORTIFY || targetPhase == TurnPhase.END_TURN;
            case FORTIFY:
                return targetPhase == TurnPhase.CLAIM_CARD || targetPhase == TurnPhase.END_TURN;
            case CLAIM_CARD:
                return targetPhase == TurnPhase.END_TURN;
            case END_TURN:
                return targetPhase == TurnPhase.REINFORCEMENT;
            default:
                return false;
        }
    }

    @Override
    public boolean isValidStateTransition(GameState currentState, GameState targetState) {
        switch (currentState) {
            case WAITING_FOR_PLAYERS:
                return targetState == GameState.REINFORCEMENT_5;
            case REINFORCEMENT_5:
                return targetState == GameState.REINFORCEMENT_3;
            case REINFORCEMENT_3:
                return targetState == GameState.HOSTILITY_ONLY;
            case HOSTILITY_ONLY:
                return targetState == GameState.NORMAL_PLAY;
            case NORMAL_PLAY:
                return targetState == GameState.PAUSED || targetState == GameState.FINISHED;
            case PAUSED:
                return targetState == GameState.NORMAL_PLAY;
            case FINISHED:
                return false; // No se puede cambiar desde FINISHED
            default:
                return false;
        }
    }

    @Override
    public String executeGameAction(Long gameId, String action) {
        Game game = gameService.findById(gameId);

        if (!canPerformAction(game, action)) {
            throw new IllegalStateException("Action '" + action + "' not allowed in current phase");
        }

        switch (action.toLowerCase()) {
            case "next_phase":
                advancePhase(game);
                break;
            case "next_turn":
                nextTurn(game);
                break;
            case "skip_attack":
                changeTurnPhase(game, TurnPhase.FORTIFY);
                break;
            case "skip_fortify":
                changeTurnPhase(game, TurnPhase.END_TURN);
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }

        gameService.save(game);
        return "Action performed: " + action;
    }

    private void advancePhase(Game game) {
        switch (game.getCurrentPhase()) {
            case REINFORCEMENT:
                changeTurnPhase(game, TurnPhase.ATTACK);
                break;
            case ATTACK:
                changeTurnPhase(game, TurnPhase.FORTIFY);
                break;
            case FORTIFY:
                changeTurnPhase(game, TurnPhase.END_TURN);
                break;
            default:
                break;
        }
    }
}

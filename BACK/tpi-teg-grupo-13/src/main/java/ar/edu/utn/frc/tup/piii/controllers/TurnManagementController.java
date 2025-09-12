package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.TurnActionDto;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para manejar las transiciones de turno y fases en el TEG.
 * Permite a los jugadores cambiar entre fases de ataque, fortificación y finalizar turno.
 */
@RestController
@RequestMapping("/api/games/{gameCode}/turn")
@Tag(name = "Turn Management", description = "Gestión de turnos y fases del juego")
@Slf4j
public class TurnManagementController {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameStateService gameStateService;

    @Autowired
    private GameMapper gameMapper;

    /**
     * Permite al jugador saltear la fase de ataque y pasar directamente a fortificación.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador que quiere saltear el ataque
     * @return Estado actualizado del juego en fase de fortificación
     */
    @PostMapping("/skip-attack/{playerId}")
    @Operation(summary = "Saltear fase de ataque",
            description = "Permite al jugador pasar directamente de ataque a fortificación")
    public ResponseEntity<GameResponseDto> skipAttackPhase(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.info("Player {} requested to skip attack phase in game {}", playerId, gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del jugador
            validatePlayerTurn(game, playerId);

            // Validar que está en fase de ataque
            if (game.getCurrentPhase() != TurnPhase.ATTACK) {
                log.warn("Player {} tried to skip attack but current phase is {}", playerId, game.getCurrentPhase());
                return ResponseEntity.badRequest().build();
            }

            // Cambiar a fase de fortificación
            boolean success = gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY);
            if (!success) {
                log.warn("Failed to change phase to FORTIFY for player {} in game {}", playerId, gameCode);
                return ResponseEntity.badRequest().build();
            }

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Player {} successfully skipped attack phase in game {}", playerId, gameCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error skipping attack phase for player {} in game {}: {}", playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Permite al jugador saltear la fase de fortificación y finalizar su turno.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador que quiere saltear la fortificación
     * @return Estado actualizado del juego con el turno finalizado
     */
    @PostMapping("/skip-fortify/{playerId}")
    @Operation(summary = "Saltear fase de fortificación",
            description = "Permite al jugador saltear la fortificación y finalizar su turno")
    public ResponseEntity<GameResponseDto> skipFortifyPhase(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.info("Player {} requested to skip fortify phase in game {}", playerId, gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del jugador
            validatePlayerTurn(game, playerId);

            // Validar que está en fase de fortificación
            if (game.getCurrentPhase() != TurnPhase.FORTIFY) {
                log.warn("Player {} tried to skip fortify but current phase is {}", playerId, game.getCurrentPhase());
                return ResponseEntity.badRequest().build();
            }

            // Cambiar a fase de fin de turno
            boolean success = gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);
            if (!success) {
                log.warn("Failed to change phase to END_TURN for player {} in game {}", playerId, gameCode);
                return ResponseEntity.badRequest().build();
            }

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Player {} successfully skipped fortify phase in game {}", playerId, gameCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error skipping fortify phase for player {} in game {}: {}", playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Finaliza el turno del jugador actual y pasa al siguiente jugador.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador que quiere finalizar su turno
     * @return Estado actualizado del juego con el siguiente jugador activo
     */
    @PostMapping("/end-turn/{playerId}")
    @Operation(summary = "Finalizar turno",
            description = "Finaliza el turno del jugador actual y pasa al siguiente")
    public ResponseEntity<GameResponseDto> endTurn(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.info("Player {} requested to end turn in game {}", playerId, gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del jugador
            validatePlayerTurn(game, playerId);

            // Validar que está en fase de fin de turno
            if (game.getCurrentPhase() == TurnPhase.END_TURN) {
                log.warn("Player {} tried to end turn but current phase is {}", playerId, game.getCurrentPhase());
                return ResponseEntity.badRequest().build();
            }

            // Avanzar al siguiente turno
            gameStateService.nextTurn(game);

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Turn ended for player {} in game {}, next player: {}",
                    playerId, gameCode, savedGame.getCurrentPlayerIndex());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error ending turn for player {} in game {}: {}", playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Permite al jugador cambiar desde la fase de ataque a la fase de fortificación.
     * Útil cuando el jugador terminó de atacar pero no quiere saltearse la fortificación.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Estado actualizado del juego en fase de fortificación
     */
    @PostMapping("/proceed-to-fortify/{playerId}")
    @Operation(summary = "Proceder a fortificación",
            description = "Cambia de la fase de ataque a la fase de fortificación")
    public ResponseEntity<GameResponseDto> proceedToFortify(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.info("Player {} requested to proceed to fortify phase in game {}", playerId, gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del jugador
            validatePlayerTurn(game, playerId);

            // Validar que está en fase de ataque
            if (game.getCurrentPhase() != TurnPhase.ATTACK) {
                log.warn("Player {} tried to proceed to fortify but current phase is {}", playerId, game.getCurrentPhase());
                return ResponseEntity.badRequest().build();
            }

            // Cambiar a fase de fortificación
            boolean success = gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY);
            if (!success) {
                log.warn("Failed to change phase to FORTIFY for player {} in game {}", playerId, gameCode);
                return ResponseEntity.badRequest().build();
            }

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Player {} successfully proceeded to fortify phase in game {}", playerId, gameCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error proceeding to fortify for player {} in game {}: {}", playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Permite al jugador proceder a reclamar una carta después de fortificar.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Estado actualizado del juego en fase de reclamación de carta
     */
    @PostMapping("/claim-card/{playerId}")
    @Operation(summary = "Proceder a reclamar carta",
            description = "Cambia a la fase de reclamación de carta si conquistó al menos un territorio")
    public ResponseEntity<GameResponseDto> proceedToClaimCard(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.info("Player {} requested to proceed to claim card phase in game {}", playerId, gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del jugador
            validatePlayerTurn(game, playerId);

            // Validar que está en fase de fortificación
            if (game.getCurrentPhase() != TurnPhase.FORTIFY) {
                log.warn("Player {} tried to proceed to claim card but current phase is {}", playerId, game.getCurrentPhase());
                return ResponseEntity.badRequest().build();
            }

            // Validar que puede reclamar carta
            if (!gameStateService.canPerformAction(game, "claim_card")) {
                log.warn("Player {} cannot claim card in current game state", playerId);
                return ResponseEntity.badRequest().build();
            }

            // Cambiar a fase de reclamación de carta
            boolean success = gameStateService.changeTurnPhase(game, TurnPhase.CLAIM_CARD);
            if (!success) {
                log.warn("Failed to change phase to CLAIM_CARD for player {} in game {}", playerId, gameCode);
                return ResponseEntity.badRequest().build();
            }

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Player {} successfully proceeded to claim card phase in game {}", playerId, gameCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error proceeding to claim card for player {} in game {}: {}", playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Permite al jugador saltear la reclamación de carta y finalizar su turno.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador que quiere saltear la reclamación
     * @return Estado actualizado del juego con el turno finalizado
     */
    @PostMapping("/skip-claim-card/{playerId}")
    @Operation(summary = "Saltear reclamación de carta",
            description = "Permite al jugador saltear la reclamación de carta y finalizar su turno")
    public ResponseEntity<GameResponseDto> skipClaimCard(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.info("Player {} requested to skip claim card phase in game {}", playerId, gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del jugador
            validatePlayerTurn(game, playerId);

            // Validar que está en fase de reclamación de carta
            if (game.getCurrentPhase() != TurnPhase.CLAIM_CARD) {
                log.warn("Player {} tried to skip claim card but current phase is {}", playerId, game.getCurrentPhase());
                return ResponseEntity.badRequest().build();
            }

            // Cambiar a fase de fin de turno
            boolean success = gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);
            if (!success) {
                log.warn("Failed to change phase to END_TURN for player {} in game {}", playerId, gameCode);
                return ResponseEntity.badRequest().build();
            }

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Player {} successfully skipped claim card phase in game {}", playerId, gameCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error skipping claim card for player {} in game {}: {}", playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ejecuta una acción genérica de turno usando el DTO estándar.
     *
     * @param gameCode Código del juego
     * @param actionDto DTO con la acción a ejecutar
     * @return Estado actualizado del juego
     */
    @PostMapping("/action")
    @Operation(summary = "Ejecutar acción de turno",
            description = "Ejecuta una acción específica en el turno actual")
    public ResponseEntity<GameResponseDto> performTurnAction(
            @PathVariable String gameCode,
            @Valid @RequestBody TurnActionDto actionDto) {

        log.info("Player {} requested action '{}' in game {}",
                actionDto.getPlayerId(), actionDto.getAction(), gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del jugador
            validatePlayerTurn(game, actionDto.getPlayerId());

            // Procesar la acción específica
            boolean success = processAction(game, actionDto.getAction());
            if (!success) {
                log.warn("Failed to process action '{}' for player {} in game {}",
                        actionDto.getAction(), actionDto.getPlayerId(), gameCode);
                return ResponseEntity.badRequest().build();
            }

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Action '{}' executed successfully for player {} in game {}",
                    actionDto.getAction(), actionDto.getPlayerId(), gameCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing action '{}' for player {} in game {}: {}",
                    actionDto.getAction(), actionDto.getPlayerId(), gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene el estado actual del turno con información detallada.
     *
     * @param gameCode Código del juego
     * @return Información del estado actual del turno
     */
    @GetMapping("/status")
    @Operation(summary = "Obtener estado del turno",
            description = "Devuelve información detallada sobre el estado actual del turno")
    public ResponseEntity<TurnStatusDto> getTurnStatus(@PathVariable String gameCode) {
        try {
            Game game = gameService.findByGameCode(gameCode);

            TurnStatusDto status = TurnStatusDto.builder()
                    .gameCode(gameCode)
                    .gameState(game.getState())
                    .currentPhase(game.getCurrentPhase())
                    .currentTurn(game.getCurrentTurn())
                    .currentPlayerIndex(game.getCurrentPlayerIndex())
                    .currentPlayerName(game.getCurrentPlayer() != null ? game.getCurrentPlayer().getDisplayName() : null)
                    .totalActivePlayers(game.getActivePlayerCount())
                    .availableActions(gameStateService.getAvailableActions(game))
                    .phaseDescription(gameStateService.getCurrentPhaseDescription(game))
                    .canSkipPhase(canSkipCurrentPhase(game))
                    .build();

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error getting turn status for game {}: {}", gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // === MÉTODOS PRIVADOS DE APOYO ===

    /**
     * Valida que sea el turno del jugador especificado.
     */
    private void validatePlayerTurn(Game game, Long playerId) {
        if (!gameStateService.isPlayerTurn(game, playerId)) {
            throw new IllegalStateException("It's not player's turn: " + playerId);
        }
    }

    /**
     * Procesa una acción específica del turno.
     */
    private boolean processAction(Game game, String action) {
        switch (action.toLowerCase()) {
            case "skip_attack":
                return gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY);
            case "skip_fortify":
                return gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);
            case "proceed_to_fortify":
                return gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY);
            case "finish_fortify":
                // Verificar si puede reclamar carta o ir directo al fin de turno
                if (gameStateService.canPerformAction(game, "claim_card")) {
                    return gameStateService.changeTurnPhase(game, TurnPhase.CLAIM_CARD);
                } else {
                    return gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);
                }
            case "claim_card":
                return gameStateService.changeTurnPhase(game, TurnPhase.CLAIM_CARD);
            case "skip_claim_card":
                return gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);
            case "end_turn":
                gameStateService.nextTurn(game);
                return true;
            default:
                log.warn("Unknown turn action: {}", action);
                return false;
        }
    }

    /**
     * Verifica si la fase actual se puede saltear.
     */
    private boolean canSkipCurrentPhase(Game game) {
        switch (game.getCurrentPhase()) {
            case ATTACK:
            case FORTIFY:
                return true;
            case REINFORCEMENT:
            case END_TURN:
                return false;
            default:
                return false;
        }
    }

    /**
     * DTO para el estado del turno.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TurnStatusDto {
        private String gameCode;
        private ar.edu.utn.frc.tup.piii.model.enums.GameState gameState;
        private TurnPhase currentPhase;
        private Integer currentTurn;
        private Integer currentPlayerIndex;
        private String currentPlayerName;
        private Long totalActivePlayers;
        private String[] availableActions;
        private String phaseDescription;
        private Boolean canSkipPhase;
    }
}
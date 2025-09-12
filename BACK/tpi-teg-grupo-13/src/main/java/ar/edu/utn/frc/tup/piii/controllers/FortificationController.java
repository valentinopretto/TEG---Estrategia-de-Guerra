package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.FortificationResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.FortificationService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para operaciones de fortificación.
 * Maneja el movimiento de ejércitos entre territorios propios conectados.
 */
@RestController
@RequestMapping("/api/games/{gameCode}/fortification")
@Tag(name = "Fortification", description = "Operaciones de fortificación y movimiento de tropas")
@Slf4j
public class FortificationController {

    @Autowired
    private FortificationService fortificationService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameStateService gameStateService;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    /**
     * Ejecuta una fortificación moviendo ejércitos entre territorios propios.
     *
     * @param gameCode Código del juego
     * @param fortifyDto Datos de la fortificación (origen, destino, ejércitos)
     * @return Resultado detallado de la fortificación
     */
    @PostMapping("/fortify")
    @Operation(summary = "Ejecutar fortificación",
            description = "Mueve ejércitos entre dos territorios propios conectados")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fortificación ejecutada exitosamente",
                    content = @Content(schema = @Schema(implementation = FortificationResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Fortificación inválida: territorios no conectados, ejércitos insuficientes, o dejaría territorio sin defensa"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No es la fase de fortificación o no es el turno del jugador"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida o territorios no encontrados"
            )
    })
    public ResponseEntity<FortificationResponseDto> performFortification(
            @PathVariable String gameCode,
            @Valid @RequestBody FortifyDto fortifyDto) {

        log.info("Fortification requested in game {} by player {} - {} armies from {} to {}",
                gameCode, fortifyDto.getPlayerId(), fortifyDto.getArmies(),
                fortifyDto.getFromCountryId(), fortifyDto.getToCountryId());

        try {
            // Validar que el juego permite fortificación
            Game game = gameService.findByGameCode(gameCode);
            validateGameStateForFortification(game);
            validatePlayerTurn(game, fortifyDto.getPlayerId());

            // Obtener información de territorios antes de la fortificación
            Territory fromTerritoryBefore = gameTerritoryService.getTerritoryByGameAndCountry(
                    game.getId(), fortifyDto.getFromCountryId());
            Territory toTerritoryBefore = gameTerritoryService.getTerritoryByGameAndCountry(
                    game.getId(), fortifyDto.getToCountryId());

            // Validar la fortificación antes de ejecutarla
            if (!fortificationService.isValidFortification(gameCode, fortifyDto)) {
                log.warn("Invalid fortification attempt in game {}", gameCode);

                // Proporcionar mensaje específico si se intenta dejar territorio sin ejércitos
                Territory fromTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                        game.getId(), fortifyDto.getFromCountryId());

                String errorMessage = "Invalid fortification";
                if (fromTerritory != null && fromTerritory.getArmies() - fortifyDto.getArmies() < 1) {
                    errorMessage = String.format("Cannot leave territory without armies. Territory has %d armies, you're trying to move %d. Must leave at least 1 army.",
                            fromTerritory.getArmies(), fortifyDto.getArmies());
                }

                FortificationResponseDto errorResponse = FortificationResponseDto.builder()
                        .success(false)
                        .message(errorMessage)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Ejecutar la fortificación
            boolean success = fortificationService.performFortification(gameCode, fortifyDto);

            if (!success) {
                log.warn("Fortification failed in game {}", gameCode);
                FortificationResponseDto errorResponse = FortificationResponseDto.builder()
                        .success(false)
                        .message("Fortification failed - operation could not be completed")
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Obtener información actualizada de territorios
            Territory fromTerritoryAfter = gameTerritoryService.getTerritoryByGameAndCountry(
                    game.getId(), fortifyDto.getFromCountryId());
            Territory toTerritoryAfter = gameTerritoryService.getTerritoryByGameAndCountry(
                    game.getId(), fortifyDto.getToCountryId());

            // Construir respuesta detallada
            FortificationResponseDto response = FortificationResponseDto.builder()
                    .fromCountryId(fortifyDto.getFromCountryId())
                    .fromCountryName(fromTerritoryBefore.getName())
                    .toCountryId(fortifyDto.getToCountryId())
                    .toCountryName(toTerritoryBefore.getName())
                    .playerName(fromTerritoryBefore.getOwnerName())
                    .armiesMoved(fortifyDto.getArmies())
                    .fromCountryRemainingArmies(fromTerritoryAfter.getArmies())
                    .toCountryFinalArmies(toTerritoryAfter.getArmies())
                    .success(true)
                    .message("Fortification completed successfully")
                    .build();

            log.info("Fortification completed successfully in game {}", gameCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during fortification in game {}: {}", gameCode, e.getMessage());
            FortificationResponseDto errorResponse = FortificationResponseDto.builder()
                    .success(false)
                    .message("Error during fortification: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Obtiene todos los territorios que un jugador puede usar como origen para fortificar.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Lista de territorios con más de 1 ejército
     */
    @GetMapping("/fortifiable-territories/{playerId}")
    @Operation(summary = "Obtener territorios fortificables",
            description = "Lista los territorios del jugador que pueden ser origen de fortificación")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de territorios fortificables obtenida",
                    content = @Content(schema = @Schema(implementation = Territory.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado de juego no permite fortificación"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Jugador o partida no encontrada"
            )
    })
    public ResponseEntity<List<Territory>> getFortifiableTerritories(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        try {
            Game game = gameService.findByGameCode(gameCode);
            validateGameStateForFortification(game);

            List<Territory> fortifiableTerritories =
                    fortificationService.getFortifiableTerritoriesForPlayer(gameCode, playerId);

            log.debug("Player {} has {} fortifiable territories in game {}",
                    playerId, fortifiableTerritories.size(), gameCode);

            return ResponseEntity.ok(fortifiableTerritories);

        } catch (Exception e) {
            log.error("Error getting fortifiable territories for player {} in game {}: {}",
                    playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene los territorios que pueden recibir ejércitos desde un territorio específico.
     * En HOSTILITY_ONLY: solo territorios adyacentes (vecinos directos)
     * En NORMAL_PLAY: territorios conectados por cadena de territorios propios
     *
     * @param gameCode Código del juego
     * @param territoryId ID del territorio origen
     * @param playerId ID del jugador (para verificar propiedad)
     * @return Lista de territorios que pueden recibir ejércitos
     */
    @GetMapping("/fortification-targets/{territoryId}/{playerId}")
    @Operation(summary = "Obtener objetivos de fortificación",
            description = "Lista los territorios que pueden recibir ejércitos. En HOSTILITY_ONLY solo adyacentes, en NORMAL_PLAY conectados por cadena.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de objetivos de fortificación obtenida",
                    content = @Content(schema = @Schema(implementation = Territory.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Territorio no puede fortificar o estado de juego inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Territorio no pertenece al jugador"
            )
    })
    public ResponseEntity<List<Territory>> getFortificationTargets(
            @PathVariable String gameCode,
            @PathVariable Long territoryId,
            @PathVariable Long playerId) {

        try {
            Game game = gameService.findByGameCode(gameCode);
            validateGameStateForFortification(game);

            List<Territory> targets = fortificationService.getFortificationTargetsForTerritory(
                    gameCode, territoryId, playerId);

            String ruleType = game.getState() == GameState.HOSTILITY_ONLY ? "adjacent only" : "connected chain";
            log.debug("Territory {} can fortify {} territories in game {} (rule: {})",
                    territoryId, targets.size(), gameCode, ruleType);

            return ResponseEntity.ok(targets);

        } catch (Exception e) {
            log.error("Error getting fortification targets for territory {} in game {}: {}",
                    territoryId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verifica si un jugador puede fortificar en el estado actual del juego.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return true si puede fortificar, false en caso contrario
     */
    @GetMapping("/can-fortify/{playerId}")
    @Operation(summary = "Verificar si puede fortificar",
            description = "Verifica si un jugador puede realizar fortificaciones en el estado actual")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resultado de la verificación",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    public ResponseEntity<Boolean> canPlayerFortify(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Verificar estado del juego
            boolean gameAllowsFortification = isGameStateValidForFortification(game);

            // Verificar turno del jugador
            boolean isPlayerTurn = gameStateService.isPlayerTurn(game, playerId);

            // Verificar fase del turno
            boolean isFortifyPhase = gameStateService.canPerformAction(game, "fortify");

            boolean canFortify = gameAllowsFortification && isPlayerTurn && isFortifyPhase;

            log.debug("Player {} can fortify in game {}: {} (gameState: {}, turn: {}, phase: {})",
                    playerId, gameCode, canFortify, gameAllowsFortification, isPlayerTurn, isFortifyPhase);

            return ResponseEntity.ok(canFortify);

        } catch (Exception e) {
            log.error("Error checking if player {} can fortify in game {}: {}",
                    playerId, gameCode, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Obtiene el máximo número de ejércitos que se pueden mover desde un territorio.
     *
     * @param gameCode Código del juego
     * @param territoryId ID del territorio
     * @return Número máximo de ejércitos movibles
     */
    @GetMapping("/max-movable-armies/{territoryId}")
    @Operation(summary = "Obtener ejércitos máximos movibles",
            description = "Calcula cuántos ejércitos se pueden mover desde un territorio")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Número máximo de ejércitos movibles calculado",
                    content = @Content(schema = @Schema(implementation = Integer.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Territorio no encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de territorio inválido"
            )
    })
    public ResponseEntity<Integer> getMaxMovableArmies(
            @PathVariable String gameCode,
            @PathVariable Long territoryId) {

        try {
            int maxMovable = fortificationService.getMaxMovableArmies(gameCode, territoryId);

            log.debug("Territory {} can move maximum {} armies in game {}",
                    territoryId, maxMovable, gameCode);

            return ResponseEntity.ok(maxMovable);

        } catch (Exception e) {
            log.error("Error getting max movable armies for territory {} in game {}: {}",
                    territoryId, gameCode, e.getMessage());
            return ResponseEntity.ok(0);
        }
    }

    /**
     * Valida si dos territorios están conectados por territorios del jugador.
     *
     * @param gameCode Código del juego
     * @param fromTerritoryId Territorio origen
     * @param toTerritoryId Territorio destino
     * @param playerId ID del jugador
     * @return true si están conectados, false en caso contrario
     */
    @GetMapping("/check-connection/{fromTerritoryId}/{toTerritoryId}/{playerId}")
    @Operation(summary = "Verificar conexión entre territorios",
            description = "Verifica si dos territorios están conectados por territorios del jugador")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resultado de la verificación de conexión",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Territorios no encontrados"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Territorios no pertenecen al jugador"
            )
    })
    public ResponseEntity<Boolean> checkTerritoryConnection(
            @PathVariable String gameCode,
            @PathVariable Long fromTerritoryId,
            @PathVariable Long toTerritoryId,
            @PathVariable Long playerId) {

        try {
            boolean connected = fortificationService.areTerritoriesConnectedByPlayer(
                    gameCode, fromTerritoryId, toTerritoryId, playerId);

            log.debug("Territories {} and {} are connected for player {} in game {}: {}",
                    fromTerritoryId, toTerritoryId, playerId, gameCode, connected);

            return ResponseEntity.ok(connected);

        } catch (Exception e) {
            log.error("Error checking territory connection in game {}: {}", gameCode, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // Validaciones

    /**
     * Valida que el juego esté en un estado que permita fortificación.
     */
    private void validateGameStateForFortification(Game game) {
        if (!isGameStateValidForFortification(game)) {
            throw new IllegalStateException("Fortification not allowed in current game state: " + game.getState());
        }
    }

    /**
     * Verifica si el estado del juego permite fortificación.
     */
    private boolean isGameStateValidForFortification(Game game) {
        switch (game.getState()) {
            case HOSTILITY_ONLY:
            case NORMAL_PLAY:
                return true;
            default:
                return false;
        }
    }

    /**
     * Valida que sea el turno del jugador y que esté en fase de fortificación.
     */
    private void validatePlayerTurn(Game game, Long playerId) {
        if (!gameStateService.isPlayerTurn(game, playerId)) {
            throw new IllegalStateException("It's not player's turn to fortify");
        }

        if (!gameStateService.canPerformAction(game, "fortify")) {
            throw new IllegalStateException("Cannot fortify in current turn phase: " + game.getCurrentPhase());
        }
    }
}
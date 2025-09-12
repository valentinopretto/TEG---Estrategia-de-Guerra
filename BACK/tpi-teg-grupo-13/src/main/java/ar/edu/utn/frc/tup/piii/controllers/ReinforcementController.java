package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementRequestDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.ReinforcementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para manejar la fase de refuerzo durante el juego normal.
 * Permite a los jugadores colocar ejércitos de refuerzo en sus territorios.
 */
@RestController
@RequestMapping("/api/games/{gameCode}/reinforcement")
@Tag(name = "Reinforcement", description = "Gestión de la fase de refuerzo durante el juego normal")
@Slf4j
public class ReinforcementController {

    @Autowired
    private ReinforcementService reinforcementService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameMapper gameMapper;

    /**
     * Permite a un jugador colocar sus ejércitos de refuerzo en sus territorios.
     *
     * POST /api/games/{gameCode}/reinforcement/place-armies
     *
     * @param gameCode Código del juego
     * @param dto Datos de colocación (playerId y armiesByCountry)
     * @return Estado actualizado del juego
     */
    @PostMapping("/place-armies")
    @Operation(
            summary = "Colocar ejércitos de refuerzo",
            description = "Permite a un jugador colocar sus ejércitos de refuerzo en los territorios que controla durante la fase de refuerzo del juego normal"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ejércitos colocados exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Colocación inválida: ejércitos insuficientes, territorios no pertenecen al jugador, o fase incorrecta"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida o jugador no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No es el turno del jugador para colocar ejércitos"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<GameResponseDto> placeReinforcementArmies(
            @PathVariable String gameCode,
            @Valid @RequestBody ReinforcementRequestDto dto) {

        log.info("Reinforcement request for game {} by player {}", gameCode, dto.getPlayerId());

        try {
            // Validar datos básicos del DTO
            if (dto.getPlayerId() == null) {
                log.warn("Player ID is null in reinforcement request");
                return ResponseEntity.badRequest().build();
            }

            if (dto.getArmiesByCountry() == null || dto.getArmiesByCountry().isEmpty()) {
                log.warn("No army placements provided in reinforcement request");
                return ResponseEntity.badRequest().build();
            }

            // Procesar la colocación de refuerzos
            reinforcementService.placeReinforcementArmies(gameCode, dto.getPlayerId(), dto.getArmiesByCountry());

            // Obtener el estado actualizado del juego
            Game updatedGame = gameService.findByGameCode(gameCode);
            GameResponseDto response = gameMapper.toResponseDto(updatedGame);

            log.info("Reinforcement completed successfully for player {} in game {}",
                    dto.getPlayerId(), gameCode);
            return ResponseEntity.ok(response);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", gameCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PlayerNotFoundException e) {
            log.error("Player not found: {}", dto.getPlayerId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid reinforcement placement: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (InvalidGameStateException e) {
            log.error("Invalid game state for reinforcement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Unexpected error during reinforcement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el estado de refuerzo para un jugador específico.
     * Incluye información sobre cuántos ejércitos puede colocar y por qué.
     *
     * GET /api/games/{gameCode}/reinforcement/status/{playerId}
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Estado de refuerzo del jugador
     */
    @GetMapping("/status/{playerId}")
    @Operation(
            summary = "Obtener estado de refuerzo",
            description = "Devuelve el estado actual de la fase de refuerzo para un jugador: ejércitos disponibles, bonus por continentes, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de refuerzo obtenido",
                    content = @Content(schema = @Schema(implementation = ReinforcementStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida o jugador no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<ReinforcementStatusDto> getReinforcementStatus(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.debug("Getting reinforcement status for player {} in game {}", playerId, gameCode);

        try {
            ReinforcementStatusDto status = reinforcementService.getReinforcementStatus(gameCode, playerId);
            return ResponseEntity.ok(status);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", gameCode);
            return ResponseEntity.notFound().build();
        } catch (PlayerNotFoundException e) {
            log.error("Player not found: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting reinforcement status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calcula cuántos ejércitos de refuerzo recibiría un jugador.
     * Útil para mostrar la información antes de que sea su turno.
     *
     * GET /api/games/{gameCode}/reinforcement/calculate/{playerId}
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Cálculo de refuerzos
     */
    @GetMapping("/calculate/{playerId}")
    @Operation(
            summary = "Calcular ejércitos de refuerzo",
            description = "Calcula cuántos ejércitos de refuerzo recibiría un jugador basado en territorios y continentes controlados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cálculo realizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ReinforcementCalculationDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida o jugador no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<ReinforcementCalculationDto> calculateReinforcements(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        log.debug("Calculating reinforcements for player {} in game {}", playerId, gameCode);

        try {
            Game game = gameService.findByGameCode(gameCode);
            ReinforcementStatusDto status = reinforcementService.getReinforcementStatus(gameCode, playerId);

            // Crear DTO simplificado solo con el cálculo
            ReinforcementCalculationDto calculation = ReinforcementCalculationDto.builder()
                    .playerId(playerId)
                    .playerName(status.getPlayerName())
                    .territoryCount(status.getOwnedTerritories().size())
                    .baseArmies(status.getBaseArmies())
                    .continentBonus(status.getContinentBonus())
                    .cardBonus(status.getCardBonus())
                    .totalArmies(status.getBaseArmies() + status.getContinentBonus() + status.getCardBonus())
                    .controlledContinents(status.getControlledContinents())
                    .build();

            return ResponseEntity.ok(calculation);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", gameCode);
            return ResponseEntity.notFound().build();
        } catch (PlayerNotFoundException e) {
            log.error("Player not found: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error calculating reinforcements: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DTO interno para el cálculo de refuerzos
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReinforcementCalculationDto {
        private Long playerId;
        private String playerName;
        private Integer territoryCount;
        private Integer baseArmies;
        private Integer continentBonus;
        private Integer cardBonus;
        private Integer totalArmies;
        private List<String> controlledContinents;
    }
}
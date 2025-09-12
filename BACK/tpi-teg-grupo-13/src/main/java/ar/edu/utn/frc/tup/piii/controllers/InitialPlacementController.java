package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.InitialArmyPlacementDto;
import ar.edu.utn.frc.tup.piii.dtos.game.InitialPlacementSummaryDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerInitialInfoDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerTerritoriesDto;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.CountryMapper;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.service.impl.InitialPlacementServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games/{gameCode}/initial-placement")
@Tag(name = "Initial Placement", description = "Gestión de la colocación inicial de ejércitos al comenzar una partida")
public class InitialPlacementController {

    @Autowired
    private InitialPlacementServiceImpl initialPlacementService;
    @Autowired
    private GameTerritoryService gameTerritoryService;
    @Autowired
    private GameService gameService;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private CountryMapper countryMapper;

    /**
     * Permite a un jugador colocar sus ejércitos iniciales.
     *
     * POST /api/games/{gameCode}/initial-placement/place-armies
     *
     * @param gameCode Código del juego
     * @param dto Datos de colocación (playerId y armiesByCountry)
     * @return Estado actualizado del juego
     */
    @PostMapping("/place-armies")
    @Operation(
            summary = "Colocar ejércitos iniciales",
            description = "Permite a un jugador colocar sus ejércitos iniciales en los territorios asignados durante la fase de setup"
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
    public ResponseEntity<GameResponseDto> placeInitialArmies(
            @PathVariable String gameCode,
            @Valid @RequestBody InitialArmyPlacementDto dto) {

        try {
            // Validar que el DTO tenga los datos necesarios
            if (dto.getPlayerId() == null) {
                return ResponseEntity.badRequest().build();
            }

            if (dto.getArmiesByCountry() == null || dto.getArmiesByCountry().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Procesar la colocación a través del servicio especializado
            initialPlacementService.placeInitialArmies(gameCode, dto.getPlayerId(), dto.getArmiesByCountry());

            // Obtener el estado actualizado del juego
            Game updatedGame = gameService.findByGameCode(gameCode);
            GameResponseDto response = gameMapper.toResponseDto(updatedGame);

            return ResponseEntity.ok(response);

        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException | InvalidGameStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el estado actual de la colocación inicial.
     *
     * GET /api/games/{gameCode}/initial-placement/status
     *
     * @param gameCode Código del juego
     * @return Estado de la colocación inicial
     */
    @GetMapping("/status")
    @Operation(
            summary = "Obtener estado de colocación inicial",
            description = "Devuelve el estado actual de la fase de colocación inicial: jugador actual, ejércitos pendientes, fase, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de colocación inicial obtenido",
                    content = @Content(schema = @Schema(implementation = InitialPlacementServiceImpl.InitialPlacementStatus.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<InitialPlacementServiceImpl.InitialPlacementStatus> getPlacementStatus(
            @PathVariable String gameCode) {

        try {
            InitialPlacementServiceImpl.InitialPlacementStatus status =
                    initialPlacementService.getPlacementStatus(gameCode);

            return ResponseEntity.ok(status);

        } catch (GameNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el estado específico de un jugador en la colocación inicial.
     *
     * GET /api/games/{gameCode}/initial-placement/player/{playerId}
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Estado específico del jugador
     */
    @GetMapping("/player/{playerId}")
    @Operation(
            summary = "Obtener estado del jugador en colocación inicial",
            description = "Devuelve información específica de un jugador durante la fase de colocación inicial"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del jugador obtenido",
                    content = @Content(schema = @Schema(implementation = InitialPlacementServiceImpl.PlayerInitialStatus.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Jugador o partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<InitialPlacementServiceImpl.PlayerInitialStatus> getPlayerStatus(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        try {
            InitialPlacementServiceImpl.PlayerInitialStatus status =
                    initialPlacementService.getPlayerStatus(gameCode, playerId);

            return ResponseEntity.ok(status);

        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene los territorios que posee un jugador específico.
     *
     * GET /api/games/{gameCode}/initial-placement/player/{playerId}/territories
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Lista de territorios que posee el jugador
     */
    @GetMapping("/player/{playerId}/territories")
    @Operation(
            summary = "Obtener territorios del jugador",
            description = "Lista todos los territorios que posee un jugador específico durante la colocación inicial, incluyendo ejércitos por colocar"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Territorios del jugador obtenidos",
                    content = @Content(schema = @Schema(implementation = PlayerTerritoriesDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Jugador o partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<PlayerTerritoriesDto> getPlayerTerritories(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Obtener territorios del jugador
            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);

            // Obtener información del jugador para la fase actual
            InitialPlacementServiceImpl.PlayerInitialStatus playerStatus =
                    initialPlacementService.getPlayerStatus(gameCode, playerId);

            PlayerTerritoriesDto response = PlayerTerritoriesDto.builder()
                    .playerId(playerId)
                    .playerName(playerStatus.getPlayerName())
                    .armiesToPlace(playerStatus.getArmiesToPlace())
                    .expectedArmiesThisRound(playerStatus.getExpectedArmiesThisRound())
                    .canPlaceArmies(playerStatus.isCanPlaceArmies())
                    .isPlayerTurn(playerStatus.isPlayerTurn())
                    .message(playerStatus.getMessage())
                    .ownedTerritories(playerTerritories.stream()
                            .map(countryMapper::mapTerritoryToDto)
                            .collect(Collectors.toList()))
                    .build();

            return ResponseEntity.ok(response);

        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el resumen completo del estado inicial del juego.
     *
     * GET /api/games/{gameCode}/initial-placement/summary
     *
     * @param gameCode Código del juego
     * @return Resumen completo del estado inicial
     */
    @GetMapping("/summary")
    @Operation(
            summary = "Obtener resumen completo de colocación inicial",
            description = "Devuelve un resumen completo del estado de la colocación inicial incluyendo todos los jugadores, territorios y progreso"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen completo obtenido",
                    content = @Content(schema = @Schema(implementation = InitialPlacementSummaryDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<InitialPlacementSummaryDto> getInitialPlacementSummary(
            @PathVariable String gameCode) {

        try {
            Game game = gameService.findByGameCode(gameCode);
            InitialPlacementServiceImpl.InitialPlacementStatus status =
                    initialPlacementService.getPlacementStatus(gameCode);

            // Obtener información de todos los jugadores
            List<PlayerInitialInfoDto> playersInfo = game.getPlayers().stream()
                    .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                    .map(player -> {
                        List<Territory> territories = gameTerritoryService.getTerritoriesByOwner(game.getId(), player.getId());
                        InitialPlacementServiceImpl.PlayerInitialStatus playerStatus =
                                initialPlacementService.getPlayerStatus(gameCode, player.getId());

                        return PlayerInitialInfoDto.builder()
                                .playerId(player.getId())
                                .playerName(player.getDisplayName())
                                .seatOrder(player.getSeatOrder())
                                .armiesToPlace(playerStatus.getArmiesToPlace())
                                .territoryCount(territories.size())
                                .isCurrentPlayer(playerStatus.isPlayerTurn())
                                .territories(territories.stream()
                                        .map(countryMapper::mapTerritoryToDto)
                                        .collect(Collectors.toList()))
                                .build();
                    })
                    .sorted(Comparator.comparing(PlayerInitialInfoDto::getSeatOrder))
                    .collect(Collectors.toList());

            InitialPlacementSummaryDto summary = InitialPlacementSummaryDto.builder()
                    .gameCode(gameCode)
                    .currentPhase(game.getState())
                    .isActive(status.isActive())
                    .message(status.getMessage())
                    .currentPlayerId(status.getCurrentPlayerId())
                    .expectedArmies(status.getExpectedArmies())
                    .players(playersInfo)
                    .build();

            return ResponseEntity.ok(summary);

        } catch (GameNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
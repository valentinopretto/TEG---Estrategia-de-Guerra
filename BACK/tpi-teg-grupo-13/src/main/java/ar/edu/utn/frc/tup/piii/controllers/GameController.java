package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.impl.GameServiceImpl;
import ar.edu.utn.frc.tup.piii.service.impl.InitialPlacementServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.InitialPlacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private InitialPlacementService initialPlacementService;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private CombatService combatService;

    @PostMapping("/create-lobby")
    @Operation(
            summary = "Crear nuevo lobby",
            description = "Crea un nuevo lobby de juego con configuraciones por defecto. El usuario se convierte automáticamente en host."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Lobby creado exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos para crear el lobby"
            )
    })
    public ResponseEntity<GameResponseDto> createLobby(@Valid @RequestBody CreateCodeDto dto) {
        if (dto.getHostUserId() == null) {
            throw new IllegalArgumentException("Debe enviar hostUserId en el CreateCodeDto");
        }

        Game createdGame = gameService.createLobbyWithDefaults(dto.getHostUserId());
        GameResponseDto response = gameMapper.toResponseDto(createdGame);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{gameCode}")
    @Operation(
            summary = "Obtener partida por código",
            description = "Recupera los datos completos de una partida usando su código único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Partida encontrada",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida no encontrada"
            )
    })
    public ResponseEntity<GameResponseDto> getGameByCode(@PathVariable String gameCode) {
        try {
            GameResponseDto game = gameService.getGameByCode(gameCode);
            return ResponseEntity.ok(game);
        } catch (GameNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/join")
    @Operation(
            summary = "Unirse a partida",
            description = "Permite a un usuario unirse a una partida existente usando el código de juego"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario unido exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede unir a la partida (llena, iniciada, etc.)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida o usuario no encontrado"
            )
    })
    public ResponseEntity<GameResponseDto> joinGame(@Valid @RequestBody JoinGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y userId en el JoinGameDto");
        }

        Game updatedGame = gameService.joinGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-bots")
    @Operation(
            summary = "Agregar bots a la partida",
            description = "Permite al host agregar bots con configuración específica a la partida"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bots agregados exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Solo el host puede agregar bots"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Configuración de bots inválida"
            )
    })
    public ResponseEntity<GameResponseDto> addBotsToGame(@Valid @RequestBody AddBotsDto dto) {
        if (dto.getGameCode() == null || dto.getRequesterId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y requesterId en el AddBotsDto");
        }

        // Validar que el requesterId es el host (se hace en el servicio)
        Game updatedGame = gameService.addBotsToGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    @Operation(
            summary = "Iniciar partida",
            description = "Permite al host iniciar la partida. Debe tener al menos 2 jugadores."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Partida iniciada exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Solo el host puede iniciar la partida"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede iniciar la partida (jugadores insuficientes, etc.)"
            )
    })
    public ResponseEntity<GameResponseDto> startGame(@Valid @RequestBody StartGameDto dto) {
        try {
            Game startedGame = gameService.startGameByHost(dto.getGameCode(), dto.getUserId());
            GameResponseDto response = gameMapper.toResponseDto(startedGame);
            return ResponseEntity.ok(response);
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{gameCode}/settings")
    @Operation(
            summary = "Actualizar configuraciones",
            description = "Permite al host actualizar las configuraciones de la partida antes de iniciarla"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuraciones actualizadas exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Solo el host puede modificar configuraciones"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Configuraciones inválidas"
            )
    })
    public ResponseEntity<GameResponseDto> updateGameSettings(
            @PathVariable String gameCode,
            @Valid @RequestBody UpdateGameSettingsDto dto) {

        if (dto.getRequesterId() == null) {
            throw new IllegalArgumentException("requesterId is required");
        }

        Game updatedGame = gameService.updateGameSettings(gameCode, dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{gameCode}")
    @Operation(
            summary = "Cancelar partida",
            description = "Permite al host cancelar una partida que no ha iniciado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Partida cancelada exitosamente"),
            @ApiResponse(responseCode = "403", description = "Solo el host puede cancelar la partida"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    public ResponseEntity<Void> cancelGame(
            @PathVariable String gameCode,
            @RequestParam String username
    ) {
        gameService.cancelGameByUsername(gameCode, username);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/kick-player")
    @Operation(
            summary = "Expulsar jugador",
            description = "Permite al host expulsar a un jugador de la partida"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Jugador expulsado exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Solo el host puede expulsar jugadores"),
            @ApiResponse(responseCode = "404", description = "Jugador o partida no encontrada")
    })
    public ResponseEntity<GameResponseDto> kickPlayer(@Valid @RequestBody KickPlayerDto dto) {
        if (dto.getGameCode() == null || dto.getPlayerId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y playerId en el KickPlayerDto");
        }

        try {
            Game updatedGame = gameService.kickPlayer(dto);
            GameResponseDto response = gameMapper.toResponseDto(updatedGame);
            return ResponseEntity.ok(response);

        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidGameStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/leave")
    @Operation(
            summary = "Salir de partida",
            description = "Permite a un jugador salir voluntariamente de la partida"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Jugador salió exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Jugador o partida no encontrada"),
            @ApiResponse(responseCode = "400", description = "No se puede salir en el estado actual")
    })
    public ResponseEntity<GameResponseDto> leaveGame(@Valid @RequestBody LeaveGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new BadRequestException("Debe enviar gameCode y userId en el LeaveGameDto");
        }

        Game game = gameService.leaveGame(dto);
        return ResponseEntity.ok(gameMapper.toResponseDto(game));
    }

    @GetMapping("/host/{userId}")
    @Operation(
            summary = "Obtener partidas donde es host",
            description = "Devuelve todas las partidas donde el usuario especificado es el host/anfitrión"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de partidas donde es host",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "ID de usuario inválido")
    })
    public ResponseEntity<List<GameResponseDto>> getHostedGames(
            @PathVariable Long userId
    ) {
        try {
            List<Game> hostedGames = gameService.findGamesByHost(userId);
            List<GameResponseDto> response = hostedGames.stream()
                    .map(gameMapper::toResponseDto)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{gameCode}/join")
    @Operation(
            summary = "Unirse al lobby",
            description = "Une un jugador a un lobby de partida existente"
    )
    public ResponseEntity<GameResponseDto> joinGameLobby(@PathVariable String gameCode, @Valid @RequestBody PlayerIdRequestDto dto) {
        try {
            Game updatedGame = gameService.joinGameLobby(gameCode, dto.getPlayerId());
            GameResponseDto response = gameMapper.toResponseDto(updatedGame);
            return ResponseEntity.ok(response);
        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException | InvalidGameStateException e) {
            return ResponseEntity.badRequest().build(); // Consider adding e.getMessage() for more info
        } catch (Exception e) {
            // Log the exception e.g. logger.error("Error joining lobby", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{gameId}/player/{playerId}/ready")
    @Operation(
            summary = "Alternar estado listo",
            description = "Cambia el estado de 'listo' de un jugador en el lobby"
    )
    public ResponseEntity<GameResponseDto> togglePlayerReady(@PathVariable String gameId, @PathVariable Long playerId) {
        try {
            Game updatedGame = gameService.togglePlayerReady(gameId, playerId);
            GameResponseDto response = gameMapper.toResponseDto(updatedGame);
            return ResponseEntity.ok(response);
        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidGameStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{gameId}/status")
    @Operation(
            summary = "Estado del lobby",
            description = "Obtiene el estado actual del lobby de la partida"
    )
    public ResponseEntity<GameResponseDto> getGameLobbyStatus(@PathVariable String gameId, @RequestParam(required = false) Long playerId) {
        try {
            Game game = gameService.getGameLobbyStatus(gameId, playerId);
            GameResponseDto response = gameMapper.toResponseDto(game);
            return ResponseEntity.ok(response);
        } catch (GameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{gameId}/resume")
    @Operation(
            summary = "Reanudar partida",
            description = "Reanuda una partida pausada"
    )
    public ResponseEntity<GameResponseDto> resumeGame(@PathVariable String gameId) {
        try {
            Game resumedGame = gameService.resumeGame(gameId);
            GameResponseDto response = gameMapper.toResponseDto(resumedGame);
            return ResponseEntity.ok(response);
        } catch (GameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidGameStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{gameId}/player/{playerId}/disconnect")
    @Operation(
            summary = "Desconectar del lobby",
            description = "Desconecta un jugador del lobby de la partida"
    )
    public ResponseEntity<GameResponseDto> disconnectFromLobby(@PathVariable String gameId, @PathVariable Long playerId) {
        try {
            Game updatedGame = gameService.disconnectFromLobby(gameId, playerId);
            GameResponseDto response = gameMapper.toResponseDto(updatedGame);
            return ResponseEntity.ok(response);
        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidGameStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
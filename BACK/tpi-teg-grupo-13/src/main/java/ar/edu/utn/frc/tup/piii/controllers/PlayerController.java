package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.player.PlayerRequestDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private UserService userService;
    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @GetMapping
    @Operation(
            summary = "Listar todos los jugadores",
            description = "Devuelve todos los jugadores registrados en el sistema (humanos y bots). Endpoint administrativo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de jugadores obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Player.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado - permisos administrativos requeridos"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<List<Player>> getAllPlayers() {
        try {
            List<Player> players = playerService.findAll();
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener jugador por ID",
            description = "Recupera la información completa de un jugador específico incluyendo estadísticas, territorios y estado actual"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Jugador encontrado",
                    content = @Content(schema = @Schema(implementation = Player.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Jugador no encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de jugador inválido"
            )
    })
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        try {
            Player player = playerService.findById(id).orElse(null);
            if (player == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(player);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(
            summary = "Crear nuevo jugador",
            description = "Crea un nuevo jugador en el sistema, puede ser humano (asociado a un usuario) o bot (con nivel de IA específico)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Jugador creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Player.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del jugador inválidos (usuario no existe, juego lleno, etc.)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario o partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuario ya participa en esta partida"
            )
    })
    public ResponseEntity<Player> createPlayer(@RequestBody PlayerRequestDto request) {
        try {
            User user = userService.getUserById(request.getUserId());
            Game game = gameService.findById(request.getGameId());

            Player player;

            if (Boolean.TRUE.equals(request.getIsBot())) {
                player = playerService.createBotPlayer(BotLevel.valueOf(request.getBotLevel()), game);
            } else {
                player = playerService.createHumanPlayer(user, game, request.getSeatOrder());
            }

            return ResponseEntity.status(201).body(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar jugador",
            description = "Elimina un jugador del sistema. Solo permitido si no está en partidas activas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Jugador eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Jugador no encontrado"),
            @ApiResponse(responseCode = "409", description = "Jugador en partida activa, no se puede eliminar"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar este jugador")
    })
    public ResponseEntity<Void> deletePlayer(
            @PathVariable Long id
    ) {
        try {
            playerService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

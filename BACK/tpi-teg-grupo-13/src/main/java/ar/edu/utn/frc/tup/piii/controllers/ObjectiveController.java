package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.objective.WinnerDto;
import ar.edu.utn.frc.tup.piii.model.Objective;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.service.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
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
@RequestMapping("/api/objectives")
public class ObjectiveController {

    @Autowired
    private ObjectiveService objectiveService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @GetMapping
    @Operation(
            summary = "Listar todos los objetivos",
            description = "Devuelve todos los objetivos disponibles en el sistema. Endpoint administrativo para ver las misiones posibles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de objetivos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Objective.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<List<Objective>> getAllObjectives() {
        try {
            List<Objective> objectives = objectiveService.findAll();
            return ResponseEntity.ok(objectives);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener objetivo por ID",
            description = "Recupera un objetivo específico por su ID único. Útil para mostrar detalles de misiones."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Objetivo encontrado",
                    content = @Content(schema = @Schema(implementation = Objective.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Objetivo no encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de objetivo inválido"
            )
    })
    public ResponseEntity<Objective> getObjectiveById(@PathVariable Long id) {
        try {
            Objective objective = objectiveService.findById(id).orElse(null);
            if (objective == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(objective);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(
            summary = "Crear nuevo objetivo",
            description = "Crea un nuevo objetivo secreto en el sistema. Solo para administradores del juego."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Objetivo creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Objective.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del objetivo inválidos"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para crear objetivos"
            )
    })
    public ResponseEntity<Objective> createObjective(@RequestBody Objective objective) {
        try {
            Objective createdObjective = objectiveService.save(objective);
            return ResponseEntity.status(201).body(createdObjective);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar objetivo",
            description = "Elimina un objetivo del sistema. Solo para administradores. No se puede eliminar si está siendo usado en partidas activas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Objetivo eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Objetivo no encontrado"),
            @ApiResponse(responseCode = "409", description = "Objetivo en uso, no se puede eliminar"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar objetivos")
    })
    public void deleteObjective(@PathVariable Long id) {
        objectiveService.deleteById(id);
    }

    @PostMapping("/assign/{gameId}")
    @Operation(
            summary = "Asignar objetivos a jugadores",
            description = "Asigna objetivos secretos aleatorios a todos los jugadores de una partida. Se ejecuta automáticamente al iniciar el juego."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Objetivos asignados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
            @ApiResponse(responseCode = "400", description = "Partida en estado inválido para asignar objetivos"),
            @ApiResponse(responseCode = "409", description = "Objetivos ya asignados en esta partida")
    })
    public ResponseEntity<String> assignObjectives(
            @PathVariable Long gameId
    ) {
        try {
            Game game = gameService.findById(gameId);
            if (game == null) {
                return ResponseEntity.notFound().build();
            }

            objectiveService.assignObjectivesToPlayers(game);
            return ResponseEntity.ok("Objectives assigned successfully to all players");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/validate")
    @Operation(
            summary = "Validar cumplimiento de objetivo",
            description = "Verifica si un jugador ha cumplido su objetivo secreto en el estado actual del juego"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validación completada",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(responseCode = "404", description = "Objetivo, partida o jugador no encontrado"),
            @ApiResponse(responseCode = "400", description = "Parámetros de validación inválidos")
    })
    public ResponseEntity<Boolean> validateObjective(
            @RequestParam Long objectiveId,
            @RequestParam Long gameId,
            @RequestParam Long playerId) {
        try {
            Game game = gameService.findById(gameId);
            Player player = playerService.findById(playerId).orElse(null);

            if (game == null || player == null) {
                return ResponseEntity.notFound().build();
            }

            boolean isAchieved = objectiveService.isObjectiveAchieved(objectiveId, game, player);
            return ResponseEntity.ok(isAchieved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/winner")
    @Operation(
            summary = "Obtener ganador de la partida",
            description = "Determina si hay un ganador en la partida verificando el cumplimiento de objetivos secretos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación de ganador completada",
                    content = @Content(schema = @Schema(implementation = WinnerDto.class))
            ),
            @ApiResponse(responseCode = "204", description = "No hay ganador aún"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
            @ApiResponse(responseCode = "400", description = "ID de partida inválido")
    })
    public ResponseEntity<WinnerDto> getWinner(@RequestParam Long gameId) {
        try {
            Game game = gameService.findById(gameId);
            if (game == null) {
                return ResponseEntity.notFound().build();
            }

            return objectiveService.findWinner(game)
                    .map(player -> ResponseEntity.ok(WinnerDto.builder()
                            .playerId(player.getId())
                            .playerName(player.getDisplayName())
                            .objectiveDescription(player.getObjective().getDescription())
                            .build()))
                    .orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

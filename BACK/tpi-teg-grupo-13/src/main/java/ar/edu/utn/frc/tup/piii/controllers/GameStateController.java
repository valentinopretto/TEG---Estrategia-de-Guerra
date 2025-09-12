package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameStateResponse;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.impl.GameStateServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/{gameId}/state")
public class GameStateController {

    @Autowired
    private GameStateServiceImpl stateService;

    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    @Operation(
            summary = "Iniciar juego",
            description = "Inicia una partida que está en estado de lobby. Distribuye territorios, asigna objetivos y comienza la fase inicial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego iniciado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede iniciar el juego (jugadores insuficientes, ya iniciado, etc.)"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para iniciar la partida")
    })
    public ResponseEntity<String> startGame(@PathVariable Long gameId) {
        try {
            Game game = gameService.findById(gameId);
            if (game == null) {
                return ResponseEntity.notFound().build();
            }

            if (stateService.startGame(game)) {
                gameService.save(game);
                return ResponseEntity.ok("Game started successfully");
            }

            return ResponseEntity.badRequest().body("Cannot start game");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error starting game: " + e.getMessage());
        }
    }

    @PostMapping("/pause")
    @Operation(
            summary = "Pausar juego",
            description = "Pausa una partida en curso. Los jugadores no pueden realizar acciones hasta que se reanude."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego pausado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede pausar el juego (no está en curso, ya pausado, etc.)"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para pausar la partida")
    })
    public ResponseEntity<String> pauseGame(@PathVariable Long gameId) {
        try {
            Game game = gameService.findById(gameId);
            if (game == null) {
                return ResponseEntity.notFound().build();
            }

            if (stateService.pauseGame(game)) {
                gameService.save(game);
                return ResponseEntity.ok("Game paused");
            }

            return ResponseEntity.badRequest().body("Cannot pause game");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error pausing game: " + e.getMessage());
        }
    }

    @PostMapping("/resume")
    @Operation(
            summary = "Reanudar juego",
            description = "Reanuda una partida que estaba pausada. Los jugadores pueden continuar donde se quedaron."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego reanudado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede reanudar el juego (no está pausado, finalizado, etc.)"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para reanudar la partida")
    })
    public ResponseEntity<String> resumeGame(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        if (stateService.resumeGame(game)) {
            gameService.save(game);
            return ResponseEntity.ok("Game resumed");
        }

        return ResponseEntity.badRequest().body("Cannot resume game");
    }

    @PostMapping("/finish")
    @Operation(
            summary = "Finalizar juego",
            description = "Finaliza una partida en curso. Determina el ganador y actualiza estadísticas finales."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego finalizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede finalizar el juego (no está en curso, ya finalizado, etc.)"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para finalizar la partida")
    })
    public ResponseEntity<String> finishGame(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        if (stateService.finishGame(game)) {
            gameService.save(game);
            return ResponseEntity.ok("Game finished");
        }

        return ResponseEntity.badRequest().body("Cannot finish game");
    }

    @PostMapping("/action/{action}")
    @Operation(
            summary = "Ejecutar acción de estado",
            description = "Ejecuta una acción específica de cambio de estado o fase del juego"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acción ejecutada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Acción no permitida en el estado actual o acción inválida"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    public ResponseEntity<String> performAction(
            @PathVariable Long gameId,
            @PathVariable String action) {

        try {
            String result = stateService.executeGameAction(gameId, action);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/current")
    @Operation(
            summary = "Obtener estado actual",
            description = "Devuelve información completa sobre el estado actual del juego, fase, turno y acciones disponibles"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actual obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = GameStateResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    public ResponseEntity<GameStateResponse> getCurrentState(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        GameStateResponse response = new GameStateResponse();
        response.setGameState(game.getState());
        response.setTurnPhase(game.getCurrentPhase());
        response.setCurrentPlayer(game.getCurrentPlayer() != null ?
                game.getCurrentPlayer().getDisplayName() : "No current player");
        response.setCurrentTurn(game.getCurrentTurn());
        response.setAvailableActions(stateService.getAvailableActions(game));
        response.setPhaseDescription(stateService.getCurrentPhaseDescription(game));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/can-perform/{action}")
    @Operation(
            summary = "Verificar si se puede realizar acción",
            description = "Verifica si una acción específica se puede realizar en el estado actual del juego"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación completada",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    public ResponseEntity<Boolean> canPerformAction(
            @PathVariable Long gameId,
            @PathVariable String action) {

        Game game = gameService.findById(gameId);
        boolean canPerform = stateService.canPerformAction(game, action);

        return ResponseEntity.ok(canPerform);
    }

}
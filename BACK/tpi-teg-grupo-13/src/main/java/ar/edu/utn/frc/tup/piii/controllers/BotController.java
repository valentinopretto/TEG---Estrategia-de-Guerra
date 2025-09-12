package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.service.interfaces.BotService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador unificado para gestión de bots.
 * Incluye ejecución de turnos, consulta de estados y gestión de perfiles de bots.
 */
@RestController
@RequestMapping("/api/bots")
@Tag(name = "Bot Management", description = "Gestión completa de bots: turnos, estados y perfiles")
@Slf4j
public class BotController {

    private final BotService botService;
    private final GameService gameService;
    private final PlayerService playerService;
    private final GameStateService gameStateService;
    private final GameMapper gameMapper;
    private final PlayerMapper playerMapper;

    private static final Logger logger = LoggerFactory.getLogger(BotController.class);


    @Autowired
    public BotController(BotService botService, GameService gameService, PlayerService playerService,
                         GameStateService gameStateService, GameMapper gameMapper, PlayerMapper playerMapper
                         ) {
        this.botService = botService;
        this.gameService = gameService;
        this.playerService = playerService;
        this.gameStateService = gameStateService;
        this.gameMapper = gameMapper;
        this.playerMapper = playerMapper;
    }

    /**
     * ENDPOINT DE PRUEBA - Para verificar que el controller funciona
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("🟢 TEST ENDPOINT FUNCIONANDO");
        return ResponseEntity.ok("Bot Controller funcionando correctamente");
    }

    /**
     * ENDPOINT DE PRUEBA CON PARÁMETROS
     */
    @GetMapping("/test/{gameCode}/{botId}")
    public ResponseEntity<String> testWithParams(
            @PathVariable String gameCode,
            @PathVariable Long botId) {
        System.out.println("🟢 TEST CON PARÁMETROS - gameCode: " + gameCode + ", botId: " + botId);
        return ResponseEntity.ok("Parámetros recibidos: " + gameCode + ", " + botId);
    }

    /**
     * Ejecuta el turno completo de un bot (Refuerzo -> Ataque -> Fortificación).
     * Los bots siempre ejecutan todas las fases automáticamente.
     *
     * @param gameCode Código del juego
     * @param botId ID del bot que debe ejecutar su turno
     * @return Estado actualizado del juego después del turno del bot
     */
    @PostMapping("/games/{gameCode}/{botId}/execute-turn")
    @Operation(summary = "Ejecutar turno completo del bot",
            description = "El bot ejecuta automáticamente todas las fases: refuerzo, ataque y fortificación")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Turno del bot ejecutado exitosamente",
                    content = @Content(schema = @Schema(implementation = GameResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No es el turno del bot, juego en estado inválido, o bot no encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida o bot no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Bot no puede ejecutar turno en el estado actual del juego"
            )
    })
    public ResponseEntity<?> executeBotTurn(
            @PathVariable String gameCode,
            @PathVariable Long botId,
            HttpServletRequest request) {
        // LOGS MÚLTIPLES PARA ASEGURAR VISIBILIDAD
        System.out.println("=".repeat(80));
        System.out.println("🚨 EJECUTANDO TURNO DEL BOT");
        System.out.println("🎯 GameCode: " + gameCode);
        System.out.println("🤖 BotId: " + botId);
        System.out.println("📡 URL: " + request.getRequestURL());
        System.out.println("🔧 Method: " + request.getMethod());
        System.out.println("=".repeat(80));
        // También usar logger
        log.info("🚨 EJECUTANDO TURNO DEL BOT - gameCode: {}, botId: {}", gameCode, botId);

        try {
            // Validaciones paso a paso con logs
            System.out.println("1️⃣ Validando parámetros...");
            if (gameCode == null || gameCode.trim().isEmpty()) {
                System.out.println("❌ GameCode inválido: " + gameCode);
                return ResponseEntity.badRequest().body("GameCode inválido");
            }
            System.out.println("4️⃣ Llamando al servicio...");

            GameResponseDto result = botService.executeBotTurnComplete(gameCode, botId);

            log.info("✅ Turno del bot ejecutado correctamente para botId={} en juego={}", botId, gameCode);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ IllegalArgumentException: " + e.getMessage());
            log.warn("❌ IllegalArgumentException en executeBotTurn para botId={}, gameCode={}: {}", botId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("❌ IllegalStateException en executeBotTurn para botId={}, gameCode={}: {}", botId, gameCode, e.getMessage());
            System.out.println("❌EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE IllegalStateException: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e) {
            log.error("❌ Error inesperado ejecutando turno del bot - botId={}, gameCode={}", botId, gameCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error inesperado");
        }

    }

    /**
     * Obtiene información específica de un bot en el juego.
     *
     * @param gameCode Código del juego
     * @param botId ID del bot
     * @return Información del bot y su estado en el juego
     */
    @GetMapping("/games/{gameCode}/{botId}/status")
    @Operation(summary = "Obtener estado del bot en juego",
            description = "Devuelve información sobre el bot y su estado actual en el juego específico")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del bot obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = BotStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de bot inválido o no es un bot"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bot o partida no encontrada"
            )
    })
    public ResponseEntity<BotStatusDto> getBotStatus(
            @PathVariable String gameCode,
            @PathVariable Long botId) {

        try {
            Optional<Player> botPlayerOpt = playerService.findById(botId);
            if (botPlayerOpt.isEmpty() || !botPlayerOpt.get().getIsBot()) {
                return ResponseEntity.badRequest().build();
            }

            PlayerEntity botPlayer = playerMapper.toEntity(botPlayerOpt.orElseThrow());
            Game game = gameService.findByGameCode(gameCode);

            BotStatusDto status = BotStatusDto.builder()
                    .botId(botId)
                    .botName(botPlayer.getBotProfile().getBotName())
                    .level(botPlayer.getBotProfile().getLevel())
                    .strategy(botPlayer.getBotProfile().getStrategy())
                    .isCurrentTurn(gameStateService.isPlayerTurn(game, botId))
                    .territoriesCount((int)botPlayer.getTerritories().stream().count())
                    .armiesCount(botPlayer.getArmiesToPlace())
                    .isActive(botPlayer.getStatus().equals(ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus.ACTIVE))
                    .canExecuteTurn(gameStateService.isPlayerTurn(game, botId) &&
                            gameStateService.canPerformAction(game, "bot_turn"))
                    .build();

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error getting bot status for bot {} in game {}: {}", botId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista todos los bots activos en el juego.
     *
     * @param gameCode Código del juego
     * @return Lista de bots en el juego
     */
    @GetMapping("/games/{gameCode}")
    @Operation(summary = "Listar bots del juego",
            description = "Devuelve todos los bots participantes en el juego específico")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de bots obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = BotStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Código de partida inválido"
            )
    })
    public ResponseEntity<List<BotStatusDto>> getGameBots(@PathVariable String gameCode) {
        try {
            Game game = gameService.findByGameCode(gameCode);

            List<BotStatusDto> bots = game.getPlayers().stream()
                    .filter(Player::getIsBot)
                    .map(botPlayer -> BotStatusDto.builder()
                            .botId(botPlayer.getId())
                            .botName(botPlayer.getDisplayName())
                            .level(botPlayer.getBotLevel())
                            .strategy(BotStrategy.AGGRESSIVE)
                            .isCurrentTurn(gameStateService.isPlayerTurn(game, botPlayer.getId()))
                            .territoriesCount(botPlayer.getTerritoryCount())
                            .armiesCount(botPlayer.getArmiesToPlace())
                            .isActive(botPlayer.getStatus().equals(ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus.ACTIVE))
                            .canExecuteTurn(gameStateService.isPlayerTurn(game, botPlayer.getId()) &&
                                    gameStateService.canPerformAction(game, "bot_turn"))
                            .build())
                    .toList();

            return ResponseEntity.ok(bots);

        } catch (Exception e) {
            log.error("Error getting bots for game {}: {}", gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== ENDPOINTS PARA PERFILES DE BOTS =====

    /**
     * Lista perfiles de bots por nivel.
     *
     * @param level Nivel de dificultad del bot
     * @return Lista de perfiles de bots del nivel especificado
     */
    @GetMapping("/profiles")
    @Operation(summary = "Listar perfiles de bots por nivel",
            description = "Devuelve todos los perfiles de bots disponibles de un nivel específico")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfiles de bots obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = BotProfileEntity.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nivel de bot inválido"
            )
    })
    public ResponseEntity<List<BotProfileEntity>> getBotsByLevel(@RequestParam BotLevel level) {
        try {
            List<BotProfileEntity> botProfiles = botService.findByLevel(level);
            return ResponseEntity.ok(botProfiles);
        } catch (Exception e) {
            log.error("Error getting bot profiles for level {}: {}", level, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene todos los perfiles de bots disponibles.
     *
     * @return Lista completa de perfiles de bots
     */
    @GetMapping("/profiles/all")
    @Operation(summary = "Listar todos los perfiles de bots",
            description = "Devuelve todos los perfiles de bots disponibles")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Todos los perfiles de bots obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = BotProfileEntity.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<List<BotProfileEntity>> getAllBotProfiles() {
        try {
            List<BotProfileEntity> botProfiles = botService.findAll();
            return ResponseEntity.ok(botProfiles);
        } catch (Exception e) {
            log.error("Error getting all bot profiles: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene un perfil de bot específico por ID.
     *
     * @param profileId ID del perfil del bot
     * @return Perfil del bot
     */
    @GetMapping("/profiles/{profileId}")
    @Operation(summary = "Obtener perfil de bot por ID",
            description = "Devuelve el perfil específico de un bot")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil del bot obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = BotProfileEntity.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Perfil de bot no encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de perfil inválido"
            )
    })
    public ResponseEntity<BotProfileEntity> getBotProfile(@PathVariable Long profileId) {
        try {
            Optional<BotProfileEntity> botProfile = botService.findById(profileId);
            return botProfile.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting bot profile {}: {}", profileId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BotStatusDto {
        private Long botId;
        private String botName;
        private BotLevel level;
        private ar.edu.utn.frc.tup.piii.model.enums.BotStrategy strategy;
        private Boolean isCurrentTurn;
        private Integer territoriesCount;
        private Integer armiesCount;
        private Boolean isActive;
        private Boolean canExecuteTurn;
    }
}
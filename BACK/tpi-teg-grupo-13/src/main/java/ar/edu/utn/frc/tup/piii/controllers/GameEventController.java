package ar.edu.utn.frc.tup.piii.controllers;
import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.entities.GameEventEntity;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import ar.edu.utn.frc.tup.piii.service.interfaces.IGameEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/games/{gameId}/events")
@CrossOrigin(origins = "*")
@Slf4j
public class GameEventController {

    private final IGameEventService gameEventService;

    public GameEventController(IGameEventService gameEventService) {
        this.gameEventService = gameEventService;
    }

    /**
     * Obtiene el historial completo de eventos de una partida
     * GET /api/games/{gameId}/events
     */
    @GetMapping
    public ResponseEntity<List<GameEventDto>> getGameHistory(@PathVariable Long gameId) {
        try {
            List<GameEventDto> events = gameEventService.getFormattedGameHistory(gameId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting game history for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene eventos de un jugador específico en una partida
     * GET /api/games/{gameId}/events/player/{playerId}
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<GameEventEntity>> getPlayerEvents(
            @PathVariable Long gameId,
            @PathVariable Long playerId) {
        try {
            List<GameEventEntity> events = gameEventService.getPlayerEventsInGame(gameId, playerId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting player events for game {} and player {}: {}", gameId, playerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene eventos filtrados por tipo
     * GET /api/games/{gameId}/events/type/{eventType}
     */
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<GameEventEntity>> getEventsByType(
            @PathVariable Long gameId,
            @PathVariable EventType eventType) {
        try {
            List<GameEventEntity> events = gameEventService.getEventsByType(gameId, eventType);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting events by type {} for game {}: {}", eventType, gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene eventos de un turno específico
     * GET /api/games/{gameId}/events/turn/{turnNumber}
     */
    @GetMapping("/turn/{turnNumber}")
    public ResponseEntity<List<GameEventEntity>> getEventsByTurn(
            @PathVariable Long gameId,
            @PathVariable Integer turnNumber) {
        try {
            List<GameEventEntity> events = gameEventService.getEventsByTurn(gameId, turnNumber);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting events for turn {} in game {}: {}", turnNumber, gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene eventos recientes de una partida
     * GET /api/games/{gameId}/events/recent?hours=24
     */
    @GetMapping("/recent")
    public ResponseEntity<List<GameEventEntity>> getRecentEvents(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<GameEventEntity> events = gameEventService.getRecentGameEvents(gameId, hours);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting recent events for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas de eventos de una partida
     * GET /api/games/{gameId}/events/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGameEventStats(@PathVariable Long gameId) {
        try {
            Map<String, Object> stats = gameEventService.getGameEventStats(gameId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting event stats for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el último evento de un tipo específico
     * GET /api/games/{gameId}/events/last/{eventType}
     */
    @GetMapping("/last/{eventType}")
    public ResponseEntity<GameEventEntity> getLastEventByType(
            @PathVariable Long gameId,
            @PathVariable EventType eventType) {
        try {
            Optional<GameEventEntity> event = gameEventService.getLastEventByType(gameId, eventType);
            if (event.isPresent()) {
                return ResponseEntity.ok(event.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting last event of type {} for game {}: {}", eventType, gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cuenta los eventos de un tipo específico
     * GET /api/games/{gameId}/events/count/{eventType}
     */
    @GetMapping("/count/{eventType}")
    public ResponseEntity<Long> countEventsByType(
            @PathVariable Long gameId,
            @PathVariable EventType eventType) {
        try {
            Long count = gameEventService.countEventsByType(gameId, eventType);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting events of type {} for game {}: {}", eventType, gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todos los ataques de un jugador en una partida
     * GET /api/games/{gameId}/events/player/{playerId}/attacks
     */
    @GetMapping("/player/{playerId}/attacks")
    public ResponseEntity<List<GameEventEntity>> getPlayerAttacks(
            @PathVariable Long gameId,
            @PathVariable Long playerId) {
        try {
            List<GameEventEntity> attacks = gameEventService.getPlayerAttacks(gameId, playerId);
            return ResponseEntity.ok(attacks);
        } catch (Exception e) {
            log.error("Error getting attacks for player {} in game {}: {}", playerId, gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las conquistas de un jugador en una partida
     * GET /api/games/{gameId}/events/player/{playerId}/conquests
     */
    @GetMapping("/player/{playerId}/conquests")
    public ResponseEntity<List<GameEventEntity>> getPlayerConquests(
            @PathVariable Long gameId,
            @PathVariable Long playerId) {
        try {
            List<GameEventEntity> conquests = gameEventService.getPlayerConquests(gameId, playerId);
            return ResponseEntity.ok(conquests);
        } catch (Exception e) {
            log.error("Error getting conquests for player {} in game {}: {}", playerId, gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ENDPOINTS PARA REGISTRAR EVENTOS =====

    /**
     * Registra una conquista de territorio
     * POST /api/games/{gameId}/events/territory-conquest
     */
    @PostMapping("/territory-conquest")
    public ResponseEntity<GameEventEntity> recordTerritoryConquest(
            @PathVariable Long gameId,
            @RequestBody TerritoryConquestRequest request) {
        try {
            GameEventEntity event = gameEventService.recordTerritoryConquest(
                    gameId,
                    request.getConquererPlayerId(),
                    request.getConqueredTerritory(),
                    request.getFromPlayer(),
                    request.getTurnNumber()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (Exception e) {
            log.error("Error recording territory conquest for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Registra un ataque
     * POST /api/games/{gameId}/events/attack
     */
    @PostMapping("/attack")
    public ResponseEntity<GameEventEntity> recordAttack(
            @PathVariable Long gameId,
            @RequestBody AttackRequest request) {
        try {
            GameEventEntity event = gameEventService.recordAttack(
                    gameId,
                    request.getAttackerPlayerId(),
                    request.getFromTerritory(),
                    request.getToTerritory(),
                    request.getTurnNumber(),
                    request.isSuccessful()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (Exception e) {
            log.error("Error recording attack for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Registra inicio de turno
     * POST /api/games/{gameId}/events/turn-start
     */
    @PostMapping("/turn-start")
    public ResponseEntity<GameEventEntity> recordTurnStart(
            @PathVariable Long gameId,
            @RequestBody TurnEventRequest request) {
        try {
            GameEventEntity event = gameEventService.recordTurnStart(
                    gameId,
                    request.getPlayerId(),
                    request.getTurnNumber()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (Exception e) {
            log.error("Error recording turn start for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Registra fin de turno
     * POST /api/games/{gameId}/events/turn-end
     */
    @PostMapping("/turn-end")
    public ResponseEntity<GameEventEntity> recordTurnEnd(
            @PathVariable Long gameId,
            @RequestBody TurnEventRequest request) {
        try {
            GameEventEntity event = gameEventService.recordTurnEnd(
                    gameId,
                    request.getPlayerId(),
                    request.getTurnNumber()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (Exception e) {
            log.error("Error recording turn end for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Registra colocación de refuerzos
     * POST /api/games/{gameId}/events/reinforcements
     */
    @PostMapping("/reinforcements")
    public ResponseEntity<GameEventEntity> recordReinforcementsPlaced(
            @PathVariable Long gameId,
            @RequestBody ReinforcementsRequest request) {
        try {
            GameEventEntity event = gameEventService.recordReinforcementsPlaced(
                    gameId,
                    request.getPlayerId(),
                    request.getTerritory(),
                    request.getReinforcements(),
                    request.getTurnNumber()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (Exception e) {
            log.error("Error recording reinforcements for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Registra fortificación
     * POST /api/games/{gameId}/events/fortification
     */
    @PostMapping("/fortification")
    public ResponseEntity<GameEventEntity> recordFortification(
            @PathVariable Long gameId,
            @RequestBody FortificationRequest request) {
        try {
            GameEventEntity event = gameEventService.recordFortification(
                    gameId,
                    request.getPlayerId(),
                    request.getFromTerritory(),
                    request.getToTerritory(),
                    request.getArmies(),
                    request.getTurnNumber()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (Exception e) {
            log.error("Error recording fortification for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== CLASES REQUEST =====

    public static class TerritoryConquestRequest {
        private Long conquererPlayerId;
        private String conqueredTerritory;
        private String fromPlayer;
        private Integer turnNumber;

        // Getters y setters
        public Long getConquererPlayerId() { return conquererPlayerId; }
        public void setConquererPlayerId(Long conquererPlayerId) { this.conquererPlayerId = conquererPlayerId; }

        public String getConqueredTerritory() { return conqueredTerritory; }
        public void setConqueredTerritory(String conqueredTerritory) { this.conqueredTerritory = conqueredTerritory; }

        public String getFromPlayer() { return fromPlayer; }
        public void setFromPlayer(String fromPlayer) { this.fromPlayer = fromPlayer; }

        public Integer getTurnNumber() { return turnNumber; }
        public void setTurnNumber(Integer turnNumber) { this.turnNumber = turnNumber; }
    }

    public static class AttackRequest {
        private Long attackerPlayerId;
        private String fromTerritory;
        private String toTerritory;
        private Integer turnNumber;
        private boolean successful;

        // Getters y setters
        public Long getAttackerPlayerId() { return attackerPlayerId; }
        public void setAttackerPlayerId(Long attackerPlayerId) { this.attackerPlayerId = attackerPlayerId; }

        public String getFromTerritory() { return fromTerritory; }
        public void setFromTerritory(String fromTerritory) { this.fromTerritory = fromTerritory; }

        public String getToTerritory() { return toTerritory; }
        public void setToTerritory(String toTerritory) { this.toTerritory = toTerritory; }

        public Integer getTurnNumber() { return turnNumber; }
        public void setTurnNumber(Integer turnNumber) { this.turnNumber = turnNumber; }

        public boolean isSuccessful() { return successful; }
        public void setSuccessful(boolean successful) { this.successful = successful; }
    }

    public static class TurnEventRequest {
        private Long playerId;
        private Integer turnNumber;

        // Getters y setters
        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }

        public Integer getTurnNumber() { return turnNumber; }
        public void setTurnNumber(Integer turnNumber) { this.turnNumber = turnNumber; }
    }

    public static class ReinforcementsRequest {
        private Long playerId;
        private String territory;
        private Integer reinforcements;
        private Integer turnNumber;

        // Getters y setters
        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }

        public String getTerritory() { return territory; }
        public void setTerritory(String territory) { this.territory = territory; }

        public Integer getReinforcements() { return reinforcements; }
        public void setReinforcements(Integer reinforcements) { this.reinforcements = reinforcements; }

        public Integer getTurnNumber() { return turnNumber; }
        public void setTurnNumber(Integer turnNumber) { this.turnNumber = turnNumber; }
    }

    public static class FortificationRequest {
        private Long playerId;
        private String fromTerritory;
        private String toTerritory;
        private Integer armies;
        private Integer turnNumber;

        // Getters y setters
        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }

        public String getFromTerritory() { return fromTerritory; }
        public void setFromTerritory(String fromTerritory) { this.fromTerritory = fromTerritory; }

        public String getToTerritory() { return toTerritory; }
        public void setToTerritory(String toTerritory) { this.toTerritory = toTerritory; }

        public Integer getArmies() { return armies; }
        public void setArmies(Integer armies) { this.armies = armies; }

        public Integer getTurnNumber() { return turnNumber; }
        public void setTurnNumber(Integer turnNumber) { this.turnNumber = turnNumber; }
    }
}

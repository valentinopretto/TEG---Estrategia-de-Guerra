package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEventEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import ar.edu.utn.frc.tup.piii.repository.GameEventRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.IGameEventService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto; // Import corregido
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class GameEventServiceImpl implements IGameEventService {

    private final GameEventRepository gameEventRepository;
    private final GameService gameService;
    private final PlayerService playerService;
    private final GameMapper gameMapper;
    private final PlayerMapper playerMapper;

    public GameEventServiceImpl(GameEventRepository gameEventRepository, GameService gameService, PlayerService playerService, GameMapper gameMapper, PlayerMapper playerMapper) {
        this.gameEventRepository = gameEventRepository;
        this.gameService = gameService;
        this.playerService = playerService;
        this.gameMapper = gameMapper;
        this.playerMapper = playerMapper;
    }

    /**
     * Registra un evento en el historial de la partida
     */
    public GameEventEntity recordEvent(Long gameId, Long actorId, EventType eventType,
                                       Integer turnNumber, String eventData) {
        Game game = gameService.findById(gameId);

        PlayerEntity actor = null;
        if (actorId != null) {
            Optional<Player> optionalActor = playerService.findById(actorId);
            if (optionalActor.isPresent()) {
                actor = playerMapper.toEntity(optionalActor.get());
            }
        }

        GameEventEntity event = new GameEventEntity();
        event.setGame(gameMapper.toEntity(game)); // Convertir Game a GameEntity
        event.setActor(actor);
        event.setType(eventType);
        event.setTurnNumber(turnNumber);
        event.setData(eventData);
        event.setTimestamp(LocalDateTime.now());

        GameEventEntity savedEvent = gameEventRepository.save(event);
        log.info("Event recorded: {} for game {} by player {}", eventType, gameId, actorId);

        return savedEvent;
    }

    /**
     * Registra una conquista de territorio
     */
    public GameEventEntity recordTerritoryConquest(Long gameId, Long conquererPlayerId,
                                                   String conqueredTerritory, String fromPlayer,
                                                   Integer turnNumber) {
        String eventData = String.format("{\"territory\":\"%s\", \"fromPlayer\":\"%s\"}",
                conqueredTerritory, fromPlayer);

        return recordEvent(gameId, conquererPlayerId, EventType.TERRITORY_CONQUERED,
                turnNumber, eventData);
    }

    /**
     * Registra un ataque realizado
     */
    public GameEventEntity recordAttack(Long gameId, Long attackerPlayerId,
                                        String fromTerritory, String toTerritory,
                                        Integer turnNumber, boolean successful) {
        String eventData = String.format("{\"from\":\"%s\", \"to\":\"%s\", \"successful\":%b}",
                fromTerritory, toTerritory, successful);

        return recordEvent(gameId, attackerPlayerId, EventType.ATTACK_PERFORMED,
                turnNumber, eventData);
    }

    /**
     * Registra el inicio de turno de un jugador
     */
    public GameEventEntity recordTurnStart(Long gameId, Long playerId, Integer turnNumber) {
        return recordEvent(gameId, playerId, EventType.TURN_STARTED, turnNumber, null);
    }

    /**
     * Registra el fin de turno de un jugador
     */
    public GameEventEntity recordTurnEnd(Long gameId, Long playerId, Integer turnNumber) {
        return recordEvent(gameId, playerId, EventType.TURN_ENDED, turnNumber, null);
    }

    /**
     * Registra el inicio de una partida
     */
    @Override
    public GameEventEntity recordGameStart(Long gameId) {
        return recordEvent(gameId, null, EventType.GAME_STARTED, 0, null);
    }

    /**
     * Registra el fin de una partida
     */
    @Override
    public GameEventEntity recordGameFinish(Long gameId, Long winnerPlayerId) {
        String eventData = winnerPlayerId != null ?
                String.format("{\"winnerId\":%d}", winnerPlayerId) : null;
        return recordEvent(gameId, winnerPlayerId, EventType.GAME_FINISHED, null, eventData);
    }

    /**
     * Registra cuando un jugador se une a la partida
     */
    @Override
    public GameEventEntity recordPlayerJoined(Long gameId, Long playerId) {
        return recordEvent(gameId, playerId, EventType.PLAYER_JOINED, null, null);
    }

    /**
     * Registra cuando un jugador abandona la partida
     */
    @Override
    public GameEventEntity recordPlayerLeft(Long gameId, Long playerId) {
        return recordEvent(gameId, playerId, EventType.PLAYER_LEFT, null, null);
    }

    /**
     * Registra cuando un jugador es eliminado
     */
    @Override
    public GameEventEntity recordPlayerEliminated(Long gameId, Long eliminatedPlayerId, Long eliminatorPlayerId) {
        String eventData = eliminatorPlayerId != null ?
                String.format("{\"eliminatorId\":%d}", eliminatorPlayerId) : null;
        return recordEvent(gameId, eliminatedPlayerId, EventType.PLAYER_ELIMINATED, null, eventData);
    }

    /**
     * Registra el intercambio de cartas
     */
    @Override
    public GameEventEntity recordCardsTraded(Long gameId, Long playerId, Integer turnNumber, String cardsData) {
        return recordEvent(gameId, playerId, EventType.CARDS_TRADED, turnNumber, cardsData);
    }

    /**
     * Registra la colocación de refuerzos
     */
    @Override
    public GameEventEntity recordReinforcementsPlaced(Long gameId, Long playerId, String territory,
                                                      Integer reinforcements, Integer turnNumber) {
        String eventData = String.format("{\"territory\":\"%s\", \"reinforcements\":%d}",
                territory, reinforcements);
        return recordEvent(gameId, playerId, EventType.REINFORCEMENTS_PLACED, turnNumber, eventData);
    }

    /**
     * Registra una fortificación
     */
    @Override
    public GameEventEntity recordFortification(Long gameId, Long playerId, String fromTerritory,
                                               String toTerritory, Integer armies, Integer turnNumber) {
        String eventData = String.format("{\"from\":\"%s\", \"to\":\"%s\", \"armies\":%d}",
                fromTerritory, toTerritory, armies);
        return recordEvent(gameId, playerId, EventType.FORTIFICATION_PERFORMED, turnNumber, eventData);
    }

    /**
     * Registra el cumplimiento de un objetivo
     */
    @Override
    public GameEventEntity recordObjectiveCompleted(Long gameId, Long playerId, String objectiveData) {
        return recordEvent(gameId, playerId, EventType.OBJECTIVE_COMPLETED, null, objectiveData);
    }

    /**
     * Obtiene el historial completo de una partida ordenado por timestamp
     */
    @Override
    @Transactional()
    public List<GameEventEntity> getGameHistory(Long gameId) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);
        return gameEventRepository.findByGameOrderByTimestampDesc(gameEntity);
    }

    /**
     * Obtiene el historial de eventos de un jugador específico en una partida
     */
    @Override
    @Transactional()
    public List<GameEventEntity> getPlayerEventsInGame(Long gameId, Long playerId) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);
        Optional<Player> optionalPlayer = playerService.findById(playerId);

        if (!optionalPlayer.isPresent()) {
            return new ArrayList<>();
        }

        return gameEventRepository.findByGame(gameEntity).stream()
                .filter(event -> event.getActor() != null && event.getActor().getId().equals(playerId))
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene eventos recientes de una partida (últimas X horas)
     */
    @Override
    @Transactional()
    public List<GameEventEntity> getRecentGameEvents(Long gameId, int hoursBack) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return gameEventRepository.findRecentEventsByGame(gameEntity, since);
    }

    /**
     * Obtiene estadísticas de eventos de una partida
     */
    @Override
    @Transactional()
    public Map<String, Object> getGameEventStats(Long gameId) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttacks", gameEventRepository.countAttacksByGame(gameEntity));

        // Obtener todas las conquistas por jugador
        List<GameEventEntity> conquests = gameEventRepository.findByGame(gameEntity).stream()
                .filter(event -> event.getType() == EventType.TERRITORY_CONQUERED)
                .collect(Collectors.toList());

        Map<String, Long> conquestsByPlayer = conquests.stream()
                .filter(event -> event.getActor() != null)
                .collect(Collectors.groupingBy(
                        event -> event.getActor().getUser().getUsername(),
                        Collectors.counting()
                ));

        stats.put("conquestsByPlayer", conquestsByPlayer);
        stats.put("totalEvents", gameEventRepository.findByGame(gameEntity).size());

        return stats;
    }

    /**
     * Obtiene el historial formateado para mostrar en el frontend
     */
    @Override
    @Transactional()
    public List<GameEventDto> getFormattedGameHistory(Long gameId) {
        List<GameEventEntity> events = getGameHistory(gameId);

        return events.stream()
                .map(this::formatEventForDisplay)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene eventos filtrados por tipo
     */
    @Override
    @Transactional()
    public List<GameEventEntity> getEventsByType(Long gameId, EventType eventType) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);

        return gameEventRepository.findByGame(gameEntity).stream()
                .filter(event -> event.getType() == eventType)
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene eventos de un turno específico
     */
    @Override
    @Transactional()
    public List<GameEventEntity> getEventsByTurn(Long gameId, Integer turnNumber) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);
        return gameEventRepository.findByGameAndTurnNumber(gameEntity, turnNumber);
    }

    /**
     * Obtiene el último evento de un tipo específico para una partida
     */
    @Override
    @Transactional()
    public Optional<GameEventEntity> getLastEventByType(Long gameId, EventType eventType) {
        List<GameEventEntity> events = getEventsByType(gameId, eventType);
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(0));
    }

    /**
     * Cuenta los eventos de un tipo específico en una partida
     */
    @Override
    @Transactional()
    public Long countEventsByType(Long gameId, EventType eventType) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);

        return gameEventRepository.findByGame(gameEntity).stream()
                .filter(event -> event.getType() == eventType)
                .count();
    }

    /**
     * Obtiene todos los ataques realizados por un jugador en una partida
     */
    @Override
    @Transactional()
    public List<GameEventEntity> getPlayerAttacks(Long gameId, Long playerId) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);
        Optional<Player> optionalPlayer = playerService.findById(playerId);

        if (!optionalPlayer.isPresent()) {
            return new ArrayList<>();
        }

        return gameEventRepository.findByActorAndType(playerMapper.toEntity(optionalPlayer.get()), EventType.ATTACK_PERFORMED);
    }

    /**
     * Obtiene todas las conquistas realizadas por un jugador en una partida
     */
    @Override
    @Transactional()
    public List<GameEventEntity> getPlayerConquests(Long gameId, Long playerId) {
        Game game = gameService.findById(gameId);
        GameEntity gameEntity = gameMapper.toEntity(game);
        Optional<Player> optionalPlayer = playerService.findById(playerId);

        if (!optionalPlayer.isPresent()) {
            return new ArrayList<>();
        }

        return gameEventRepository.findByActorAndType(playerMapper.toEntity(optionalPlayer.get()), EventType.TERRITORY_CONQUERED);
    }

    /**
     * Convierte un evento en un DTO formateado para el frontend
     */
    private GameEventDto formatEventForDisplay(GameEventEntity event) {
        return GameEventDto.builder()
                .id(event.getId())
                .turnNumber(event.getTurnNumber())
                .type(event.getType())
                .timestamp(event.getTimestamp())
                .actorName(getActorName(event)) // Usando el método helper
                .description(formatEventMessage(event)) // Usando description en lugar de message
                .data(event.getData()) // Usando data en lugar de rawData
                .build();
    }

    /**
     * Formatea el mensaje del evento para mostrar al usuario
     */
    private String formatEventMessage(GameEventEntity event) {
        String actorName = getActorName(event);

        switch (event.getType()) {
            case TERRITORY_CONQUERED:
                return parseConquestMessage(actorName, event.getData());
            case ATTACK_PERFORMED:
                return parseAttackMessage(actorName, event.getData());
            case TURN_STARTED:
                return actorName + " comenzó su turno";
            case TURN_ENDED:
                return actorName + " terminó su turno";
            case GAME_STARTED:
                return "La partida ha comenzado";
            case GAME_FINISHED:
                return parseGameFinishedMessage(actorName, event.getData());
            case PLAYER_JOINED:
                return actorName + " se unió a la partida";
            case PLAYER_LEFT:
                return actorName + " abandonó la partida";
            case PLAYER_ELIMINATED:
                return parsePlayerEliminatedMessage(actorName, event.getData());
            case CARDS_TRADED:
                return actorName + " intercambió cartas";
            case REINFORCEMENTS_PLACED:
                return parseReinforcementsMessage(actorName, event.getData());
            case FORTIFICATION_PERFORMED:
                return parseFortificationMessage(actorName, event.getData());
            case OBJECTIVE_COMPLETED:
                return actorName + " completó un objetivo";
            default:
                return "Evento: " + event.getType().name();
        }
    }

    /**
     * Obtiene el nombre del actor (usuario, bot o sistema)
     */
    private String getActorName(GameEventEntity event) {
        if (event.getActor() == null) {
            return "Sistema";
        }

        try {
            // Verificar si es un usuario
            if (event.getActor().getUser() != null && event.getActor().getUser().getUsername() != null) {
                return event.getActor().getUser().getUsername();
            }

            // Verificar si es un bot
            if (event.getActor().getBotProfile() != null && event.getActor().getBotProfile().getBotName() != null) {
                return event.getActor().getBotProfile().getBotName();
            }

            // Fallback al nombre del player si existe
            if (event.getActor().getUser().getUsername() != null) {
                return event.getActor().getUser().getUsername();
            }

        } catch (Exception e) {
            log.warn("Error getting actor name for event {}: {}", event.getId(), e.getMessage());
        }

        return "Jugador Desconocido";
    }

    private String parseConquestMessage(String actorName, String data) {
        try {
            if (data != null && data.contains("territory")) {
                // Parseo simple del JSON
                String territory = extractJsonValue(data, "territory");
                String fromPlayer = extractJsonValue(data, "fromPlayer");
                return String.format("%s conquistó %s de %s", actorName, territory, fromPlayer);
            }
        } catch (Exception e) {
            log.warn("Error parsing conquest data: " + data, e);
        }
        return actorName + " conquistó un territorio";
    }

    private String parseAttackMessage(String actorName, String data) {
        try {
            if (data != null && data.contains("from")) {
                String from = extractJsonValue(data, "from");
                String to = extractJsonValue(data, "to");
                String successful = extractJsonValue(data, "successful");

                if ("true".equals(successful)) {
                    return String.format("%s atacó exitosamente desde %s hacia %s", actorName, from, to);
                } else {
                    return String.format("%s atacó desde %s hacia %s (fallido)", actorName, from, to);
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing attack data: " + data, e);
        }
        return actorName + " realizó un ataque";
    }

    private String extractJsonValue(String json, String key) {
        // Parseo simple para extraer valores del JSON
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex != -1) {
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return json.substring(startIndex, endIndex);
            }
        }
        // Buscar valores booleanos o numéricos
        searchKey = "\"" + key + "\":";
        startIndex = json.indexOf(searchKey);
        if (startIndex != -1) {
            startIndex += searchKey.length();
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf("}", startIndex);
            }
            if (endIndex != -1) {
                return json.substring(startIndex, endIndex).trim();
            }
        }
        return "";
    }

    /**
     * Parsea el mensaje de fin de partida
     */
    private String parseGameFinishedMessage(String actorName, String data) {
        if (actorName.equals("Sistema")) {
            return "La partida ha terminado";
        }
        return actorName + " ganó la partida";
    }

    /**
     * Parsea el mensaje de jugador eliminado
     */
    private String parsePlayerEliminatedMessage(String eliminatedPlayer, String data) {
        try {
            if (data != null && data.contains("eliminatorId")) {
                String eliminatorId = extractJsonValue(data, "eliminatorId");
                return String.format("%s fue eliminado", eliminatedPlayer);
            }
        } catch (Exception e) {
            log.warn("Error parsing elimination data: " + data, e);
        }
        return eliminatedPlayer + " fue eliminado de la partida";
    }

    /**
     * Parsea el mensaje de refuerzos colocados
     */
    private String parseReinforcementsMessage(String actorName, String data) {
        try {
            if (data != null && data.contains("territory")) {
                String territory = extractJsonValue(data, "territory");
                String reinforcements = extractJsonValue(data, "reinforcements");
                return String.format("%s colocó %s refuerzos en %s", actorName, reinforcements, territory);
            }
        } catch (Exception e) {
            log.warn("Error parsing reinforcements data: " + data, e);
        }
        return actorName + " colocó refuerzos";
    }

    /**
     * Parsea el mensaje de fortificación
     */
    private String parseFortificationMessage(String actorName, String data) {
        try {
            if (data != null && data.contains("from")) {
                String from = extractJsonValue(data, "from");
                String to = extractJsonValue(data, "to");
                String armies = extractJsonValue(data, "armies");
                return String.format("%s movió %s ejércitos desde %s hacia %s", actorName, armies, from, to);
            }
        } catch (Exception e) {
            log.warn("Error parsing fortification data: " + data, e);
        }
        return actorName + " realizó una fortificación";
    }
}
package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.entities.GameEventEntity;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IGameEventService {

    /**
     * Registra un evento genérico en el historial de la partida
     */
    GameEventEntity recordEvent(Long gameId, Long actorId, EventType eventType,
                                Integer turnNumber, String eventData);

    /**
     * Registra una conquista de territorio
     */
    GameEventEntity recordTerritoryConquest(Long gameId, Long conquererPlayerId,
                                            String conqueredTerritory, String fromPlayer,
                                            Integer turnNumber);

    /**
     * Registra un ataque realizado
     */
    GameEventEntity recordAttack(Long gameId, Long attackerPlayerId,
                                 String fromTerritory, String toTerritory,
                                 Integer turnNumber, boolean successful);

    /**
     * Registra el inicio de turno de un jugador
     */
    GameEventEntity recordTurnStart(Long gameId, Long playerId, Integer turnNumber);

    /**
     * Registra el fin de turno de un jugador
     */
    GameEventEntity recordTurnEnd(Long gameId, Long playerId, Integer turnNumber);

    /**
     * Registra el inicio de una partida
     */
    GameEventEntity recordGameStart(Long gameId);

    /**
     * Registra el fin de una partida
     */
    GameEventEntity recordGameFinish(Long gameId, Long winnerPlayerId);

    /**
     * Registra cuando un jugador se une a la partida
     */
    GameEventEntity recordPlayerJoined(Long gameId, Long playerId);

    /**
     * Registra cuando un jugador abandona la partida
     */
    GameEventEntity recordPlayerLeft(Long gameId, Long playerId);

    /**
     * Registra cuando un jugador es eliminado
     */
    GameEventEntity recordPlayerEliminated(Long gameId, Long eliminatedPlayerId, Long eliminatorPlayerId);

    /**
     * Registra el intercambio de cartas
     */
    GameEventEntity recordCardsTraded(Long gameId, Long playerId, Integer turnNumber, String cardsData);

    /**
     * Registra la colocación de refuerzos
     */
    GameEventEntity recordReinforcementsPlaced(Long gameId, Long playerId, String territory,
                                               Integer reinforcements, Integer turnNumber);

    /**
     * Registra una fortificación
     */
    GameEventEntity recordFortification(Long gameId, Long playerId, String fromTerritory,
                                        String toTerritory, Integer armies, Integer turnNumber);

    /**
     * Registra el cumplimiento de un objetivo
     */
    GameEventEntity recordObjectiveCompleted(Long gameId, Long playerId, String objectiveData);

    /**
     * Obtiene el historial completo de una partida ordenado por timestamp
     */
    List<GameEventEntity> getGameHistory(Long gameId);

    /**
     * Obtiene el historial de eventos de un jugador específico en una partida
     */
    List<GameEventEntity> getPlayerEventsInGame(Long gameId, Long playerId);

    /**
     * Obtiene eventos recientes de una partida (últimas X horas)
     */
    List<GameEventEntity> getRecentGameEvents(Long gameId, int hoursBack);

    /**
     * Obtiene estadísticas de eventos de una partida
     */
    Map<String, Object> getGameEventStats(Long gameId);

    /**
     * Obtiene el historial formateado para mostrar en el frontend
     */
    List<GameEventDto> getFormattedGameHistory(Long gameId);

    /**
     * Obtiene eventos filtrados por tipo
     */
    List<GameEventEntity> getEventsByType(Long gameId, EventType eventType);

    /**
     * Obtiene eventos de un turno específico
     */
    List<GameEventEntity> getEventsByTurn(Long gameId, Integer turnNumber);

    /**
     * Obtiene el último evento de un tipo específico para una partida
     */
    Optional<GameEventEntity> getLastEventByType(Long gameId, EventType eventType);

    /**
     * Cuenta los eventos de un tipo específico en una partida
     */
    Long countEventsByType(Long gameId, EventType eventType);

    /**
     * Obtiene todos los ataques realizados por un jugador en una partida
     */
    List<GameEventEntity> getPlayerAttacks(Long gameId, Long playerId);

    /**
     * Obtiene todas las conquistas realizadas por un jugador en una partida
     */
    List<GameEventEntity> getPlayerConquests(Long gameId, Long playerId);
}

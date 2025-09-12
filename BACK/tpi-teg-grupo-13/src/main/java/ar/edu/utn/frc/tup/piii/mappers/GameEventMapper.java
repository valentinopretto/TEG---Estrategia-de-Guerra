package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.entities.GameEventEntity;
import ar.edu.utn.frc.tup.piii.model.GameEvent;
import org.springframework.stereotype.Component;

@Component
public class GameEventMapper {

    public GameEvent toModel(GameEventEntity entity) {
        if (entity == null) return null;

        return GameEvent.builder()
                .id(entity.getId())
                .turnNumber(entity.getTurnNumber())
                .actorName(entity.getActor() != null ? getPlayerDisplayName(entity.getActor()) : "Sistema")
                .type(entity.getType())
                .description(generateDescription(entity))
                .data(entity.getData())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public GameEventEntity toEntity(GameEvent model) {
        if (model == null) return null;

        GameEventEntity entity = new GameEventEntity();
        entity.setId(model.getId());
        entity.setTurnNumber(model.getTurnNumber());
        entity.setType(model.getType());
        entity.setData(model.getData());
        entity.setTimestamp(model.getTimestamp());

        return entity;
    }

    private String getPlayerDisplayName(ar.edu.utn.frc.tup.piii.entities.PlayerEntity player) {
        if (player.getUser() != null) {
            return player.getUser().getUsername();
        } else if (player.getBotProfile() != null) {
            return player.getBotProfile().getBotName();
        }
        return "Unknown";
    }

    private String generateDescription(GameEventEntity entity) {
        String actorName = entity.getActor() != null ? getPlayerDisplayName(entity.getActor()) : "Sistema";

        switch (entity.getType()) {
            case GAME_STARTED:
                return "El juego ha comenzado";
            case PLAYER_JOINED:
                return actorName + " se unió al juego";
            case ATTACK_PERFORMED:
                return actorName + " realizó un ataque";
            case TERRITORY_CONQUERED:
                return actorName + " conquistó un territorio";
            case TURN_STARTED:
                return "Turno de " + actorName;
            case TURN_ENDED:
                return actorName + " terminó su turno";
            case PLAYER_ELIMINATED:
                return actorName + " fue eliminado";
            case GAME_FINISHED:
                return "El juego ha terminado";
            default:
                return entity.getType().toString();
        }
    }
    public GameEventDto toDto(GameEvent model) {
        if (model == null) return null;
        return GameEventDto.builder()
                .id(model.getId())
                .type(model.getType())
                .description(model.getDescription())
                .timestamp(model.getTimestamp())
                .build();
    }
}

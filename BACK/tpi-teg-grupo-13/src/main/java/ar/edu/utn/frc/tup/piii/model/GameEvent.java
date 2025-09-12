package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEvent {
    private Long id;
    private Integer turnNumber;
    private String actorName;
    private EventType type;
    private String description;
    private String data;
    private LocalDateTime timestamp;

    public static GameEvent create(EventType type, String actorName, String description) {
        return GameEvent.builder()
                .type(type)
                .actorName(actorName)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
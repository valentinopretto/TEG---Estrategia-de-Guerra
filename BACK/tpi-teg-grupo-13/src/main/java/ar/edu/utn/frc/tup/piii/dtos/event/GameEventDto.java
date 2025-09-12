package ar.edu.utn.frc.tup.piii.dtos.event;

import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEventDto {
    private Long id;
    private Integer turnNumber;
    private String actorName;
    private EventType type;
    private String description;
    private String data;
    private LocalDateTime timestamp;
}

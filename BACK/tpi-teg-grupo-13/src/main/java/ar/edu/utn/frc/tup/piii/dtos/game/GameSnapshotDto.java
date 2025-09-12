package ar.edu.utn.frc.tup.piii.dtos.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSnapshotDto {
    private Long id;
    private Long gameId;
    private Integer turnNumber;
    private GameStateDto gameState;
    private LocalDateTime createdAt;
    private Boolean createdBySystem = true;
}

package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnActionDto {
    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Game ID is required")
    private Long gameId;

    private String action; // "end_turn", "skip_phase", etc.
}

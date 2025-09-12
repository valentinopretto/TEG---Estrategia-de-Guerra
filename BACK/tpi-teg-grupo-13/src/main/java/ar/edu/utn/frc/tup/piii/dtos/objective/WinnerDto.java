package ar.edu.utn.frc.tup.piii.dtos.objective;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class WinnerDto {
    private Long playerId;
    private String playerName;
    private String objectiveDescription;
}

package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FortifyDto {
    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Source country ID is required")
    private Long fromCountryId;

    @NotNull(message = "Target country ID is required")
    private Long toCountryId;

    @Min(value = 1, message = "Must move at least 1 army")
    private Integer armies;
}

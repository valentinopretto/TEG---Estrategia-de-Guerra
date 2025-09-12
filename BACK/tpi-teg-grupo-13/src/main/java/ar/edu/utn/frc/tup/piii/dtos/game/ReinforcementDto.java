package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReinforcementDto {
    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotEmpty(message = "Reinforcements map cannot be empty")
    private Map<Long, Integer> reinforcements; // countryId -> armies

    @Min(value = 1, message = "Total armies must be positive")
    private Integer totalArmies;
}

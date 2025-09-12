
package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReinforcementRequestDto {

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotEmpty(message = "Army placement map cannot be empty")
    private Map<Long, Integer> armiesByCountry; // countryId -> cantidad de ejércitos
}
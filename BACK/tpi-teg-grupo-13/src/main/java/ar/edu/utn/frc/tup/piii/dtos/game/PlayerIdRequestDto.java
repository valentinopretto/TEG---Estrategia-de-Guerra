package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PlayerIdRequestDto {
    @NotNull
    private Long playerId;
}

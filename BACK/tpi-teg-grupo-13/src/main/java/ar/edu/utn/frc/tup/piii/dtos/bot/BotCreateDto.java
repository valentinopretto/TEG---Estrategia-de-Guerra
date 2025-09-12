package ar.edu.utn.frc.tup.piii.dtos.bot;

import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotCreateDto {
    @NotNull(message = "Bot level is required")
    private BotLevel level;

    @NotNull(message = "Bot strategy is required")
    private BotStrategy strategy;

    @NotNull(message = "Game ID is required")
    private Long gameCode;

}

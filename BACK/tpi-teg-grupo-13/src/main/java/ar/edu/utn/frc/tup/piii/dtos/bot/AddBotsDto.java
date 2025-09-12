package ar.edu.utn.frc.tup.piii.dtos.bot;

import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBotsDto {
    @NotBlank(message = "El código de partida es obligatorio")
    private String gameCode;

    @NotNull(message = "La cantidad de bots es obligatoria")
    @Min(value = 1, message = "Debe agregar al menos 1 bot")
    @Max(value = 5, message = "No se pueden agregar más de 5 bots")
    private Integer numberOfBots;

    @NotNull(message = "El nivel de bot es obligatorio")
    private BotLevel botLevel;

    private BotStrategy botStrategy; // Opcional, si no se especifica se asigna aleatoriamente

    @NotNull(message = "El ID del solicitante es obligatorio")
    private Long requesterId; // ID del usuario que solicita agregar bots
}
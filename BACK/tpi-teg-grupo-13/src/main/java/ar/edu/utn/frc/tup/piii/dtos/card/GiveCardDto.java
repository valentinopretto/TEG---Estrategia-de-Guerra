package ar.edu.utn.frc.tup.piii.dtos.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para entregar carta a jugador")
public class GiveCardDto {
    @NotNull
    @Schema(description = "Código del juego", example = "ABC123")
    private String gameCode;

    @NotNull
    @Schema(description = "ID del jugador que recibirá la carta", example = "1")
    private Long playerId;

}

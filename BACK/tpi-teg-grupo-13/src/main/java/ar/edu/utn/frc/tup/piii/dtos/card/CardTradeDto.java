package ar.edu.utn.frc.tup.piii.dtos.card;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardTradeDto {
    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotEmpty(message = "Must provide cards to trade")
    @Size(min = 3, max = 3, message = "Must trade exactly 3 cards")
    private List<Long> cardIds;

    /**
     * ID del juego en el que se realiza el intercambio
     */
    @NotNull(message = "Game ID is required")
    private Long gameId;

}

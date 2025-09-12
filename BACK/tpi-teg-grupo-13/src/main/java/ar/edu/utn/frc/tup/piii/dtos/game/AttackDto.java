package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.Max;
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
public class AttackDto {
    //el que juega
    @NotNull(message = "Player ID is required")
    private Long playerId;

    //pais que ataca
    @NotNull(message = "Attacker country ID is required")
    private Long attackerCountryId;
    //pais que se denfiende
    @NotNull(message = "Defender country ID is required")
    private Long defenderCountryId;

    //con cuantos ejercitos ataco
    @NotNull(message = "Attacking armies is required")
    @Min(value = 1, message = "Must attack with at least 1 army")
    @Max(value = 3, message = "Cannot attack with more than 3 armies at once") //Esto será necesario?
    private Integer attackingArmies;

    //dadosss
    @Min(value = 1, message = "Must use at least 1 die")
    private Integer attackerDice;
    @Min(value = 1, message = "Must use at least 1 die")
    private Integer defenderDice;
}
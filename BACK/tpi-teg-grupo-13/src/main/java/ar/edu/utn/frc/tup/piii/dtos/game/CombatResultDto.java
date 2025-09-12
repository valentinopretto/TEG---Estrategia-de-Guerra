package ar.edu.utn.frc.tup.piii.dtos.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CombatResultDto {
    private Long attackerCountryId;
    private String attackerCountryName;
    private Long defenderCountryId;
    private String defenderCountryName;
    private String attackerPlayerName;
    private String defenderPlayerName;
    private List<Integer> attackerDice;
    private List<Integer> defenderDice;
    private Integer attackerLosses;
    private Integer defenderLosses;
    private Boolean territoryConquered;
    private Integer attackerRemainingArmies;
    private Integer defenderRemainingArmies;
}

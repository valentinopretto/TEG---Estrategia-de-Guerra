package ar.edu.utn.frc.tup.piii.dtos.player;

import ar.edu.utn.frc.tup.piii.dtos.country.TerritoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerTerritoriesDto {
    private Long playerId;
    private String playerName;
    private Integer armiesToPlace;
    private Integer expectedArmiesThisRound;
    private Boolean canPlaceArmies;
    private Boolean isPlayerTurn;
    private String message;
    private List<TerritoryDto> ownedTerritories;
}
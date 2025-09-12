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
public class PlayerInitialInfoDto {
    private Long playerId;
    private String playerName;
    private Integer seatOrder;
    private Integer armiesToPlace;
    private Integer territoryCount;
    private Boolean isCurrentPlayer;
    private List<TerritoryDto> territories;
}

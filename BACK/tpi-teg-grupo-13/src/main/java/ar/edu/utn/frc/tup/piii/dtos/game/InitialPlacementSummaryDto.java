package ar.edu.utn.frc.tup.piii.dtos.game;

import ar.edu.utn.frc.tup.piii.dtos.player.PlayerInitialInfoDto;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitialPlacementSummaryDto {
    private String gameCode;
    private GameState currentPhase;
    private Boolean isActive;
    private String message;
    private Long currentPlayerId;
    private Integer expectedArmies;
    private List<PlayerInitialInfoDto> players;
}
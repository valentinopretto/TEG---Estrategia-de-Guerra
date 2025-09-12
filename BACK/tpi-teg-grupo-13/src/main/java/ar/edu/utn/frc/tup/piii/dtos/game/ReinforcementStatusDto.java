package ar.edu.utn.frc.tup.piii.dtos.game;

import ar.edu.utn.frc.tup.piii.dtos.country.TerritoryDto;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReinforcementStatusDto {
    private Long playerId;
    private String playerName;
    private GameState gameState;
    private TurnPhase currentPhase;
    private Integer armiesToPlace;
    private Integer baseArmies; // Por territorios
    private Integer continentBonus; // Por continentes controlados
    private Integer cardBonus; // Por intercambio de cartas
    private Integer totalArmies; // Total a colocar
    private Boolean isPlayerTurn;
    private Boolean canReinforce;
    private String message;
    private List<TerritoryDto> ownedTerritories;
    private List<String> controlledContinents;
}
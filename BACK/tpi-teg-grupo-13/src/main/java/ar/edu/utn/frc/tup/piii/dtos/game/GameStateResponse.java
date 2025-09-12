package ar.edu.utn.frc.tup.piii.dtos.game;

import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import lombok.Data;

@Data
public class GameStateResponse {
    private GameState gameState;
    private TurnPhase turnPhase;
    private String currentPlayer;
    private Integer currentTurn;
    private String[] availableActions;
    private String phaseDescription;
}
package ar.edu.utn.frc.tup.piii.dtos.game;

import ar.edu.utn.frc.tup.piii.dtos.continent.ContinentResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateDto {
    private Long gameId;
    private GameState status;
    private TurnPhase currentPhase;
    private Integer currentTurn;
    private String currentPlayerName;
    private Long currentPlayerId;
    private List<PlayerResponseDto> players;
    private Map<Long, CountryResponseDto> territories;
    private List<ContinentResponseDto> continents;
    private List<GameEventDto> recentEvents;
    private Integer remainingCards;
    private Boolean canEndTurn;
    private Boolean isGameOver;
    private String winnerName;
}

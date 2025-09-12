package ar.edu.utn.frc.tup.piii.dtos.game;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.continent.ContinentResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para devolver los datos de la partida completa tras cada operación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResponseDto {
    private Long id;
    private String gameCode;
    private String createdByUsername;
    private GameState state;
    private TurnPhase currentPhase;
    private Integer currentTurn;
    private Integer currentPlayerIndex;
    private Integer maxPlayers;
    private Integer turnTimeLimit;
    private Boolean chatEnabled;
    private Boolean pactsAllowed;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PlayerResponseDto> players;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<Long, CountryResponseDto> territories;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ContinentResponseDto> continents;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<GameEventDto> recentEvents;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ChatMessageResponseDto> recentMessages;

    private String currentPlayerName;

    private Boolean canStart;
    private Boolean isGameOver;
    private String winnerName;
}

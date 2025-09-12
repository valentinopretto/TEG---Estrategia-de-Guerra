package ar.edu.utn.frc.tup.piii.dtos.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStatisticsDto {
    private Integer totalTurns;
    private Duration gameDuration;
    private Map<String, Integer> playerTerritories;
    private Map<String, Integer> playerArmies;
    private Integer totalAttacks;
    private Integer territoriesConquered;
    private String mostAggressivePlayer;
    private String mostDefensivePlayer;
}

package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    private Long id;
    private String username;
    private String displayName;
    private Boolean isBot;
    private BotLevel botLevel;
    private BotProfileEntity botProfile;
    private PlayerStatus status;
    private PlayerColor color;
    private Integer armiesToPlace;
    private Integer seatOrder;
    private LocalDateTime joinedAt;
    private LocalDateTime eliminatedAt;
    private Objective objective;

    @Builder.Default
    private List<Card> hand = new ArrayList<>();

    @Builder.Default
    private List<Long> territoryIds = new ArrayList<>();

    @Builder.Default
    private Integer tradeCount = 0;

    // Business logic methods
    public boolean isEliminated() {
        return status == PlayerStatus.ELIMINATED;
    }

    public boolean isHuman() {
        return !isBot;
    }

    public int getTerritoryCount() {
        return territoryIds.size();
    }

    public int getTotalArmies(Map<Long, Territory> territories) {
        return territoryIds.stream()
                .mapToInt(id -> territories.get(id).getArmies())
                .sum();
    }

    public boolean canAttack(Map<Long, Territory> territories) {
        return territoryIds.stream()
                .anyMatch(id -> territories.get(id).getArmies() > 1);
    }
}
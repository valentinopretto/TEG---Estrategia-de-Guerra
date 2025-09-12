package ar.edu.utn.frc.tup.piii.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Territory {
    private Long id;
    private String name;
    private String continentName;
    private Long ownerId;
    private String ownerName;
    private Integer armies;
    private Integer lastConqueredTurn;
    private Double positionX;
    private Double positionY;

    @Builder.Default
    private Set<Long> neighborIds = new HashSet<>();

    // Business logic methods
    public boolean canAttack() {
        return armies > 1;
    }

    public boolean isNeighbor(Long territoryId) {
        return neighborIds.contains(territoryId);
    }

    public boolean canAttackTerritory(Long targetId) {
        return canAttack() && isNeighbor(targetId);
    }

    public void addArmies(int count) {
        this.armies += count;
    }

    public void removeArmies(int count) {
        this.armies = Math.max(0, this.armies - count);
    }
/*
    public Integer getLastConqueredTurn() {return lastConqueredTurn;}

    public void setLastConqueredTurn(Integer lastConqueredTurn) {this.lastConqueredTurn = lastConqueredTurn;}
*/
}
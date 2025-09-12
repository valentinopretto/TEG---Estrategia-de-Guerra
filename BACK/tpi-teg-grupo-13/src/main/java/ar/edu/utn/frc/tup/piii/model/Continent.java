package ar.edu.utn.frc.tup.piii.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Continent {
    private Long id;
    private String name;
    private Integer bonusArmies;

    @Builder.Default
    private List<Long> countryIds = new ArrayList<>();

    public boolean isControlledBy(Long playerId, Map<Long, Territory> territories) {
        return countryIds.stream()
                .allMatch(countryId -> {
                    Territory territory = territories.get(countryId);
                    return territory != null && playerId.equals(territory.getOwnerId());
                });
    }
}
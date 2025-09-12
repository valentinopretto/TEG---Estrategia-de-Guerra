package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    private Long id;
    private String countryName;
    private CardType type;
    private Long ownerId;
    private Boolean isInDeck;

    public boolean isWildcard() {
        return type == CardType.WILDCARD;
    }
}

package ar.edu.utn.frc.tup.piii.dtos.card;

import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponseDto {

    /**
     * ID único de la carta
     */
    private Long id;

    /**
     * Nombre del país representado en la carta (null para wildcards)
     */
    private String countryName;

    /**
     * Tipo de carta (INFANTRY, CAVALRY, CANNON, WILDCARD)
     */
    private CardType type;

    /**
     * Indica si la carta está disponible en el mazo
     */
    private Boolean isInDeck;

    /**
     * Información adicional sobre si es una carta wildcard
     */
    public boolean isWildcard() {
        return type == CardType.WILDCARD;
    }

    /**
     * Obtiene el nombre para mostrar en la interfaz
     */
    public String getDisplayName() {
        if (isWildcard()) {
            return "Wildcard";
        }
        return countryName != null ? countryName : "Unknown Territory";
    }

}

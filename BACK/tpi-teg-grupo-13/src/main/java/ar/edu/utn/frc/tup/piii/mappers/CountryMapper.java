package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.country.TerritoryDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import org.springframework.stereotype.Component;

/**
 * Convierte del modelo Country a CountryResponseDto.
 */
@Component
public class CountryMapper {
    public CountryResponseDto toResponseDto(Territory model) {
        if (model == null) return null;
        return CountryResponseDto.builder()
                .id(model.getId())
                .name(model.getName())
                .continentName(model.getContinentName())
                .ownerName(model.getOwnerName())
                .armies(model.getArmies())
                .positionX(model.getPositionX())
                .positionY(model.getPositionY())
                .neighborIds(model.getNeighborIds() != null ? model.getNeighborIds() : null)
                .canBeAttacked(false) // Lógica de “puede ser atacado” según tu modelo
                .canAttack(false)     // Lógica de “puede atacar” según tu modelo
                .build();
    }
    public TerritoryDto mapTerritoryToDto(Territory territory) {
        return TerritoryDto.builder()
                .id(territory.getId())
                .name(territory.getName())
                .continentName(territory.getContinentName())
                .armies(territory.getArmies())
                .positionX(territory.getPositionX())
                .positionY(territory.getPositionY())
                .build();
    }
}

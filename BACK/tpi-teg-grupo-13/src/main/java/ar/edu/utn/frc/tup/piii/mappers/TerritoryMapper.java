package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import org.springframework.stereotype.Component;

@Component
public class TerritoryMapper {
    public CountryResponseDto toResponseDto(Territory model) {
        if (model == null) return null;
        return CountryResponseDto.builder()
                .id(model.getId())
                .name(model.getName())
                .continentName(model.getContinentName())
                .ownerName(model.getOwnerName())
                .armies(model.getArmies())
                .neighborIds(model.getNeighborIds())
                .build();
    }
}

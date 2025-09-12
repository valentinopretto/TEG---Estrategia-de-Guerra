package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.continent.ContinentResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.entities.ContinentEntity;
import ar.edu.utn.frc.tup.piii.model.Continent;
import ar.edu.utn.frc.tup.piii.model.Territory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convierte del modelo Continent a ContinentResponseDto.
 */
@Component
public class ContinentMapper {
    public ContinentResponseDto toResponseDto(Continent model, Map<Long, Territory> territories) {
        if (model == null) return null;

        return ContinentResponseDto.builder()
                .id(model.getId())
                .name(model.getName())
                .bonusArmies(model.getBonusArmies())
                .countries(model.getCountryIds().stream()
                        .map(id -> {
                            Territory territory = territories.get(id);
                            return CountryResponseDto.builder()
                                    .id(id)
                                    .name(territory != null ? territory.getName() : "Desconocido")
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .totalCountries(model.getCountryIds().size())
                .controlledCountries((int) model.getCountryIds().stream()
                        .filter(id -> {
                            Territory t = territories.get(id);
                            return t != null && t.getOwnerId() != null;
                        }).count())
                .isControlled(model.isControlledBy(
                        model.getCountryIds().isEmpty() ? null :
                                territories.get(model.getCountryIds().get(0)).getOwnerId(),
                        territories
                ))
                .controllerName(model.isControlledBy(
                        model.getCountryIds().isEmpty() ? null :
                                territories.get(model.getCountryIds().get(0)).getOwnerId(),
                        territories
                ) ? territories.get(model.getCountryIds().get(0)).getOwnerName() : null)
                .build();
    }

    public ContinentEntity toEntity(Continent model) {
        if (model == null) return null;

        ContinentEntity entity = new ContinentEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setBonusArmies(model.getBonusArmies());

        return entity;
    }

}

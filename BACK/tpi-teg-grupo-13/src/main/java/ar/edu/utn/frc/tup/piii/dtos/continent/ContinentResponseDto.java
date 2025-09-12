package ar.edu.utn.frc.tup.piii.dtos.continent;

import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContinentResponseDto {
    private Long id;
    private String name;
    private Integer bonusArmies;
    private List<CountryResponseDto> countries;
    private String controllerName;
    private Boolean isControlled;
    private Integer totalCountries;
    private Integer controlledCountries;
}

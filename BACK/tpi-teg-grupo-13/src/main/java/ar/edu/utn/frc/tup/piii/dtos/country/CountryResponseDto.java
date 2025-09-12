package ar.edu.utn.frc.tup.piii.dtos.country;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryResponseDto {
    private Long id;
    private String name;
    private String continentName;
    private String ownerName;
    private Integer armies;
    private Double positionX;
    private Double positionY;
    private Set<Long> neighborIds;
    private Boolean canBeAttacked;
    private Boolean canAttack;
}

package ar.edu.utn.frc.tup.piii.dtos.objective;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveResponseDto {
    private Long id;
    private String description;
    private Boolean isAchieved;
    private Boolean isCommon;
    private String type;
}

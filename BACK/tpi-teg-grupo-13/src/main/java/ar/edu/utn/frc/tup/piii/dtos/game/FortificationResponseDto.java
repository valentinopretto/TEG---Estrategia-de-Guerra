package ar.edu.utn.frc.tup.piii.dtos.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FortificationResponseDto {
    private Long fromCountryId;
    private String fromCountryName;
    private Long toCountryId;
    private String toCountryName;
    private String playerName;
    private Integer armiesMoved;
    private Integer fromCountryRemainingArmies;
    private Integer toCountryFinalArmies;
    private Boolean success;
    private String message;
}
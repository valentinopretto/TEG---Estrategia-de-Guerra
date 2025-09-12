package ar.edu.utn.frc.tup.piii.dtos.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class InitialArmyPlacementDto {
    //esta es para que el jugador pueda enviar desde el front los ejercitos que quiere poner en cada pais
    private Long playerId;
    private Map<Long, Integer> armiesByCountry; // countryId -> cantidadDeEjercitos
}


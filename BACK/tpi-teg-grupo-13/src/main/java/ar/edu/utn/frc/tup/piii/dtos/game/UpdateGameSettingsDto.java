package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGameSettingsDto {

    private Long requesterId; // ID del usuario que hace la petición (debe ser el host)

    @Min(value = 2, message = "Minimum 2 players required")
    @Max(value = 6, message = "Maximum 6 players allowed")
    private Integer maxPlayers;

    @Min(value = 30, message = "Minimum turn time is 30 seconds")
    @Max(value = 600, message = "Maximum turn time is 600 seconds (10 minutes)")
    private Integer turnTimeLimit;

    private Boolean chatEnabled;

    private Boolean pactsAllowed;


    @Override
    public String toString() {
        return "UpdateGameSettingsDto{" +
                "requesterId=" + requesterId +
                ", maxPlayers=" + maxPlayers +
                ", turnTimeLimit=" + turnTimeLimit +
                ", chatEnabled=" + chatEnabled +
                ", pactsAllowed=" + pactsAllowed +
                '}';
    }
}
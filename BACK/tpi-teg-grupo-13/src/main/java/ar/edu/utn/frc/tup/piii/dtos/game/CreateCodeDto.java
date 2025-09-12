package ar.edu.utn.frc.tup.piii.dtos.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCodeDto {
    @JsonProperty("hostUserId")
    private Long hostUserId;
}
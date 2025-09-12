package ar.edu.utn.frc.tup.piii.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldErrorDto {
    private String field;
    private String message;
    private Object rejectedValue;
}

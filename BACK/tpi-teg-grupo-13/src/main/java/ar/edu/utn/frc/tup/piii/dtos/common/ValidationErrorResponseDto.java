package ar.edu.utn.frc.tup.piii.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorResponseDto {
    private String message;
    private List<FieldErrorDto> fieldErrors;
    private String timestamp;
    private String path;
    private Integer status;

    public static ValidationErrorResponseDto of(String message, List<FieldErrorDto> fieldErrors, String path) {
        return ValidationErrorResponseDto.builder()
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now().toString())
                .path(path)
                .status(400)
                .build();
    }
}


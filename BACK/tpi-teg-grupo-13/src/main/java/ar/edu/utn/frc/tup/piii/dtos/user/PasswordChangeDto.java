package ar.edu.utn.frc.tup.piii.dtos.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeDto {
    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}

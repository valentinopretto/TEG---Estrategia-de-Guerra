package ar.edu.utn.frc.tup.piii.dtos.user;

import jakarta.validation.constraints.NotBlank;

public class UserLoginDto {
    @NotBlank private String username;
    @NotBlank private String password;

    public @NotBlank String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank String username) {
        this.username = username;
    }

    public @NotBlank String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }
}

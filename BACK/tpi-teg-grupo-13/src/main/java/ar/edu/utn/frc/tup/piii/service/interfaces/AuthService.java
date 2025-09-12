package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.auth.Credential;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.model.User;

public interface AuthService {
//    JwtResponseDto register(UserRegisterDto dto);
//    JwtResponseDto login(UserLoginDto dto);
    User register(UserRegisterDto dto);
    User login(Credential credential);
}



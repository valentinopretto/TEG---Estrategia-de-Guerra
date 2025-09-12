package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.user.PasswordChangeDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.model.User;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User getUserById(Long userId);
    User getUserByUserName(String username);
    Boolean existsByUserName(String username);
    Boolean existsByEmail(String email);

    List<User> getAllUsers();

    User getUserByUserNameAndPasswordHash(String userName, String password);

    void save(User user);

    User getUserByEmailAndPasswordHash(String email, String password);

    void updateUser(Long id, UserUpdateDto dto);

    void changePassword(Long id, @Valid PasswordChangeDto dto);
}

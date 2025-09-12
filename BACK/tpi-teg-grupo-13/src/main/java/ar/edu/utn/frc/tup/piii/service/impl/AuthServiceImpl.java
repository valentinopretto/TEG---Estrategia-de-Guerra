package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.auth.Credential;
import ar.edu.utn.frc.tup.piii.dtos.auth.EmailIdentity;
import ar.edu.utn.frc.tup.piii.dtos.auth.UsernameIdentity;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.exceptions.EmailAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exceptions.UserAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.service.interfaces.AuthService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final BCryptPasswordEncoder encoder;
    private final UserService userService;

    @Autowired
    public AuthServiceImpl(UserService userService, BCryptPasswordEncoder encoder) {
        this.userService = userService;
        this.encoder = encoder;
    }

    @Override
    public User register(UserRegisterDto dto) {
        if (userService.existsByUserName(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + dto.getUsername());
        }
        if (userService.existsByEmail(dto.getEmail())){
            throw new EmailAlreadyExistsException("Email already registered: " + dto.getEmail());
        }

        User user = UserMapper.toModel(dto);
        user.setPasswordHash(encoder.encode(dto.getPassword()));
        userService.save(user);
        return user;
    }

    @Override
    public User login(Credential credential) {
        if(credential.getIdentity() instanceof UsernameIdentity){
            return loginWithIdentity((UsernameIdentity) credential.getIdentity(), credential.getPassword());
        } else {
            return loginWithIdentity((EmailIdentity) credential.getIdentity(), credential.getPassword());
        }
    }

    private User loginWithIdentity(UsernameIdentity identity, String password) {
        User user = userService.getUserByUserName(identity.getUserName());
        if(!encoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid user or password");
        }
        return updateLastLogin(user);
    }

    private User loginWithIdentity(EmailIdentity identity, String password) {
        User user = userService.getUserByEmailAndPasswordHash(identity.getEmail(), password);
        if(!encoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return updateLastLogin(user);
    }

    private User updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userService.save(user);
        return user;
    }
}
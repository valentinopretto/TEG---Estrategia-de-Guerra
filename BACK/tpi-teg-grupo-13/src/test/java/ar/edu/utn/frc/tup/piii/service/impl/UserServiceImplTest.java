package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.user.PasswordChangeDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exceptions.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private User user;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@email.com");
        userEntity.setPasswordHash("hashedPassword");
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setLastLogin(LocalDateTime.now());
        userEntity.setIsActive(true);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .passwordHash("hashedPassword")
                .lastLogin(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserByUserName_WhenUserExists_ShouldReturnUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // When
        User result = userService.getUserByUserName(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserByUserName_WhenUserNotExists_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUserName(username))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with username: nonexistent");
    }

    @Test
    void existsByUserName_WhenUserExists_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        Boolean result = userService.existsByUserName(username);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByUserName_WhenUserNotExists_ShouldReturnFalse() {
        // Given
        String username = "nonexistent";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        // When
        Boolean result = userService.existsByUserName(username);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Given
        String email = "test@email.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        Boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        // Given
        String email = "nonexistent@email.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When
        Boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Given
        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setUsername("testuser2");
        userEntity2.setEmail("test2@email.com");

        User user2 = User.builder()
                .id(2L)
                .username("testuser2")
                .email("test2@email.com")
                .build();

        List<UserEntity> userEntities = Arrays.asList(userEntity, userEntity2);

        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.toModel(userEntity)).thenReturn(user);
        when(userMapper.toModel(userEntity2)).thenReturn(user2);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("testuser2");
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldCallRepository() {
        // Given
        when(userMapper.toEntity(user)).thenReturn(userEntity);

        // When
        userService.save(user);

        // Then
        verify(userRepository).save(userEntity);
    }

    @Test
    void getUserByUserNameAndPasswordHash_WhenUserExists_ShouldReturnUser() {
        // Given
        String username = "testuser";
        String password = "password123";
        when(userRepository.findByUsernameAndPasswordHash(username, password))
                .thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // When
        User result = userService.getUserByUserNameAndPasswordHash(username, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    void getUserByUserNameAndPasswordHash_WhenUserNotExists_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        String password = "password123";
        when(userRepository.findByUsernameAndPasswordHash(username, password))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUserNameAndPasswordHash(username, password))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Username or password invalid!");
    }

    @Test
    void getUserByEmailAndPasswordHash_WhenUserExists_ShouldReturnUser() {
        // Given
        String email = "test@email.com";
        String password = "password123";
        when(userRepository.findByEmailAndPasswordHash(email, password))
                .thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // When
        User result = userService.getUserByEmailAndPasswordHash(email, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void getUserByEmailAndPasswordHash_WhenUserNotExists_ShouldThrowException() {
        // Given
        String email = "nonexistent@email.com";
        String password = "password123";
        when(userRepository.findByEmailAndPasswordHash(email, password))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmailAndPasswordHash(email, password))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Email or password invalid!");
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateUser() {
        // Given
        Long userId = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);
        when(userMapper.toEntity(any(User.class))).thenReturn(userEntity);

        // When
        userService.updateUser(userId, updateDto);

        // Then
        verify(userRepository).save(userEntity);
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldThrowException() {
        // Given
        Long userId = 999L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_WhenCurrentPasswordMatches_ShouldUpdatePassword() {
        // Given
        Long userId = 1L;
        PasswordChangeDto passwordDto = new PasswordChangeDto();
        passwordDto.setCurrentPassword("hashedPassword");
        passwordDto.setNewPassword("newHashedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);
        when(userMapper.toEntity(any(User.class))).thenReturn(userEntity);

        // When
        userService.changePassword(userId, passwordDto);

        // Then
        verify(userRepository).save(userEntity);
    }

    @Test
    void changePassword_WhenUserNotExists_ShouldThrowException() {
        // Given
        Long userId = 999L;
        PasswordChangeDto passwordDto = new PasswordChangeDto();
        passwordDto.setCurrentPassword("currentPassword");
        passwordDto.setNewPassword("newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, passwordDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuario no encontrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_WhenCurrentPasswordIsIncorrect_ShouldThrowException() {
        // Given
        Long userId = 1L;
        PasswordChangeDto passwordDto = new PasswordChangeDto();
        passwordDto.setCurrentPassword("wrongPassword");
        passwordDto.setNewPassword("newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, passwordDto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("La contraseña actual es incorrecta");

        verify(userRepository, never()).save(any());
    }
}
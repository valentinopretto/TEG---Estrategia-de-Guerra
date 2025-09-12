package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;
    private UserEntity userEntity;
    private User user;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        testTime = LocalDateTime.now();

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPasswordHash("hashedPassword123");
        userEntity.setEmail("test@email.com");
        userEntity.setAvatarUrl("http://example.com/avatar.jpg");
        userEntity.setLastLogin(testTime);
        userEntity.setCreatedAt(testTime);
        userEntity.setIsActive(true);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("hashedPassword123")
                .email("test@email.com")
                .avatarUrl("http://example.com/avatar.jpg")
                .lastLogin(testTime)
                .build();
    }

    @Test
    void toModel_WithValidEntity_ShouldMapCorrectly() {
        // When
        User result = userMapper.toModel(userEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userEntity.getId());
        assertThat(result.getUsername()).isEqualTo(userEntity.getUsername());
        assertThat(result.getPasswordHash()).isEqualTo(userEntity.getPasswordHash());
        assertThat(result.getEmail()).isEqualTo(userEntity.getEmail());
        assertThat(result.getAvatarUrl()).isEqualTo(userEntity.getAvatarUrl());
        assertThat(result.getLastLogin()).isEqualTo(userEntity.getLastLogin());
    }

    @Test
    void toModel_WithNullFields_ShouldHandleNulls() {
        // Given
        userEntity.setAvatarUrl(null);
        userEntity.setLastLogin(null);

        // When
        User result = userMapper.toModel(userEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvatarUrl()).isNull();
        assertThat(result.getLastLogin()).isNull();
        // Otros campos no nulos deben mantenerse
        assertThat(result.getUsername()).isEqualTo(userEntity.getUsername());
        assertThat(result.getEmail()).isEqualTo(userEntity.getEmail());
    }

    @Test
    void toEntity_WithValidUser_ShouldMapCorrectly() {
        // When
        UserEntity result = userMapper.toEntity(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getPasswordHash()).isEqualTo(user.getPasswordHash());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getAvatarUrl());
        assertThat(result.getLastLogin()).isEqualTo(user.getLastLogin());
    }

    @Test
    void toEntity_WithNullUser_ShouldReturnNull() {
        // When
        UserEntity result = userMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithNullFields_ShouldHandleNulls() {
        // Given
        user = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("hashedPassword123")
                .email("test@email.com")
                .avatarUrl(null)
                .lastLogin(null)
                .build();

        // When
        UserEntity result = userMapper.toEntity(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAvatarUrl()).isNull();
        assertThat(result.getLastLogin()).isNull();
        // Otros campos no nulos deben mantenerse
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void mappingRoundTrip_ShouldPreserveData() {
        // When
        User mappedUser = userMapper.toModel(userEntity);
        UserEntity mappedBackEntity = userMapper.toEntity(mappedUser);

        // Then
        assertThat(mappedBackEntity.getId()).isEqualTo(userEntity.getId());
        assertThat(mappedBackEntity.getUsername()).isEqualTo(userEntity.getUsername());
        assertThat(mappedBackEntity.getPasswordHash()).isEqualTo(userEntity.getPasswordHash());
        assertThat(mappedBackEntity.getEmail()).isEqualTo(userEntity.getEmail());
        assertThat(mappedBackEntity.getAvatarUrl()).isEqualTo(userEntity.getAvatarUrl());
        assertThat(mappedBackEntity.getLastLogin()).isEqualTo(userEntity.getLastLogin());
    }
}
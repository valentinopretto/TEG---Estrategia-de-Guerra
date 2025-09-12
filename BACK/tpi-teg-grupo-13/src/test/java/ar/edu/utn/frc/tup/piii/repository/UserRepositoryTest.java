package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity user1;
    private UserEntity user2;
    private UserEntity inactiveUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = new UserEntity();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1.setPasswordHash("hashedpassword1");
        user1.setIsActive(true);
        user1.setCreatedAt(LocalDateTime.now());

        user2 = new UserEntity();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPasswordHash("hashedpassword2");
        user2.setIsActive(true);
        user2.setCreatedAt(LocalDateTime.now());

        inactiveUser = new UserEntity();
        inactiveUser.setUsername("inactiveuser");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPasswordHash("hashedpassword3");
        inactiveUser.setIsActive(false);
        inactiveUser.setCreatedAt(LocalDateTime.now());

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        inactiveUser = userRepository.save(inactiveUser);

        entityManager.flush();
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        Optional<UserEntity> found = userRepository.findByUsername("testuser1");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser1");
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    void findByUsername_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<UserEntity> found = userRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_WhenEmailExists_ShouldReturnUser() {
        Optional<UserEntity> found = userRepository.findByEmail("test2@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser2");
    }

    @Test
    void findByEmail_WhenEmailNotExists_ShouldReturnEmpty() {
        Optional<UserEntity> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_WhenUserExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByUsername("testuser1");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WhenUserNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByEmail("test1@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveUsers() {
        List<UserEntity> activeUsers = userRepository.findByIsActiveTrue();

        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers).extracting(UserEntity::getUsername)
                .containsExactlyInAnyOrder("testuser1", "testuser2");
        assertThat(activeUsers).allMatch(UserEntity::getIsActive);
    }
}

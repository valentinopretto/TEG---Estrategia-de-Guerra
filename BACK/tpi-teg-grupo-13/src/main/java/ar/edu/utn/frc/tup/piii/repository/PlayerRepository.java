package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    List<PlayerEntity> findByGame(GameEntity game);
    List<PlayerEntity> findByUser(UserEntity user);
    List<PlayerEntity> findByGameAndStatus(GameEntity game, PlayerStatus status);
    Optional<PlayerEntity> findByGameAndUser(GameEntity game, UserEntity user);
    Optional<PlayerEntity> findByGameAndColor(GameEntity game, PlayerColor color);
    Optional<PlayerEntity> findByGameAndUserId(GameEntity game, Long userId);

    @Query("SELECT p FROM PlayerEntity p WHERE p.game = :game AND p.status != 'ELIMINATED' ORDER BY p.seatOrder")
    List<PlayerEntity> findActivePlayersByGame(@Param("game") GameEntity game);

    @Query("SELECT p FROM PlayerEntity p WHERE p.game = :game AND p.botProfile IS NOT NULL")
    List<PlayerEntity> findBotPlayersByGame(@Param("game") GameEntity game);

    @Query("SELECT p FROM PlayerEntity p WHERE p.user IS NOT NULL AND p.status = 'ACTIVE'")
    List<PlayerEntity> findAllHumanActivePlayers();

    @Query("SELECT COUNT(p) FROM PlayerEntity p WHERE p.user = :user AND p.status != 'ELIMINATED'")
    long countActiveGamesByUser(@Param("user") UserEntity user);

    @Modifying
    @Query("UPDATE PlayerEntity p SET p.armiesToPlace = p.armiesToPlace + :armies WHERE p.id = :playerId")
    void addArmiesToPlace(@Param("playerId") Long playerId, @Param("armies") int armies);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PlayerEntity p SET p.armiesToPlace = GREATEST(0, p.armiesToPlace - :armies) WHERE p.id = :playerId")
    void removeArmiesToPlace(@Param("playerId") Long playerId, @Param("armies") int armies);
}
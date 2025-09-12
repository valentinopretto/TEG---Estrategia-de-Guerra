package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {

    List<GameEntity> findByStatus(GameState status);
    List<GameEntity> findByCreatedBy(UserEntity createdBy);

    @Query("SELECT g FROM GameEntity g WHERE g.status IN :statuses")
    List<GameEntity> findByStatusIn(@Param("statuses") List<GameState> statuses);

    @Query("SELECT g FROM GameEntity g WHERE g.status = 'WAITING_FOR_PLAYERS' AND SIZE(g.players) < g.maxPlayers")
    List<GameEntity> findAvailableGames();

    @Query("SELECT g FROM GameEntity g JOIN g.players p WHERE p.user = :user")
    List<GameEntity> findGamesByUser(@Param("user") UserEntity user);

    @Query("SELECT g FROM GameEntity g WHERE g.lastModified < :cutoffTime AND g.status = 'PAUSED'")
    List<GameEntity> findAbandonedGames(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(g) FROM GameEntity g WHERE g.status = 'IN_PROGRESS'")
    long countActiveGames();

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GameEntity g WHERE g.gameCode = :gameCode")
    boolean existsByGameCode(@Param("gameCode") String gameCode);

    @Query("SELECT g FROM GameEntity g LEFT JOIN FETCH g.players WHERE g.gameCode = :gameCode")
    Optional<GameEntity> findByGameCode(@Param("gameCode") String gameCode);

    @Query("SELECT g FROM GameEntity g " + "LEFT JOIN FETCH g.createdBy " + "LEFT JOIN FETCH g.players " + "WHERE g.gameCode = :gameCode")
    Optional<GameEntity> findForSettings(@Param("gameCode") String gameCode);


    List<GameEntity> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}

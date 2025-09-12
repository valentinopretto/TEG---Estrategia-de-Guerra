package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.GameSnapshotEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameSnapshotRepository extends JpaRepository<GameSnapshotEntity, Long> {
    List<GameSnapshotEntity> findByGame(GameEntity game);
    List<GameSnapshotEntity> findByGameAndCreatedBySystemTrue(GameEntity game);
    Optional<GameSnapshotEntity> findByGameAndTurnNumber(GameEntity game, Integer turnNumber);

    @Query("SELECT gs FROM GameSnapshotEntity gs WHERE gs.game = :game ORDER BY gs.createdAt DESC")
    List<GameSnapshotEntity> findByGameOrderByCreatedAtDesc(@Param("game") GameEntity game);

    @Query("SELECT gs FROM GameSnapshotEntity gs WHERE gs.game = :game ORDER BY gs.createdAt DESC LIMIT 1")
    Optional<GameSnapshotEntity> findLatestSnapshotByGame(@Param("game") GameEntity game);

    @Query("SELECT gs FROM GameSnapshotEntity gs WHERE gs.createdAt < :cutoffTime AND gs.createdBySystem = true")
    List<GameSnapshotEntity> findOldAutoSnapshots(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(gs) FROM GameSnapshotEntity gs WHERE gs.game = :game")
    Long countSnapshotsByGame(@Param("game") GameEntity game);
}

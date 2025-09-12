package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.GameEventEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameEventRepository extends JpaRepository<GameEventEntity, Long> {
    List<GameEventEntity> findByGame(GameEntity game);
    List<GameEventEntity> findByActor(PlayerEntity actor);
    List<GameEventEntity> findByType(EventType type);
    List<GameEventEntity> findByGameAndTurnNumber(GameEntity game, Integer turnNumber);

    @Query("SELECT ge FROM GameEventEntity ge WHERE ge.game = :game ORDER BY ge.timestamp DESC")
    List<GameEventEntity> findByGameOrderByTimestampDesc(@Param("game") GameEntity game);

    @Query("SELECT ge FROM GameEventEntity ge WHERE ge.game = :game AND ge.timestamp >= :since ORDER BY ge.timestamp DESC")
    List<GameEventEntity> findRecentEventsByGame(@Param("game") GameEntity game, @Param("since") LocalDateTime since);

    @Query("SELECT ge FROM GameEventEntity ge WHERE ge.actor = :player AND ge.type = :eventType")
    List<GameEventEntity> findByActorAndType(@Param("player") PlayerEntity player, @Param("eventType") EventType eventType);

    @Query("SELECT COUNT(ge) FROM GameEventEntity ge WHERE ge.game = :game AND ge.type = 'ATTACK_PERFORMED'")
    Long countAttacksByGame(@Param("game") GameEntity game);

    @Query("SELECT COUNT(ge) FROM GameEventEntity ge WHERE ge.actor = :player AND ge.type = 'TERRITORY_CONQUERED'")
    Long countConquestsByPlayer(@Param("player") PlayerEntity player);
}
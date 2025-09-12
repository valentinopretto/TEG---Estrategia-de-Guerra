package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {
    List<CardEntity> findByGame(GameEntity game);
    List<CardEntity> findByOwner(PlayerEntity owner);
    List<CardEntity> findByGameAndIsInDeckTrue(GameEntity game);
    List<CardEntity> findByGameAndOwnerIsNull(GameEntity game);
    List<CardEntity> findByOwnerAndType(PlayerEntity owner, CardType type);

    @Query("SELECT c FROM CardEntity c WHERE c.game = :game AND c.isInDeck = true ORDER BY FUNCTION('RAND')")
    List<CardEntity> findAvailableCardsRandomOrder(@Param("game") GameEntity game);

    @Query("SELECT COUNT(c) FROM CardEntity c WHERE c.owner = :player")
    Long countCardsByPlayer(@Param("player") PlayerEntity player);

    @Query("SELECT c FROM CardEntity c WHERE c.owner = :player AND c.type = :type")
    List<CardEntity> findPlayerCardsByType(@Param("player") PlayerEntity player, @Param("type") CardType type);

    @Query("SELECT c.type, COUNT(c) FROM CardEntity c WHERE c.owner = :player GROUP BY c.type")
    List<Object[]> countCardsByTypeForPlayer(@Param("player") PlayerEntity player);
}
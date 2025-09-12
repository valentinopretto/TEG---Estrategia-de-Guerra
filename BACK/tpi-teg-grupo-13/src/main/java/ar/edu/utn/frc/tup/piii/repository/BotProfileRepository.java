package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BotProfileRepository extends JpaRepository<BotProfileEntity, Long> {
    List<BotProfileEntity> findByLevel(BotLevel level);
    List<BotProfileEntity> findByStrategy(BotStrategy strategy);
    Optional<BotProfileEntity> findByLevelAndStrategy(BotLevel level, BotStrategy strategy);

    @Query("SELECT bp FROM BotProfileEntity bp WHERE bp.level = :level ORDER BY FUNCTION('RAND')")
    List<BotProfileEntity> findRandomBotsByLevel(@Param("level") BotLevel level);

    @Query("SELECT bp FROM BotProfileEntity bp ORDER BY FUNCTION('RAND')")
    List<BotProfileEntity> findAllRandomOrder();


}

package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameTerritoryRepository extends JpaRepository<GameTerritoryEntity, Long> {
    List<GameTerritoryEntity> findByGame(GameEntity game);
    List<GameTerritoryEntity> findByOwner(PlayerEntity owner);
    List<GameTerritoryEntity> findByGameAndOwner(GameEntity game, PlayerEntity owner);
    Optional<GameTerritoryEntity> findByGameAndCountry(GameEntity game, CountryEntity country);

    @Query("SELECT gt FROM GameTerritoryEntity gt WHERE gt.game = :game AND gt.armies > 1")
    List<GameTerritoryEntity> findTerritoriesCanAttack(@Param("game") GameEntity game);

    @Query("SELECT gt FROM GameTerritoryEntity gt WHERE gt.owner = :player AND gt.armies > 1")
    List<GameTerritoryEntity> findPlayerTerritoriesCanAttack(@Param("player") PlayerEntity player);

    @Query("SELECT SUM(gt.armies) FROM GameTerritoryEntity gt WHERE gt.owner = :player")
    Integer getTotalArmiesByPlayer(@Param("player") PlayerEntity player);

    @Query("SELECT COUNT(gt) FROM GameTerritoryEntity gt WHERE gt.owner = :player")
    Long countTerritoriesByPlayer(@Param("player") PlayerEntity player);

    @Query("SELECT gt FROM GameTerritoryEntity gt WHERE gt.game = :game AND gt.country.continent.name = :continentName")
    List<GameTerritoryEntity> findTerritoriesByContinentName(@Param("game") GameEntity game, @Param("continentName") String continentName);
}
package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.ContinentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContinentRepository extends JpaRepository<ContinentEntity, Long> {
    Optional<ContinentEntity> findByName(String name);

    @Query("SELECT c FROM ContinentEntity c ORDER BY c.bonusArmies DESC")
    List<ContinentEntity> findAllOrderByBonusArmiesDesc();

    @Query("SELECT c FROM ContinentEntity c WHERE SIZE(c.countries) = (SELECT MIN(SIZE(c2.countries)) FROM ContinentEntity c2)")
    List<ContinentEntity> findSmallestContinents();

    @Query("SELECT c FROM ContinentEntity c WHERE SIZE(c.countries) = (SELECT MAX(SIZE(c2.countries)) FROM ContinentEntity c2)")
    List<ContinentEntity> findLargestContinents();
}
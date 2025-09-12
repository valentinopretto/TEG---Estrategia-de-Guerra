package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.ContinentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<CountryEntity, Long> {
    Optional<CountryEntity> findByName(String name);

    List<CountryEntity> findByContinent(ContinentEntity continent);

    @Query("SELECT c FROM CountryEntity c WHERE c.continent.name = :continentName")
    List<CountryEntity> findByContinentName(@Param("continentName") String continentName);

    /*
     * /
     * 
     * @Query("SELECT c FROM CountryEntity c JOIN c.neighbors n WHERE n.id = :countryId"
     * )
     * List<CountryEntity> findNeighborsByCountryId(@Param("countryId") Long
     * countryId);
     * 
     * @Query("SELECT c FROM CountryEntity c WHERE SIZE(c.neighbors) = (SELECT MAX(SIZE(c2.neighbors)) FROM CountryEntity c2)"
     * )
     * List<CountryEntity> findCountriesWithMostNeighbors();
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM CountryEntity c JOIN c.neighbors n " +
            "WHERE (c.id = :id1 AND n.id = :id2) " +
            "OR (c.id = :id2 AND n.id = :id1)")
    boolean areCountriesNeighbors(@Param("id1") Long countryId1, @Param("id2") Long countryId2);

    @Query("SELECT c FROM CountryEntity c JOIN c.neighbors n WHERE n.id = :countryId")
    List<CountryEntity> findCountriesThatHaveAsNeighbor(@Param("countryId") Long countryId);
}
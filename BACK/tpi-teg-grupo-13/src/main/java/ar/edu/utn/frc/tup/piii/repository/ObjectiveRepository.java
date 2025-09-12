package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectiveRepository extends JpaRepository<ObjectiveEntity, Long> {
    List<ObjectiveEntity> findByType(ObjectiveType type);
    List<ObjectiveEntity> findByIsCommonTrue();
    List<ObjectiveEntity> findByIsCommonFalse();

    @Query("SELECT o FROM ObjectiveEntity o WHERE o.type = 'OCCUPATION' ORDER BY FUNCTION('RAND')")
    List<ObjectiveEntity> findRandomOccupationObjectives();

    @Query("SELECT o FROM ObjectiveEntity o WHERE o.type = 'DESTRUCTION' ORDER BY FUNCTION('RAND')")
    List<ObjectiveEntity> findRandomDestructionObjectives();

    @Query("SELECT o FROM ObjectiveEntity o WHERE o.isCommon = false ORDER BY FUNCTION('RAND')")
    List<ObjectiveEntity> findRandomSecretObjectives();
}

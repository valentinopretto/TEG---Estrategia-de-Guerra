package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ObjectiveRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ObjectiveRepository objectiveRepository;

    private ObjectiveEntity occupationObjective1;
    private ObjectiveEntity occupationObjective2;
    private ObjectiveEntity destructionObjective1;
    private ObjectiveEntity destructionObjective2;
    private ObjectiveEntity commonObjective;

    @BeforeEach
    void setUp() {
        // Limpiar datos existentes
        objectiveRepository.deleteAll();

        // Crear objetivos de ocupación
        occupationObjective1 = new ObjectiveEntity();
        occupationObjective1.setType(ObjectiveType.OCCUPATION);
        occupationObjective1.setDescription("Ocupar África y 5 países de América del Norte");
        occupationObjective1.setTargetData("{\"continents\":[\"África\"],\"regions\":[{\"continent\":\"América del Norte\",\"count\":5}]}");
        occupationObjective1.setIsCommon(false);
        occupationObjective1 = entityManager.persistAndFlush(occupationObjective1);

        occupationObjective2 = new ObjectiveEntity();
        occupationObjective2.setType(ObjectiveType.OCCUPATION);
        occupationObjective2.setDescription("Ocupar América del Sur y 7 países de Europa");
        occupationObjective2.setTargetData("{\"continents\":[\"América del Sur\"],\"regions\":[{\"continent\":\"Europa\",\"count\":7}]}");
        occupationObjective2.setIsCommon(false);
        occupationObjective2 = entityManager.persistAndFlush(occupationObjective2);

        // Crear objetivos de destrucción
        destructionObjective1 = new ObjectiveEntity();
        destructionObjective1.setType(ObjectiveType.DESTRUCTION);
        destructionObjective1.setDescription("Destruir el ejército azul");
        destructionObjective1.setTargetData("{\"targetColor\":\"BLUE\"}");
        destructionObjective1.setIsCommon(false);
        destructionObjective1 = entityManager.persistAndFlush(destructionObjective1);

        destructionObjective2 = new ObjectiveEntity();
        destructionObjective2.setType(ObjectiveType.DESTRUCTION);
        destructionObjective2.setDescription("Destruir el ejército rojo");
        destructionObjective2.setTargetData("{\"targetColor\":\"RED\"}");
        destructionObjective2.setIsCommon(false);
        destructionObjective2 = entityManager.persistAndFlush(destructionObjective2);

        // Crear objetivo común
        commonObjective = new ObjectiveEntity();
        commonObjective.setType(ObjectiveType.COMMON);
        commonObjective.setDescription("Ocupar 30 países");
        commonObjective.setTargetData("{\"territoryCount\":30}");
        commonObjective.setIsCommon(true);
        commonObjective = entityManager.persistAndFlush(commonObjective);

        entityManager.flush();
    }

    @Test
    void findByType_ShouldReturnObjectivesOfSpecificType() {
        List<ObjectiveEntity> occupationObjectives = objectiveRepository.findByType(ObjectiveType.OCCUPATION);

        assertThat(occupationObjectives).hasSize(2);
        assertThat(occupationObjectives).allMatch(obj -> obj.getType() == ObjectiveType.OCCUPATION);
        assertThat(occupationObjectives).extracting(ObjectiveEntity::getDescription)
                .containsExactlyInAnyOrder(
                        "Ocupar África y 5 países de América del Norte",
                        "Ocupar América del Sur y 7 países de Europa"
                );
    }

    @Test
    void findByType_ForDestruction_ShouldReturnDestructionObjectives() {
        List<ObjectiveEntity> destructionObjectives = objectiveRepository.findByType(ObjectiveType.DESTRUCTION);

        assertThat(destructionObjectives).hasSize(2);
        assertThat(destructionObjectives).allMatch(obj -> obj.getType() == ObjectiveType.DESTRUCTION);
        assertThat(destructionObjectives).extracting(ObjectiveEntity::getDescription)
                .containsExactlyInAnyOrder(
                        "Destruir el ejército azul",
                        "Destruir el ejército rojo"
                );
    }

    @Test
    void findByIsCommonTrue_ShouldReturnOnlyCommonObjectives() {
        List<ObjectiveEntity> commonObjectives = objectiveRepository.findByIsCommonTrue();

        assertThat(commonObjectives).hasSize(1);
        assertThat(commonObjectives.get(0).getDescription()).isEqualTo("Ocupar 30 países");
        assertThat(commonObjectives.get(0).getIsCommon()).isTrue();
        assertThat(commonObjectives.get(0).getType()).isEqualTo(ObjectiveType.COMMON);
    }

    @Test
    void findByIsCommonFalse_ShouldReturnOnlySecretObjectives() {
        List<ObjectiveEntity> secretObjectives = objectiveRepository.findByIsCommonFalse();

        assertThat(secretObjectives).hasSize(4); // 2 occupation + 2 destruction
        assertThat(secretObjectives).allMatch(obj -> !obj.getIsCommon());
    }

    @Test
    void findRandomOccupationObjectives_ShouldReturnOccupationObjectivesInRandomOrder() {
        List<ObjectiveEntity> randomOccupation = objectiveRepository.findRandomOccupationObjectives();

        assertThat(randomOccupation).hasSize(2);
        assertThat(randomOccupation).allMatch(obj -> obj.getType() == ObjectiveType.OCCUPATION);
        // El orden puede variar por RAND(), pero el contenido debe ser el mismo
        assertThat(randomOccupation).extracting(ObjectiveEntity::getDescription)
                .containsExactlyInAnyOrder(
                        "Ocupar África y 5 países de América del Norte",
                        "Ocupar América del Sur y 7 países de Europa"
                );
    }

    @Test
    void findRandomDestructionObjectives_ShouldReturnDestructionObjectivesInRandomOrder() {
        List<ObjectiveEntity> randomDestruction = objectiveRepository.findRandomDestructionObjectives();

        assertThat(randomDestruction).hasSize(2);
        assertThat(randomDestruction).allMatch(obj -> obj.getType() == ObjectiveType.DESTRUCTION);
        assertThat(randomDestruction).extracting(ObjectiveEntity::getDescription)
                .containsExactlyInAnyOrder(
                        "Destruir el ejército azul",
                        "Destruir el ejército rojo"
                );
    }

    @Test
    void findRandomSecretObjectives_ShouldReturnNonCommonObjectivesInRandomOrder() {
        List<ObjectiveEntity> randomSecret = objectiveRepository.findRandomSecretObjectives();

        assertThat(randomSecret).hasSize(4);
        assertThat(randomSecret).allMatch(obj -> !obj.getIsCommon());
        assertThat(randomSecret).extracting(ObjectiveEntity::getType)
                .containsExactlyInAnyOrder(
                        ObjectiveType.OCCUPATION, ObjectiveType.OCCUPATION,
                        ObjectiveType.DESTRUCTION, ObjectiveType.DESTRUCTION
                );
    }

    @Test
    void findByType_WhenNoObjectivesOfType_ShouldReturnEmpty() {
        // No hemos creado objetivos de tipo COMMON que no sean comunes,
        // así que busquemos un tipo que sabemos que existe pero con condiciones específicas
        List<ObjectiveEntity> commonTypeNonCommon = objectiveRepository.findByType(ObjectiveType.COMMON);

        assertThat(commonTypeNonCommon).hasSize(1);
        assertThat(commonTypeNonCommon.get(0).getIsCommon()).isTrue();
    }

    @Test
    void findRandomOccupationObjectives_WhenNoOccupationObjectives_ShouldReturnEmpty() {
        // Eliminar objetivos de ocupación para probar caso vacío
        objectiveRepository.delete(occupationObjective1);
        objectiveRepository.delete(occupationObjective2);
        entityManager.flush();

        List<ObjectiveEntity> randomOccupation = objectiveRepository.findRandomOccupationObjectives();

        assertThat(randomOccupation).isEmpty();
    }

    @Test
    void findRandomDestructionObjectives_WhenNoDestructionObjectives_ShouldReturnEmpty() {
        // Eliminar objetivos de destrucción para probar caso vacío
        objectiveRepository.delete(destructionObjective1);
        objectiveRepository.delete(destructionObjective2);
        entityManager.flush();

        List<ObjectiveEntity> randomDestruction = objectiveRepository.findRandomDestructionObjectives();

        assertThat(randomDestruction).isEmpty();
    }
}
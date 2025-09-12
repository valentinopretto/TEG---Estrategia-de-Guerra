package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.objective.ObjectiveResponseDto;
import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.model.Objective;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectiveMapperTest {

    private ObjectiveMapper objectiveMapper;
    private ObjectiveEntity objectiveEntity;
    private Objective objective;

    @BeforeEach
    void setUp() {
        objectiveMapper = new ObjectiveMapper();

        // Setup ObjectiveEntity
        objectiveEntity = new ObjectiveEntity();
        objectiveEntity.setId(1L);
        objectiveEntity.setType(ObjectiveType.OCCUPATION);
        objectiveEntity.setDescription("Occupy North America and South America");
        objectiveEntity.setTargetData("North America,South America");
        objectiveEntity.setIsCommon(false);

        // Setup Objective model
        objective = Objective.builder()
                .id(1L)
                .type(ObjectiveType.OCCUPATION)
                .description("Occupy North America and South America")
                .targetData("North America,South America")
                .isCommon(false)
                .isAchieved(true)
                .build();
    }

    @Test
    void toModel_WithValidEntity_ShouldMapCorrectly() {
        // When
        Objective result = objectiveMapper.toModel(objectiveEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(ObjectiveType.OCCUPATION);
        assertThat(result.getDescription()).isEqualTo("Occupy North America and South America");
        assertThat(result.getTargetData()).isEqualTo("North America,South America");
        assertThat(result.getIsCommon()).isFalse();
        assertThat(result.getIsAchieved()).isFalse(); // Always set to false in toModel
    }

    @Test
    void toModel_WithNullEntity_ShouldReturnNull() {
        // When
        Objective result = objectiveMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithCommonObjective_ShouldMapCorrectly() {
        // Given
        objectiveEntity.setIsCommon(true);
        objectiveEntity.setType(ObjectiveType.COMMON);
        objectiveEntity.setDescription("Common objective for all players");

        // When
        Objective result = objectiveMapper.toModel(objectiveEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsCommon()).isTrue();
        assertThat(result.getType()).isEqualTo(ObjectiveType.COMMON);
        assertThat(result.getDescription()).isEqualTo("Common objective for all players");
    }

    @Test
    void toModel_WithDestructionObjective_ShouldMapCorrectly() {
        // Given
        objectiveEntity.setType(ObjectiveType.DESTRUCTION);
        objectiveEntity.setDescription("Destroy the red player");
        objectiveEntity.setTargetData("RED");

        // When
        Objective result = objectiveMapper.toModel(objectiveEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(ObjectiveType.DESTRUCTION);
        assertThat(result.getTargetData()).isEqualTo("RED");
    }

    @Test
    void toModel_WithNullTargetData_ShouldMapCorrectly() {
        // Given
        objectiveEntity.setTargetData(null);

        // When
        Objective result = objectiveMapper.toModel(objectiveEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTargetData()).isNull();
    }

    @Test
    void toModel_WithEmptyTargetData_ShouldMapCorrectly() {
        // Given
        objectiveEntity.setTargetData("");

        // When
        Objective result = objectiveMapper.toModel(objectiveEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTargetData()).isEqualTo("");
    }

    @Test
    void toEntity_WithValidModel_ShouldMapCorrectly() {
        // When
        ObjectiveEntity result = objectiveMapper.toEntity(objective);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(ObjectiveType.OCCUPATION);
        assertThat(result.getDescription()).isEqualTo("Occupy North America and South America");
        assertThat(result.getTargetData()).isEqualTo("North America,South America");
        assertThat(result.getIsCommon()).isFalse();
    }

    @Test
    void toEntity_WithNullModel_ShouldReturnNull() {
        // When
        ObjectiveEntity result = objectiveMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithDestructionObjective_ShouldMapCorrectly() {
        // Given
        Objective destructionObjective = Objective.builder()
                .id(2L)
                .type(ObjectiveType.DESTRUCTION)
                .description("Destroy blue player")
                .targetData("BLUE")
                .isCommon(false)
                .isAchieved(false)
                .build();

        // When
        ObjectiveEntity result = objectiveMapper.toEntity(destructionObjective);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getType()).isEqualTo(ObjectiveType.DESTRUCTION);
        assertThat(result.getTargetData()).isEqualTo("BLUE");
    }

    @Test
    void toResponseDto_WithValidModel_ShouldMapCorrectly() {
        // When
        ObjectiveResponseDto result = objectiveMapper.toResponseDto(objective);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Occupy North America and South America");
        assertThat(result.getIsAchieved()).isTrue();
        assertThat(result.getIsCommon()).isFalse();
        assertThat(result.getType()).isEqualTo("OCCUPATION");
    }

    @Test
    void toResponseDto_WithNullModel_ShouldReturnNull() {
        // When
        ObjectiveResponseDto result = objectiveMapper.toResponseDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toResponseDto_WithNullType_ShouldMapCorrectly() {
        // Given
        objective.setType(null);

        // When
        ObjectiveResponseDto result = objectiveMapper.toResponseDto(objective);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Occupy North America and South America");
    }

    @Test
    void toResponseDto_WithAchievedObjective_ShouldMapCorrectly() {
        // Given
        objective.setIsAchieved(true);

        // When
        ObjectiveResponseDto result = objectiveMapper.toResponseDto(objective);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsAchieved()).isTrue();
    }

    @Test
    void toResponseDto_WithNotAchievedObjective_ShouldMapCorrectly() {
        // Given
        objective.setIsAchieved(false);

        // When
        ObjectiveResponseDto result = objectiveMapper.toResponseDto(objective);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsAchieved()).isFalse();
    }

    @Test
    void mappingRoundTrip_ShouldPreserveBasicData() {
        // When
        Objective mappedModel = objectiveMapper.toModel(objectiveEntity);
        ObjectiveEntity mappedBackEntity = objectiveMapper.toEntity(mappedModel);

        // Then
        assertThat(mappedBackEntity.getId()).isEqualTo(objectiveEntity.getId());
        assertThat(mappedBackEntity.getType()).isEqualTo(objectiveEntity.getType());
        assertThat(mappedBackEntity.getDescription()).isEqualTo(objectiveEntity.getDescription());
        assertThat(mappedBackEntity.getTargetData()).isEqualTo(objectiveEntity.getTargetData());
        assertThat(mappedBackEntity.getIsCommon()).isEqualTo(objectiveEntity.getIsCommon());
    }

    @Test
    void objective_getTargetContinents_ShouldParseCorrectly() {
        // Given
        Objective occupationObjective = Objective.builder()
                .type(ObjectiveType.OCCUPATION)
                .targetData("North America,South America,Europe")
                .build();

        // When
        List<String> continents = occupationObjective.getTargetContinents();

        // Then
        assertThat(continents).hasSize(3);
        assertThat(continents).containsExactly("North America", "South America", "Europe");
    }

    @Test
    void objective_getTargetColor_ShouldParseCorrectly() {
        // Given
        Objective destructionObjective = Objective.builder()
                .type(ObjectiveType.DESTRUCTION)
                .targetData("RED")
                .build();

        // When
        PlayerColor color = destructionObjective.getTargetColor();

        // Then
        assertThat(color).isEqualTo(PlayerColor.RED);
    }

    @Test
    void objective_setTargetColor_ShouldSetDataCorrectly() {
        // Given
        Objective destructionObjective = Objective.builder()
                .type(ObjectiveType.DESTRUCTION)
                .build();

        // When
        destructionObjective.setTargetColor(PlayerColor.BLUE);

        // Then
        assertThat(destructionObjective.getTargetData()).isEqualTo("BLUE");
    }

    @Test
    void objective_setTargetContinents_ShouldSetDataCorrectly() {
        // Given
        Objective occupationObjective = Objective.builder()
                .type(ObjectiveType.OCCUPATION)
                .build();
        List<String> continents = Arrays.asList("Asia", "Africa");

        // When
        occupationObjective.setTargetContinents(continents);

        // Then
        assertThat(occupationObjective.getTargetData()).isEqualTo("Asia,Africa");
    }

    @Test
    void toModel_WithLongDescription_ShouldMapCorrectly() {
        // Given
        String longDescription = "This is a very long objective description that contains a lot of text to test " +
                "how the mapper handles longer content. It should map correctly without any issues and preserve " +
                "all the content without truncation.";
        objectiveEntity.setDescription(longDescription);

        // When
        Objective result = objectiveMapper.toModel(objectiveEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo(longDescription);
    }

    @Test
    void toEntity_WithNullIsCommon_ShouldHandleCorrectly() {
        // Given
        objective.setIsCommon(null);

        // When
        ObjectiveEntity result = objectiveMapper.toEntity(objective);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsCommon()).isNull();
    }
}
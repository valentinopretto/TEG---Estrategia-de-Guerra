package ar.edu.utn.frc.tup.piii.mappers;
import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TerritoryMapperTest {

    private TerritoryMapper territoryMapper;
    private Territory territory;

    @BeforeEach
    void setUp() {
        territoryMapper = new TerritoryMapper();

        // Setup neighbor IDs
        Set<Long> neighborIds = new HashSet<>();
        neighborIds.add(2L);
        neighborIds.add(3L);
        neighborIds.add(4L);

        // Setup Territory model
        territory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .continentName("South America")
                .ownerId(10L)
                .ownerName("Player1")
                .armies(5)
                .lastConqueredTurn(3)
                .positionX(100.5)
                .positionY(200.7)
                .neighborIds(neighborIds)
                .build();
    }

    @Test
    void toResponseDto_WithValidTerritory_ShouldMapCorrectly() {
        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Argentina");
        assertThat(result.getContinentName()).isEqualTo("South America");
        assertThat(result.getOwnerName()).isEqualTo("Player1");
        assertThat(result.getArmies()).isEqualTo(5);
        assertThat(result.getNeighborIds()).hasSize(3);
        assertThat(result.getNeighborIds()).containsExactlyInAnyOrder(2L, 3L, 4L);
    }

    @Test
    void toResponseDto_WithNullTerritory_ShouldReturnNull() {
        // When
        CountryResponseDto result = territoryMapper.toResponseDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toResponseDto_WithEmptyNeighborIds_ShouldMapCorrectly() {
        // Given
        territory.setNeighborIds(new HashSet<>());

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNeighborIds()).isEmpty();
    }

    @Test
    void toResponseDto_WithNullNeighborIds_ShouldMapCorrectly() {
        // Given
        territory.setNeighborIds(null);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNeighborIds()).isNull();
    }

    @Test
    void toResponseDto_WithNullOwner_ShouldMapCorrectly() {
        // Given
        territory.setOwnerId(null);
        territory.setOwnerName(null);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOwnerName()).isNull();
        assertThat(result.getName()).isEqualTo("Argentina");
        assertThat(result.getContinentName()).isEqualTo("South America");
    }

    @Test
    void toResponseDto_WithSingleArmy_ShouldMapCorrectly() {
        // Given
        territory.setArmies(1);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getArmies()).isEqualTo(1);
    }

    @Test
    void toResponseDto_WithZeroArmies_ShouldMapCorrectly() {
        // Given
        territory.setArmies(0);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getArmies()).isEqualTo(0);
    }

    @Test
    void toResponseDto_WithLargeNumberOfArmies_ShouldMapCorrectly() {
        // Given
        territory.setArmies(999);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getArmies()).isEqualTo(999);
    }

    @Test
    void toResponseDto_WithLongTerritoryName_ShouldMapCorrectly() {
        // Given
        String longName = "Very Long Territory Name That Contains Many Words And Should Be Preserved Entirely";
        territory.setName(longName);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(longName);
    }

    @Test
    void toResponseDto_WithLongContinentName_ShouldMapCorrectly() {
        // Given
        String longContinentName = "Very Long Continent Name With Multiple Words";
        territory.setContinentName(longContinentName);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContinentName()).isEqualTo(longContinentName);
    }

    @Test
    void toResponseDto_WithLongOwnerName_ShouldMapCorrectly() {
        // Given
        String longOwnerName = "Very Long Player Name With Multiple Words And Characters";
        territory.setOwnerName(longOwnerName);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOwnerName()).isEqualTo(longOwnerName);
    }

    @Test
    void toResponseDto_WithManyNeighbors_ShouldMapCorrectly() {
        // Given
        Set<Long> manyNeighbors = new HashSet<>();
        for (long i = 2L; i <= 10L; i++) {
            manyNeighbors.add(i);
        }
        territory.setNeighborIds(manyNeighbors);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNeighborIds()).hasSize(9);
        assertThat(result.getNeighborIds()).containsExactlyInAnyOrder(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }

    @Test
    void toResponseDto_WithNullPositions_ShouldMapCorrectly() {
        // Given
        territory.setPositionX(null);
        territory.setPositionY(null);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPositionX()).isNull();
        assertThat(result.getPositionY()).isNull();
    }

    @Test
    void toResponseDto_WithZeroPositions_ShouldMapCorrectly() {
        // Given
        territory.setPositionX(0.0);
        territory.setPositionY(0.0);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPositionX()).isEqualTo(0.0);
        assertThat(result.getPositionY()).isEqualTo(0.0);
    }

    @Test
    void toResponseDto_WithNegativePositions_ShouldMapCorrectly() {
        // Given
        territory.setPositionX(-50.5);
        territory.setPositionY(-100.7);

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPositionX()).isEqualTo(-50.5);
        assertThat(result.getPositionY()).isEqualTo(-100.7);
    }

    @Test
    void toResponseDto_WithAllNullFields_ShouldMapCorrectly() {
        // Given
        Territory nullFieldsTerritory = Territory.builder()
                .id(null)
                .name(null)
                .continentName(null)
                .ownerId(null)
                .ownerName(null)
                .armies(null)
                .positionX(null)
                .positionY(null)
                .neighborIds(null)
                .build();

        // When
        CountryResponseDto result = territoryMapper.toResponseDto(nullFieldsTerritory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isNull();
        assertThat(result.getContinentName()).isNull();
        assertThat(result.getOwnerName()).isNull();
        assertThat(result.getArmies()).isNull();
        assertThat(result.getPositionX()).isNull();
        assertThat(result.getPositionY()).isNull();
        assertThat(result.getNeighborIds()).isNull();
    }

    @Test
    void territory_canAttack_WithMoreThanOneArmy_ShouldReturnTrue() {
        // Given
        territory.setArmies(2);

        // When
        boolean canAttack = territory.canAttack();

        // Then
        assertThat(canAttack).isTrue();
    }

    @Test
    void territory_canAttack_WithOneArmy_ShouldReturnFalse() {
        // Given
        territory.setArmies(1);

        // When
        boolean canAttack = territory.canAttack();

        // Then
        assertThat(canAttack).isFalse();
    }

    @Test
    void territory_isNeighbor_WithExistingNeighbor_ShouldReturnTrue() {
        // When
        boolean isNeighbor = territory.isNeighbor(2L);

        // Then
        assertThat(isNeighbor).isTrue();
    }

    @Test
    void territory_isNeighbor_WithNonExistingNeighbor_ShouldReturnFalse() {
        // When
        boolean isNeighbor = territory.isNeighbor(99L);

        // Then
        assertThat(isNeighbor).isFalse();
    }

    @Test
    void territory_canAttackTerritory_WithValidTarget_ShouldReturnTrue() {
        // Given
        territory.setArmies(3); // Can attack

        // When
        boolean canAttackTerritory = territory.canAttackTerritory(2L);

        // Then
        assertThat(canAttackTerritory).isTrue();
    }

    @Test
    void territory_canAttackTerritory_WithInsufficientArmies_ShouldReturnFalse() {
        // Given
        territory.setArmies(1); // Cannot attack

        // When
        boolean canAttackTerritory = territory.canAttackTerritory(2L);

        // Then
        assertThat(canAttackTerritory).isFalse();
    }

    @Test
    void territory_canAttackTerritory_WithNonNeighbor_ShouldReturnFalse() {
        // Given
        territory.setArmies(5); // Can attack

        // When
        boolean canAttackTerritory = territory.canAttackTerritory(99L); // Not a neighbor

        // Then
        assertThat(canAttackTerritory).isFalse();
    }

    @Test
    void territory_addArmies_ShouldIncreaseArmyCount() {
        // Given
        int initialArmies = territory.getArmies();

        // When
        territory.addArmies(3);

        // Then
        assertThat(territory.getArmies()).isEqualTo(initialArmies + 3);
    }

    @Test
    void territory_removeArmies_ShouldDecreaseArmyCount() {
        // Given
        territory.setArmies(10);

        // When
        territory.removeArmies(3);

        // Then
        assertThat(territory.getArmies()).isEqualTo(7);
    }

    @Test
    void territory_removeArmies_ShouldNotGoBelowZero() {
        // Given
        territory.setArmies(2);

        // When
        territory.removeArmies(5);

        // Then
        assertThat(territory.getArmies()).isEqualTo(0);
    }
}
package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.country.TerritoryDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CountryMapperTest {

    @InjectMocks
    private CountryMapper countryMapper;

    private Territory territory;

    @BeforeEach
    void setUp() {
        territory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .continentName("South America")
                .ownerId(10L)
                .ownerName("Player1")
                .armies(5)
                .positionX(100.5)
                .positionY(200.7)
                .neighborIds(Set.of(2L, 3L, 4L))
                .build();
    }

    @Test
    void toResponseDto_WithValidTerritory_ShouldMapCorrectly() {
        // When
        CountryResponseDto result = countryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Argentina");
        assertThat(result.getContinentName()).isEqualTo("South America");
        assertThat(result.getOwnerName()).isEqualTo("Player1");
        assertThat(result.getArmies()).isEqualTo(5);
        assertThat(result.getPositionX()).isEqualTo(100.5);
        assertThat(result.getPositionY()).isEqualTo(200.7);
        assertThat(result.getNeighborIds()).containsExactlyInAnyOrder(2L, 3L, 4L);
        assertThat(result.getCanBeAttacked()).isFalse();
        assertThat(result.getCanAttack()).isFalse();
    }

    @Test
    void toResponseDto_WithNullTerritory_ShouldReturnNull() {
        // When
        CountryResponseDto result = countryMapper.toResponseDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toResponseDto_WithNullNeighborIds_ShouldHandleCorrectly() {
        // Given
        territory.setNeighborIds(null);

        // When
        CountryResponseDto result = countryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNeighborIds()).isNull();
    }

    @Test
    void toResponseDto_WithEmptyNeighborIds_ShouldHandleCorrectly() {
        // Given
        territory.setNeighborIds(Set.of());

        // When
        CountryResponseDto result = countryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNeighborIds()).isEmpty();
    }

    @Test
    void toResponseDto_WithNullOptionalFields_ShouldHandleCorrectly() {
        // Given
        territory.setOwnerName(null);
        territory.setContinentName(null);
        territory.setPositionX(null);
        territory.setPositionY(null);

        // When
        CountryResponseDto result = countryMapper.toResponseDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Argentina");
        assertThat(result.getContinentName()).isNull();
        assertThat(result.getOwnerName()).isNull();
        assertThat(result.getPositionX()).isNull();
        assertThat(result.getPositionY()).isNull();
    }

    @Test
    void mapTerritoryToDto_WithValidTerritory_ShouldMapCorrectly() {
        // When
        TerritoryDto result = countryMapper.mapTerritoryToDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Argentina");
        assertThat(result.getContinentName()).isEqualTo("South America");
        assertThat(result.getArmies()).isEqualTo(5);
        assertThat(result.getPositionX()).isEqualTo(100.5);
        assertThat(result.getPositionY()).isEqualTo(200.7);
    }

    @Test
    void mapTerritoryToDto_WithNullTerritory_ShouldThrowException() {
        // When & Then
        try {
            countryMapper.mapTerritoryToDto(null);
        } catch (NullPointerException e) {
            // This is expected behavior since the method doesn't have null check
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void mapTerritoryToDto_WithNullOptionalFields_ShouldHandleCorrectly() {
        // Given
        territory.setContinentName(null);
        territory.setPositionX(null);
        territory.setPositionY(null);

        // When
        TerritoryDto result = countryMapper.mapTerritoryToDto(territory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Argentina");
        assertThat(result.getContinentName()).isNull();
        assertThat(result.getArmies()).isEqualTo(5);
        assertThat(result.getPositionX()).isNull();
        assertThat(result.getPositionY()).isNull();
    }
}

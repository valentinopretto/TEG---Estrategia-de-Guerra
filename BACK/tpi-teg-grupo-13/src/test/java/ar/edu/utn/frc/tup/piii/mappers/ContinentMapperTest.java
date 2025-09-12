package ar.edu.utn.frc.tup.piii.mappers;


import ar.edu.utn.frc.tup.piii.dtos.continent.ContinentResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.entities.ContinentEntity;
import ar.edu.utn.frc.tup.piii.model.Continent;
import ar.edu.utn.frc.tup.piii.model.Territory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ContinentMapperTest {

    @InjectMocks
    private ContinentMapper continentMapper;

    private Continent continent;
    private ContinentEntity continentEntity;
    private Map<Long, Territory> territories;
    private Territory territory1;
    private Territory territory2;

    @BeforeEach
    void setUp() {
        // Setup territories
        territory1 = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(10L)
                .ownerName("Player1")
                .armies(5)
                .build();

        territory2 = Territory.builder()
                .id(2L)
                .name("Brasil")
                .ownerId(10L)
                .ownerName("Player1")
                .armies(3)
                .build();

        territories = new HashMap<>();
        territories.put(1L, territory1);
        territories.put(2L, territory2);

        // Setup Continent model
        continent = Continent.builder()
                .id(1L)
                .name("South America")
                .bonusArmies(2)
                .countryIds(Arrays.asList(1L, 2L))
                .build();

        // Setup ContinentEntity
        continentEntity = new ContinentEntity();
        continentEntity.setId(1L);
        continentEntity.setName("South America");
        continentEntity.setBonusArmies(2);
    }

    @org.testng.annotations.Test
    void toResponseDto_WithValidData_ShouldMapCorrectly() {
        // When
        ContinentResponseDto result = continentMapper.toResponseDto(continent, territories);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("South America");
        assertThat(result.getBonusArmies()).isEqualTo(2);
        assertThat(result.getTotalCountries()).isEqualTo(2);
        assertThat(result.getControlledCountries()).isEqualTo(2);
        assertThat(result.getIsControlled()).isTrue();
        assertThat(result.getControllerName()).isEqualTo("Player1");
        assertThat(result.getCountries()).hasSize(2);

        // Verify countries mapping
        CountryResponseDto country1 = result.getCountries().get(0);
        assertThat(country1.getId()).isEqualTo(1L);
        assertThat(country1.getName()).isEqualTo("Argentina");
    }

    @Test
    void toResponseDto_WithNullModel_ShouldReturnNull() {
        // When
        ContinentResponseDto result = continentMapper.toResponseDto(null, territories);

        // Then
        assertThat(result).isNull();
    }


    @Test
    void toResponseDto_WithPartiallyControlledContinent_ShouldCalculateCorrectly() {
        // Given
        territory2.setOwnerId(20L); // Different owner
        territory2.setOwnerName("Player2");
        territories.put(2L, territory2);

        // When
        ContinentResponseDto result = continentMapper.toResponseDto(continent, territories);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getControlledCountries()).isEqualTo(2);
        assertThat(result.getIsControlled()).isFalse();
        assertThat(result.getControllerName()).isNull();
    }


    @Test
    void toEntity_WithValidModel_ShouldMapCorrectly() {
        // When
        ContinentEntity result = continentMapper.toEntity(continent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("South America");
        assertThat(result.getBonusArmies()).isEqualTo(2);
    }

    @Test
    void toEntity_WithNullModel_ShouldReturnNull() {
        // When
        ContinentEntity result = continentMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }
}

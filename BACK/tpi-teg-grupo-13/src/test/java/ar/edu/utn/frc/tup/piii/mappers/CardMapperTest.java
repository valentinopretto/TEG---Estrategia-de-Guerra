package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardMapperTest {

    private CardMapper cardMapper;
    private CardEntity cardEntity;
    private Card card;
    private CountryEntity countryEntity;
    private PlayerEntity playerEntity;

    @BeforeEach
    void setUp() {
        cardMapper = new CardMapper();

        // Setup Country
        countryEntity = new CountryEntity();
        countryEntity.setId(1L);
        countryEntity.setName("Argentina");

        // Setup Player
        playerEntity = new PlayerEntity();
        playerEntity.setId(2L);

        // Setup CardEntity
        cardEntity = new CardEntity();
        cardEntity.setId(1L);
        cardEntity.setCountry(countryEntity);
        cardEntity.setType(CardType.INFANTRY);
        cardEntity.setOwner(playerEntity);
        cardEntity.setIsInDeck(false);

        // Setup Card model
        card = Card.builder()
                .id(1L)
                .countryName("Argentina")
                .type(CardType.INFANTRY)
                .ownerId(2L)
                .isInDeck(false)
                .build();
    }

    @Test
    void toModel_WithValidEntity_ShouldMapCorrectly() {
        // When
        Card result = cardMapper.toModel(cardEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCountryName()).isEqualTo("Argentina");
        assertThat(result.getType()).isEqualTo(CardType.INFANTRY);
        assertThat(result.getOwnerId()).isEqualTo(2L);
        assertThat(result.getIsInDeck()).isFalse();
    }

    @Test
    void toModel_WithNullEntity_ShouldReturnNull() {
        // When
        Card result = cardMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithNullCountry_ShouldMapNullCountryName() {
        // Given
        cardEntity.setCountry(null);

        // When
        Card result = cardMapper.toModel(cardEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCountryName()).isNull();
        assertThat(result.getType()).isEqualTo(CardType.INFANTRY);
        assertThat(result.getOwnerId()).isEqualTo(2L);
    }

    @Test
    void toModel_WithNullOwner_ShouldMapNullOwnerId() {
        // Given
        cardEntity.setOwner(null);

        // When
        Card result = cardMapper.toModel(cardEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCountryName()).isEqualTo("Argentina");
        assertThat(result.getType()).isEqualTo(CardType.INFANTRY);
        assertThat(result.getOwnerId()).isNull();
    }

    @Test
    void toModel_WithWildcardType_ShouldMapCorrectly() {
        // Given
        cardEntity.setType(CardType.WILDCARD);
        cardEntity.setCountry(null); // Wildcards don't have countries

        // When
        Card result = cardMapper.toModel(cardEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(CardType.WILDCARD);
        assertThat(result.getCountryName()).isNull();
        assertThat(result.isWildcard()).isTrue();
    }

    @Test
    void toModel_WithDifferentCardTypes_ShouldMapCorrectly() {
        // Test CAVALRY
        cardEntity.setType(CardType.CAVALRY);
        Card cavalryResult = cardMapper.toModel(cardEntity);
        assertThat(cavalryResult.getType()).isEqualTo(CardType.CAVALRY);
        assertThat(cavalryResult.isWildcard()).isFalse();

        // Test CANNON
        cardEntity.setType(CardType.CANNON);
        Card cannonResult = cardMapper.toModel(cardEntity);
        assertThat(cannonResult.getType()).isEqualTo(CardType.CANNON);
        assertThat(cannonResult.isWildcard()).isFalse();
    }

    @Test
    void toModel_WithCardInDeck_ShouldMapCorrectly() {
        // Given
        cardEntity.setIsInDeck(true);
        cardEntity.setOwner(null); // Cards in deck have no owner

        // When
        Card result = cardMapper.toModel(cardEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsInDeck()).isTrue();
        assertThat(result.getOwnerId()).isNull();
    }

    @Test
    void toEntity_WithValidCard_ShouldMapCorrectly() {
        // When
        CardEntity result = cardMapper.toEntity(card);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(CardType.INFANTRY);
        assertThat(result.getIsInDeck()).isFalse();
        // Note: Country and Owner are not mapped in toEntity method
        assertThat(result.getCountry()).isNull();
        assertThat(result.getOwner()).isNull();
    }

    @Test
    void toEntity_WithNullCard_ShouldReturnNull() {
        // When
        CardEntity result = cardMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithWildcard_ShouldMapCorrectly() {
        // Given
        Card wildcardCard = Card.builder()
                .id(2L)
                .countryName(null)
                .type(CardType.WILDCARD)
                .ownerId(null)
                .isInDeck(true)
                .build();

        // When
        CardEntity result = cardMapper.toEntity(wildcardCard);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getType()).isEqualTo(CardType.WILDCARD);
        assertThat(result.getIsInDeck()).isTrue();
    }

    @Test
    void toEntity_WithDifferentCardTypes_ShouldMapCorrectly() {
        // Test CAVALRY
        Card cavalryCard = Card.builder()
                .id(3L)
                .type(CardType.CAVALRY)
                .isInDeck(false)
                .build();

        CardEntity cavalryResult = cardMapper.toEntity(cavalryCard);
        assertThat(cavalryResult.getType()).isEqualTo(CardType.CAVALRY);

        // Test CANNON
        Card cannonCard = Card.builder()
                .id(4L)
                .type(CardType.CANNON)
                .isInDeck(false)
                .build();

        CardEntity cannonResult = cardMapper.toEntity(cannonCard);
        assertThat(cannonResult.getType()).isEqualTo(CardType.CANNON);
    }

    @Test
    void mappingRoundTrip_ShouldPreserveBasicData() {
        // When
        Card mappedCard = cardMapper.toModel(cardEntity);
        CardEntity mappedBackEntity = cardMapper.toEntity(mappedCard);

        // Then
        assertThat(mappedBackEntity.getId()).isEqualTo(cardEntity.getId());
        assertThat(mappedBackEntity.getType()).isEqualTo(cardEntity.getType());
        assertThat(mappedBackEntity.getIsInDeck()).isEqualTo(cardEntity.getIsInDeck());
        // Note: Country and Owner relationships are not preserved in round trip
        // because toEntity doesn't map these relationships
    }

    @Test
    void toModel_WithNullIsInDeck_ShouldHandleCorrectly() {
        // Given
        cardEntity.setIsInDeck(null);

        // When
        Card result = cardMapper.toModel(cardEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsInDeck()).isNull();
    }

    @Test
    void card_isWildcard_ShouldWorkCorrectly() {
        // Test with wildcard
        Card wildcardCard = Card.builder()
                .type(CardType.WILDCARD)
                .build();
        assertThat(wildcardCard.isWildcard()).isTrue();

        // Test with non-wildcard
        Card infantryCard = Card.builder()
                .type(CardType.INFANTRY)
                .build();
        assertThat(infantryCard.isWildcard()).isFalse();
    }
}
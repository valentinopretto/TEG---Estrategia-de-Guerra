package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.card.CardResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.card.CardTradeDto;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.exceptions.BadRequestException;
import ar.edu.utn.frc.tup.piii.exceptions.GameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.CardMapper;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.CardRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private GameTerritoryService gameTerritoryService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private GameStateServiceImpl gameStateServiceImpl;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private CardEntity cardEntity;
    private Player player;
    private PlayerEntity playerEntity;
    private Game game;
    private GameEntity gameEntity;
    private CardResponseDto cardResponseDto;

    @BeforeEach
    void setUp() {
        card = Card.builder()
                .id(1L)
                .countryName("Argentina")
                .type(CardType.INFANTRY)
                .ownerId(1L)
                .isInDeck(false)
                .build();

        cardEntity = new CardEntity();
        cardEntity.setId(1L);
        cardEntity.setType(CardType.INFANTRY);
        cardEntity.setIsInDeck(false);

        player = Player.builder()
                .id(1L)
                .username("testPlayer")
                .color(PlayerColor.RED)
                .status(PlayerStatus.ACTIVE)
                .build();

        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setTradeCount(0);

        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .players(Arrays.asList(player))
                .build();

        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameCode("TEST123");

        cardResponseDto = CardResponseDto.builder()
                .id(1L)
                .countryName("Argentina")
                .type(CardType.INFANTRY)
                .isInDeck(false)
                .build();
    }

    @Test
    void testSaveCard() {
        when(cardMapper.toEntity(card)).thenReturn(cardEntity);
        when(cardRepository.save(cardEntity)).thenReturn(cardEntity);
        when(cardMapper.toModel(cardEntity)).thenReturn(card);

        Card saved = cardService.save(card);

        assertThat(saved).isNotNull();
        assertThat(saved.getType()).isEqualTo(CardType.INFANTRY);
        verify(cardRepository).save(cardEntity);
    }

    @Test
    void testSaveCardThrowsException() {
        when(cardMapper.toEntity(card)).thenReturn(cardEntity);
        when(cardRepository.save(cardEntity)).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> cardService.save(card))
                .isInstanceOf(GameStateException.class)
                .hasMessageContaining("Error saving card");
    }

    @Test
    void testFindByIdExists() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardEntity));
        when(cardMapper.toModel(cardEntity)).thenReturn(card);

        Optional<Card> result = cardService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(CardType.INFANTRY);
    }

    @Test
    void testFindByIdNotExists() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Card> result = cardService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindByIdWithNullId() {
        assertThatThrownBy(() -> cardService.findById(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Card ID cannot be null");
    }

    @Test
    void testFindAll() {
        List<CardEntity> entities = Arrays.asList(cardEntity);
        when(cardRepository.findAll()).thenReturn(entities);
        when(cardMapper.toModel(cardEntity)).thenReturn(card);

        List<Card> result = cardService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CardType.INFANTRY);
        verify(cardRepository).findAll();
    }

    @Test
    void testFindByGame() {
        List<CardEntity> entities = Arrays.asList(cardEntity);
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(cardRepository.findByGame(gameEntity)).thenReturn(entities);
        when(cardMapper.toModel(cardEntity)).thenReturn(card);

        List<Card> result = cardService.findByGame(game);

        assertThat(result).hasSize(1);
        verify(cardRepository).findByGame(gameEntity);
    }

    @Test
    void testFindByGameWithNullGame() {
        assertThatThrownBy(() -> cardService.findByGame(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Game cannot be null");
    }

    @Test
    void testFindByPlayer() {
        List<CardEntity> entities = Arrays.asList(cardEntity);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(entities);
        when(cardMapper.toModel(cardEntity)).thenReturn(card);

        List<Card> result = cardService.findByPlayer(player);

        assertThat(result).hasSize(1);
        verify(cardRepository).findByOwner(playerEntity);
    }

    @Test
    void testFindByPlayerNotFound() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.findByPlayer(player))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("Player not found with id: 1");
    }

    @Test
    void testDeleteById() {
        when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.deleteById(1L);

        verify(cardRepository).deleteById(1L);
    }

    @Test
    void testDeleteByIdNotExists() {
        when(cardRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.deleteById(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Card not found with id: 1");
    }

    @Test
    void testDeleteByIdWithNullId() {
        assertThatThrownBy(() -> cardService.deleteById(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Card ID cannot be null");
    }

    @Test
    void testGetPlayerCards() {
        List<CardEntity> entities = Arrays.asList(cardEntity);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(entities);
        when(cardMapper.toModel(cardEntity)).thenReturn(card);
        when(cardMapper.toResponseDto(card)).thenReturn(cardResponseDto);

        List<CardResponseDto> result = cardService.getPlayerCards(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CardType.INFANTRY);
    }

    @Test
    void testDrawCard() {
        Card availableCard = Card.builder()
                .id(2L)
                .type(CardType.CAVALRY)
                .isInDeck(true)
                .build();

        CardEntity availableCardEntity = new CardEntity();
        availableCardEntity.setId(2L);
        availableCardEntity.setType(CardType.CAVALRY);
        availableCardEntity.setIsInDeck(true);

        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(cardRepository.findByGameAndIsInDeckTrue(gameEntity)).thenReturn(Arrays.asList(availableCardEntity));
        when(cardMapper.toModel(availableCardEntity)).thenReturn(availableCard);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(availableCardEntity));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));

        Card drawnCard = cardService.drawCard(game, player);

        assertThat(drawnCard).isNotNull();
        verify(cardRepository).save(availableCardEntity);
    }

    @Test
    void testDrawCardNoAvailableCards() {
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(cardRepository.findByGameAndIsInDeckTrue(gameEntity)).thenReturn(Collections.emptyList());
        when(cardRepository.findByGameAndOwnerIsNull(gameEntity)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> cardService.drawCard(game, player))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No cards available in deck");
    }

    @Test
    void testGiveCardToPlayer() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardEntity));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));

        cardService.giveCardToPlayer(card, player);

        verify(cardRepository).save(cardEntity);
        assertThat(cardEntity.getOwner()).isEqualTo(playerEntity);
        assertThat(cardEntity.getIsInDeck()).isFalse();
    }

    @Test
    void testTradeCardsValid() {
        Card card1 = Card.builder().id(1L).type(CardType.INFANTRY).ownerId(1L).build();
        Card card2 = Card.builder().id(2L).type(CardType.CAVALRY).ownerId(1L).build();
        Card card3 = Card.builder().id(3L).type(CardType.CANNON).ownerId(1L).build();

        CardTradeDto tradeDto = CardTradeDto.builder()
                .playerId(1L)
                .cardIds(Arrays.asList(1L, 2L, 3L))
                .gameId(1L)
                .build();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(new CardEntity()));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(new CardEntity()));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(new CardEntity()));
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(card1)
                .thenReturn(card2)
                .thenReturn(card3);

        int tradeValue = cardService.tradeCards(tradeDto);

        assertThat(tradeValue).isEqualTo(4); // Primera trade: 1 * 3 + 1 = 4
        verify(playerRepository).save(playerEntity);
        assertThat(playerEntity.getTradeCount()).isEqualTo(1);
    }

    @Test
    void testTradeCardsPlayerNotFound() {
        CardTradeDto tradeDto = CardTradeDto.builder()
                .playerId(99L)
                .cardIds(Arrays.asList(1L, 2L, 3L))
                .build();

        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.tradeCards(tradeDto))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("Player not found");
    }

    @Test
    void testCanTradeCardsValid() {
        Card card1 = Card.builder().type(CardType.INFANTRY).build();
        Card card2 = Card.builder().type(CardType.CAVALRY).build();
        Card card3 = Card.builder().type(CardType.CANNON).build();
        List<Card> cards = Arrays.asList(card1, card2, card3);

        boolean result = cardService.canTradeCards(cards);

        assertThat(result).isTrue();
    }

    @Test
    void testCanTradeCardsInvalid() {
        Card card1 = Card.builder().type(CardType.INFANTRY).build();
        Card card2 = Card.builder().type(CardType.INFANTRY).build();
        List<Card> cards = Arrays.asList(card1, card2);

        boolean result = cardService.canTradeCards(cards);

        assertThat(result).isFalse();
    }

    @Test
    void testCalculateTradeValue() {
        assertThat(cardService.calculateTradeValue(1)).isEqualTo(4);  // 1 * 3 + 1
        assertThat(cardService.calculateTradeValue(2)).isEqualTo(7);  // 2 * 3 + 1
        assertThat(cardService.calculateTradeValue(3)).isEqualTo(10); // 3 * 3 + 1
        assertThat(cardService.calculateTradeValue(4)).isEqualTo(15); // (4-1) * 5
        assertThat(cardService.calculateTradeValue(5)).isEqualTo(20); // (5-1) * 5
    }

    @Test
    void testIsValidCardCombinationThreeOfSameType() {
        Card card1 = Card.builder().type(CardType.INFANTRY).build();
        Card card2 = Card.builder().type(CardType.INFANTRY).build();
        Card card3 = Card.builder().type(CardType.INFANTRY).build();
        List<Card> cards = Arrays.asList(card1, card2, card3);

        boolean result = cardService.isValidCardCombination(cards);

        assertThat(result).isTrue();
    }

    @Test
    void testIsValidCardCombinationOneOfEachType() {
        Card card1 = Card.builder().type(CardType.INFANTRY).build();
        Card card2 = Card.builder().type(CardType.CAVALRY).build();
        Card card3 = Card.builder().type(CardType.CANNON).build();
        List<Card> cards = Arrays.asList(card1, card2, card3);

        boolean result = cardService.isValidCardCombination(cards);

        assertThat(result).isTrue();
    }

    @Test
    void testIsValidCardCombinationWithWildcards() {
        Card card1 = Card.builder().type(CardType.WILDCARD).build();
        Card card2 = Card.builder().type(CardType.WILDCARD).build();
        Card card3 = Card.builder().type(CardType.WILDCARD).build();
        List<Card> cards = Arrays.asList(card1, card2, card3);

        boolean result = cardService.isValidCardCombination(cards);

        assertThat(result).isTrue();
    }

    @Test
    void testIsValidCardCombinationInvalid() {
        Card card1 = Card.builder().type(CardType.INFANTRY).build();
        Card card2 = Card.builder().type(CardType.INFANTRY).build();
        Card card3 = Card.builder().type(CardType.CAVALRY).build();
        List<Card> cards = Arrays.asList(card1, card2, card3);

        boolean result = cardService.isValidCardCombination(cards);

        assertThat(result).isFalse();
    }

    @Test
    void testHasPlayerMaxCards() {
        List<CardResponseDto> playerCards = Arrays.asList(
                cardResponseDto, cardResponseDto, cardResponseDto, cardResponseDto, cardResponseDto
        );
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(Arrays.asList(
                cardEntity, cardEntity, cardEntity, cardEntity, cardEntity
        ));
        when(cardMapper.toModel(any(CardEntity.class))).thenReturn(card);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(cardResponseDto);

        boolean result = cardService.hasPlayerMaxCards(player);

        assertThat(result).isTrue();
    }

    @Test
    void testMustTradeCards() {
        List<CardResponseDto> playerCards = Arrays.asList(
                cardResponseDto, cardResponseDto, cardResponseDto, cardResponseDto, cardResponseDto
        );
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(Arrays.asList(
                cardEntity, cardEntity, cardEntity, cardEntity, cardEntity
        ));
        when(cardMapper.toModel(any(CardEntity.class))).thenReturn(card);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(cardResponseDto);

        boolean result = cardService.mustTradeCards(player);

        assertThat(result).isTrue();
    }

    @Test
    void testGetMaxCardsAllowed() {
        int maxCards = cardService.getMaxCardsAllowed();

        assertThat(maxCards).isEqualTo(5);
    }

    @Test
    void testGetCardsByType() {
        List<CardEntity> entities = Arrays.asList(cardEntity);
        when(cardRepository.findAll()).thenReturn(entities);
        when(cardMapper.toModel(cardEntity)).thenReturn(card);

        List<Card> result = cardService.getCardsByType(CardType.INFANTRY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CardType.INFANTRY);
    }

    @Test
    void testGetCardsByTypeWithNullType() {
        assertThatThrownBy(() -> cardService.getCardsByType(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Card type cannot be null");
    }

    @Test
    void testCountCardsByType() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwnerAndType(playerEntity, CardType.INFANTRY))
                .thenReturn(Arrays.asList(cardEntity, cardEntity));

        int count = cardService.countCardsByType(player, CardType.INFANTRY);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testHasThreeOfSameType() {
        Card card1 = Card.builder().type(CardType.INFANTRY).build();
        Card card2 = Card.builder().type(CardType.INFANTRY).build();
        Card card3 = Card.builder().type(CardType.INFANTRY).build();
        List<Card> cards = Arrays.asList(card1, card2, card3);

        boolean result = cardService.hasThreeOfSameType(cards);

        assertThat(result).isTrue();
    }

    @Test
    void testHasOneOfEachType() {
        Card card1 = Card.builder().type(CardType.INFANTRY).build();
        Card card2 = Card.builder().type(CardType.CAVALRY).build();
        Card card3 = Card.builder().type(CardType.CANNON).build();
        List<Card> cards = Arrays.asList(card1, card2, card3);

        boolean result = cardService.hasOneOfEachType(cards);

        assertThat(result).isTrue();
    }

    @Test
    void testGetRandomCard() {
        CardEntity randomCardEntity = new CardEntity();
        randomCardEntity.setId(2L);
        randomCardEntity.setType(CardType.CAVALRY);

        Card randomCard = Card.builder()
                .id(2L)
                .type(CardType.CAVALRY)
                .build();

        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(cardRepository.findAvailableCardsRandomOrder(gameEntity))
                .thenReturn(Arrays.asList(randomCardEntity));
        when(cardMapper.toModel(randomCardEntity)).thenReturn(randomCard);

        Card result = cardService.getRandomCard(game);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(CardType.CAVALRY);
    }

    @Test
    void testGetRandomCardNoAvailableCards() {
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(cardRepository.findAvailableCardsRandomOrder(gameEntity))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> cardService.getRandomCard(game))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No cards available in deck");
    }

    @Test
    void testGetAllAvailableCards() {
        List<CardEntity> entities = Arrays.asList(cardEntity);
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(cardRepository.findByGameAndIsInDeckTrue(gameEntity)).thenReturn(entities);
        when(cardMapper.toModel(cardEntity)).thenReturn(card);

        List<Card> result = cardService.getAllAvailableCards(game);

        assertThat(result).hasSize(1);
        verify(cardRepository).findByGameAndIsInDeckTrue(gameEntity);
    }

    @Test
    void testCanPlayerTrade() {
        List<CardResponseDto> playerCards = Arrays.asList(cardResponseDto, cardResponseDto, cardResponseDto);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(Arrays.asList(cardEntity, cardEntity, cardEntity));
        when(cardMapper.toModel(any(CardEntity.class))).thenReturn(card);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(cardResponseDto);

        boolean result = cardService.canPlayerTrade(1L);

        assertThat(result).isTrue();
    }

    @Test
    void testMustPlayerTrade() {
        List<CardResponseDto> playerCards = Arrays.asList(
                cardResponseDto, cardResponseDto, cardResponseDto, cardResponseDto, cardResponseDto
        );
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(Arrays.asList(
                cardEntity, cardEntity, cardEntity, cardEntity, cardEntity
        ));
        when(cardMapper.toModel(any(CardEntity.class))).thenReturn(card);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(cardResponseDto);

        boolean result = cardService.mustPlayerTrade(1L);

        assertThat(result).isTrue();
    }

    @Test
    void testGetPlayerTradeCount() {
        playerEntity.setTradeCount(3);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));

        int tradeCount = cardService.getPlayerTradeCount(1L);

        assertThat(tradeCount).isEqualTo(3);
    }

    @Test
    void testGetPlayerTradeCountNotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        int tradeCount = cardService.getPlayerTradeCount(99L);

        assertThat(tradeCount).isEqualTo(0);
    }

    @Test
    void testGetNextTradeValue() {
        playerEntity.setTradeCount(2);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));

        int nextTradeValue = cardService.getNextTradeValue(1L);

        assertThat(nextTradeValue).isEqualTo(10); // calculateTradeValue(3) = 3 * 3 + 1 = 10
    }

    @Test
    void testCanClaimTerritoryBonus() {
        Territory territory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(1L)
                .build();

        CardResponseDto countryCard = CardResponseDto.builder()
                .id(1L)
                .countryName("Argentina")
                .type(CardType.INFANTRY)
                .build();

        when(gameTerritoryService.getTerritoryByGameAndCountryName(1L, "Argentina"))
                .thenReturn(territory);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(Arrays.asList(cardEntity));
        when(cardMapper.toModel(cardEntity)).thenReturn(card);
        when(cardMapper.toResponseDto(card)).thenReturn(countryCard);

        boolean result = cardService.canClaimTerritoryBonus(1L, 1L, "Argentina");

        assertThat(result).isTrue();
    }

    @Test
    void testCanClaimTerritoryBonusNotOwner() {
        Territory territory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(2L) // Different owner
                .build();

        when(gameTerritoryService.getTerritoryByGameAndCountryName(1L, "Argentina"))
                .thenReturn(territory);

        boolean result = cardService.canClaimTerritoryBonus(1L, 1L, "Argentina");

        assertThat(result).isFalse();
    }

    @Test
    void testClaimTerritoryBonus() {
        Territory territory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(1L)
                .build();

        CardResponseDto countryCard = CardResponseDto.builder()
                .id(1L)
                .countryName("Argentina")
                .type(CardType.INFANTRY)
                .build();

        when(gameTerritoryService.getTerritoryByGameAndCountryName(1L, "Argentina"))
                .thenReturn(territory);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerEntity));
        when(cardRepository.findByOwner(playerEntity)).thenReturn(Arrays.asList(cardEntity));
        when(cardMapper.toModel(cardEntity)).thenReturn(card);
        when(cardMapper.toResponseDto(card)).thenReturn(countryCard);

        cardService.claimTerritoryBonus(1L, 1L, "Argentina");

        verify(gameTerritoryService).addArmiesToTerritory(1L, 1L, 2);
    }

    @Test
    void testClaimTerritoryBonusNotEligible() {
        Territory territory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(2L) // Different owner
                .build();

        when(gameTerritoryService.getTerritoryByGameAndCountryName(1L, "Argentina"))
                .thenReturn(territory);

        assertThatThrownBy(() -> cardService.claimTerritoryBonus(1L, 1L, "Argentina"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No eligible for territory bonus");
    }
}
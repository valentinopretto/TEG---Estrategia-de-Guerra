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
import ar.edu.utn.frc.tup.piii.repository.CardRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.CardService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private GameStateServiceImpl gameStateServiceImpl;


    @Autowired
    private PlayerMapper playerMapper;

    // Configuración del juego TEG
    private static final int MAX_CARDS_ALLOWED = 5;
    private static final int MUST_TRADE_CARDS_THRESHOLD = 5;
    private static final int CARDS_REQUIRED_FOR_TRADE = 3;

    @Override
    public Card save(Card card) {
        try {
            CardEntity entity = cardMapper.toEntity(card);
            return cardMapper.toModel(cardRepository.save(entity));
        } catch (Exception e) {
            throw new GameStateException("Error saving card: " + e.getMessage());
        }
    }

    @Override
    public Optional<Card> findById(Long id) {

        if (id == null) {
            throw new BadRequestException("Card ID cannot be null");
        }
        return cardRepository.findById(id).map(cardMapper::toModel);
    }

    @Override
    public List<Card> findAll() {
        return cardRepository.findAll().stream()
                .map(cardMapper::toModel)
                .toList();
    }

    @Override
    public List<Card> findByGame(Game game) {
        if (game == null || game.getId() == null) {
            throw new BadRequestException("Game cannot be null");
        }
        GameEntity gameEntity = gameMapper.toEntity(game);
        return cardRepository.findByGame(gameEntity).stream()
                .map(cardMapper::toModel)
                .toList();
    }

    @Override
    public List<Card> findByPlayer(Player player) {
        if (player == null || player.getId() == null) {
            throw new BadRequestException("Player cannot be null");
        }

        PlayerEntity playerEntity = playerRepository.findById(player.getId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + player.getId()));

        return cardRepository.findByOwner(playerEntity).stream()
                .map(cardMapper::toModel)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new BadRequestException("Card ID cannot be null");
        }
        if (!cardRepository.existsById(id)) {
            throw new BadRequestException("Card not found with id: " + id);
        }
        cardRepository.deleteById(id);
    }

    @Override
    public List<CardResponseDto> getPlayerCards(Long playerId) {
        if (playerId == null) {
            throw new BadRequestException("Player ID cannot be null");
        }
        PlayerEntity playerEntity = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));

        return cardRepository.findByOwner(playerEntity).stream()
                .map(card -> cardMapper.toResponseDto(cardMapper.toModel(card)))
                .toList();
    }

    @Override
    public Card drawCard(Game game, Player player) {

        List<Card> availableCards = getAllAvailableCards(game);

        if (availableCards.isEmpty()) {
            recycleDeck(game);
            availableCards = getAllAvailableCards(game);

            if (availableCards.isEmpty()) {
                throw new IllegalStateException("No cards available in deck for game: " + game.getId());
            }
        }

        // Tomar una carta aleatoria en lugar de la primera
        Collections.shuffle(availableCards);
        Card card = availableCards.get(0);

        giveCardToPlayer(card, player);
        return card;
    }

    @Override
    public List<Card> drawCards(Game game, Player player, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        List<Card> availableCards = getAllAvailableCards(game);

        if (availableCards.size() < count) {
            recycleDeck(game);
            availableCards = getAllAvailableCards(game);

            if (availableCards.size() < count) {
                throw new IllegalStateException("Not enough cards available in deck");
            }
        }

        List<Card> drawnCards = availableCards.stream()
                .limit(count)
                .collect(Collectors.toList());

        drawnCards.forEach(card -> giveCardToPlayer(card, player));
        return drawnCards;
    }

    @Override
    public void giveCardToPlayer(Card card, Player player) {
        if (card == null || card.getId() == null) {
            throw new BadRequestException("Card cannot be null");
        }
        if (player == null || player.getId() == null) {
            throw new BadRequestException("Player cannot be null");
        }

        CardEntity cardEntity = cardRepository.findById(card.getId())
                .orElseThrow(() -> new BadRequestException("Card not found with id: " + card.getId()));

        PlayerEntity playerEntity = playerRepository.findById(player.getId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + player.getId()));

        cardEntity.setOwner(playerEntity);
        cardEntity.setIsInDeck(false);
        cardRepository.save(cardEntity);
    }

    @Override
    public int tradeCards(CardTradeDto tradeDto) {
        // Validar que el jugador existe
        PlayerEntity playerEntity = playerRepository.findById(tradeDto.getPlayerId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        // Obtener las cartas del intercambio
        List<Card> cardsToTrade = tradeDto.getCardIds().stream()
                .map(id -> findById(id).orElseThrow(() -> new BadRequestException("Card not found with id: " + id)))
                .collect(Collectors.toList());

        // Validar que las cartas pertenecen al jugador
        boolean allCardsOwnedByPlayer = cardsToTrade.stream()
                .allMatch(card -> Objects.equals(card.getOwnerId(), tradeDto.getPlayerId()));

        if (!allCardsOwnedByPlayer) {
            throw new BadRequestException("Player does not own all specified cards");
        }

        // Validar que se puede hacer el intercambio
        if (!canTradeCards(cardsToTrade)) {
            throw new BadRequestException("Invalid card combination for trade");
        }

        // INCREMENTAR EL CONTADOR DE TRADES
        playerEntity.setTradeCount(playerEntity.getTradeCount() + 1);
        playerRepository.save(playerEntity);

        // CALCULAR EL VALOR USANDO LA NUEVA LÓGICA
        int tradeValue = calculateTradeValue(playerEntity.getTradeCount());

        // Devolver las cartas al mazo
        returnCardsToDeck(cardsToTrade);

        return tradeValue;
    }

    @Override
    public boolean canTradeCards(List<Card> cards) {
        if (cards == null || cards.size() != CARDS_REQUIRED_FOR_TRADE) {
            return false;
        }

        return isValidCardCombination(cards);
    }

    @Override
    public int calculateTradeValue(int tradeNumber) {
        if (tradeNumber <= 3) {
            return tradeNumber * 3 + 1;
        } else {
            return (tradeNumber - 1) * 5;
        }
    }

    @Override
    public boolean isValidCardCombination(List<Card> cards) {
        if (cards == null || cards.size() != CARDS_REQUIRED_FOR_TRADE) {
            return false;
        }

        // Contar wildcards
        long wildcardCount = cards.stream()
                .filter(Card::isWildcard)
                .count();

        // Si hay 3 wildcards, es válido
        if (wildcardCount == 3) {
            return true;
        }

        // Si hay wildcards, pueden completar cualquier combinación
        if (wildcardCount > 0) {
            return true; // Los wildcards pueden completar cualquier combinación
        }

        // Sin wildcards: verificar 3 iguales o 3 diferentes
        return hasThreeOfSameType(cards) || hasOneOfEachType(cards);
    }

    // ========== VALIDACIONES ==========

    @Override
    public boolean hasPlayerMaxCards(Player player) {
        int cardCount = getPlayerCards(player.getId()).size();
        return cardCount >= MAX_CARDS_ALLOWED;
    }

    @Override
    public boolean mustTradeCards(Player player) {
        int cardCount = getPlayerCards(player.getId()).size();
        return cardCount >= MUST_TRADE_CARDS_THRESHOLD;
    }

    @Override
    public int getMaxCardsAllowed() {
        return MAX_CARDS_ALLOWED;
    }

    // ========== TIPOS DE CARTAS ==========

    @Override
    public List<Card> getCardsByType(CardType type) {
        if (type == null) {
            throw new BadRequestException("Card type cannot be null");
        }
        return cardRepository.findAll().stream()
                .filter(card -> card.getType() == type)
                .map(cardMapper::toModel)
                .toList();
    }

    @Override
    public int countCardsByType(Player player, CardType type) {
        if (player == null || player.getId() == null) {
            throw new BadRequestException("Player cannot be null");
        }
        if (type == null) {
            throw new BadRequestException("Card type cannot be null");
        }
        PlayerEntity playerEntity = playerRepository.findById(player.getId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        return cardRepository.findByOwnerAndType(playerEntity, type).size();
    }

    @Override
    public boolean hasThreeOfSameType(List<Card> cards) {

        if (cards == null || cards.size() != CARDS_REQUIRED_FOR_TRADE) {
            return false;
        }

        Map<CardType, Long> typeCount = cards.stream()
                .filter(card -> !card.isWildcard())
                .collect(Collectors.groupingBy(Card::getType, Collectors.counting()));

        return typeCount.values().stream().anyMatch(count -> count >= 3);
    }

    @Override
    public boolean hasOneOfEachType(List<Card> cards) {
        if (cards == null || cards.size() != CARDS_REQUIRED_FOR_TRADE) {
            return false;
        }

        Set<CardType> nonWildcardTypes = cards.stream()
                .filter(card -> !card.isWildcard())
                .map(Card::getType)
                .collect(Collectors.toSet());

        // Para tener uno de cada tipo (sin wildcards), necesitamos exactamente 3 tipos diferentes
        return nonWildcardTypes.size() == 3;
    }

    // ========== UTILIDADES ==========

    @Override
    public void shufflePlayerHand(Player player) {
        if (player == null || player.getId() == null) {
            throw new BadRequestException("Player cannot be null");
        }
        List<CardResponseDto> playerCards = getPlayerCards(player.getId());

    }

    @Override
    public Card getRandomCard(Game game) {
        if (game == null || game.getId() == null) {
            throw new BadRequestException("Game cannot be null");
        }

        GameEntity gameEntity = gameMapper.toEntity(game);
        List<CardEntity> availableCards = cardRepository.findAvailableCardsRandomOrder(gameEntity);

        if (availableCards.isEmpty()) {
            throw new IllegalStateException("No cards available in deck");
        }

        return cardMapper.toModel(availableCards.get(0));
    }

    @Override
    public List<Card> getAllAvailableCards(Game game) {
        if (game == null || game.getId() == null) {
            throw new BadRequestException("Game cannot be null");
        }
        GameEntity gameEntity = gameMapper.toEntity(game);
        return cardRepository.findByGameAndIsInDeckTrue(gameEntity).stream()
                .map(cardMapper::toModel)
                .toList();
    }

    @Override
    public boolean canPlayerTrade(Long playerId) {
        List<CardResponseDto> playerCards = getPlayerCards(playerId);
        return playerCards.size() >= 3;
    }

    @Override
    public boolean mustPlayerTrade(Long playerId) {
        List<CardResponseDto> playerCards = getPlayerCards(playerId);
        return playerCards.size() >= getMaxCardsAllowed();
    }

    /**
     * Otorga una carta al jugador si ha conquistado al menos un territorio en su turno
     */
    public void grantCardIfEligible(Player player, Game game) {

        if (GameStateServiceImpl.hasPlayerConqueredThisTurn(game.getId(), player.getId())) {
            drawCard(game, player);
        }

    }

    private void recycleDeck(Game game) {
        GameEntity gameEntity = gameMapper.toEntity(game);
        List<CardEntity> usedCards = cardRepository.findByGameAndOwnerIsNull(gameEntity);

        usedCards.forEach(card -> {
            card.setIsInDeck(true);
            card.setOwner(null);
        });

        cardRepository.saveAll(usedCards);
    }

    private void returnCardsToDeck(List<Card> cards) {
        List<CardEntity> cardEntities = cards.stream()
                .map(card -> cardRepository.findById(card.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Card not found")))
                .collect(Collectors.toList());

        cardEntities.forEach(card -> {
            card.setOwner(null);
            card.setIsInDeck(true);
        });

        cardRepository.saveAll(cardEntities);
    }

/*
     * Obtiene el número de intercambios realizados por el jugador
     * (Esto requeriría una tabla adicional para tracking, por ahora retorna 0)
*/
    @Override
    public int getPlayerTradeCount(Long playerId) {
        return playerRepository.findById(playerId)
                .map(PlayerEntity::getTradeCount)
                .orElse(0);
    }

    @Override
    public int getNextTradeValue(Long playerId) {
        int currentTradeCount = getPlayerTradeCount(playerId);
        return calculateTradeValue(currentTradeCount + 1);
    }

    /**
     * Verifica si el jugador puede recibir el premio de +2 ejércitos
     * por tener un país y su carta correspondiente.
     */
    public boolean canClaimTerritoryBonus(Long gameId, Long playerId, String countryName) {
        // Buscar el país por nombre
        // Necesitarás un método en GameTerritoryService para esto
        Territory territory = gameTerritoryService.getTerritoryByGameAndCountryName(gameId, countryName);
        if (territory == null || !playerId.equals(territory.getOwnerId())) {
            return false;
        }

        // Verificar que el jugador tiene la carta de ese país
        List<CardResponseDto> playerCards = getPlayerCards(playerId);
        return playerCards.stream()
                .anyMatch(card -> countryName.equals(card.getCountryName()));
    }

    /**
     * Aplica el premio de +2 ejércitos por coincidencia país-carta.
     */
    @Transactional
    public void claimTerritoryBonus(Long gameId, Long playerId, String countryName) {
        if (!canClaimTerritoryBonus(gameId, playerId, countryName)) {
            throw new BadRequestException("No eligible for territory bonus");
        }

        // Buscar el territorio por nombre y agregar ejércitos
        Territory territory = gameTerritoryService.getTerritoryByGameAndCountryName(gameId, countryName);
        gameTerritoryService.addArmiesToTerritory(gameId, territory.getId(), 2);
    }

}
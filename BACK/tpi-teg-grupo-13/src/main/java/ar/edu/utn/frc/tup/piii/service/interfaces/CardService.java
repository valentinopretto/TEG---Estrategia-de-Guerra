package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.card.CardResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.card.CardTradeDto;
import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import java.util.List;
import java.util.Optional;

public interface CardService {

    Card save(Card card);
    Optional<Card> findById(Long id);
    List<Card> findAll();
    List<Card> findByGame(Game game);
    List<Card> findByPlayer(Player player);
    void deleteById(Long id);

    List<CardResponseDto> getPlayerCards(Long playerId);
    Card drawCard(Game game, Player player);
    List<Card> drawCards(Game game, Player player, int count);
    void giveCardToPlayer(Card card, Player player);

    int tradeCards(CardTradeDto tradeDto);
    boolean canTradeCards(List<Card> cards);

    int calculateTradeValue(int tradeNumber);

    boolean isValidCardCombination(List<Card> cards);

    boolean hasPlayerMaxCards(Player player);

    boolean mustTradeCards(Player player);

    int getMaxCardsAllowed();

    List<Card> getCardsByType(CardType type);

    int countCardsByType(Player player, CardType type);

    boolean hasThreeOfSameType(List<Card> cards);
    boolean hasOneOfEachType(List<Card> cards);

    void shufflePlayerHand(Player player);

    Card getRandomCard(Game game);

    List<Card> getAllAvailableCards(Game game);

    boolean canPlayerTrade(Long playerId);

    boolean mustPlayerTrade(Long playerId);
    int getPlayerTradeCount(Long playerId);
    int getNextTradeValue(Long playerId);

    void claimTerritoryBonus(Long gameId, Long playerId, String countryName);
}
package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.card.CardResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.card.CardTradeDto;
import ar.edu.utn.frc.tup.piii.dtos.card.GiveCardDto;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import ar.edu.utn.frc.tup.piii.service.interfaces.CardService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private GameService gameService;

    private GiveCardDto giveCardDto;
    private Player player;
    private Game game;
    private Card card;

    @BeforeEach
    void setUp() {
        giveCardDto = new GiveCardDto("GAME123", 1L);
        player = new Player();
        player.setId(1L);
        game = new Game();
        card = new Card();
        card.setId(100L);
        card.setCountryName("Argentina");
        card.setType(CardType.valueOf("INFANTRY"));
    }

    @Test
    void giveCardToPlayer_ShouldReturnCardResponseDto() throws Exception {
        when(gameService.findByGameCode("GAME123")).thenReturn(game);
        when(playerService.findById(1L)).thenReturn(Optional.of(player));
        when(cardService.drawCard(game, player)).thenReturn(card);

        mockMvc.perform(post("/api/cards/give-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(giveCardDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.countryName").value("Argentina"))
                .andExpect(jsonPath("$.type").value("INFANTRY"));
    }

    @Test
    void getPlayerCards_ShouldReturnListOfCards() throws Exception {
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(100L)
                .countryName("Chile")
                .type(CardType.valueOf("CAVALRY"))
                .build();
        when(cardService.getPlayerCards(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/cards/player/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].countryName").value("Chile"));
    }

    @Test
    void canPlayerTrade_ShouldReturnTrue_WhenPlayerHasThreeOrMoreCards() throws Exception {
        when(cardService.getPlayerCards(1L)).thenReturn(List.of(new CardResponseDto(), new CardResponseDto(), new CardResponseDto()));

        mockMvc.perform(get("/api/cards/player/1/can-trade"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void canPlayerTrade_ShouldReturnFalse_WhenPlayerHasLessThanThreeCards() throws Exception {
        when(cardService.getPlayerCards(1L)).thenReturn(Collections.singletonList(new CardResponseDto()));

        mockMvc.perform(get("/api/cards/player/1/can-trade"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void claimTerritoryBonus_ShouldReturnSuccessMessage() throws Exception {
        CardController.TerritoryBonusDto dto = CardController.TerritoryBonusDto.builder()
                .gameId(1L)
                .playerId(1L)
                .countryName("Brasil")
                .build();

        mockMvc.perform(post("/api/cards/territory-bonus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.armiesAdded").value(2));
    }

    @Test
    void tradeCards_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CardTradeDto invalidDto = new CardTradeDto();

        mockMvc.perform(post("/api/cards/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void tradeCards_ShouldReturnTradeResult() throws Exception {
        CardTradeDto tradeDto = CardTradeDto.builder()
                .playerId(1L)
                .cardIds(List.of(1L, 2L, 3L))
                .gameId(1L)
                .build();

        when(cardService.tradeCards(any(CardTradeDto.class))).thenReturn(7);
        when(cardService.getPlayerTradeCount(1L)).thenReturn(2);
        when(cardService.getNextTradeValue(1L)).thenReturn(10);

        mockMvc.perform(post("/api/cards/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.armiesReceived").value(7))
                .andExpect(jsonPath("$.completedTrades").value(2))
                .andExpect(jsonPath("$.nextTradeValue").value(10));
    }

    @Test
    void tradeCards_ShouldReturn400_WhenInvalidData() throws Exception {
        CardTradeDto invalidDto = CardTradeDto.builder()
                .playerId(1L)
                .cardIds(List.of(1L, 2L)) // Solo 2 cartas en lugar de 3
                .build();

        mockMvc.perform(post("/api/cards/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tradeCards_ShouldReturn400_WhenPlayerNotFound() throws Exception {
        CardTradeDto tradeDto = CardTradeDto.builder()
                .playerId(1L)
                .cardIds(List.of(1L, 2L, 3L))
                .gameId(1L)
                .build();

        when(cardService.tradeCards(any())).thenThrow(new RuntimeException("Player not found"));

        mockMvc.perform(post("/api/cards/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tradeCards_ShouldReturn400_WhenNullPlayerId() throws Exception {
        CardTradeDto invalidDto = CardTradeDto.builder()
                .cardIds(List.of(1L, 2L, 3L))
                .build();

        mockMvc.perform(post("/api/cards/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void tradeCards_ShouldReturn400_WhenGameIdIsNull() throws Exception {
        CardTradeDto tradeDto = CardTradeDto.builder()
                .playerId(1L)
                .cardIds(List.of(1L, 2L, 3L))
                // gameId es null
                .build();

        mockMvc.perform(post("/api/cards/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tradeCards_ShouldReturn400_WhenNotExactly3Cards() throws Exception {
        CardTradeDto tradeDto = CardTradeDto.builder()
                .playerId(1L)
                .cardIds(List.of(1L, 2L)) // Solo 2 cartas
                .gameId(1L)
                .build();

        mockMvc.perform(post("/api/cards/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDto)))
                .andExpect(status().isBadRequest());
    }
}
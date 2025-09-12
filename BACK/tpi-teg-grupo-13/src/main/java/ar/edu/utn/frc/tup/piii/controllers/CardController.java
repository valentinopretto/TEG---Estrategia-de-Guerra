package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.card.CardResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.card.CardTradeDto;
import ar.edu.utn.frc.tup.piii.dtos.card.GiveCardDto;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.service.impl.CardServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.CardService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cards")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "Cards", description = "Gestión de cartas del juego TEG")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameService gameService;

    @PostMapping("/give-card")
    @Operation(summary = "Entregar carta a jugador",
            description = "Entrega una carta aleatoria del mazo a un jugador específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Carta entregada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Jugador o juego no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "No hay cartas disponibles en el mazo",
                    content = @Content)
    })
    public ResponseEntity<CardResponseDto> giveCardToPlayer(
            @Valid @RequestBody GiveCardDto giveCardDto) {

        // Obtener el juego
        Game game = gameService.findByGameCode(giveCardDto.getGameCode());

        // Obtener el jugador
        Player player = playerService.findById(giveCardDto.getPlayerId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        // Entregar carta
        Card drawnCard = cardService.drawCard(game, player);

        // Convertir a DTO de respuesta
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(drawnCard.getId())
                .countryName(drawnCard.getCountryName())
                .type(drawnCard.getType())
                .build();

        return ResponseEntity.ok(responseDto);
    }


    /**
     * Obtiene todas las cartas de un jugador específico
     */
    @GetMapping("/player/{playerId}")
    @Operation(summary = "Obtener cartas del jugador",
            description = "Devuelve todas las cartas que posee un jugador específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Lista de cartas obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CardResponseDto.class)))),
            @ApiResponse(responseCode = "404",
                    description = "Jugador no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "ID de jugador inválido",
                    content = @Content)
    })
    public ResponseEntity<List<CardResponseDto>> getPlayerCards(
            @PathVariable Long playerId) {
        List<CardResponseDto> playerCards = cardService.getPlayerCards(playerId);
        return ResponseEntity.ok(playerCards);
    }

    @PostMapping("/trade")
    @Operation(summary = "Intercambiar cartas por ejércitos",
            description = "Permite intercambiar exactamente 3 cartas por ejércitos según las reglas del TEG. " +
                    "El valor de ejércitos incrementa con cada intercambio: 1°=4, 2°=7, 3°=10, 4°=15, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Intercambio realizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeResultDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Datos de intercambio inválidos (cartas no válidas, no pertenecen al jugador, etc.)",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Jugador o cartas no encontrados",
                    content = @Content)
    })
    public ResponseEntity<TradeResultDto> tradeCards(
            @Valid @RequestBody CardTradeDto tradeDto) {
        int armiesReceived = cardService.tradeCards(tradeDto);
        int newTradeCount = cardService.getPlayerTradeCount(tradeDto.getPlayerId());
        int nextTradeValue = cardService.getNextTradeValue(tradeDto.getPlayerId());

        TradeResultDto result = TradeResultDto.builder()
                .armiesReceived(armiesReceived)
                .completedTrades(newTradeCount)
                .nextTradeValue(nextTradeValue)
                .build();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/player/{playerId}/can-trade")
    @Operation(summary = "Verificar si puede intercambiar",
            description = "Verifica si el jugador tiene al menos 3 cartas para poder realizar un intercambio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Verificación completada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class,
                                    description = "true si puede intercambiar, false si no"))),
            @ApiResponse(responseCode = "404",
                    description = "Jugador no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "ID de jugador inválido",
                    content = @Content)
    })
    public ResponseEntity<Boolean> canPlayerTrade(
            @PathVariable Long playerId) {
        List<CardResponseDto> playerCards = cardService.getPlayerCards(playerId);
        boolean canTrade = playerCards.size() >= 3;
        return ResponseEntity.ok(canTrade);
    }

    @PostMapping("/territory-bonus")
    @Operation(summary = "Reclamar premio país-carta",
            description = "Otorga +2 ejércitos al tener un país y su carta correspondiente")
    public ResponseEntity<TerritoryBonusResultDto> claimTerritoryBonus(
            @Valid @RequestBody TerritoryBonusDto bonusDto) {

        cardService.claimTerritoryBonus(bonusDto.getGameId(), bonusDto.getPlayerId(), bonusDto.getCountryName());

        TerritoryBonusResultDto result = TerritoryBonusResultDto.builder()
                .success(true)
                .armiesAdded(2)
                .message("Bonus applied: +2 armies added to territory")
                .build();

        return ResponseEntity.ok(result);
    }

    // DTOs para el endpoint
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TerritoryBonusDto {
        @NotNull private Long gameId;
        @NotNull private Long playerId;
        @NotNull private String countryName;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TerritoryBonusResultDto {
        private Boolean success;
        private Integer armiesAdded;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Resultado del intercambio de cartas")
    public static class TradeResultDto {
        @Schema(description = "Cantidad de ejércitos recibidos en este intercambio",
                example = "4",
                minimum = "4")
        private Integer armiesReceived;

        @Schema(description = "Número total de intercambios realizados por el jugador",
                example = "1",
                minimum = "1")
        private Integer completedTrades;

        @Schema(description = "Cantidad de ejércitos que recibirá en el próximo intercambio",
                example = "7",
                minimum = "4")
        private Integer nextTradeValue;
    }
}
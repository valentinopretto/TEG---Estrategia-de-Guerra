package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.country.TerritoryDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.CountryMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReinforcementServiceImpl implements ReinforcementService {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Autowired
    private GameStateService gameStateService;

    @Autowired
    private CountryMapper countryMapper;

    // Constantes del juego
    private static final int MIN_REINFORCEMENT_ARMIES = 3;
    private static final int TERRITORIES_PER_ARMY = 2;

    @Override
    @Transactional
    public void placeReinforcementArmies(String gameCode, Long playerId, Map<Long, Integer> armiesByCountry) {
        log.info("Starting reinforcement placement for player {} in game {}", playerId, gameCode);

        Game game = gameService.findByGameCode(gameCode);
        Player player = playerService.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));

        // Validaciones
        validateReinforcementPlacement(game, player, armiesByCountry);

        // Colocar ejércitos en los territorios
        placeArmiesOnTerritories(game.getId(), armiesByCountry);

        // Reducir ejércitos pendientes del jugador
        int totalPlaced = armiesByCountry.values().stream().mapToInt(Integer::intValue).sum();
        playerService.removeArmiesToPlace(playerId, totalPlaced);

        // Si el jugador no tiene más ejércitos para colocar, avanzar a la fase de ataque
        if (playerService.getArmiesToPlace(playerId) == 0) {
            log.info("Player {} completed reinforcement, advancing to attack phase", playerId);
            gameStateService.changeTurnPhase(game, TurnPhase.ATTACK);
            gameService.save(game);
        }
    }

    @Override
    public int calculateReinforcementArmies(Game game, Player player) {
        int baseArmies = calculateBaseArmies(player.getTerritoryCount());
        int continentBonus = calculateContinentBonus(game, player);

        // Por ahora no consideramos bonus por cartas (se haría en otra fase)
        int totalArmies = baseArmies + continentBonus;

        log.debug("Reinforcement calculation for player {}: base={}, continent={}, total={}",
                player.getDisplayName(), baseArmies, continentBonus, totalArmies);

        return totalArmies;
    }

    @Override
    public int calculateBaseArmies(int territoryCount) {
        // Fórmula TEG: territorios/2 redondeado hacia abajo, mínimo 3
        int baseArmies = territoryCount / TERRITORIES_PER_ARMY;
        return Math.max(MIN_REINFORCEMENT_ARMIES, baseArmies);
    }

    @Override
    public int calculateContinentBonus(Game game, Player player) {
        int totalBonus = 0;

        for (Continent continent : game.getContinents()) {
            if (continent.isControlledBy(player.getId(), game.getTerritories())) {
                totalBonus += continent.getBonusArmies();
                log.debug("Player {} controls continent {} for {} bonus armies",
                        player.getDisplayName(), continent.getName(), continent.getBonusArmies());
            }
        }

        return totalBonus;
    }

    @Override
    public ReinforcementStatusDto getReinforcementStatus(String gameCode, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);
        Player player = playerService.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        boolean isPlayerTurn = gameStateService.isPlayerTurn(game, playerId);
        boolean canReinforce = canPerformReinforcement(game, player);

        // Obtener territorios del jugador
        List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);
        List<TerritoryDto> territoryDtos = playerTerritories.stream()
                .map(countryMapper::mapTerritoryToDto)
                .collect(Collectors.toList());

        // Calcular ejércitos si es el turno del jugador y está en fase de refuerzo
        int armiesToPlace = 0;
        int baseArmies = 0;
        int continentBonus = 0;
        int totalArmies = 0;

        if (isPlayerTurn && game.getCurrentPhase() == TurnPhase.REINFORCEMENT) {
            // Si el jugador ya tiene ejércitos asignados, usar esos
            armiesToPlace = playerService.getArmiesToPlace(playerId);

            // Si no tiene ejércitos asignados, calcular y asignar
            if (armiesToPlace == 0) {
                baseArmies = calculateBaseArmies(playerTerritories.size());
                continentBonus = calculateContinentBonus(game, player);
                totalArmies = baseArmies + continentBonus;

                // Asignar los ejércitos al jugador
                playerService.addArmiesToPlace(playerId, totalArmies);
                armiesToPlace = totalArmies;
            } else {
                // Si ya tiene ejércitos, calcular los valores para mostrar
                totalArmies = armiesToPlace;
                baseArmies = calculateBaseArmies(playerTerritories.size());
                continentBonus = calculateContinentBonus(game, player);
            }
        }

        // Obtener continentes controlados
        List<String> controlledContinents = game.getContinents().stream()
                .filter(continent -> continent.isControlledBy(player.getId(), game.getTerritories()))
                .map(Continent::getName)
                .collect(Collectors.toList());

        String message = generateStatusMessage(game, isPlayerTurn, canReinforce);

        return ReinforcementStatusDto.builder()
                .playerId(playerId)
                .playerName(player.getDisplayName())
                .gameState(game.getState())
                .currentPhase(game.getCurrentPhase())
                .armiesToPlace(armiesToPlace)
                .baseArmies(baseArmies)
                .continentBonus(continentBonus)
                .cardBonus(0) // Por ahora no implementado
                .totalArmies(totalArmies)
                .isPlayerTurn(isPlayerTurn)
                .canReinforce(canReinforce)
                .message(message)
                .ownedTerritories(territoryDtos)
                .controlledContinents(controlledContinents)
                .build();
    }

    @Override
    public boolean canPerformReinforcement(Game game, Player player) {
        // Debe ser NORMAL_PLAY
        if (game.getState() != GameState.NORMAL_PLAY) {
            return false;
        }

        // Debe estar en fase de REINFORCEMENT
        if (game.getCurrentPhase() != TurnPhase.REINFORCEMENT) {
            return false;
        }

        // Debe ser el turno del jugador
        if (!gameStateService.isPlayerTurn(game, player.getId())) {
            return false;
        }

        // El jugador debe tener territorios
        return player.getTerritoryCount() > 0;
    }

    // Métodos privados de apoyo

    private void validateReinforcementPlacement(Game game, Player player, Map<Long, Integer> armiesByCountry) {
        // Validar estado del juego
        if (!canPerformReinforcement(game, player)) {
            throw new InvalidGameStateException("Cannot perform reinforcement in current game state");
        }

        // Validar que tiene ejércitos para colocar
        int playerArmies = playerService.getArmiesToPlace(player.getId());
        if (playerArmies <= 0) {
            throw new IllegalArgumentException("Player has no armies to place");
        }

        // Validar cantidad total
        int totalToPlace = armiesByCountry.values().stream().mapToInt(Integer::intValue).sum();
        if (totalToPlace <= 0) {
            throw new IllegalArgumentException("Must place at least 1 army");
        }

        if (totalToPlace > playerArmies) {
            throw new IllegalArgumentException(
                    String.format("Trying to place %d armies but only have %d available", totalToPlace, playerArmies));
        }

        // Validar propiedad de territorios
        validateTerritoryOwnership(game.getId(), player.getId(), armiesByCountry.keySet());

        // Validar que cada territorio reciba al menos 1 ejército (opcional, según reglas)
        for (Map.Entry<Long, Integer> entry : armiesByCountry.entrySet()) {
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException("Cannot place negative armies");
            }
        }
    }

    private void validateTerritoryOwnership(Long gameId, Long playerId, Set<Long> countryIds) {
        for (Long countryId : countryIds) {
            Territory territory = gameTerritoryService.getTerritoryByGameAndCountry(gameId, countryId);

            if (territory == null) {
                throw new IllegalArgumentException("Territory not found: " + countryId);
            }

            if (!playerId.equals(territory.getOwnerId())) {
                throw new IllegalArgumentException("Player doesn't own territory: " + countryId);
            }
        }
    }

    private void placeArmiesOnTerritories(Long gameId, Map<Long, Integer> armiesByCountry) {
        for (Map.Entry<Long, Integer> entry : armiesByCountry.entrySet()) {
            Long countryId = entry.getKey();
            Integer armies = entry.getValue();

            if (armies > 0) {
                gameTerritoryService.addArmiesToTerritory(gameId, countryId, armies);
                log.debug("Placed {} armies on territory {}", armies, countryId);
            }
        }
    }

    private String generateStatusMessage(Game game, boolean isPlayerTurn, boolean canReinforce) {
        if (game.getState() != GameState.NORMAL_PLAY) {
            return "Game is not in normal play phase";
        }

        if (game.getCurrentPhase() != TurnPhase.REINFORCEMENT) {
            return "Not in reinforcement phase";
        }

        if (!isPlayerTurn) {
            return "Waiting for other player's turn";
        }

        if (canReinforce) {
            return "Place your reinforcement armies on your territories";
        }

        return "Cannot perform reinforcement";
    }
}
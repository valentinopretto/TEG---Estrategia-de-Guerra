package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository; // AGREGAR ESTO
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameInitializationServiceImpl {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ObjectiveService objectiveService;
    @Autowired
    private GameTerritoryService gameTerritoryService;
    @Autowired
    private GameMapper gameMapper;

    private final Random random = new Random();

    //reparto de paises, objetivos y prep inicial

    @Transactional
    public void initializeGame(GameEntity gameEntity) {

        Game game = gameMapper.toModel(gameEntity);

        validateGameCanStart(game);

        // orden de players
        assignSeatOrderFixed(gameEntity);

        // reparto de territories
        distributeCountries(game);

        // reparto de objetivos no cuomunes
        assignObjectives(game);
        // jugadores estan activos?
        setAllPlayersActive(gameEntity);

        // preparo la fase inical
        prepareInitialPlacement(game);
        // estado de juego
        setupGameState(gameEntity, game);
    }

    private void assignSeatOrderFixed(GameEntity gameEntity) {

        List<PlayerEntity> playerEntities = gameEntity.getPlayers();

        // solo jugadores activos o esperando
        List<PlayerEntity> activePlayers = playerEntities.stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        Collections.shuffle(activePlayers, random);


        for (int i = 0; i < activePlayers.size(); i++) {
            PlayerEntity player = activePlayers.get(i);
            player.setSeatOrder(i);
            player.setStatus(PlayerStatus.ACTIVE);


            if (player.getArmiesToPlace() == null) {
                player.setArmiesToPlace(0);
            }
            if (player.getJoinedAt() == null) {
                player.setJoinedAt(LocalDateTime.now());
            }


            player.setGame(gameEntity);
        }

        playerRepository.saveAll(activePlayers);
    }


    private void validateGameCanStart(Game game) {
        if (game.getState() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot start game. Current state: " + game.getState());
        }

        long activePlayerCount = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ACTIVE)
                .count();

        if (activePlayerCount < 2) {
            throw new InvalidGameStateException("Minimum 2 players required to start. Current: " + activePlayerCount);
        }
    }

    //los jugadores tine eque estar avtivos : ACTIVE
    private void setAllPlayersActive(GameEntity gameEntity) {

        List<PlayerEntity> allPlayers = gameEntity.getPlayers();

        for (PlayerEntity player : allPlayers) {
            if (player.getStatus() != PlayerStatus.ELIMINATED) {
                player.setStatus(PlayerStatus.ACTIVE);
            }
        }

        playerRepository.saveAll(allPlayers);
    }

    private void distributeCountries(Game game) {

        List<Territory> allTerritories = gameTerritoryService.getAllAvailableTerritories();
        Collections.shuffle(allTerritories, random);

        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .sorted(Comparator.comparing(Player::getSeatOrder))
                .toList();

        int playerCount = activePlayers.size();
        int totalCountries = allTerritories.size();
        int baseCountries = totalCountries / playerCount;
        int remainingCountries = totalCountries % playerCount;

        int countryIndex = 0;
        for (Player player : activePlayers) {
            for (int i = 0; i < baseCountries; i++) {
                assignCountryToPlayer(game, allTerritories.get(countryIndex++), player);
            }
        }

        if (remainingCountries > 0) {
            List<Player> playersForExtra = new ArrayList<>(activePlayers);
            Collections.shuffle(playersForExtra, random);

            for (int i = 0; i < remainingCountries; i++) {
                assignCountryToPlayer(game, allTerritories.get(countryIndex++), playersForExtra.get(i));
            }
        }
    }


    private void assignCountryToPlayer(Game game, Territory territory, Player player) {
        gameTerritoryService.assignTerritoryToPlayer(game.getId(), territory.getId(), player.getId(), 1);
    }


    private void assignObjectives(Game game) {
        List<Objective> availableObjectives = objectiveService.findByType(ObjectiveType.OCCUPATION);
        availableObjectives.addAll(objectiveService.findByType(ObjectiveType.DESTRUCTION));
        Collections.shuffle(availableObjectives, random);

        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .sorted(Comparator.comparing(Player::getSeatOrder))
                .toList();

        for (int i = 0; i < activePlayers.size(); i++) {
            Player player = activePlayers.get(i);
            Objective objective = availableObjectives.get(i % availableObjectives.size());

            // para camibar al de la derecha
            if (objective.getType() == ObjectiveType.DESTRUCTION) {
                objective = handleDestructionObjective(objective, player, activePlayers);
            }

            playerService.assignObjective(player.getId(), objective);
        }
    }

    private Objective handleDestructionObjective(Objective objective, Player player, List<Player> players) {
        PlayerColor targetColor = objective.getTargetColor();

        if (targetColor == null || player.getColor() == targetColor || !colorExistsInGame(targetColor, players)) {
            Player rightPlayer = getRightPlayer(player, players);
            objective.setTargetColor(rightPlayer.getColor());
        }

        return objective;
    }

    private boolean colorExistsInGame(PlayerColor color, List<Player> players) {
        return players.stream().anyMatch(p -> p.getColor() == color);
    }

    private Player getRightPlayer(Player player, List<Player> players) {
        int currentIndex = players.indexOf(player);
        int rightIndex = (currentIndex + 1) % players.size();
        return players.get(rightIndex);
    }

    private void prepareInitialPlacement(Game game) {
        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .toList();

        for (Player player : activePlayers) {
            playerService.addArmiesToPlace(player.getId(), 5);
        }
    }

    private void setupGameState(GameEntity gameEntity, Game game) {
        gameEntity.setStatus(GameState.REINFORCEMENT_5); // ARRANCO CON 5, LUEGO 3 Y LUEGO JUEGO NORMALLLLL RECORDAR
        gameEntity.setCurrentPhase(TurnPhase.REINFORCEMENT);
        gameEntity.setStartedAt(LocalDateTime.now());
        gameEntity.setCurrentTurn(1);
        gameEntity.setCurrentPlayerIndex(0); // (seatOrder = 0)

        gameRepository.save(gameEntity);
    }
}
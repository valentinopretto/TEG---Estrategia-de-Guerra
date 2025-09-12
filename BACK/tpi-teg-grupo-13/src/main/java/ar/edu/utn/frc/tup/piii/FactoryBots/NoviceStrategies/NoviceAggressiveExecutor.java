package ar.edu.utn.frc.tup.piii.FactoryBots.NoviceStrategies;

import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyExecutor;
import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class NoviceAggressiveExecutor implements BotStrategyExecutor {

    @Autowired
    private CombatService combatService;

    @Autowired
    private FortificationService fortificationService;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Autowired
    private GameStateService gameStateService;

    @Autowired
    private InitialPlacementService initialPlacementService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private IGameEventService gameEventService;

    private final Random random = new Random();
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private GameService gameService;
    @Autowired
    private ReinforcementService reinforcementService;

    @Override
    public BotLevel getLevel() {
        return BotLevel.NOVICE;
    }

    @Override
    public BotStrategy getStrategy() {
        return BotStrategy.AGGRESSIVE;
    }

    @Override
    public void executeTurn(PlayerEntity botPlayer, GameEntity game) {
        log.info("Ejecutando turno para bot NOVICE-AGGRESSIVE: {}", botPlayer.getBotProfile().getBotName());

        // Registrar inicio de turno
        gameEventService.recordTurnStart(game.getId(), botPlayer.getId(), game.getCurrentTurn());

        // Verificar el estado del juego y ejecutar la acción correspondiente
        GameState currentState = game.getStatus();

        try {
            switch (currentState) {
                case REINFORCEMENT_5:
                    performInitialPlacement(botPlayer, game, 5);
                    break;
                case REINFORCEMENT_3:
                    performInitialPlacement(botPlayer, game, 3);
                    advanceToNextPhase(game);
                    break;
                case HOSTILITY_ONLY:
                    // Solo ataque en esta fase
                    performBotAttack(botPlayer, game);
                    advanceToNextPhase(game);
                    performBotFortify(botPlayer, game);
                    advanceToNextPhase(game);
                    break;
                case NORMAL_PLAY:
                    // ORDEN CORRECTO de las fases del turno
                    performBotReinforcement(botPlayer, game);  // 1º Refuerzo
                    advanceToNextPhase(game);
                    performBotAttack(botPlayer, game);         // 2º Ataque
                    advanceToNextPhase(game);
                    performBotFortify(botPlayer, game);        // 3º Reagrupación
                    advanceToNextPhase(game);
                    break;
                default:
                    log.warn("Estado de juego no manejado por el bot: {}", currentState);
            }

            // Registrar fin de turno
            gameEventService.recordTurnEnd(game.getId(), botPlayer.getId(), game.getCurrentTurn());

        } catch (Exception e) {
            log.error("Error durante la ejecución del turno del bot: {}", e.getMessage());
            // Registrar el error como evento
            String errorData = String.format("{\"error\":\"%s\", \"phase\":\"%s\"}",
                    e.getMessage(), currentState.name());
            gameEventService.recordEvent(game.getId(), botPlayer.getId(), EventType.TURN_ENDED,
                    game.getCurrentTurn(), errorData);
        }
    }

    /**
     * Maneja la colocación inicial de ejércitos (REINFORCEMENT_5 y REINFORCEMENT_3)
     */
    private void performInitialPlacement(PlayerEntity botPlayer, GameEntity game, int armiesToPlace) {
        log.info("Bot realizando colocación inicial de {} ejércitos", armiesToPlace);

        try {
            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot novato no tiene territorios para colocación inicial");
                return;
            }

            // Estrategia: distribuir ejércitos en territorios fronterizos
            Map<Long, Integer> armiesDistribution = distributeInitialArmies(
                    playerTerritories, game, botPlayer.getId(), armiesToPlace);

            // Usar el servicio de colocación inicial
            initialPlacementService.placeInitialArmies(
                    game.getGameCode(), botPlayer.getId(), armiesDistribution);

            // Registrar la colocación inicial en el historial
            for (Map.Entry<Long, Integer> entry : armiesDistribution.entrySet()) {
                Territory territory = playerTerritories.stream()
                        .filter(t -> t.getId().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null);

                if (territory != null) {
                    gameEventService.recordReinforcementsPlaced(
                            game.getId(),
                            botPlayer.getId(),
                            territory.getName(),
                            entry.getValue(),
                            game.getCurrentTurn()
                    );
                }
            }

            log.info("Bot completó colocación inicial de {} ejércitos", armiesToPlace);

        } catch (Exception e) {
            log.error("Error en colocación inicial del bot: {}", e.getMessage());
        }
    }

    /**
     * Distribuye los ejércitos iniciales priorizando territorios fronterizos
     */
    private Map<Long, Integer> distributeInitialArmies(List<Territory> territories,
                                                       GameEntity game, Long playerId, int totalArmies) {
        Map<Long, Integer> distribution = new HashMap<>();

        // Encontrar territorios fronterizos
        List<Territory> borderTerritories = territories.stream()
                .filter(territory -> {
                    List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                            game.getId(), territory.getId());
                    return neighbors.stream()
                            .anyMatch(neighbor -> !neighbor.getOwnerId().equals(playerId));
                })
                .toList();

        List<Territory> targetTerritories = borderTerritories.isEmpty() ? territories : borderTerritories;

        // Distribución simple: repartir equitativamente con sesgo hacia los más fuertes
        int armiesPerTerritory = totalArmies / targetTerritories.size();
        int remainingArmies = totalArmies % targetTerritories.size();

        for (Territory territory : targetTerritories) {
            int armiesToAssign = armiesPerTerritory;
            if (remainingArmies > 0) {
                armiesToAssign++;
                remainingArmies--;
            }
            distribution.put(territory.getId(), armiesToAssign);
        }

        return distribution;
    }

    @Override
    public void performBotReinforcement(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot NOVICE-AGGRESSIVE realizando refuerzos para jugador: {}", botPlayer.getId());

        try {
            // Verificar si puede realizar refuerzos usando el servicio oficial
            Player player = playerService.findById(botPlayer.getId())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            Game gameModel = gameMapper.toModel(game);

            if (!reinforcementService.canPerformReinforcement(gameModel, player)) {
                log.info("Bot no puede realizar refuerzos en este momento");
                return;
            }

            // Obtener el estado de refuerzos (esto calculará automáticamente los ejércitos disponibles)
            ReinforcementStatusDto status = reinforcementService.getReinforcementStatus(
                    game.getGameCode(), botPlayer.getId());

            int availableReinforcements = status.getArmiesToPlace();

            if (availableReinforcements <= 0) {
                log.info("Bot no tiene refuerzos disponibles");
                return;
            }

            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot no tiene territorios para reforzar");
                return;
            }

            // Encontrar territorios fronterizos (prioritarios para estrategia agresiva)
            List<Territory> borderTerritories = playerTerritories.stream()
                    .filter(territory -> {
                        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                                game.getId(), territory.getId());
                        return neighbors.stream()
                                .anyMatch(neighbor -> !neighbor.getOwnerId().equals(botPlayer.getId()));
                    })
                    .toList();

            List<Territory> targetTerritories = borderTerritories.isEmpty() ?
                    playerTerritories : borderTerritories;

            // Estrategia agresiva: reforzar el territorio más fuerte entre los fronterizos
            Territory strongestTerritory = targetTerritories.stream()
                    .max((t1, t2) -> Integer.compare(t1.getArmies(), t2.getArmies()))
                    .orElse(targetTerritories.get(0));

            // Colocar todos los refuerzos en el territorio más fuerte
            Map<Long, Integer> reinforcements = new HashMap<>();
            reinforcements.put(strongestTerritory.getId(), availableReinforcements);

            // Usar el servicio oficial de refuerzos
            reinforcementService.placeReinforcementArmies(
                    game.getGameCode(),
                    botPlayer.getId(),
                    reinforcements
            );

            // Registrar la acción en el historial
            gameEventService.recordReinforcementsPlaced(
                    game.getId(),
                    botPlayer.getId(),
                    strongestTerritory.getName(),
                    availableReinforcements,
                    game.getCurrentTurn()
            );

            log.info("Bot reforzó territorio: {} con {} ejércitos usando ReinforcementService",
                    strongestTerritory.getName(), availableReinforcements);

        } catch (Exception e) {
            log.error("Error en refuerzos del bot NOVICE-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotAttack(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot NOVICE-AGGRESSIVE realizando ataques para jugador: {}", botPlayer.getId());

        try {
            List<Territory> attackableTerritories = combatService.getAttackableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (attackableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde atacar");
                return;
            }

            // Realizar varios ataques (comportamiento agresivo)
            int maxAttacks = 3;
            int attackCount = 0;

            for (Territory attackerTerritory : attackableTerritories) {
                if (attackCount >= maxAttacks) break;

                List<Territory> targets = combatService.getTargetsForTerritory(
                        game.getGameCode(), attackerTerritory.getId(), botPlayer.getId());

                if (targets.isEmpty()) continue;

                // Estrategia agresiva: atacar el territorio más débil
                Territory weakestTarget = targets.stream()
                        .min((t1, t2) -> Integer.compare(t1.getArmies(), t2.getArmies()))
                        .orElse(targets.get(0));

                // Calcular ejércitos para atacar
                int attackingArmies = Math.min(
                        attackerTerritory.getArmies() - 1,
                        3
                );

                if (attackingArmies > 0) {
                    AttackDto attackDto = AttackDto.builder()
                            .playerId(botPlayer.getId())
                            .attackerCountryId(attackerTerritory.getId())
                            .defenderCountryId(weakestTarget.getId())
                            .attackingArmies(attackingArmies)
                            .build();

                    CombatResultDto result = combatService.performCombat(game.getGameCode(), attackDto);

                    // Registrar el ataque en el historial
                    gameEventService.recordAttack(
                            game.getId(),
                            botPlayer.getId(),
                            attackerTerritory.getName(),
                            weakestTarget.getName(),
                            game.getCurrentTurn(),
                            result.getTerritoryConquered()
                    );

                    // Si conquistó el territorio, registrar la conquista
                    if (result.getTerritoryConquered()) {
                        // Obtener el nombre del jugador que perdió el territorio
                        String formerOwnerName = getPlayerName(weakestTarget.getOwnerId());

                        gameEventService.recordTerritoryConquest(
                                game.getId(),
                                botPlayer.getId(),
                                weakestTarget.getName(),
                                formerOwnerName,
                                game.getCurrentTurn()
                        );
                    }

                    log.info("Bot atacó desde {} hacia {}: Conquistado={}",
                            attackerTerritory.getName(), weakestTarget.getName(),
                            result.getTerritoryConquered());

                    attackCount++;
                }
            }

        } catch (Exception e) {
            log.error("Error en ataques del bot NOVICE-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotFortify(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot NOVICE-AGGRESSIVE realizando fortificación para jugador: {}", botPlayer.getId());

        try {
            List<Territory> fortifiableTerritories = fortificationService.getFortifiableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (fortifiableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde fortificar");
                return;
            }

            // Buscar territorio seguro para mover ejércitos
            Territory safeTerritory = fortifiableTerritories.stream()
                    .filter(territory -> {
                        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                                game.getId(), territory.getId());
                        return neighbors.stream()
                                .allMatch(neighbor -> neighbor.getOwnerId().equals(botPlayer.getId()));
                    })
                    .findFirst()
                    .orElse(null);

            if (safeTerritory == null) {
                log.info("Bot no encontró territorios seguros para fortificar");
                return;
            }

            List<Territory> possibleTargets = fortificationService.getFortificationTargetsForTerritory(
                    game.getGameCode(), safeTerritory.getId(), botPlayer.getId());

            if (possibleTargets.isEmpty()) {
                log.info("No hay objetivos válidos para fortificación");
                return;
            }

            // Elegir objetivo fronterizo al azar
            Territory targetTerritory = possibleTargets.get(random.nextInt(possibleTargets.size()));

            int maxMovableArmies = fortificationService.getMaxMovableArmies(
                    game.getGameCode(), safeTerritory.getId());

            if (maxMovableArmies > 0) {
                int armiesToMove = Math.max(1, maxMovableArmies / 2);

                FortifyDto fortifyDto = new FortifyDto();
                fortifyDto.setPlayerId(botPlayer.getId());
                fortifyDto.setFromCountryId(safeTerritory.getId());
                fortifyDto.setToCountryId(targetTerritory.getId());
                fortifyDto.setArmies(armiesToMove);

                boolean success = fortificationService.performFortification(
                        game.getGameCode(), fortifyDto);

                if (success) {
                    // Registrar la fortificación en el historial
                    gameEventService.recordFortification(
                            game.getId(),
                            botPlayer.getId(),
                            safeTerritory.getName(),
                            targetTerritory.getName(),
                            armiesToMove,
                            game.getCurrentTurn()
                    );

                    log.info("Bot fortificó desde {} hacia {} con {} ejércitos",
                            safeTerritory.getName(), targetTerritory.getName(), armiesToMove);
                }
            }

        } catch (Exception e) {
            log.error("Error en fortificación del bot NOVICE-AGGRESSIVE: {}", e.getMessage());
        }
    }

    /**
     * Obtiene el nombre de un jugador por su ID
     */
    private String getPlayerName(Long playerId) {
        try {
            return playerService.findById(playerId)
                    .map(player -> {
                        if (player.getDisplayName() != null) {
                            return player.getDisplayName();
                        } else if (player.getBotProfile() != null && player.getBotProfile().getBotName() != null) {
                            return player.getBotProfile().getBotName();
                        }
                        return "Jugador Desconocido";
                    })
                    .orElse("Jugador Desconocido");
        } catch (Exception e) {
            log.warn("Error obteniendo nombre del jugador {}: {}", playerId, e.getMessage());
            return "Jugador Desconocido";
        }
    }

    /**
     * Avanza a la siguiente fase del turno o termina el turno
     */
    private void advanceToNextPhase(GameEntity game) {
        try {
            TurnPhase currentPhase = game.getCurrentPhase();

            switch (currentPhase) {
                case REINFORCEMENT:
                    gameStateService.changeTurnPhase(gameMapper.toModel(game), TurnPhase.ATTACK);
                    break;
                case ATTACK:
                    gameStateService.changeTurnPhase(gameMapper.toModel(game), TurnPhase.FORTIFY);
                    break;
                case FORTIFY:
                    gameStateService.changeTurnPhase(gameMapper.toModel(game), TurnPhase.END_TURN);
                    break;
                case END_TURN:
                    gameStateService.changeTurnPhase(gameMapper.toModel(game), TurnPhase.REINFORCEMENT);
                    break;
                default:
                    log.warn("Fase no manejada para avance: {}", currentPhase);
            }

            log.info("Bot avanzó de fase {} a {}", currentPhase, game.getCurrentPhase());

        } catch (Exception e) {
            log.error("Error al avanzar fase del bot: {}", e.getMessage());
        }
    }

    // Métodos no implementados pero requeridos por la interfaz
    @Override
    public double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies) {
        return 0;
    }

    @Override
    public List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game) {
        return List.of();
    }

    @Override
    public List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game) {
        return List.of();
    }
}
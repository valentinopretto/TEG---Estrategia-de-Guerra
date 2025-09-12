package ar.edu.utn.frc.tup.piii.FactoryBots.ExpertStrategies;

import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyExecutor;
import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExpertAggressiveExecutor implements BotStrategyExecutor {

    @Autowired
    private CombatService combatService;

    @Autowired
    private FortificationService fortificationService;

    @Autowired
    private ReinforcementService reinforcementService;

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
    @Autowired
    private GameMapper gameMapper;

    private final Random random = new Random();

    // Estructura simplificada para objetivos estratégicos
    private static class StrategicTarget {
        Long territoryId;
        String name;
        int priority;
        String type;

        public StrategicTarget(Long territoryId, String name, int priority, String type) {
            this.territoryId = territoryId;
            this.name = name;
            this.priority = priority;
            this.type = type;
        }
    }

    // Estructura para movimientos de fortificación
    private static class FortificationMove {
        Long sourceId;
        String sourceName;
        Long targetId;
        String targetName;
        int armies;
        String purpose;

        public FortificationMove(Long sourceId, String sourceName, Long targetId, String targetName,
                                 int armies, String purpose) {
            this.sourceId = sourceId;
            this.sourceName = sourceName;
            this.targetId = targetId;
            this.targetName = targetName;
            this.armies = armies;
            this.purpose = purpose;
        }
    }

    @Override
    public BotLevel getLevel() {
        return BotLevel.EXPERT;
    }

    @Override
    public BotStrategy getStrategy() {
        return BotStrategy.AGGRESSIVE;
    }

    @Override
    public void executeTurn(PlayerEntity botPlayer, GameEntity game) {
        log.info("Ejecutando turno EXPERT-AGGRESSIVE para bot: {}", botPlayer.getBotProfile().getBotName());

        // Verificar el estado del juego y ejecutar la acción correspondiente
        GameState currentState = game.getStatus();
        // Registrar inicio de turno del bot
        gameEventService.recordTurnStart(game.getId(), botPlayer.getId(), game.getCurrentTurn());

        switch (currentState) {
            case REINFORCEMENT_5:
                performInitialPlacement(botPlayer, game, 5);
                break;
            case REINFORCEMENT_3:
                performInitialPlacement(botPlayer, game, 3);
                advanceToNextPhase(game);
                break;
            case HOSTILITY_ONLY:
                performBotAttack(botPlayer, game);
                advanceToNextPhase(game);
                performBotFortify(botPlayer, game);
                advanceToNextPhase(game);
                break;
            case NORMAL_PLAY:
                performBotReinforcement(botPlayer, game);
                advanceToNextPhase(game);
                performBotAttack(botPlayer, game);
                advanceToNextPhase(game);
                performBotFortify(botPlayer, game);
                advanceToNextPhase(game);
                break;
            default:
                log.warn("Estado de juego no manejado por el bot: {}", currentState);
        }
        //registrar fin de turno
        gameEventService.recordTurnEnd(game.getId(), botPlayer.getId(), game.getCurrentTurn());
    }

    /**
     * Maneja la colocación inicial de ejércitos (REINFORCEMENT_5 y REINFORCEMENT_3)
     */
    private void performInitialPlacement(PlayerEntity botPlayer, GameEntity game, int armiesToPlace) {
        log.info("Bot EXPERT realizando colocación inicial de {} ejércitos", armiesToPlace);

        try {
            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot experto no tiene territorios para colocación inicial");
                return;
            }

            // Estrategia experta: analizar objetivo y distribuir estratégicamente
            ObjectiveEntity objective = botPlayer.getObjective();
            String objectiveType = (objective != null) ? objective.getType().name() : "GENERAL";

            Map<Long, Integer> armiesDistribution = distributeInitialArmiesExpert(
                    playerTerritories, game, botPlayer, armiesToPlace, objectiveType);

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

            log.info("Bot EXPERT completó colocación inicial de {} ejércitos con estrategia {}",
                    armiesToPlace, objectiveType);

        } catch (Exception e) {
            log.error("Error en colocación inicial del bot EXPERT: {}", e.getMessage());
        }
    }

    /**
     * Distribución experta de ejércitos iniciales basada en objetivos
     */
    private Map<Long, Integer> distributeInitialArmiesExpert(List<Territory> territories,
                                                             GameEntity game, PlayerEntity botPlayer,
                                                             int totalArmies, String objectiveType) {
        Map<Long, Integer> distribution = new HashMap<>();

        // Identificar objetivos estratégicos
        List<StrategicTarget> strategicTargets = identifyStrategicTargets(game, botPlayer, objectiveType);

        // Encontrar territorios que coinciden con objetivos estratégicos
        List<Territory> priorityTerritories = territories.stream()
                .filter(t -> strategicTargets.stream()
                        .anyMatch(st -> isStrategicallyRelevant(t, st, game, botPlayer.getId())))
                .collect(Collectors.toList());

        if (priorityTerritories.isEmpty()) {
            priorityTerritories = findBorderTerritories(territories, game, botPlayer.getId());
        }

        // Distribución según objetivo
        switch (objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                // Concentrar 80% en el territorio más fuerte
                Territory strongest = priorityTerritories.stream()
                        .max(Comparator.comparingInt(Territory::getArmies))
                        .orElse(territories.get(0));
                distribution.put(strongest.getId(), (int) (totalArmies * 0.8));

                if (priorityTerritories.size() > 1 && totalArmies > 1) {
                    Territory second = priorityTerritories.stream()
                            .filter(t -> !t.getId().equals(strongest.getId()))
                            .findFirst().orElse(null);
                    if (second != null) {
                        distribution.put(second.getId(), totalArmies - distribution.get(strongest.getId()));
                    }
                }
                break;

            case "OCCUPATION":
                // Distribuir equitativamente entre territorios estratégicos
                int armiesPerTerritory = totalArmies / priorityTerritories.size();
                int remainder = totalArmies % priorityTerritories.size();

                for (Territory territory : priorityTerritories) {
                    int armies = armiesPerTerritory + (remainder > 0 ? 1 : 0);
                    distribution.put(territory.getId(), armies);
                    if (remainder > 0) remainder--;
                }
                break;

            default:
                // Estrategia balanceada para otros objetivos
                distributeBalanced(distribution, priorityTerritories, totalArmies);
        }

        return distribution;
    }

    @Override
    public void performBotReinforcement(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot EXPERT-AGGRESSIVE realizando refuerzos para jugador: {}", botPlayer.getId());

        try {
            Player player = playerService.findById(botPlayer.getId())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            Game gameModel = gameMapper.toModel(game);

            if (!reinforcementService.canPerformReinforcement(gameModel, player)) {
                log.info("Bot EXPERT no puede realizar refuerzos en este momento");
                return;
            }

            ReinforcementStatusDto status = reinforcementService.getReinforcementStatus(
                    game.getGameCode(), botPlayer.getId());

            int availableReinforcements = status.getArmiesToPlace();

            if (availableReinforcements <= 0) {
                log.info("Bot EXPERT no tiene refuerzos disponibles");
                return;
            }

            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot EXPERT no tiene territorios para reforzar");
                return;
            }

            // Análisis estratégico basado en objetivo
            ObjectiveEntity objective = botPlayer.getObjective();
            String objectiveType = (objective != null) ? objective.getType().name() : "GENERAL";

            List<StrategicTarget> strategicTargets = identifyStrategicTargets(game, botPlayer, objectiveType);

            // Planificar distribución de refuerzos según objetivo
            Map<Long, Integer> reinforcementPlan = planExpertReinforcements(
                    playerTerritories, strategicTargets, availableReinforcements, objectiveType, game, botPlayer.getId());

            reinforcementService.placeReinforcementArmies(
                    game.getGameCode(),
                    botPlayer.getId(),
                    reinforcementPlan
            );

            // Registrar refuerzos colocados
            for (Map.Entry<Long, Integer> entry : reinforcementPlan.entrySet()) {
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

            log.info("Bot EXPERT distribuyó {} refuerzos con estrategia {}",
                    availableReinforcements, objectiveType);

        } catch (Exception e) {
            log.error("Error en refuerzos del bot EXPERT-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotAttack(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot EXPERT-AGGRESSIVE ejecutando ataques para jugador: {}", botPlayer.getId());

        try {
            List<Territory> attackableTerritories = combatService.getAttackableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (attackableTerritories.isEmpty()) {
                log.info("Bot EXPERT no tiene territorios desde donde atacar");
                return;
            }

            ObjectiveEntity objective = botPlayer.getObjective();
            String objectiveType = (objective != null) ? objective.getType().name() : "GENERAL";

            List<StrategicTarget> strategicTargets = identifyStrategicTargets(game, botPlayer, objectiveType);

            // Realizar ataques estratégicos (máximo 8 para nivel expert)
            int maxAttacks = getMaxAttacksForObjective(objectiveType);
            int attackCount = 0;

            // Ordenar territorios atacantes por prioridad estratégica
            List<Territory> prioritizedAttackers = prioritizeAttackers(attackableTerritories, strategicTargets);

            for (Territory attackerTerritory : prioritizedAttackers) {
                if (attackCount >= maxAttacks) break;

                List<Territory> targets = combatService.getTargetsForTerritory(
                        game.getGameCode(), attackerTerritory.getId(), botPlayer.getId());

                if (targets.isEmpty()) continue;

                // Seleccionar mejor objetivo según estrategia
                Territory bestTarget = selectBestTarget(targets, strategicTargets, objectiveType);

                if (bestTarget != null && shouldAttack(attackerTerritory, bestTarget, objectiveType)) {
                    int attackingArmies = calculateOptimalAttackForce(
                            attackerTerritory.getArmies(), bestTarget.getArmies(), objectiveType);

                    if (attackingArmies > 0) {
                        AttackDto attackDto = AttackDto.builder()
                                .playerId(botPlayer.getId())
                                .attackerCountryId(attackerTerritory.getId())
                                .defenderCountryId(bestTarget.getId())
                                .attackingArmies(attackingArmies)
                                .build();

                        CombatResultDto result = combatService.performCombat(game.getGameCode(), attackDto);

                        // Registrar el ataque en el historial
                        gameEventService.recordAttack(
                                game.getId(),
                                botPlayer.getId(),
                                attackerTerritory.getName(),
                                bestTarget.getName(),
                                game.getCurrentTurn(),
                                result.getTerritoryConquered()
                        );

                        // Si conquistó el territorio, registrar la conquista
                        if (result.getTerritoryConquered()) {
                            String formerOwnerName = getPlayerName(bestTarget.getOwnerId());
                            gameEventService.recordTerritoryConquest(
                                    game.getId(),
                                    botPlayer.getId(),
                                    bestTarget.getName(),
                                    formerOwnerName,
                                    game.getCurrentTurn()
                            );
                        }

                        log.info("Bot EXPERT atacó desde {} hacia {}: Conquistado={} (Estrategia: {})",
                                attackerTerritory.getName(), bestTarget.getName(),
                                result.getTerritoryConquered(), objectiveType);

                        attackCount++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error en ataques del bot EXPERT-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotFortify(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot EXPERT-AGGRESSIVE realizando fortificación para jugador: {}", botPlayer.getId());

        try {
            List<Territory> fortifiableTerritories = fortificationService.getFortifiableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (fortifiableTerritories.isEmpty()) {
                log.info("Bot EXPERT no tiene territorios desde donde fortificar");
                return;
            }

            ObjectiveEntity objective = botPlayer.getObjective();
            String objectiveType = (objective != null) ? objective.getType().name() : "GENERAL";

            // Encontrar el mejor movimiento de fortificación estratégico
            Optional<FortificationMove> bestMove = findBestExpertFortificationMove(
                    fortifiableTerritories, game, botPlayer.getId(), objectiveType);

            if (bestMove.isPresent()) {
                FortificationMove move = bestMove.get();

                FortifyDto fortifyDto = new FortifyDto();
                fortifyDto.setPlayerId(botPlayer.getId());
                fortifyDto.setFromCountryId(move.sourceId);
                fortifyDto.setToCountryId(move.targetId);
                fortifyDto.setArmies(move.armies);

                boolean success = fortificationService.performFortification(game.getGameCode(), fortifyDto);

                if (success) {
                    // Registrar la fortificación en el historial
                    gameEventService.recordFortification(
                            game.getId(),
                            botPlayer.getId(),
                            move.sourceName,
                            move.targetName,
                            move.armies,
                            game.getCurrentTurn()
                    );
                    log.info("Bot EXPERT fortificó: {} -> {} ({} ejércitos) - Propósito: {}",
                            move.sourceName, move.targetName, move.armies, move.purpose);
                }
            } else {
                log.info("Bot EXPERT no encontró movimientos de fortificación beneficiosos");
            }

        } catch (Exception e) {
            log.error("Error en fortificación del bot EXPERT-AGGRESSIVE: {}", e.getMessage());
        }
    }

    // Métodos auxiliares simplificados

    private List<StrategicTarget> identifyStrategicTargets(GameEntity game, PlayerEntity botPlayer, String objectiveType) {
        List<StrategicTarget> targets = new ArrayList<>();

        try {
            List<Territory> allTerritories = gameTerritoryService.getAllAvailableTerritories();
            List<Territory> enemyTerritories = allTerritories.stream()
                    .filter(t -> !t.getOwnerId().equals(botPlayer.getId()))
                    .collect(Collectors.toList());

            switch (objectiveType.toUpperCase()) {
                case "OCCUPATION":
                    targets.addAll(identifyOccupationTargets(botPlayer, enemyTerritories));
                    break;
                case "DESTRUCTION":
                    targets.addAll(identifyDestructionTargets(botPlayer, enemyTerritories));
                    break;
                default:
                    targets.addAll(identifyGeneralTargets(enemyTerritories));
            }

        } catch (Exception e) {
            log.error("Error identificando objetivos estratégicos: {}", e.getMessage());
        }

        return targets.stream()
                .sorted((a, b) -> Integer.compare(b.priority, a.priority))
                .collect(Collectors.toList());
    }

    private List<StrategicTarget> identifyOccupationTargets(PlayerEntity botPlayer, List<Territory> enemyTerritories) {
        List<StrategicTarget> targets = new ArrayList<>();
        ObjectiveEntity objective = botPlayer.getObjective();

        if (objective != null && objective.getTargetData() != null) {
            String[] targetContinents = objective.getTargetData().split(",");

            for (String continent : targetContinents) {
                enemyTerritories.stream()
                        .filter(t -> continent.trim().equalsIgnoreCase(t.getContinentName()))
                        .forEach(t -> targets.add(new StrategicTarget(
                                t.getId(), t.getName(), 90 - t.getArmies(), "OCCUPATION")));
            }
        }
        return targets;
    }

    private List<StrategicTarget> identifyDestructionTargets(PlayerEntity botPlayer, List<Territory> enemyTerritories) {
        List<StrategicTarget> targets = new ArrayList<>();
        ObjectiveEntity objective = botPlayer.getObjective();

        if (objective != null && objective.getTargetData() != null) {
            try {
                Long targetPlayerId = Long.parseLong(objective.getTargetData().trim());
                enemyTerritories.stream()
                        .filter(t -> t.getOwnerId().equals(targetPlayerId))
                        .forEach(t -> targets.add(new StrategicTarget(
                                t.getId(), t.getName(), 95 - t.getArmies(), "DESTRUCTION")));
            } catch (NumberFormatException e) {
                log.warn("No se pudo parsear el ID del jugador objetivo: {}", objective.getTargetData());
            }
        }
        return targets;
    }

    private List<StrategicTarget> identifyGeneralTargets(List<Territory> enemyTerritories) {
        return enemyTerritories.stream()
                .filter(t -> t.getArmies() <= 3)
                .map(t -> new StrategicTarget(t.getId(), t.getName(), 50 - t.getArmies(), "EXPANSION"))
                .collect(Collectors.toList());
    }

    private Map<Long, Integer> planExpertReinforcements(List<Territory> playerTerritories,
                                                        List<StrategicTarget> strategicTargets,
                                                        int availableReinforcements,
                                                        String objectiveType,
                                                        GameEntity game,
                                                        Long playerId) {
        Map<Long, Integer> plan = new HashMap<>();

        switch (objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                // Concentrar en el territorio más fuerte en frontera
                Territory strongestBorder = findStrongestBorderTerritory(playerTerritories, game, playerId);
                if (strongestBorder != null) {
                    plan.put(strongestBorder.getId(), availableReinforcements);
                }
                break;

            case "OCCUPATION":
                // Distribuir en territorios que dan acceso a continentes objetivo
                distributeForOccupation(plan, playerTerritories, availableReinforcements);
                break;

            default:
                // Distribución equilibrada en fronteras
                List<Territory> borderTerritories = findBorderTerritories(playerTerritories, game, playerId);
                distributeBalanced(plan, borderTerritories.isEmpty() ? playerTerritories : borderTerritories,
                        availableReinforcements);
        }

        return plan;
    }

    private List<Territory> findBorderTerritories(List<Territory> territories, GameEntity game, Long playerId) {
        return territories.stream()
                .filter(territory -> {
                    List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(game.getId(), territory.getId());
                    return neighbors.stream().anyMatch(neighbor -> !neighbor.getOwnerId().equals(playerId));
                })
                .collect(Collectors.toList());
    }

    private Territory findStrongestBorderTerritory(List<Territory> territories, GameEntity game, Long playerId) {
        return findBorderTerritories(territories, game, playerId).stream()
                .max(Comparator.comparingInt(Territory::getArmies))
                .orElse(territories.get(0));
    }

    private void distributeForOccupation(Map<Long, Integer> plan, List<Territory> territories, int available) {
        List<Territory> sorted = territories.stream()
                .sorted((a, b) -> Integer.compare(b.getArmies(), a.getArmies()))
                .collect(Collectors.toList());

        if (!sorted.isEmpty()) {
            plan.put(sorted.get(0).getId(), Math.max(1, available * 70 / 100));
            if (sorted.size() > 1 && available > 1) {
                plan.put(sorted.get(1).getId(), available - plan.get(sorted.get(0).getId()));
            }
        }
    }

    private void distributeBalanced(Map<Long, Integer> plan, List<Territory> territories, int available) {
        if (territories.isEmpty()) return;

        int perTerritory = Math.max(1, available / Math.min(3, territories.size()));
        int remainder = available % Math.min(3, territories.size());

        for (int i = 0; i < Math.min(3, territories.size()) && available > 0; i++) {
            int toAssign = perTerritory + (remainder > 0 ? 1 : 0);
            plan.put(territories.get(i).getId(), toAssign);
            available -= toAssign;
            if (remainder > 0) remainder--;
        }
    }

    private boolean isStrategicallyRelevant(Territory territory, StrategicTarget target, GameEntity game, Long playerId) {
        // Verificar si el territorio está cerca de objetivos estratégicos
        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(game.getId(), territory.getId());
        return neighbors.stream().anyMatch(n -> n.getId().equals(target.territoryId));
    }

    private int getMaxAttacksForObjective(String objectiveType) {
        switch (objectiveType.toUpperCase()) {
            case "DESTRUCTION": return 10;
            case "OCCUPATION": return 8;
            default: return 6;
        }
    }

    private List<Territory> prioritizeAttackers(List<Territory> attackers, List<StrategicTarget> targets) {
        return attackers.stream()
                .sorted((a, b) -> {
                    int priorityA = targets.stream()
                            .filter(t -> t.territoryId.equals(a.getId()))
                            .mapToInt(t -> t.priority)
                            .max().orElse(0);
                    int priorityB = targets.stream()
                            .filter(t -> t.territoryId.equals(b.getId()))
                            .mapToInt(t -> t.priority)
                            .max().orElse(0);
                    return Integer.compare(priorityB, priorityA);
                })
                .collect(Collectors.toList());
    }

    private Territory selectBestTarget(List<Territory> targets, List<StrategicTarget> strategicTargets, String objectiveType) {
        return targets.stream()
                .max((a, b) -> {
                    int priorityA = strategicTargets.stream()
                            .filter(st -> st.territoryId.equals(a.getId()))
                            .mapToInt(st -> st.priority)
                            .max().orElse(0);
                    int priorityB = strategicTargets.stream()
                            .filter(st -> st.territoryId.equals(b.getId()))
                            .mapToInt(st -> st.priority)
                            .max().orElse(0);

                    if (priorityA != priorityB) {
                        return Integer.compare(priorityA, priorityB);
                    }
                    return Integer.compare(b.getArmies(), a.getArmies()); // Preferir territorios más débiles en caso de empate
                })
                .orElse(targets.get(0));
    }

    private boolean shouldAttack(Territory attacker, Territory target, String objectiveType) {
        double probability = evaluateAttackProbability(null, attacker.getArmies(), target.getArmies());
        double minProbability = getMinAttackProbability(objectiveType);
        return probability >= minProbability;
    }

    private double getMinAttackProbability(String objectiveType) {
        switch (objectiveType.toUpperCase()) {
            case "DESTRUCTION": return 0.40;
            case "OCCUPATION": return 0.50;
            default: return 0.55;
        }
    }

    private int calculateOptimalAttackForce(int attackerArmies, int defenderArmies, String objectiveType) {
        int baseForce = Math.min(attackerArmies - 1, 3);

        switch (objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                return Math.min(attackerArmies - 1, defenderArmies + 2);
            default:
                return Math.max(1, baseForce);
        }
    }

    private Optional<FortificationMove> findBestExpertFortificationMove(List<Territory> fortifiableTerritories,
                                                                        GameEntity game, Long playerId,
                                                                        String objectiveType) {
        for (Territory source : fortifiableTerritories) {
            if (source.getArmies() <= 2) continue;

            List<Territory> targets = fortificationService.getFortificationTargetsForTerritory(
                    game.getGameCode(), source.getId(), playerId);

            for (Territory target : targets) {
                if (shouldFortifyExpert(source, target, objectiveType, game, playerId)) {
                    int armiesToMove = calculateExpertFortificationArmies(source.getArmies(), objectiveType);

                    return Optional.of(new FortificationMove(
                            source.getId(), source.getName(),
                            target.getId(), target.getName(),
                            armiesToMove, "Consolidación estratégica " + objectiveType
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private boolean shouldFortifyExpert(Territory source, Territory target, String objectiveType,
                                        GameEntity game, Long playerId) {
        List<Territory> targetNeighbors = gameTerritoryService.getNeighborTerritories(game.getId(), target.getId());
        List<Territory> sourceNeighbors = gameTerritoryService.getNeighborTerritories(game.getId(), source.getId());

        boolean targetIsBorder = targetNeighbors.stream().anyMatch(n -> !n.getOwnerId().equals(playerId));
        boolean sourceIsSafe = sourceNeighbors.stream().allMatch(n -> n.getOwnerId().equals(playerId));

        if ("DESTRUCTION".equals(objectiveType)) {
            return targetIsBorder && target.getArmies() > source.getArmies();
        }

        return sourceIsSafe && targetIsBorder && target.getArmies() < source.getArmies() - 2;
    }

    private int calculateExpertFortificationArmies(int sourceArmies, String objectiveType) {
        switch (objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                return Math.max(1, sourceArmies * 80 / 100 - 1);
            default:
                return Math.max(1, sourceArmies / 2);
        }
    }

    /**
     * Avanza a la siguiente fase del turno
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

            log.info("Bot EXPERT avanzó de fase {} a {}", currentPhase, game.getCurrentPhase());

        } catch (Exception e) {
            log.error("Error al avanzar fase del bot EXPERT: {}", e.getMessage());
        }
    }

    // Métodos no implementados pero requeridos por la interfaz
    @Override
    public double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies) {
        // Cálculo más sofisticado para bot experto
        if (attackerArmies <= 0 || defenderArmies <= 0) return 0.0;

        // Fórmula mejorada considerando ventaja del atacante
        double ratio = (double) attackerArmies / defenderArmies;
        double baseProbability = Math.min(0.95, ratio * 0.6);

        // Ajuste por número de dados
        if (attackerArmies >= 3) baseProbability += 0.1;
        if (defenderArmies == 1) baseProbability += 0.15;

        return Math.max(0.05, baseProbability);
    }

    @Override
    public List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game) {
        // Implementación vacía - la lógica está en selectBestTarget
        return List.of();
    }

    @Override
    public List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game) {
        // Implementación vacía - la lógica está en los métodos de fortificación
        return List.of();
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
}
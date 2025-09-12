package ar.edu.utn.frc.tup.piii.FactoryBots.BalancedStrategies;

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
public class BalancedAggressiveExecutor implements BotStrategyExecutor {

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
    private GameMapper gameMapper;

    @Autowired
    private GameService gameService;

    // Nuevo servicio para registrar eventos
    @Autowired
    private IGameEventService gameEventService;

    private final Random random = new Random();

    @Override
    public BotLevel getLevel() {
        return BotLevel.BALANCED;
    }

    @Override
    public BotStrategy getStrategy() {
        return BotStrategy.AGGRESSIVE;
    }

    @Override
    public void executeTurn(PlayerEntity botPlayer, GameEntity game) {
        log.info("Ejecutando turno para bot BALANCED-AGGRESSIVE: {}", botPlayer.getBotProfile().getBotName());

        // Registrar inicio de turno del bot
        gameEventService.recordTurnStart(game.getId(), botPlayer.getId(), game.getCurrentTurn());

        // Verificar el estado del juego y ejecutar la acción correspondiente
        GameState currentState = game.getStatus();

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
                performBotAttack(botPlayer, game);        // 2º Ataque
                advanceToNextPhase(game);
                performBotFortify(botPlayer, game);       // 3º Reagrupación
                advanceToNextPhase(game);
                break;
            default:
                log.warn("Estado de juego no manejado por el bot: {}", currentState);
        }

        // Registrar fin de turno del bot
        gameEventService.recordTurnEnd(game.getId(), botPlayer.getId(), game.getCurrentTurn());
    }

    /**
     * Maneja la colocación inicial de ejércitos (REINFORCEMENT_5 y REINFORCEMENT_3)
     * Igual que novato pero con distribución más inteligente
     */
    private void performInitialPlacement(PlayerEntity botPlayer, GameEntity game, int armiesToPlace) {
        log.info("Bot BALANCED realizando colocación inicial de {} ejércitos", armiesToPlace);

        try {
            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot balanceado no tiene territorios para colocación inicial");
                return;
            }

            // Estrategia balanceada: priorizar territorios fronterizos más estratégicos
            Map<Long, Integer> armiesDistribution = distributeInitialArmiesBalanced(
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

            log.info("Bot BALANCED completó colocación inicial de {} ejércitos", armiesToPlace);

        } catch (Exception e) {
            log.error("Error en colocación inicial del bot BALANCED: {}", e.getMessage());
        }
    }

    /**
     * Distribuye los ejércitos iniciales de forma más inteligente que el novato
     */
    private Map<Long, Integer> distributeInitialArmiesBalanced(List<Territory> territories,
                                                               GameEntity game, Long playerId, int totalArmies) {
        Map<Long, Integer> distribution = new HashMap<>();

        // Encontrar territorios fronterizos y calcular su prioridad
        List<Territory> prioritizedTerritories = territories.stream()
                .filter(territory -> {
                    List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                            game.getId(), territory.getId());
                    return neighbors.stream()
                            .anyMatch(neighbor -> !neighbor.getOwnerId().equals(playerId));
                })
                .sorted((t1, t2) -> {
                    int t1Priority = calculateTerritoryPriority(t1, game, playerId);
                    int t2Priority = calculateTerritoryPriority(t2, game, playerId);
                    return Integer.compare(t2Priority, t1Priority); // Mayor prioridad primero
                })
                .toList();

        List<Territory> targetTerritories = prioritizedTerritories.isEmpty() ? territories : prioritizedTerritories;

        // Distribución inteligente: más ejércitos a territorios más prioritarios
        if (targetTerritories.size() <= 2) {
            // Si pocos territorios, distribuir equitativamente
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
        } else {
            // Si varios territorios, dar más a los prioritarios
            int highPriorityCount = Math.min(2, targetTerritories.size());
            int armiesForHighPriority = (totalArmies * 2) / 3; // 2/3 para los prioritarios
            int armiesForOthers = totalArmies - armiesForHighPriority;

            // Distribuir en territorios de alta prioridad
            for (int i = 0; i < highPriorityCount; i++) {
                Territory territory = targetTerritories.get(i);
                int armies = armiesForHighPriority / highPriorityCount;
                if (i < armiesForHighPriority % highPriorityCount) armies++;
                distribution.put(territory.getId(), armies);
            }

            // Distribuir el resto
            int remainingTerritories = targetTerritories.size() - highPriorityCount;
            if (remainingTerritories > 0 && armiesForOthers > 0) {
                int armiesPerOther = armiesForOthers / remainingTerritories;
                int extraArmies = armiesForOthers % remainingTerritories;

                for (int i = highPriorityCount; i < targetTerritories.size(); i++) {
                    Territory territory = targetTerritories.get(i);
                    int armies = armiesPerOther;
                    if (extraArmies > 0) {
                        armies++;
                        extraArmies--;
                    }
                    if (armies > 0) {
                        distribution.put(territory.getId(), armies);
                    }
                }
            }
        }

        return distribution;
    }

    @Override
    public void performBotReinforcement(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot BALANCED-AGGRESSIVE realizando refuerzos para jugador: {}", botPlayer.getId());

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

            // Estrategia BALANCED-AGGRESSIVE: distribuir de forma más inteligente
            Map<Long, Integer> reinforcements = distributeReinforcementsBalanced(
                    playerTerritories, game, botPlayer.getId(), availableReinforcements);

            // Usar el servicio oficial de refuerzos
            reinforcementService.placeReinforcementArmies(
                    game.getGameCode(),
                    botPlayer.getId(),
                    reinforcements
            );

            // Registrar cada refuerzo en el historial
            for (Map.Entry<Long, Integer> entry : reinforcements.entrySet()) {
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

            log.info("Bot BALANCED reforzó {} territorios con {} ejércitos total usando ReinforcementService",
                    reinforcements.size(), availableReinforcements);

        } catch (Exception e) {
            log.error("Error en refuerzos del bot BALANCED-AGGRESSIVE: {}", e.getMessage());
        }
    }

    /**
     * Distribuye refuerzos de forma balanceada entre territorios prioritarios
     */
    private Map<Long, Integer> distributeReinforcementsBalanced(List<Territory> territories,
                                                                GameEntity game, Long playerId, int totalReinforcements) {
        Map<Long, Integer> distribution = new HashMap<>();

        // Encontrar territorios fronterizos y priorizarlos
        List<Territory> borderTerritories = territories.stream()
                .filter(territory -> {
                    List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                            game.getId(), territory.getId());
                    return neighbors.stream()
                            .anyMatch(neighbor -> !neighbor.getOwnerId().equals(playerId));
                })
                .sorted((t1, t2) -> {
                    int t1Priority = calculateTerritoryPriority(t1, game, playerId);
                    int t2Priority = calculateTerritoryPriority(t2, game, playerId);
                    return Integer.compare(t2Priority, t1Priority);
                })
                .toList();

        if (borderTerritories.isEmpty()) {
            // Si no hay frontera, reforzar el más débil
            Territory weakestTerritory = territories.stream()
                    .min((t1, t2) -> Integer.compare(t1.getArmies(), t2.getArmies()))
                    .orElse(territories.get(0));
            distribution.put(weakestTerritory.getId(), totalReinforcements);
            return distribution;
        }

        // Estrategia balanceada: 70% al más prioritario, 30% distribuido entre otros
        if (totalReinforcements <= 3) {
            // Pocos refuerzos: todo al más prioritario
            Territory topPriority = borderTerritories.get(0);
            distribution.put(topPriority.getId(), totalReinforcements);
        } else {
            // Más refuerzos: distribuir inteligentemente
            Territory topPriority = borderTerritories.get(0);
            int primaryReinforcements = (totalReinforcements * 7) / 10; // 70%
            int remainingReinforcements = totalReinforcements - primaryReinforcements;

            distribution.put(topPriority.getId(), primaryReinforcements);

            // Distribuir el resto entre otros territorios fronterizos débiles
            List<Territory> secondaryTargets = borderTerritories.stream()
                    .skip(1)
                    .filter(t -> t.getArmies() <= 3) // Solo territorios vulnerables
                    .limit(2) // Máximo 2 territorios adicionales
                    .toList();

            if (!secondaryTargets.isEmpty() && remainingReinforcements > 0) {
                int armiesPerSecondary = remainingReinforcements / secondaryTargets.size();
                int extraArmies = remainingReinforcements % secondaryTargets.size();

                for (Territory territory : secondaryTargets) {
                    int armies = armiesPerSecondary;
                    if (extraArmies > 0) {
                        armies++;
                        extraArmies--;
                    }
                    if (armies > 0) {
                        distribution.put(territory.getId(), armies);
                    }
                }
            } else if (remainingReinforcements > 0) {
                // Si no hay secundarios válidos, dar todo al prioritario
                distribution.put(topPriority.getId(),
                        distribution.get(topPriority.getId()) + remainingReinforcements);
            }
        }

        return distribution;
    }

    @Override
    public void performBotAttack(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot BALANCED-AGGRESSIVE realizando ataques para jugador: {}", botPlayer.getId());

        try {
            // Estrategia BALANCED-AGGRESSIVE para ataques:
            // - Evalúa probabilidades de éxito antes de atacar
            // - Ataca solo cuando tiene ventaja significativa
            // - Puede realizar más ataques que NOVICE pero de forma más calculada

            List<Territory> attackableTerritories = combatService.getAttackableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (attackableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde atacar");
                return;
            }

            // Realizar ataques calculados
            int maxAttacks = 5; // Más agresivo que NOVICE pero no ilimitado
            int attackCount = 0;

            for (Territory attackerTerritory : attackableTerritories) {
                if (attackCount >= maxAttacks) break;

                List<Territory> targets = combatService.getTargetsForTerritory(
                        game.getGameCode(), attackerTerritory.getId(), botPlayer.getId());

                if (targets.isEmpty()) continue;

                // Encontrar el mejor objetivo evaluando probabilidades
                Territory bestTarget = null;
                double bestProbability = 0.0;

                for (Territory target : targets) {
                    double probability = evaluateAttackProbability(
                            botPlayer, attackerTerritory.getArmies(), target.getArmies());

                    if (probability > bestProbability && probability > 0.65) { // Solo atacar si prob > 65%
                        bestProbability = probability;
                        bestTarget = target;
                    }
                }

                if (bestTarget != null) {
                    // Calcular ejércitos para atacar de manera más inteligente
                    int attackingArmies = calculateOptimalAttackForce(
                            attackerTerritory.getArmies(), bestTarget.getArmies());

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

                        // Si conquistó el territorio, registrar la conquista también
                        if (result.getTerritoryConquered()) {
                            // Obtener el nombre del jugador anterior (defensor)
                            String fromPlayerName = "Jugador desconocido"; // Fallback
                            try {
                                // Aquí podrías obtener el nombre del jugador defensor si tienes acceso
                                // Por ahora dejamos un placeholder
                                fromPlayerName = "Oponente";
                            } catch (Exception e) {
                                log.warn("No se pudo obtener el nombre del jugador defensor");
                            }

                            gameEventService.recordTerritoryConquest(
                                    game.getId(),
                                    botPlayer.getId(),
                                    bestTarget.getName(),
                                    fromPlayerName,
                                    game.getCurrentTurn()
                            );
                        }

                        log.info("Bot atacó desde {} hacia {} con probabilidad {}: Conquistado={}",
                                attackerTerritory.getName(), bestTarget.getName(),
                                String.format("%.2f", bestProbability), result.getTerritoryConquered());

                        attackCount++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error en ataques del bot BALANCED-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotFortify(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot BALANCED-AGGRESSIVE realizando fortificación para jugador: {}", botPlayer.getId());

        try {
            // Estrategia BALANCED-AGGRESSIVE para fortificación:
            // - Identifica territorios vulnerables y los refuerza
            // - Balancea entre defensa y preparación para ataques futuros

            List<Territory> fortifiableTerritories = fortificationService.getFortifiableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (fortifiableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde fortificar");
                return;
            }

            // Estrategia más sofisticada: encontrar el mejor movimiento
            Territory bestSource = null;
            Territory bestTarget = null;
            int bestScore = 0;

            for (Territory source : fortifiableTerritories) {
                List<Territory> possibleTargets = fortificationService.getFortificationTargetsForTerritory(
                        game.getGameCode(), source.getId(), botPlayer.getId());

                for (Territory target : possibleTargets) {
                    int score = calculateFortificationScore(source, target, game, botPlayer.getId());
                    if (score > bestScore) {
                        bestScore = score;
                        bestSource = source;
                        bestTarget = target;
                    }
                }
            }

            if (bestSource != null && bestTarget != null) {
                int maxMovableArmies = fortificationService.getMaxMovableArmies(
                        game.getGameCode(), bestSource.getId());

                if (maxMovableArmies > 0) {
                    // Calcular cuántos ejércitos mover (más estratégico que NOVICE)
                    int armiesToMove = calculateOptimalFortificationAmount(
                            bestSource.getArmies(), bestTarget.getArmies(), maxMovableArmies);

                    FortifyDto fortifyDto = new FortifyDto();
                    fortifyDto.setPlayerId(botPlayer.getId());
                    fortifyDto.setFromCountryId(bestSource.getId());
                    fortifyDto.setToCountryId(bestTarget.getId());
                    fortifyDto.setArmies(armiesToMove);

                    boolean success = fortificationService.performFortification(
                            game.getGameCode(), fortifyDto);

                    if (success) {
                        // Registrar la fortificación en el historial
                        gameEventService.recordFortification(
                                game.getId(),
                                botPlayer.getId(),
                                bestSource.getName(),
                                bestTarget.getName(),
                                armiesToMove,
                                game.getCurrentTurn()
                        );

                        log.info("Bot fortificó desde {} hacia {} con {} ejércitos (Score: {})",
                                bestSource.getName(), bestTarget.getName(), armiesToMove, bestScore);
                    } else {
                        log.warn("Falló la fortificación del bot");
                    }
                }
            } else {
                log.info("Bot no encontró movimientos de fortificación beneficiosos");
            }

        } catch (Exception e) {
            log.error("Error en fortificación del bot BALANCED-AGGRESSIVE: {}", e.getMessage());
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

    @Override
    public double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies) {
        // Evaluación más sofisticada que NOVICE
        if (attackerArmies <= 1) return 0.0;

        // Fórmula mejorada considerando ventaja numérica
        double ratio = (double) attackerArmies / defenderArmies;

        // Probabilidad base según ratio
        double probability;
        if (ratio >= 3.0) {
            probability = 0.9; // Muy alta probabilidad si tengo 3x más ejércitos
        } else if (ratio >= 2.0) {
            probability = 0.75; // Alta probabilidad si tengo 2x más
        } else if (ratio >= 1.5) {
            probability = 0.6; // Moderada si tengo 1.5x más
        } else if (ratio >= 1.2) {
            probability = 0.4; // Baja si tengo solo 1.2x más
        } else {
            probability = 0.2; // Muy baja si tengo menos o similar
        }

        return probability;
    }

    @Override
    public List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game) {
        // Implementación futura - por ahora retorna lista vacía
        return List.of();
    }

    @Override
    public List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game) {
        // Implementación futura - por ahora retorna lista vacía
        return List.of();
    }

    // Métodos auxiliares para cálculos estratégicos

    private int calculateTerritoryPriority(Territory territory, GameEntity game, Long playerId) {
        // Calcula prioridad basada en vulnerabilidad y potencial ofensivo
        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                game.getId(), territory.getId());

        int enemyNeighbors = 0;
        int weakEnemyNeighbors = 0;

        for (Territory neighbor : neighbors) {
            if (!neighbor.getOwnerId().equals(playerId)) {
                enemyNeighbors++;
                if (neighbor.getArmies() < territory.getArmies()) {
                    weakEnemyNeighbors++;
                }
            }
        }

        // Score: más puntos por tener vecinos enemigos débiles, menos por ser muy débil
        int score = (weakEnemyNeighbors * 3) + (enemyNeighbors * 2) - (territory.getArmies() / 2);
        return score;
    }

    private int calculateOptimalAttackForce(int attackerArmies, int defenderArmies) {
        // Calcula la fuerza óptima para atacar
        int maxAttackForce = attackerArmies - 1; // Dejar al menos 1

        if (defenderArmies <= 2) {
            return Math.min(maxAttackForce, 2); // Ataque conservador contra débiles
        } else if (defenderArmies <= 4) {
            return Math.min(maxAttackForce, 3); // Ataque moderado
        } else {
            return Math.min(maxAttackForce, Math.max(3, defenderArmies - 1)); // Ataque fuerte
        }
    }

    private int calculateFortificationScore(Territory source, Territory target, GameEntity game, Long playerId) {
        // Calcula score para movimiento de fortificación
        List<Territory> targetNeighbors = gameTerritoryService.getNeighborTerritories(
                game.getId(), target.getId());
        List<Territory> sourceNeighbors = gameTerritoryService.getNeighborTerritories(
                game.getId(), source.getId());

        int targetEnemyNeighbors = (int) targetNeighbors.stream()
                .filter(n -> !n.getOwnerId().equals(playerId))
                .count();

        int sourceEnemyNeighbors = (int) sourceNeighbors.stream()
                .filter(n -> !n.getOwnerId().equals(playerId))
                .count();

        // Priorizar mover hacia territorios fronterizos desde territorios seguros
        int score = (targetEnemyNeighbors * 3) - (sourceEnemyNeighbors * 2);

        // Bonus si el objetivo es muy débil
        if (target.getArmies() <= 2) {
            score += 2;
        }

        return score;
    }

    /**
     * Calcula cantidad óptima a mover - MÉTODO FALTANTE
     */
    private int calculateOptimalFortificationAmount(int sourceArmies, int targetArmies, int maxMovable) {
        // Calcula cantidad óptima a mover
        int idealTargetStrength = 4; // Fuerza ideal para territorio fronterizo

        // Si el objetivo ya está bien defendido, no mover mucho
        if (targetArmies >= idealTargetStrength) {
            return Math.min(1, maxMovable); // Solo 1 ejército como refuerzo mínimo
        }

        // Calcular cuántos ejércitos necesita el objetivo para llegar al ideal
        int needed = idealTargetStrength - targetArmies;

        // No dejar el origen muy débil (mínimo 2 ejércitos)
        int safeToMove = Math.max(0, sourceArmies - 2);

        // Mover lo que se necesita o lo que se puede, lo que sea menor
        return Math.min(Math.min(needed, maxMovable), safeToMove);
    }

    /**
     * Registra eventos adicionales que podrían estar faltando
     */
    private void recordBotDecision(GameEntity game, PlayerEntity botPlayer, String decisionType, String details) {
        try {
            // Registrar decisiones importantes del bot para debugging/análisis
            String eventData = String.format("{\"decisionType\":\"%s\", \"details\":\"%s\"}",
                    decisionType, details);

            // Nota: Este método requeriría un nuevo tipo de evento como BOT_DECISION
            // Por ahora solo logueamos
            log.info("Bot {} tomó decisión: {} - {}",
                    botPlayer.getBotProfile().getBotName(), decisionType, details);

        } catch (Exception e) {
            log.warn("Error registrando decisión del bot: {}", e.getMessage());
        }
    }
}
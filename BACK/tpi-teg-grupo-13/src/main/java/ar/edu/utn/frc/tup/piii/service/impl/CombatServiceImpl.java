package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CombatServiceImpl implements CombatService {

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameStateService gameStateService;

    private final Random random = new Random();

    @Override
    @Transactional
    public CombatResultDto performCombat(String gameCode, AttackDto attackDto) {
        // obtener el juego y validar estado
        Game game = gameService.findByGameCode(gameCode);
        validateGameStateForCombat(game);

        //obtener territorios involucrados
        Territory attackerTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                game.getId(), attackDto.getAttackerCountryId());
        Territory defenderTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                game.getId(), attackDto.getDefenderCountryId());

        // validar el ataque
        validateAttack(game, attackDto, attackerTerritory, defenderTerritory);

        // determinar cantidad de dados
        int attackerDiceCount = determineAttackerDice(attackerTerritory);
        int defenderDiceCount = determineDefenderDice(defenderTerritory);

        // tirar dados
        List<Integer> attackerDice = rollDice(attackerDiceCount);
        List<Integer> defenderDice = rollDice(defenderDiceCount);

        // resolver combate
        CombatResult combatResult = resolveCombat(attackerDice, defenderDice);

        // aplicar perdidas
        applyLosses(game.getId(), attackDto, combatResult);

        //verificar conquista
        boolean territoryConquered = checkAndHandleConquest(game.getId(), attackDto, combatResult);

        // construir resultado
        return buildCombatResultDto(
                game.getId(),
                attackerTerritory, defenderTerritory,
                attackerDice, defenderDice,
                combatResult, territoryConquered);
    }


    private void validateAttack(Game game, AttackDto attackDto, Territory attacker, Territory defender) {
        // los territorios exist?
        if (attacker == null) {
            throw new IllegalArgumentException("Attacker territory not found: " + attackDto.getAttackerCountryId());
        }
        if (defender == null) {
            throw new IllegalArgumentException("Defender territory not found: " + attackDto.getDefenderCountryId());
        }

        // el pais desde el que ataca es suyo?
        if (!attackDto.getPlayerId().equals(attacker.getOwnerId())) {
            throw new IllegalArgumentException("Player doesn't own the attacking territory");
        }

        // es su propio territorio?
        if (attackDto.getPlayerId().equals(defender.getOwnerId())) {
            throw new IllegalArgumentException("Cannot attack your own territory");
        }

        // son territorios vecinos?
        if (!areTerritoriesNeighbors(attacker.getId(), defender.getId())) {
            throw new IllegalArgumentException("Territories are not neighbors");
        }

        // tiene suficientes armies?
        if (attacker.getArmies() <= 1) {
            throw new IllegalArgumentException("Attacking territory must have more than 1 army");
        }

        int maxAttackingArmies = attacker.getArmies() - 1; // Debe dejar al menos 1
        if (attackDto.getAttackingArmies() < 1 || attackDto.getAttackingArmies() > maxAttackingArmies) {
            throw new IllegalArgumentException(
                    String.format("Invalid attacking armies. Must be between 1 and %d", maxAttackingArmies));
        }
    }


    private int determineAttackerDice(Territory attacker) {
        int availableArmies = attacker.getArmies() - 1; // Debe dejar 1 ejército

        if (availableArmies <= 0) {
            throw new IllegalArgumentException("Territory must have more than 1 army to attack");
        }

        // Máximo 3 dados, limitado por ejércitos disponibles
        return Math.min(3, availableArmies);
    }



    private int determineDefenderDice(Territory defender) {
        return Math.min(3, defender.getArmies());
    }

    private List<Integer> rollDice(int diceCount) {
        return IntStream.range(0, diceCount)
                .map(i -> random.nextInt(6) + 1) // Dados de 1-6
                .boxed()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }

    //Resuelve el combate comparando dados

    private CombatResult resolveCombat(List<Integer> attackerDice, List<Integer> defenderDice) {
        int attackerLosses = 0;
        int defenderLosses = 0;

        // Comparar dados de mayor a menor
        int comparisons = Math.min(attackerDice.size(), defenderDice.size());

        for (int i = 0; i < comparisons; i++) {
            int attackerRoll = attackerDice.get(i);
            int defenderRoll = defenderDice.get(i);

            if (attackerRoll > defenderRoll) {
                // Atacante gana
                defenderLosses++;
            } else {
                // Defensor gana (empate gana el defensor)
                attackerLosses++;
            }
        }

        return new CombatResult(attackerLosses, defenderLosses);
    }


     //Aplica las perdidas de ejercitos a los territorios.

    private void applyLosses(Long gameId, AttackDto attackDto, CombatResult result) {
        // Reducir armis del atacante
        if (result.attackerLosses > 0) {
            gameTerritoryService.addArmiesToTerritory(
                    gameId, attackDto.getAttackerCountryId(), -result.attackerLosses);
        }

        // Reducir armis del defensor
        if (result.defenderLosses > 0) {
            gameTerritoryService.addArmiesToTerritory(
                    gameId, attackDto.getDefenderCountryId(), -result.defenderLosses);
        }
    }


    // Verifica si el territorio fue conquistado y maneja la transferencia.

    private boolean checkAndHandleConquest(Long gameId, AttackDto attackDto, CombatResult result) {

        Territory defenderTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                gameId, attackDto.getDefenderCountryId());

        if (defenderTerritory.getArmies() <= 0) {
            // conquistado
            int armiesToMove = attackDto.getAttackingArmies() - result.attackerLosses;

            // Transferir  territorio
            gameTerritoryService.transferTerritoryOwnership(
                    gameId, attackDto.getDefenderCountryId(), attackDto.getPlayerId(), 1);

            // Reducir armis del territorio atacante
            gameTerritoryService.addArmiesToTerritory(
                    gameId, attackDto.getAttackerCountryId(), -1);

            /*
            // Registrar el turno de conquista
            defenderTerritoryEntity.setLastConqueredTurn(gameEntity.getCurrentTurn());
            gameTerritoryService.save(defenderTerritoryEntity);
             */

            return true;
        }

        return false;
    }

    private CombatResultDto buildCombatResultDto(
            Long gameId,
            Territory attackerTerritory, Territory defenderTerritory,
            List<Integer> attackerDice, List<Integer> defenderDice,
            CombatResult result, boolean territoryConquered) {

        // Get updated territories after the combat
        Territory updatedAttacker = gameTerritoryService.getTerritoryByGameAndCountry(
                gameId, attackerTerritory.getId());
        Territory updatedDefender = gameTerritoryService.getTerritoryByGameAndCountry(
                gameId, defenderTerritory.getId());

        return CombatResultDto.builder()
                .attackerCountryId(attackerTerritory.getId())
                .attackerCountryName(attackerTerritory.getName())
                .defenderCountryId(defenderTerritory.getId())
                .defenderCountryName(defenderTerritory.getName())
                .attackerPlayerName(attackerTerritory.getOwnerName())
                .defenderPlayerName(defenderTerritory.getOwnerName())
                .attackerDice(attackerDice)
                .defenderDice(defenderDice)
                .attackerLosses(result.attackerLosses)
                .defenderLosses(result.defenderLosses)
                .territoryConquered(territoryConquered)
                .attackerRemainingArmies(updatedAttacker != null ? updatedAttacker.getArmies() : 0)
                .defenderRemainingArmies(updatedDefender != null ? updatedDefender.getArmies() : 0)
                .build();
    }

    //son vecinos?
    private boolean areTerritoriesNeighbors(Long territoryId1, Long territoryId2) {
        return gameTerritoryService.areTerritoriesNeighbors(territoryId1, territoryId2);
    }

   //esta es una clase interna para el rsutado del combate, la llamariamos con CombatServiceImpl.CombatResult,
    //para llamarla aca adentro sin tener que instanciarla

    private static class CombatResult {
        final int attackerLosses;
        final int defenderLosses;

        CombatResult(int attackerLosses, int defenderLosses) {
            this.attackerLosses = attackerLosses;
            this.defenderLosses = defenderLosses;
        }
    }



    @Override
    public List<Territory> getAttackableTerritoriesForPlayer(String gameCode, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);
        List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);

        return playerTerritories.stream()
                .filter(territory -> territory.getArmies() > 1) // Puede atacar
                .collect(Collectors.toList());
    }

    @Override
    public List<Territory> getTargetsForTerritory(String gameCode, Long territoryId, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);
        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(game.getId(), territoryId);

        return neighbors.stream()
                .filter(neighbor -> !playerId.equals(neighbor.getOwnerId())) // No es suyo
                .collect(Collectors.toList());
    }

    @Transactional
    public CombatResultDto performCombatWithValidation(String gameCode, AttackDto attackDto) {
        Game game = gameService.findByGameCode(gameCode);

        validateGameStateForCombat(game);
        validatePlayerTurn(game, attackDto.getPlayerId());

        return performCombat(gameCode, attackDto);
    }

    private void validateGameStateForCombat(Game game) {
        if (!isGameStateValidForCombat(game)) {
            throw new IllegalStateException("Combat not allowed in current game state: " + game.getState());
        }
    }

    private boolean isGameStateValidForCombat(Game game) {
        return switch (game.getState()) {
            case HOSTILITY_ONLY, NORMAL_PLAY -> true;
            default -> false;
        };
    }

    private void validatePlayerTurn(Game game, Long playerId) {
        if (!gameStateService.isPlayerTurn(game, playerId)) {
            throw new IllegalStateException("It's not player's turn to attack");
        }

        if (!gameStateService.canPerformAction(game, "attack")) {
            throw new IllegalStateException("Cannot attack in current turn phase: " + game.getCurrentPhase());
        }
    }
}
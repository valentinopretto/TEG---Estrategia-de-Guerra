package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.model.Game;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador especializado para operaciones de combate en el TEG.
 * Maneja ataques, validaciones y consultas relacionadas con el combate.
 */
@RestController
@RequestMapping("/api/games/{gameCode}/combat")
@Tag(name = "Combat", description = "Operaciones de combate y ataque")
@Slf4j
public class CombatController {

    @Autowired
    private CombatService combatService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameStateService gameStateService;

    /**
     * Ejecuta un ataque entre dos territorios.
     *
     * @param gameCode Código del juego
     * @param attackDto Datos del ataque (atacante, defensor, ejércitos)
     * @return Resultado del combate con dados, pérdidas y conquista
     */
    @PostMapping("/attack")
    @Operation(summary = "Ejecutar ataque",
            description = "Realiza un ataque entre dos territorios vecinos según las reglas del TEG")
    public ResponseEntity<CombatResultDto> attack(
            @PathVariable String gameCode,
            @Valid @RequestBody AttackDto attackDto) {

        log.info("Attack requested in game {} by player {} from territory {} to territory {}",
                gameCode, attackDto.getPlayerId(), attackDto.getAttackerCountryId(), attackDto.getDefenderCountryId());

        try {
            // Validar que el juego permite ataques
            Game game = gameService.findByGameCode(gameCode);
            validateGameStateForCombat(game);
            validatePlayerTurn(game, attackDto.getPlayerId());

            // Ejecutar el combate
            CombatResultDto result = combatService.performCombat(gameCode, attackDto);

            log.info("Attack completed. Territory conquered: {}, Attacker losses: {}, Defender losses: {}",
                    result.getTerritoryConquered(), result.getAttackerLosses(), result.getDefenderLosses());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error during attack in game {}: {}", gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene todos los territorios que un jugador puede usar para atacar.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return Lista de territorios con más de 1 ejército
     */
    @GetMapping("/attackable-territories/{playerId}")
    @Operation(summary = "Obtener territorios atacables",
            description = "Lista los territorios del jugador que pueden atacar (con más de 1 ejército)")
    public ResponseEntity<List<Territory>> getAttackableTerritories(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        try {
            Game game = gameService.findByGameCode(gameCode);
            validateGameStateForCombat(game);

            List<Territory> attackableTerritories = combatService.getAttackableTerritoriesForPlayer(gameCode, playerId);

            log.debug("Player {} has {} attackable territories in game {}",
                    playerId, attackableTerritories.size(), gameCode);

            return ResponseEntity.ok(attackableTerritories);

        } catch (Exception e) {
            log.error("Error getting attackable territories for player {} in game {}: {}",
                    playerId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene los territorios enemigos que un territorio específico puede atacar.
     *
     * @param gameCode Código del juego
     * @param territoryId ID del territorio atacante
     * @param playerId ID del jugador (para verificar propiedad)
     * @return Lista de territorios enemigos vecinos
     */
    @GetMapping("/attack-targets/{territoryId}/{playerId}")
    @Operation(summary = "Obtener objetivos de ataque",
            description = "Lista los territorios enemigos que puede atacar un territorio específico")
    public ResponseEntity<List<Territory>> getAttackTargets(
            @PathVariable String gameCode,
            @PathVariable Long territoryId,
            @PathVariable Long playerId) {

        try {
            Game game = gameService.findByGameCode(gameCode);
            validateGameStateForCombat(game);

            List<Territory> targets = combatService.getTargetsForTerritory(gameCode, territoryId, playerId);

            log.debug("Territory {} can attack {} enemy territories in game {}",
                    territoryId, targets.size(), gameCode);

            return ResponseEntity.ok(targets);

        } catch (Exception e) {
            log.error("Error getting attack targets for territory {} in game {}: {}",
                    territoryId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verifica si un jugador puede atacar en el estado actual del juego.
     *
     * @param gameCode Código del juego
     * @param playerId ID del jugador
     * @return true si puede atacar, false en caso contrario
     */
    @GetMapping("/can-attack/{playerId}")
    @Operation(summary = "Verificar si puede atacar",
            description = "Verifica si un jugador puede realizar ataques en el estado actual")
    public ResponseEntity<Boolean> canPlayerAttack(
            @PathVariable String gameCode,
            @PathVariable Long playerId) {

        try {
            Game game = gameService.findByGameCode(gameCode);

            // Verificar estado del juego
            boolean gameAllowsAttacks = isGameStateValidForCombat(game);

            // Verificar turno del jugador
            boolean isPlayerTurn = gameStateService.isPlayerTurn(game, playerId);

            // Verificar fase del turno
            boolean isAttackPhase = gameStateService.canPerformAction(game, "attack");

            boolean canAttack = gameAllowsAttacks && isPlayerTurn && isAttackPhase;

            log.debug("Player {} can attack in game {}: {} (gameState: {}, turn: {}, phase: {})",
                    playerId, gameCode, canAttack, gameAllowsAttacks, isPlayerTurn, isAttackPhase);

            return ResponseEntity.ok(canAttack);

        } catch (Exception e) {
            log.error("Error checking if player {} can attack in game {}: {}",
                    playerId, gameCode, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // Validaciones

    /**
     * Valida que el juego esté en un estado que permita combates.
     */
    private void validateGameStateForCombat(Game game) {
        if (!isGameStateValidForCombat(game)) {
            throw new IllegalStateException("Combat not allowed in current game state: " + game.getState());
        }
    }

    /**
     * Verifica si el estado del juego permite combates.
     */
    private boolean isGameStateValidForCombat(Game game) {
        switch (game.getState()) {
            case HOSTILITY_ONLY:
            case NORMAL_PLAY:
                return true;
            default:
                return false;
        }
    }

    /**
     * Valida que sea el turno del jugador.
     */
    private void validatePlayerTurn(Game game, Long playerId) {
        if (!gameStateService.isPlayerTurn(game, playerId)) {
            throw new IllegalStateException("It's not player's turn to attack");
        }

        if (!gameStateService.canPerformAction(game, "attack")) {
            throw new IllegalStateException("Cannot attack in current turn phase: " + game.getCurrentPhase());
        }
    }
}
package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementStatusDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;

import java.util.Map;

/**
 * Service interface for managing reinforcement phase during normal gameplay.
 * Handles calculation and placement of reinforcement armies.
 */
public interface ReinforcementService {

    /**
     * Places reinforcement armies on territories during the reinforcement phase.
     *
     * @param gameCode the game code
     * @param playerId the player ID
     * @param armiesByCountry map of countryId -> armies to place
     */
    void placeReinforcementArmies(String gameCode, Long playerId, Map<Long, Integer> armiesByCountry);

    /**
     * Calculates the total reinforcement armies a player should receive.
     * Base: floor(territories/2) with minimum of 3
     * Plus continent bonuses
     * Plus any card trade bonuses
     *
     * @param game the game
     * @param player the player
     * @return total armies to place
     */
    int calculateReinforcementArmies(Game game, Player player);

    /**
     * Gets the base reinforcement armies from territory count.
     * Formula: max(3, floor(territoryCount/2))
     *
     * @param territoryCount number of territories owned
     * @return base armies
     */
    int calculateBaseArmies(int territoryCount);

    /**
     * Calculates continent bonus armies for controlled continents.
     *
     * @param game the game
     * @param player the player
     * @return total continent bonus
     */
    int calculateContinentBonus(Game game, Player player);

    /**
     * Gets the reinforcement status for a player.
     *
     * @param gameCode the game code
     * @param playerId the player ID
     * @return reinforcement status DTO
     */
    ReinforcementStatusDto getReinforcementStatus(String gameCode, Long playerId);

    /**
     * Validates if reinforcement can be performed.
     *
     * @param game the game
     * @param player the player
     * @return true if valid
     */
    boolean canPerformReinforcement(Game game, Player player);
}
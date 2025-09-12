package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.service.interfaces.FortificationService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FortificationServiceImpl implements FortificationService {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Override
    @Transactional
    public boolean performFortification(String gameCode, FortifyDto fortifyDto) {
        log.info("Starting fortification in game {} - Player {} moving {} armies from {} to {}",
                gameCode, fortifyDto.getPlayerId(), fortifyDto.getArmies(),
                fortifyDto.getFromCountryId(), fortifyDto.getToCountryId());

        try {
            // 1. Validar que la fortificación sea legal
            if (!isValidFortification(gameCode, fortifyDto)) {
                log.warn("Invalid fortification attempt in game {}", gameCode);
                return false;
            }

            // 2. Realizar la transferencia de ejércitos
            // Quitar ejércitos del territorio origen
            gameTerritoryService.addArmiesToTerritory(
                    gameService.findByGameCode(gameCode).getId(),
                    fortifyDto.getFromCountryId(),
                    -fortifyDto.getArmies()
            );

            // Añadir ejércitos al territorio destino
            gameTerritoryService.addArmiesToTerritory(
                    gameService.findByGameCode(gameCode).getId(),
                    fortifyDto.getToCountryId(),
                    fortifyDto.getArmies()
            );

            log.info("Fortification completed successfully in game {}", gameCode);
            return true;

        } catch (Exception e) {
            log.error("Error during fortification in game {}: {}", gameCode, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Territory> getFortifiableTerritoriesForPlayer(String gameCode, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);
        List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);

        return playerTerritories.stream()
                .filter(territory -> territory.getArmies() > 1) // Puede fortificar
                .collect(Collectors.toList());
    }

    @Override
    public List<Territory> getFortificationTargetsForTerritory(String gameCode, Long fromTerritoryId, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);

        // Verificar que el jugador es dueño del territorio origen
        Territory fromTerritory = gameTerritoryService.getTerritoryByGameAndCountry(game.getId(), fromTerritoryId);
        if (fromTerritory == null || !playerId.equals(fromTerritory.getOwnerId())) {
            throw new IllegalArgumentException("Player doesn't own the specified territory");
        }

        // Verificar que el territorio puede fortificar
        if (fromTerritory.getArmies() <= 1) {
            return Collections.emptyList();
        }

        // Obtener todos los territorios del jugador
        List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);

        // En HOSTILITY_ONLY solo se permite fortificación entre territorios ADYACENTES
        if (game.getState() == GameState.HOSTILITY_ONLY) {
            return getAdjacentFortificationTargets(game.getId(), fromTerritoryId, playerTerritories);
        }

        // En NORMAL_PLAY se permite fortificación a través de cadenas de territorios conectados
        return playerTerritories.stream()
                .filter(territory -> !territory.getId().equals(fromTerritoryId)) // No incluir el mismo territorio
                .filter(territory -> areTerritoriesConnectedByPlayer(gameCode, fromTerritoryId, territory.getId(), playerId))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene territorios adyacentes (solo vecinos directos) para fortificación en HOSTILITY_ONLY.
     */
    private List<Territory> getAdjacentFortificationTargets(Long gameId, Long fromTerritoryId, List<Territory> playerTerritories) {
        // Obtener vecinos directos del territorio
        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(gameId, fromTerritoryId);

        // Crear un set de IDs de territorios del jugador para búsqueda rápida
        Set<Long> playerTerritoryIds = playerTerritories.stream()
                .map(Territory::getId)
                .collect(Collectors.toSet());

        // Filtrar solo vecinos que pertenecen al jugador
        return neighbors.stream()
                .filter(neighbor -> playerTerritoryIds.contains(neighbor.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean areTerritoriesConnectedByPlayer(String gameCode, Long fromTerritoryId, Long toTerritoryId, Long playerId) {
        if (fromTerritoryId.equals(toTerritoryId)) {
            return true; // El mismo territorio está "conectado" consigo mismo
        }

        Game game = gameService.findByGameCode(gameCode);

        // En HOSTILITY_ONLY solo se permite fortificación entre territorios ADYACENTES
        if (game.getState() == GameState.HOSTILITY_ONLY) {
            return areTerritoriesAdjacent(game.getId(), fromTerritoryId, toTerritoryId, playerId);
        }

        // En NORMAL_PLAY se permite fortificación a través de cadenas de territorios conectados
        List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);

        // Crear un mapa de territorios del jugador para búsqueda rápida
        Set<Long> playerTerritoryIds = playerTerritories.stream()
                .map(Territory::getId)
                .collect(Collectors.toSet());

        // Usar BFS para encontrar conexión
        return findPathBFS(game.getId(), fromTerritoryId, toTerritoryId, playerTerritoryIds);
    }

    /**
     * Verifica si dos territorios son adyacentes (vecinos directos) y ambos pertenecen al jugador.
     */
    private boolean areTerritoriesAdjacent(Long gameId, Long fromTerritoryId, Long toTerritoryId, Long playerId) {
        // Verificar que ambos territorios pertenecen al jugador
        Territory fromTerritory = gameTerritoryService.getTerritoryByGameAndCountry(gameId, fromTerritoryId);
        Territory toTerritory = gameTerritoryService.getTerritoryByGameAndCountry(gameId, toTerritoryId);

        if (fromTerritory == null || toTerritory == null) {
            return false;
        }

        if (!playerId.equals(fromTerritory.getOwnerId()) || !playerId.equals(toTerritory.getOwnerId())) {
            return false;
        }

        // Verificar si son vecinos directos
        return gameTerritoryService.areTerritoriesNeighbors(fromTerritoryId, toTerritoryId);
    }

    /**
     * Utiliza búsqueda en anchura (BFS) para encontrar un camino entre dos territorios
     * que pase solo por territorios del jugador.
     */
    private boolean findPathBFS(Long gameId, Long fromId, Long toId, Set<Long> playerTerritoryIds) {
        if (!playerTerritoryIds.contains(fromId) || !playerTerritoryIds.contains(toId)) {
            return false;
        }

        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        queue.offer(fromId);
        visited.add(fromId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();

            if (currentId.equals(toId)) {
                return true; // Encontrado el destino
            }

            // Obtener vecinos del territorio actual
            List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(gameId, currentId);

            for (Territory neighbor : neighbors) {
                Long neighborId = neighbor.getId();

                // Solo continuar si el vecino pertenece al jugador y no ha sido visitado
                if (playerTerritoryIds.contains(neighborId) && !visited.contains(neighborId)) {
                    visited.add(neighborId);
                    queue.offer(neighborId);
                }
            }
        }

        return false; // No se encontró camino
    }

    @Override
    public boolean isValidFortification(String gameCode, FortifyDto fortifyDto) {
        try {
            Game game = gameService.findByGameCode(gameCode);

            // 1. Validar estado del juego
            if (!isGameStateValidForFortification(game)) {
                log.warn("Game state {} doesn't allow fortification", game.getState());
                return false;
            }

            // 2. Obtener territorios
            Territory fromTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                    game.getId(), fortifyDto.getFromCountryId());
            Territory toTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                    game.getId(), fortifyDto.getToCountryId());

            if (fromTerritory == null || toTerritory == null) {
                log.warn("One or both territories not found");
                return false;
            }

            // 3. Validar propiedad de ambos territorios
            if (!fortifyDto.getPlayerId().equals(fromTerritory.getOwnerId()) ||
                    !fortifyDto.getPlayerId().equals(toTerritory.getOwnerId())) {
                log.warn("Player doesn't own both territories");
                return false;
            }

            // 4. Validar que no sea el mismo territorio
            if (fortifyDto.getFromCountryId().equals(fortifyDto.getToCountryId())) {
                log.warn("Cannot fortify from territory to itself");
                return false;
            }

            // 5. Validar cantidad mínima de ejércitos a mover
            if (fortifyDto.getArmies() < 1) {
                log.warn("Must move at least 1 army");
                return false;
            }

            // 6. VALIDACIÓN CRÍTICA: No se puede dejar el territorio origen sin ejércitos
            // Debe quedar AL MENOS 1 ejército en el territorio origen
            if (fromTerritory.getArmies() - fortifyDto.getArmies() < 1) {
                log.warn("Cannot leave territory without armies. Territory has: {}, trying to move: {}, minimum required to stay: 1",
                        fromTerritory.getArmies(), fortifyDto.getArmies());
                return false;
            }

            // 7. Validar conexión entre territorios según las reglas del estado actual
            if (!areTerritoriesConnectedByPlayer(gameCode, fortifyDto.getFromCountryId(),
                    fortifyDto.getToCountryId(), fortifyDto.getPlayerId())) {
                log.warn("Territories are not connected according to current game state rules");
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating fortification: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getMaxMovableArmies(String gameCode, Long territoryId) {
        Game game = gameService.findByGameCode(gameCode);
        Territory territory = gameTerritoryService.getTerritoryByGameAndCountry(game.getId(), territoryId);

        if (territory == null) {
            return 0;
        }

        // REGLA FUNDAMENTAL: Debe quedar AL MENOS 1 ejército en el territorio
        // Por lo tanto, el máximo movible es (total - 1)
        int maxMovable = Math.max(0, territory.getArmies() - 1);

        log.debug("Territory {} has {} armies, can move maximum {}",
                territoryId, territory.getArmies(), maxMovable);

        return maxMovable;
    }

    /**
     * Verifica si el estado del juego permite fortificación.
     */
    private boolean isGameStateValidForFortification(Game game) {
        switch (game.getState()) {
            case HOSTILITY_ONLY:
            case NORMAL_PLAY:
                return true;
            default:
                return false;
        }
    }
}
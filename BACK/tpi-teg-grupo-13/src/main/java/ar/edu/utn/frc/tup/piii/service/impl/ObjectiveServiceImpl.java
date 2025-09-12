package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.ObjectiveMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.ObjectiveRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ObjectiveServiceImpl implements ObjectiveService {

    @Autowired
    private ObjectiveRepository objectiveRepository;

    @Autowired
    private ObjectiveMapper objectiveMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    private static final int COMMON_OBJECTIVE_TERRITORIES = 30; // TEG standard

    @Override
    public Objective save(Objective objective) {
        ObjectiveEntity saved = objectiveRepository.save(objectiveMapper.toEntity(objective));
        return objectiveMapper.toModel(saved);
    }

    @Override
    public Optional<Objective> findById(Long id) {
        return objectiveRepository.findById(id).map(objectiveMapper::toModel);
    }

    @Override
    public List<Objective> findAll() {
        return objectiveRepository.findAll().stream()
                .map(objectiveMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Objective> findByType(ObjectiveType type) {
        return objectiveRepository.findByType(type).stream()
                .map(objectiveMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        objectiveRepository.deleteById(id);
    }

    @Override
    public List<Objective> createObjectivesForGame(Game game) {
        return findAll();
    }

    @Override
    public void assignObjectivesToPlayers(Game game) {
        List<Objective> objectives = new ArrayList<>(findAll());
        Collections.shuffle(objectives);

        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Objective o = objectives.get(i % objectives.size());
            p.setObjective(o);
        }
    }

    @Override
    public boolean isObjectiveAchieved(Long objectiveId, Game game, Player player) {
        Optional<Objective> obj = findById(objectiveId);
        return obj.map(o -> validateObjectiveCompletion(o, game, player)).orElse(false);
    }

    @Override
    public boolean validateObjectiveCompletion(Objective objective, Game game, Player player) {
        if (objective == null) {
            log.warn("Objective is null for player {}", player.getDisplayName());
            return false;
        }

        ObjectiveType type = objective.getType();
        log.debug("Validating {} objective for player {}", type, player.getDisplayName());

        switch (type) {
            case COMMON:
                return validateCommonObjective(game, player);
            case OCCUPATION:
                return validateOccupationObjective(objective, game, player);
            case DESTRUCTION:
                return validateDestructionObjective(objective, game, player);
            default:
                log.warn("Unknown objective type: {}", type);
                return false;
        }
    }

    @Override
    public Optional<Player> findWinner(Game game) {
        log.debug("Checking for winner in game {}", game.getGameCode());

        return game.getPlayers().stream()
                .filter(player -> player.getStatus() == PlayerStatus.ACTIVE)
                .filter(player -> {
                    Objective objective = player.getObjective();
                    if (objective == null) {
                        log.warn("Player {} has no objective assigned", player.getDisplayName());
                        return false;
                    }
                    boolean achieved = validateObjectiveCompletion(objective, game, player);
                    if (achieved) {
                        log.info("Player {} has achieved their objective: {}",
                                player.getDisplayName(), objective.getDescription());
                    }
                    return achieved;
                })
                .findFirst();
    }

    /**
     * Valida el objetivo común: controlar 30 territorios
     */
    private boolean validateCommonObjective(Game game, Player player) {
        // Contar territorios que pertenecen al jugador
        List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), player.getId());
        int territoryCount = playerTerritories.size();

        log.debug("Player {} has {} territories (needs {} for common objective)",
                player.getDisplayName(), territoryCount, COMMON_OBJECTIVE_TERRITORIES);

        return territoryCount >= COMMON_OBJECTIVE_TERRITORIES;
    }

    /**
     * Valida objetivos de ocupación (control de continentes específicos)
     */
    private boolean validateOccupationObjective(Objective objective, Game game, Player player) {
        List<String> targetContinents = objective.getTargetContinents();

        if (targetContinents == null || targetContinents.isEmpty()) {
            log.warn("Occupation objective has no target continents defined");
            return false;
        }

        log.debug("Player {} needs to control continents: {}",
                player.getDisplayName(), targetContinents);

        // Verificar que el jugador controla TODOS los continentes requeridos
        for (String continentName : targetContinents) {
            boolean controlsContinent = doesPlayerControlContinent(game, player, continentName);

            if (!controlsContinent) {
                log.debug("Player {} does NOT control continent {}",
                        player.getDisplayName(), continentName);
                return false;
            }

            log.debug("Player {} DOES control continent {}",
                    player.getDisplayName(), continentName);
        }

        // Si llegamos aquí, el jugador controla todos los continentes requeridos
        return true;
    }

    /**
     * Verifica si un jugador controla completamente un continente
     */
    private boolean doesPlayerControlContinent(Game game, Player player, String continentName) {
        // Usar el método del servicio que ya existe
        return gameTerritoryService.doesPlayerControlContinent(game.getId(), player.getId(), continentName);
    }

    /**
     * Valida objetivos de destrucción (eliminar jugador de color específico)
     */
    private boolean validateDestructionObjective(Objective objective, Game game, Player player) {
        PlayerColor targetColor = objective.getTargetColor();

        if (targetColor == null) {
            log.warn("Destruction objective has no target color defined");
            return false;
        }

        log.debug("Player {} needs to eliminate color {}",
                player.getDisplayName(), targetColor);

        // Verificar si existe algún jugador activo con ese color
        boolean targetColorEliminated = game.getPlayers().stream()
                .filter(p -> p.getColor() == targetColor)
                .allMatch(p -> p.getStatus() == PlayerStatus.ELIMINATED);

        if (targetColorEliminated) {
            log.info("Player {} has eliminated all {} players",
                    player.getDisplayName(), targetColor);
        }

        return targetColorEliminated;
    }

    @Override
    public List<Objective> getCommonObjectives() {
        return findByType(ObjectiveType.COMMON);
    }

    @Override
    public List<Objective> getOccupationObjectives() {
        return findByType(ObjectiveType.OCCUPATION);
    }

    @Override
    public List<Objective> getDestructionObjectives() {
        return findByType(ObjectiveType.DESTRUCTION);
    }

    @Override
    public String getObjectiveProgress(Long objectiveId, Game game, Player player) {
        Optional<Objective> objectiveOpt = findById(objectiveId);
        if (objectiveOpt.isEmpty()) {
            return "Objective not found";
        }

        Objective objective = objectiveOpt.get();

        switch (objective.getType()) {
            case COMMON:
                List<Territory> territories = gameTerritoryService.getTerritoriesByOwner(game.getId(), player.getId());
                return String.format("Territories: %d/%d", territories.size(), COMMON_OBJECTIVE_TERRITORIES);

            case OCCUPATION:
                List<String> targetContinents = objective.getTargetContinents();
                long controlledCount = targetContinents.stream()
                        .filter(continent -> doesPlayerControlContinent(game, player, continent))
                        .count();
                return String.format("Continents controlled: %d/%d", controlledCount, targetContinents.size());

            case DESTRUCTION:
                PlayerColor targetColor = objective.getTargetColor();
                boolean eliminated = game.getPlayers().stream()
                        .filter(p -> p.getColor() == targetColor)
                        .allMatch(p -> p.getStatus() == PlayerStatus.ELIMINATED);
                return eliminated ? "Target eliminated!" : "Target still active";

            default:
                return "Unknown objective type";
        }
    }
}
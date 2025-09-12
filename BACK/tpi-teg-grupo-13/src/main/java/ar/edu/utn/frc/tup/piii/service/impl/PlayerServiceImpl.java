package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.ObjectiveMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ObjectiveMapper objectiveMapper;


    @Override
    public Player save(Player player) {
        PlayerEntity entity = playerMapper.toEntity(player);
        PlayerEntity savedEntity = playerRepository.save(entity);
        return playerMapper.toModel(savedEntity);
    }


    @Override
    public Optional<Player> findById(Long Id) {
        return playerRepository.findById(Id)
                .map(playerMapper::toModel);
    }

    @Override
    public List<Player> findAll() {
        List<PlayerEntity> playerEntities = playerRepository.findAll();

        if (playerEntities.isEmpty()) {
            throw new PlayerNotFoundException("No players found");
        }

        return playerEntities.stream()
                .map(playerMapper::toModel)
                .toList();
    }
/*
    @Override
    public int getConqueredCountriesThisTurn(Long playerId, GameEntity game) {
        return (int) game.getTerritories().stream()
                .filter(t -> t.getOwner() != null && t.getOwner().getId().equals(playerId)
                        && t.getLastConqueredTurn() != null
                        && t.getLastConqueredTurn().equals(game.getCurrentTurn()))
                .count();
    }
*/
    @Override
    public List<Player> findByGame(Game game) {
        GameEntity entity = gameMapper.toEntity(game);
        List<PlayerEntity> playerEntity = playerRepository.findByGame(entity);
        if (playerEntity.isEmpty()) {
            throw new PlayerNotFoundException("No players found");
        }

        return playerEntity.stream()
                .map(playerMapper::toModel)
                .toList();
    }

    @Override
    public List<Player> findActivePlayersByGame(Game game) {
        // return playerRepository.findByGameAndStatus(game, PlayerStatus.ACTIVE);
        GameEntity entity = gameMapper.toEntity(game);
        List<PlayerEntity> playerEntity = playerRepository.findActivePlayersByGame(entity);
        if (playerEntity.isEmpty()) {
            throw new PlayerNotFoundException("No players found");
        }

        return playerEntity.stream()
                .map(playerMapper::toModel)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public Player createHumanPlayer(User user, Game game, int seatOrder) {
        PlayerEntity entity = new PlayerEntity();

        entity.setUser(userMapper.toEntity(user));
        entity.setGame(gameMapper.toEntity(game));
        entity.setSeatOrder(seatOrder);
        entity.setStatus(PlayerStatus.ACTIVE);
        entity.setJoinedAt(LocalDateTime.now());
        entity.setArmiesToPlace(0);


        PlayerColor assignedColor = getNextAvailableColor(game.getPlayers());
        entity.setColor(assignedColor);

        PlayerEntity saved = playerRepository.save(entity);
        return playerMapper.toModel(saved);
    }

    private PlayerColor getNextAvailableColor(List<Player> existingPlayers) {
        List<PlayerColor> usedColors = existingPlayers.stream()
                .map(Player::getColor)
                .toList();

        for (PlayerColor color : PlayerColor.values()) {
            if (!usedColors.contains(color)) {
                return color;
            }
        }

        throw new IllegalStateException("No available colors left for players");
    }



    @Override
    public Player createBotPlayer(BotLevel level, Game game) {
        //TODO: crear bot, necesito el service

        return  null;
    }


    @Override
    public void eliminatePlayer(Long playerId) {
        updateStatus(playerId, PlayerStatus.ELIMINATED);
    }

    @Override
    public void activatePlayer(Long playerId) {
        updateStatus(playerId, PlayerStatus.ACTIVE);
    }

    @Override
    public void updateStatus(Long playerId, PlayerStatus status) {
        playerRepository.findById(playerId).ifPresent(player -> {
            player.setStatus(status);
            playerRepository.save(player);
        });
    }

    @Override
    public boolean isEliminated(Long playerId) {
        return playerRepository.findById(playerId)
                .map(p -> p.getStatus() == PlayerStatus.ELIMINATED)
                .orElse(false);
    }

    @Override
    public boolean isActive(Long playerId) {
        return playerRepository.findById(playerId)
                .map(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    public void assignObjective(Long playerId, Objective objective) {
        ObjectiveEntity entityOb = objectiveMapper.toEntity(objective);
        playerRepository.findById(playerId).ifPresent(player -> {
            player.setObjective(entityOb);
            playerRepository.save(player);
        });
    }

    @Override
    public boolean hasWon(Long playerId, Game game) {
        // TODO: Implementar según reglas de victoria
        return hasAchievedObjective(playerId);
    }

    @Override
    public boolean hasAchievedObjective(Long playerId) {
        return false; // TODO: logica para saber si el jugador cumpli su objetivo
    }


    @Override
    @Transactional
    public void addArmiesToPlace(Long playerId, int armies) {
        playerRepository.addArmiesToPlace(playerId, armies);
    }

    @Override
    @Transactional
    public void removeArmiesToPlace(Long playerId, int armies) {
        playerRepository.removeArmiesToPlace(playerId, armies);
    }

    @Override
    public int getArmiesToPlace(Long playerId) {
        return playerRepository.findById(playerId)
                .map(playerMapper::toModel)
                .map(Player::getArmiesToPlace)
                .orElse(0);
    }


    @Override
    public boolean canPerformAction(Long playerId, Game game) {
        return isActive(playerId) && belongsToGame(playerId, game.getId());
    }

    @Override
    public boolean isPlayerTurn(Long playerId, Game game) {
        return game.getPlayers().get(game.getCurrentPlayerIndex()).getId().equals(playerId);
    }

    @Override
    public boolean belongsToGame(Long playerId, Long gameId) {
        return playerRepository.findById(playerId)
                .map(player -> player.getGame().getId().equals(gameId))
                .orElse(false);
    }
}
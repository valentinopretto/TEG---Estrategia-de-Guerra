package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Component
public class GameMapper {

    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private GameEventMapper gameEventMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private TerritoryMapper territoryMapper;
    @Autowired
    private ContinentMapper continentMapper;
    @Autowired
    private CountryRepository countryRepository;

    public Game toModel(GameEntity entity) {
        if (entity == null) return null;

        return Game.builder()
                .id(entity.getId())
                .gameCode(entity.getGameCode())
                .createdByUserId(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdByUsername(entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null)
                .state(entity.getStatus())
                .currentPhase(entity.getCurrentPhase())
                .currentTurn(entity.getCurrentTurn())
                .currentPlayerIndex(entity.getCurrentPlayerIndex())
                .maxPlayers(entity.getMaxPlayers())
                .turnTimeLimit(entity.getTurnTimeLimit())
                .chatEnabled(entity.getChatEnabled())
                .pactsAllowed(entity.getPactsAllowed())
                .createdAt(entity.getCreatedAt())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .lastModified(entity.getLastModified())
                .players(entity.getPlayers().stream()
                        .map(playerMapper::toModel)
                        .collect(Collectors.toList()))
                .territories(mapTerritories(entity.getTerritories()))
                .deck(entity.getDeck().stream()
                        .map(cardMapper::toModel)
                        .collect(Collectors.toList()))
                .events(entity.getEvents().stream()
                        .map(gameEventMapper::toModel)
                        .collect(Collectors.toList()))
                .chatMessages(entity.getChatMessages().stream()
                        .map(chatMessageMapper::toModel)
                        .collect(Collectors.toList()))
                .build();
    }

    private Map<Long, Territory> mapTerritories(java.util.List<GameTerritoryEntity> territoryEntities) {
        Map<Long, Territory> territories = new HashMap<>();
        for (GameTerritoryEntity entity : territoryEntities) {
            // Get both direct and inverse neighbors
            Set<Long> neighborIds = new HashSet<>();
            
            // Direct neighbors (where this country is the source)
            entity.getCountry().getNeighbors().forEach(neighbor -> neighborIds.add(neighbor.getId()));
            
            // Inverse neighbors (where other countries have this country as neighbor)
            List<CountryEntity> countriesWithThisAsNeighbor = countryRepository
                    .findCountriesThatHaveAsNeighbor(entity.getCountry().getId());
            countriesWithThisAsNeighbor.forEach(c -> neighborIds.add(c.getId()));
            
            Territory territory = Territory.builder()
                    .id(entity.getCountry().getId())
                    .name(entity.getCountry().getName())
                    .continentName(entity.getCountry().getContinent().getName())
                    .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                    .ownerName(entity.getOwner() != null ? getPlayerDisplayName(entity.getOwner()) : null)
                    .armies(entity.getArmies())
                    .neighborIds(neighborIds)
                    .build();
            territories.put(territory.getId(), territory);
        }
        return territories;
    }

    public GameEntity toEntity(Game model) {
        if (model == null) return null;

        GameEntity entity = new GameEntity();
        entity.setId(model.getId());
        entity.setGameCode(model.getGameCode());
        entity.setStatus(model.getState());
        entity.setCurrentPhase(model.getCurrentPhase());
        entity.setCurrentTurn(model.getCurrentTurn());
        entity.setCurrentPlayerIndex(model.getCurrentPlayerIndex());
        entity.setMaxPlayers(model.getMaxPlayers());
        entity.setTurnTimeLimit(model.getTurnTimeLimit());
        entity.setChatEnabled(model.getChatEnabled());
        entity.setPactsAllowed(model.getPactsAllowed());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setStartedAt(model.getStartedAt());
        entity.setFinishedAt(model.getFinishedAt());
        entity.setLastModified(model.getLastModified());


        if (model.getContinents() != null) {
            entity.setContinents(
                    model.getContinents().stream()
                            .map(continentMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }

        return entity;
    }


    private String getPlayerDisplayName(PlayerEntity player) {
        if (player.getUser() != null) {
            return player.getUser().getUsername();
        } else if (player.getBotProfile() != null) {
            return player.getBotProfile().getBotName();
        }
        return "Unknown";
    }

    public GameResponseDto toResponseDto(GameEntity entity) {
        if (entity == null) {
            return null;
        }
        Game model = toModel(entity);
        return toResponseDto(model);
    }

    public GameResponseDto toResponseDto(Game model) {
        if (model == null) {
            return null;
        }

        GameResponseDto.GameResponseDtoBuilder builder = GameResponseDto.builder()
                .id(model.getId())
                .gameCode(model.getGameCode())
                .createdByUsername(model.getCreatedByUsername())
                .state(model.getState())
                .currentPhase(model.getCurrentPhase())
                .currentTurn(model.getCurrentTurn())
                .currentPlayerIndex(model.getCurrentPlayerIndex())
                .maxPlayers(model.getMaxPlayers())
                .turnTimeLimit(model.getTurnTimeLimit())
                .chatEnabled(model.getChatEnabled())
                .pactsAllowed(model.getPactsAllowed())
                .createdAt(model.getCreatedAt())
                .startedAt(model.getStartedAt())
                .finishedAt(model.getFinishedAt())
                .currentPlayerName(model.getCreatedByUsername())
                .canStart(
                        model.getState() == GameState.WAITING_FOR_PLAYERS &&
                                model.getPlayers() != null &&
                                model.getPlayers().stream().anyMatch(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                )
                .isGameOver(model.getState() == GameState.FINISHED);

        if (model.getPlayers() != null && !model.getPlayers().isEmpty()) {
            builder.players(
                    model.getPlayers().stream()
                            .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                            .map(playerMapper::toResponseDto)
                            .collect(Collectors.toList())
            );
        }

        if (model.getTerritories() != null && !model.getTerritories().isEmpty()) {
            Map<Long, CountryResponseDto> mappedTerritories =
                    model.getTerritories().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> territoryMapper.toResponseDto(entry.getValue())
                            ));
            builder.territories(mappedTerritories);
        }

        if (model.getContinents() != null && !model.getContinents().isEmpty()) {
            builder.continents(
                    model.getContinents().stream()
                            .map(c -> continentMapper.toResponseDto(c, model.getTerritories()))
                            .collect(Collectors.toList())
            );
        }

        if (model.getEvents() != null && !model.getEvents().isEmpty()) {
            builder.recentEvents(
                    model.getEvents().stream()
                            .map(gameEventMapper::toDto)
                            .collect(Collectors.toList())
            );
        }

        if (model.getChatMessages() != null && !model.getChatMessages().isEmpty()) {
            builder.recentMessages(
                    model.getChatMessages().stream()
                            .map(chatMessageMapper::toResponseDto)
                            .collect(Collectors.toList())
            );
        }

        return builder.build();
    }
}

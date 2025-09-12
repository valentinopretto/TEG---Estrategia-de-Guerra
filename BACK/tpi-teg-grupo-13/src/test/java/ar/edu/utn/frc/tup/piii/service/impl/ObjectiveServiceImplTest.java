package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.ObjectiveMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.ObjectiveRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObjectiveServiceImplTest {

    @Mock
    private ObjectiveRepository objectiveRepository;

    @Mock
    private ObjectiveMapper objectiveMapper;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private GameTerritoryService gameTerritoryService;

    @InjectMocks
    private ObjectiveServiceImpl objectiveService;

    private Objective objective;
    private ObjectiveEntity objectiveEntity;
    private Game game;
    private GameEntity gameEntity;
    private Player player;
    private PlayerEntity playerEntity;

    @BeforeEach
    void setUp() {
        objective = Objective.builder()
                .id(1L)
                .type(ObjectiveType.COMMON)
                .description("Test objective")
                .build();

        objectiveEntity = new ObjectiveEntity();
        objectiveEntity.setId(1L);
        objectiveEntity.setType(ObjectiveType.COMMON);
        objectiveEntity.setDescription("Test objective");

        player = Player.builder()
                .id(1L)
                .username("testPlayer")
                .color(PlayerColor.RED)
                .status(PlayerStatus.ACTIVE)
                .build();

        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);

        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .players(Arrays.asList(player))
                .build();

        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameCode("TEST123");
    }

    @Test
    void testSaveObjective() {
        when(objectiveMapper.toEntity(objective)).thenReturn(objectiveEntity);
        when(objectiveRepository.save(objectiveEntity)).thenReturn(objectiveEntity);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        Objective saved = objectiveService.save(objective);

        assertThat(saved).isNotNull();
        assertThat(saved.getType()).isEqualTo(ObjectiveType.COMMON);
        verify(objectiveRepository).save(objectiveEntity);
    }

    @Test
    void testFindByIdExists() {
        when(objectiveRepository.findById(1L)).thenReturn(Optional.of(objectiveEntity));
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        Optional<Objective> result = objectiveService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(ObjectiveType.COMMON);
    }

    @Test
    void testFindByIdNotExists() {
        when(objectiveRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Objective> result = objectiveService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindByType() {
        List<ObjectiveEntity> entityList = Arrays.asList(objectiveEntity);
        when(objectiveRepository.findByType(ObjectiveType.COMMON)).thenReturn(entityList);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.findByType(ObjectiveType.COMMON);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ObjectiveType.COMMON);
        verify(objectiveRepository).findByType(ObjectiveType.COMMON);
    }

    @Test
    void testValidateCommonObjective_Valid() {
        when(playerMapper.toEntity(player)).thenReturn(playerEntity);
        when(gameTerritoryService.countWithMinArmies(playerEntity, 2)).thenReturn(20L);

        boolean result = objectiveService.validateObjectiveCompletion(objective, game, player);

        assertThat(result).isTrue();
        verify(gameTerritoryService).countWithMinArmies(playerEntity, 2);
    }


    @Test
    void testValidateDestructionObjective_Valid() {
        Player eliminatedPlayer = Player.builder()
                .color(PlayerColor.BLUE)
                .status(PlayerStatus.ELIMINATED)
                .build();

        Game gameWithEliminated = Game.builder()
                .players(Arrays.asList(player, eliminatedPlayer))
                .build();

        Objective destructionObjective = Objective.builder()
                .type(ObjectiveType.DESTRUCTION)
                .targetData("BLUE")
                .build();

        boolean result = objectiveService.validateObjectiveCompletion(destructionObjective, gameWithEliminated, player);

        assertThat(result).isTrue();
    }

    @Test
    void testValidateDestructionObjective_Invalid() {
        Player activePlayer = Player.builder()
                .color(PlayerColor.BLUE)
                .status(PlayerStatus.ACTIVE)
                .build();

        Game gameWithActive = Game.builder()
                .players(Arrays.asList(player, activePlayer))
                .build();

        Objective destructionObjective = Objective.builder()
                .type(ObjectiveType.DESTRUCTION)
                .targetData("BLUE")
                .build();

        boolean result = objectiveService.validateObjectiveCompletion(destructionObjective, gameWithActive, player);

        assertThat(result).isFalse();
    }

    @Test
    void testValidateOccupationObjective_Valid() {
        Objective occupationObjective = Objective.builder()
                .type(ObjectiveType.OCCUPATION)
                .targetData("South America,North America")
                .build();

        GameTerritoryEntity t1 = new GameTerritoryEntity();
        t1.setOwner(playerEntity);
        GameTerritoryEntity t2 = new GameTerritoryEntity();
        t2.setOwner(playerEntity);

        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerMapper.toEntity(player)).thenReturn(playerEntity);
        when(gameTerritoryService.getByContinent(gameEntity, "South America")).thenReturn(Arrays.asList(t1));
        when(gameTerritoryService.getByContinent(gameEntity, "North America")).thenReturn(Arrays.asList(t2));

        boolean result = objectiveService.validateObjectiveCompletion(occupationObjective, game, player);

        assertThat(result).isTrue();
    }

    @Test
    void testGetCommonObjectives() {
        List<ObjectiveEntity> entities = Arrays.asList(objectiveEntity);
        when(objectiveRepository.findByType(ObjectiveType.COMMON)).thenReturn(entities);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.getCommonObjectives();

        assertThat(result).hasSize(1);
        verify(objectiveRepository).findByType(ObjectiveType.COMMON);
    }

    @Test
    void testGetOccupationObjectives() {
        List<ObjectiveEntity> entities = Arrays.asList(objectiveEntity);
        when(objectiveRepository.findByType(ObjectiveType.OCCUPATION)).thenReturn(entities);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.getOccupationObjectives();

        assertThat(result).hasSize(1);
        verify(objectiveRepository).findByType(ObjectiveType.OCCUPATION);
    }

    @Test
    void testGetDestructionObjectives() {
        List<ObjectiveEntity> entities = Arrays.asList(objectiveEntity);
        when(objectiveRepository.findByType(ObjectiveType.DESTRUCTION)).thenReturn(entities);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.getDestructionObjectives();

        assertThat(result).hasSize(1);
        verify(objectiveRepository).findByType(ObjectiveType.DESTRUCTION);
    }

    @Test
    void testFindAll() {
        List<ObjectiveEntity> entities = Arrays.asList(objectiveEntity);
        when(objectiveRepository.findAll()).thenReturn(entities);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.findAll();

        assertThat(result).hasSize(1);
        verify(objectiveRepository).findAll();
    }

    @Test
    void testDeleteById() {
        objectiveService.deleteById(1L);

        verify(objectiveRepository).deleteById(1L);
    }

    @Test
    void testCreateObjectivesForGame() {
        List<ObjectiveEntity> entities = Arrays.asList(objectiveEntity);
        when(objectiveRepository.findAll()).thenReturn(entities);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.createObjectivesForGame(game);

        assertThat(result).hasSize(1);
    }

    @Test
    void testIsObjectiveAchieved() {
        when(objectiveRepository.findById(1L)).thenReturn(Optional.of(objectiveEntity));
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);
        when(playerMapper.toEntity(player)).thenReturn(playerEntity);
        when(gameTerritoryService.countWithMinArmies(playerEntity, 2)).thenReturn(20L);

        boolean result = objectiveService.isObjectiveAchieved(1L, game, player);

        assertThat(result).isTrue();
    }

    @Test
    void testIsObjectiveAchieved_ObjectiveNotFound() {
        when(objectiveRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = objectiveService.isObjectiveAchieved(999L, game, player);

        assertThat(result).isFalse();
    }

    @Test
    void testFindWinner() {
        Player playerWithObjective = Player.builder()
                .id(1L)
                .objective(objective)
                .build();

        Game gameWithWinner = Game.builder()
                .players(Arrays.asList(playerWithObjective))
                .build();

        when(playerMapper.toEntity(playerWithObjective)).thenReturn(playerEntity);
        when(gameTerritoryService.countWithMinArmies(playerEntity, 2)).thenReturn(20L);

        Optional<Player> winner = objectiveService.findWinner(gameWithWinner);

        assertThat(winner).isPresent();
        assertThat(winner.get().getId()).isEqualTo(1L);
    }

    @Test
    void testFindWinner_NoWinner() {
        Player playerWithoutObjective = Player.builder()
                .id(1L)
                .objective(null)
                .build();

        Game gameWithoutWinner = Game.builder()
                .players(Arrays.asList(playerWithoutObjective))
                .build();

        Optional<Player> winner = objectiveService.findWinner(gameWithoutWinner);

        assertThat(winner).isEmpty();
    }

    @Test
    void testGetObjectiveProgress() {
        String progress = objectiveService.getObjectiveProgress(1L, game, player);

        assertThat(progress).isEqualTo("Progress tracking not implemented yet");
    }

    @Test
    void testAssignObjectivesToPlayers() {
        List<ObjectiveEntity> entities = Arrays.asList(objectiveEntity);
        when(objectiveRepository.findAll()).thenReturn(entities);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        objectiveService.assignObjectivesToPlayers(game);

        assertThat(game.getPlayers().get(0).getObjective()).isEqualTo(objective);
    }
}
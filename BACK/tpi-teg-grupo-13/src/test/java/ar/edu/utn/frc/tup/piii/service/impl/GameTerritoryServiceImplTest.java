package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameTerritoryServiceImplTest {

    @Spy @InjectMocks
    private GameTerritoryServiceImpl service;

    @Mock private GameTerritoryRepository gameTerritoryRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private GameRepository gameRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private PlayerMapper playerMapper;

    private GameEntity game;
    private PlayerEntity player;
    private CountryEntity country;
    private CountryEntity neighCountry;
    private GameTerritoryEntity territory;

    @BeforeEach
    void setUp() {
        game = new GameEntity();
        game.setId(1L);

        player = new PlayerEntity();
        player.setId(10L);

        country = new CountryEntity();
        country.setId(100L);
        country.setName("India");
        ContinentEntity cont = new ContinentEntity();
        cont.setId(5L);
        cont.setName("Asia");
        country.setContinent(cont);
        country.setNeighbors(new HashSet<>());

        neighCountry = new CountryEntity();
        neighCountry.setId(200L);
        neighCountry.setName("China");
        neighCountry.setContinent(cont);
        neighCountry.setNeighbors(new HashSet<>());

        territory = new GameTerritoryEntity();
        territory.setGame(game);
        territory.setCountry(country);
        territory.setOwner(player);
        territory.setArmies(3);
    }

    @Test
    void getByOwner_shouldReturnList() {
        when(gameTerritoryRepository.findByOwner(player))
                .thenReturn(List.of(territory));
        var out = service.getByOwner(player);
        assertSame(territory, out.get(0));
    }

    @Test
    void getByContinent_shouldReturnList() {
        when(gameTerritoryRepository.findTerritoriesByContinentName(game, "Asia"))
                .thenReturn(List.of(territory));
        var out = service.getByContinent(game, "Asia");
        assertSame(territory, out.get(0));
    }

    @Test
    void countWithMinArmies_filtersCorrectly() {
        var low = new GameTerritoryEntity(); low.setArmies(1);
        var high = new GameTerritoryEntity(); high.setArmies(5);
        when(gameTerritoryRepository.findByOwner(player))
                .thenReturn(List.of(low, high));
        assertEquals(1, service.countWithMinArmies(player, 2));
    }

    @Test
    void assignTerritoryToPlayer_updatesExisting() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.of(territory));

        service.assignTerritoryToPlayer(1L, 100L, 10L, 7);

        verify(gameTerritoryRepository).save(territory);
        assertEquals(7, territory.getArmies());
    }

    @Test
    void assignTerritoryToPlayer_createsNew_whenMissing() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.empty());

        service.assignTerritoryToPlayer(1L, 100L, 10L, 2);

        ArgumentCaptor<GameTerritoryEntity> cap =
                ArgumentCaptor.forClass(GameTerritoryEntity.class);
        verify(gameTerritoryRepository).save(cap.capture());
        var saved = cap.getValue();
        assertEquals(2, saved.getArmies());
        assertEquals(player, saved.getOwner());
    }

    @Test
    void assignTerritoryToPlayer_throwsGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.assignTerritoryToPlayer(1L,100L,10L,1));
    }

    @Test
    void assignTerritoryToPlayer_throwsCountryNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.assignTerritoryToPlayer(1L,100L,10L,1));
    }

    @Test
    void assignTerritoryToPlayer_throwsPlayerNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(playerRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(PlayerNotFoundException.class,
                () -> service.assignTerritoryToPlayer(1L,100L,10L,1));
    }

    @Test
    void getTerritoryByGameAndCountry_mapsCorrectly() {
        country.getNeighbors().add(neighCountry);
        territory.setArmies(4);
        territory.setOwner(player);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.of(territory));
        Player mapped = new Player();
        mapped.setDisplayName("Bob");
        when(playerMapper.toModel(player)).thenReturn(mapped);

        Territory t = service.getTerritoryByGameAndCountry(1L,100L);
        assertEquals("Bob", t.getOwnerName());
        assertTrue(t.getNeighborIds().contains(neighCountry.getId()));
    }

    @Test
    void getTerritoryByGameAndCountry_returnsNullIfMissing() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.empty());
        assertNull(service.getTerritoryByGameAndCountry(1L,100L));
    }

    @Test
    void getTerritoryByGameAndCountry_throwsGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.getTerritoryByGameAndCountry(1L,100L));
    }

    @Test
    void getTerritoryByGameAndCountry_throwsCountryNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.getTerritoryByGameAndCountry(1L,100L));
    }

    @Test
    void addArmiesToTerritory_happyPath() {
        territory.setArmies(2);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.of(territory));
        service.addArmiesToTerritory(1L,100L,3);
        verify(gameTerritoryRepository).save(territory);
        assertEquals(5, territory.getArmies());
    }

    @Test
    void addArmiesToTerritory_throwsGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.addArmiesToTerritory(1L,100L,1));
    }

    @Test
    void addArmiesToTerritory_throwsCountryNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.addArmiesToTerritory(1L,100L,1));
    }

    @Test
    void addArmiesToTerritory_throwsTerritoryMissing() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.addArmiesToTerritory(1L,100L,1));
    }

    @Test
    void getTerritoriesByOwner_happyAndExceptions() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(gameTerritoryRepository.findByGameAndOwner(game, player))
                .thenReturn(List.of(territory));
        when(playerMapper.toModel(player)).thenReturn(new Player());
        assertEquals(1, service.getTerritoriesByOwner(1L,10L).size());

        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.getTerritoriesByOwner(1L,10L));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(PlayerNotFoundException.class,
                () -> service.getTerritoriesByOwner(1L,10L));
    }

    @Test
    void getAllTerritoriesInGame_happyAndException() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameTerritoryRepository.findByGame(game))
                .thenReturn(List.of(territory));
        when(playerMapper.toModel(player)).thenReturn(new Player());
        assertEquals(1, service.getAllTerritoriesInGame(1L).size());

        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.getAllTerritoriesInGame(1L));
    }


    @Test
    void getTotalArmiesByPlayer_various() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(gameTerritoryRepository.getTotalArmiesByPlayer(player)).thenReturn(20);
        assertEquals(20, service.getTotalArmiesByPlayer(1L,10L));

        when(gameTerritoryRepository.getTotalArmiesByPlayer(player)).thenReturn(null);
        assertEquals(0, service.getTotalArmiesByPlayer(1L,10L));

        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.getTotalArmiesByPlayer(1L,10L));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(PlayerNotFoundException.class,
                () -> service.getTotalArmiesByPlayer(1L,10L));
    }

    @Test
    void getTerritoriesCanAttack_various() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(gameTerritoryRepository.findPlayerTerritoriesCanAttack(player))
                .thenReturn(List.of(territory));
        when(playerMapper.toModel(player)).thenReturn(new Player());
        assertEquals(1, service.getTerritoriesCanAttack(1L,10L).size());

        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.getTerritoriesCanAttack(1L,10L));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(PlayerNotFoundException.class,
                () -> service.getTerritoriesCanAttack(1L,10L));
    }

    @Test
    void transferTerritoryOwnership_invokesStaticAndSaves() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.of(territory));

        try (MockedStatic<GameStateServiceImpl> ms =
                     mockStatic(GameStateServiceImpl.class)) {
            service.transferTerritoryOwnership(1L,100L,10L,9);
            ms.verify(() -> GameStateServiceImpl.registerConquest(1L,10L));
        }
        verify(gameTerritoryRepository).save(territory);

        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.transferTerritoryOwnership(1L,100L,10L,1));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.transferTerritoryOwnership(1L,100L,10L,1));

        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(playerRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(PlayerNotFoundException.class,
                () -> service.transferTerritoryOwnership(1L,100L,10L,1));

        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(gameTerritoryRepository.findByGameAndCountry(game, country))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.transferTerritoryOwnership(1L,100L,10L,1));
    }

    @Test
    void areTerritoriesNeighbors_passThroughRepo() {
        when(countryRepository.areCountriesNeighbors(1L,2L)).thenReturn(true);
        assertTrue(service.areTerritoriesNeighbors(1L,2L));
        when(countryRepository.areCountriesNeighbors(1L,2L)).thenReturn(false);
        assertFalse(service.areTerritoriesNeighbors(1L,2L));
    }

    @Test
    void getNeighborTerritories_emptyIfCountryMissing() {
        when(countryRepository.findById(100L)).thenReturn(Optional.empty());
        assertTrue(service.getNeighborTerritories(1L,100L).isEmpty());
    }

    @Test
    void getNeighborTerritories_directAndInverse() {
        country.getNeighbors().add(neighCountry);
        when(countryRepository.findById(100L)).thenReturn(Optional.of(country));
        when(countryRepository.findCountriesThatHaveAsNeighbor(100L))
                .thenReturn(List.of(neighCountry));

        doReturn(Territory.builder().id(200L).build())
                .when(service).getTerritoryByGameAndCountry(1L,200L);

        var list = service.getNeighborTerritories(1L,100L);
        assertEquals(1, list.size());
        assertEquals(200L, list.get(0).getId());
    }

    @Test
    void getTerritoryByGameAndCountryName_various() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findByName("India")).thenReturn(Optional.of(country));
        when(gameTerritoryRepository.findByGameAndCountry(game,country))
                .thenReturn(Optional.of(territory));
        when(playerMapper.toModel(player)).thenReturn(new Player());
        assertNotNull(service.getTerritoryByGameAndCountryName(1L,"India"));

        when(gameTerritoryRepository.findByGameAndCountry(game,country))
                .thenReturn(Optional.empty());
        assertNull(service.getTerritoryByGameAndCountryName(1L,"India"));

        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(GameNotFoundException.class,
                () -> service.getTerritoryByGameAndCountryName(1L,"India"));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(countryRepository.findByName("India")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.getTerritoryByGameAndCountryName(1L,"India"));
    }

    @Test
    void getAllAvailableTerritories_buildsDefaultTerritory() {
        country.getNeighbors().add(neighCountry);
        when(countryRepository.findAll()).thenReturn(List.of(country));
        when(countryRepository.findCountriesThatHaveAsNeighbor(100L))
                .thenReturn(List.of());

        var list = service.getAllAvailableTerritories();
        assertEquals(1, list.size());
        var t = list.get(0);
        assertEquals(100L, t.getId());
        assertEquals("India", t.getName());
        assertEquals("Asia", t.getContinentName());
        assertEquals(1, t.getArmies());
        assertTrue(t.getNeighborIds().contains(200L));
    }
}
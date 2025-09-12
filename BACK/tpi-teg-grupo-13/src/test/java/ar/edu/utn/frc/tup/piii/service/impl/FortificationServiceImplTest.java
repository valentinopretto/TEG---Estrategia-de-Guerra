package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FortificationServiceImplTest {
    @Mock
    private GameService gameService;
    @Mock
    private GameTerritoryService gameTerritoryService;
    @InjectMocks
    private FortificationServiceImpl fortificationService;

    private Game game;
    private Territory t1, t2, t3;
    private FortifyDto fortifyDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        game = Game.builder().id(1L).gameCode("CODE").state(GameState.NORMAL_PLAY).build();
        t1 = Territory.builder().id(1L).ownerId(10L).armies(5).build();
        t2 = Territory.builder().id(2L).ownerId(10L).armies(2).build();
        t3 = Territory.builder().id(3L).ownerId(20L).armies(3).build();
        fortifyDto = new FortifyDto(10L, 1L, 2L, 2);
    }

    @Test
    void testPerformFortification_Success() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        when(gameTerritoryService.getTerritoriesByOwner(eq(1L), eq(10L))).thenReturn(Arrays.asList(t1, t2));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(1L))).thenReturn(Arrays.asList(t2));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(2L))).thenReturn(Arrays.asList(t1));
        doNothing().when(gameTerritoryService).addArmiesToTerritory(anyLong(), anyLong(), anyInt());
        
        boolean result = fortificationService.performFortification("CODE", fortifyDto);
        assertTrue(result);
        verify(gameTerritoryService, times(2)).addArmiesToTerritory(anyLong(), anyLong(), anyInt());
    }

    @Test
    void testPerformFortification_Invalid() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        when(gameTerritoryService.areTerritoriesNeighbors(eq(1L), eq(2L))).thenReturn(false);
        
        boolean result = fortificationService.performFortification("CODE", fortifyDto);
        assertFalse(result);
    }

    @Test
    void testPerformFortification_Exception() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        when(gameTerritoryService.areTerritoriesNeighbors(eq(1L), eq(2L))).thenReturn(true);
        doThrow(new RuntimeException("error")).when(gameTerritoryService).addArmiesToTerritory(anyLong(), anyLong(), anyInt());
        
        boolean result = fortificationService.performFortification("CODE", fortifyDto);
        assertFalse(result);
    }

    @Test
    void testIsValidFortification_Success() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        when(gameTerritoryService.getTerritoriesByOwner(eq(1L), eq(10L))).thenReturn(Arrays.asList(t1, t2));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(1L))).thenReturn(Arrays.asList(t2));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(2L))).thenReturn(Arrays.asList(t1));
        
        boolean valid = fortificationService.isValidFortification("CODE", fortifyDto);
        assertTrue(valid);
    }

    @Test
    void testIsValidFortification_InvalidGameState() {
        Game g = Game.builder().id(1L).gameCode("CODE").state(GameState.REINFORCEMENT_5).build();
        when(gameService.findByGameCode(anyString())).thenReturn(g);
        
        boolean valid = fortificationService.isValidFortification("CODE", fortifyDto);
        assertFalse(valid);
    }

    @Test
    void testIsValidFortification_NotOwner() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        Territory notOwner = Territory.builder().id(1L).ownerId(99L).armies(5).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(notOwner);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        
        boolean valid = fortificationService.isValidFortification("CODE", fortifyDto);
        assertFalse(valid);
    }

    @Test
    void testIsValidFortification_SameTerritory() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        FortifyDto dto = new FortifyDto(10L, 1L, 1L, 2);
        
        boolean valid = fortificationService.isValidFortification("CODE", dto);
        assertFalse(valid);
    }

    @Test
    void testIsValidFortification_TooFewArmies() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        FortifyDto dto = new FortifyDto(10L, 1L, 2L, 0);
        
        boolean valid = fortificationService.isValidFortification("CODE", dto);
        assertFalse(valid);
    }

    @Test
    void testIsValidFortification_LeaveOriginEmpty() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        Territory oneArmy = Territory.builder().id(1L).ownerId(10L).armies(2).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(oneArmy);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        FortifyDto dto = new FortifyDto(10L, 1L, 2L, 2);
        
        boolean valid = fortificationService.isValidFortification("CODE", dto);
        assertFalse(valid);
    }

    @Test
    void testIsValidFortification_NotConnected() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(t2);
        when(gameTerritoryService.areTerritoriesNeighbors(eq(1L), eq(2L))).thenReturn(false);
        
        boolean valid = fortificationService.isValidFortification("CODE", fortifyDto);
        assertFalse(valid);
    }

    @Test
    void testGetFortifiableTerritoriesForPlayer() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoriesByOwner(eq(1L), eq(10L))).thenReturn(Arrays.asList(t1, t2));
        
        List<Territory> result = fortificationService.getFortifiableTerritoriesForPlayer("CODE", 10L);
        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
    }

    @Test
    void testGetFortificationTargetsForTerritory_NormalPlay() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        when(gameTerritoryService.getTerritoriesByOwner(eq(1L), eq(10L))).thenReturn(Arrays.asList(t1, t2));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(1L))).thenReturn(Arrays.asList(t2));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(2L))).thenReturn(Arrays.asList(t1));
        
        List<Territory> result = fortificationService.getFortificationTargetsForTerritory("CODE", 1L, 10L);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void testGetFortificationTargetsForTerritory_NotOwner() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        Territory notOwner = Territory.builder().id(1L).ownerId(99L).armies(5).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(notOwner);
        
        assertThrows(IllegalArgumentException.class, () ->
                fortificationService.getFortificationTargetsForTerritory("CODE", 1L, 10L));
    }

    @Test
    void testGetFortificationTargetsForTerritory_OneArmy() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        Territory oneArmy = Territory.builder().id(1L).ownerId(10L).armies(1).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(oneArmy);
        
        List<Territory> result = fortificationService.getFortificationTargetsForTerritory("CODE", 1L, 10L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAreTerritoriesConnectedByPlayer_SameTerritory() {
        boolean result = fortificationService.areTerritoriesConnectedByPlayer("CODE", 1L, 1L, 10L);
        assertTrue(result);
    }

    @Test
    void testGetMaxMovableArmies() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(t1);
        
        int max = fortificationService.getMaxMovableArmies("CODE", 1L);
        assertEquals(4, max);
    }

    @Test
    void testGetMaxMovableArmies_TerritoryNull() {
        when(gameService.findByGameCode(anyString())).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(null);
        
        int max = fortificationService.getMaxMovableArmies("CODE", 1L);
        assertEquals(0, max);
    }

    @Test
    void testAreTerritoriesConnectedByPlayer_AreAdjacent_HostilityOnly() {
        Game hostilityGame = Game.builder().id(1L).gameCode("CODE").state(GameState.HOSTILITY_ONLY).build();
        when(gameService.findByGameCode(anyString())).thenReturn(hostilityGame);
        // Ambos territorios pertenecen al jugador y son vecinos
        Territory from = Territory.builder().id(1L).ownerId(10L).armies(3).build();
        Territory to = Territory.builder().id(2L).ownerId(10L).armies(2).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(from);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(to);
        when(gameTerritoryService.areTerritoriesNeighbors(eq(1L), eq(2L))).thenReturn(true);
        boolean result = fortificationService.areTerritoriesConnectedByPlayer("CODE", 1L, 2L, 10L);
        assertTrue(result);
    }

    @Test
    void testAreTerritoriesConnectedByPlayer_NotAdjacent_HostilityOnly() {
        Game hostilityGame = Game.builder().id(1L).gameCode("CODE").state(GameState.HOSTILITY_ONLY).build();
        when(gameService.findByGameCode(anyString())).thenReturn(hostilityGame);
        // Ambos territorios pertenecen al jugador pero NO son vecinos
        Territory from = Territory.builder().id(1L).ownerId(10L).armies(3).build();
        Territory to = Territory.builder().id(2L).ownerId(10L).armies(2).build();
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(from);
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(2L))).thenReturn(to);
        when(gameTerritoryService.areTerritoriesNeighbors(eq(1L), eq(2L))).thenReturn(false);
        boolean result = fortificationService.areTerritoriesConnectedByPlayer("CODE", 1L, 2L, 10L);
        assertFalse(result);
    }

    @Test
    void testGetFortificationTargetsForTerritory_AdjacentTargets_HostilityOnly() {
        Game hostilityGame = Game.builder().id(1L).gameCode("CODE").state(GameState.HOSTILITY_ONLY).build();
        when(gameService.findByGameCode(anyString())).thenReturn(hostilityGame);
        Territory from = Territory.builder().id(1L).ownerId(10L).armies(3).build();
        Territory to1 = Territory.builder().id(2L).ownerId(10L).armies(2).build();
        Territory to2 = Territory.builder().id(3L).ownerId(20L).armies(2).build(); // No es del jugador
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(from);
        when(gameTerritoryService.getTerritoriesByOwner(eq(1L), eq(10L))).thenReturn(Arrays.asList(from, to1));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(1L))).thenReturn(Arrays.asList(to1, to2));
        List<Territory> result = fortificationService.getFortificationTargetsForTerritory("CODE", 1L, 10L);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void testGetFortificationTargetsForTerritory_AdjacentTargets_HostilityOnly_None() {
        Game hostilityGame = Game.builder().id(1L).gameCode("CODE").state(GameState.HOSTILITY_ONLY).build();
        when(gameService.findByGameCode(anyString())).thenReturn(hostilityGame);
        Territory from = Territory.builder().id(1L).ownerId(10L).armies(3).build();
        Territory to1 = Territory.builder().id(2L).ownerId(20L).armies(2).build(); // No es del jugador
        when(gameTerritoryService.getTerritoryByGameAndCountry(eq(1L), eq(1L))).thenReturn(from);
        when(gameTerritoryService.getTerritoriesByOwner(eq(1L), eq(10L))).thenReturn(Arrays.asList(from));
        when(gameTerritoryService.getNeighborTerritories(eq(1L), eq(1L))).thenReturn(Arrays.asList(to1));
        List<Territory> result = fortificationService.getFortificationTargetsForTerritory("CODE", 1L, 10L);
        assertTrue(result.isEmpty());
    }
}

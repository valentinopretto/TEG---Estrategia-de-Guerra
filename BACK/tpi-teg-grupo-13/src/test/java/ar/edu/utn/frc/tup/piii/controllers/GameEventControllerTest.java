package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.entities.GameEventEntity;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import ar.edu.utn.frc.tup.piii.service.interfaces.IGameEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameEventController.class)
public class GameEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IGameEventService gameEventService;

    private ObjectMapper objectMapper;
    private GameEventDto sampleEventDto;
    private GameEventEntity sampleEventEntity;
    private List<GameEventDto> sampleEventDtoList;
    private List<GameEventEntity> sampleEventEntityList;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();

        // Setup sample GameEventDto
        sampleEventDto = GameEventDto.builder()
                .id(1L)
                .turnNumber(1)
                .actorName("TestPlayer")
                .type(EventType.ATTACK_PERFORMED)
                .description("TestPlayer atacó desde Argentina a Brasil")
                .data("{\"from\":\"Argentina\", \"to\":\"Brasil\", \"successful\":true}")
                .timestamp(LocalDateTime.of(2025, 6, 30, 12, 0))
                .build();

        // Setup sample GameEventEntity
        sampleEventEntity = new GameEventEntity();
        sampleEventEntity.setId(1L);
        sampleEventEntity.setTurnNumber(1);
        sampleEventEntity.setType(EventType.ATTACK_PERFORMED);
        sampleEventEntity.setData("{\"from\":\"Argentina\", \"to\":\"Brasil\", \"successful\":true}");
        sampleEventEntity.setTimestamp(LocalDateTime.of(2025, 6, 30, 12, 0));

        // Setup lists
        sampleEventDtoList = Arrays.asList(sampleEventDto);
        sampleEventEntityList = Arrays.asList(sampleEventEntity);
    }

    // ===== GET ENDPOINTS TESTS =====

    @Test
    public void getGameHistory_Success() throws Exception {
        when(gameEventService.getFormattedGameHistory(1L)).thenReturn(sampleEventDtoList);

        mockMvc.perform(get("/api/games/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].actorName").value("TestPlayer"))
                .andExpect(jsonPath("$[0].type").value("ATTACK_PERFORMED"))
                .andExpect(jsonPath("$[0].description").value("TestPlayer atacó desde Argentina a Brasil"));
    }

    @Test
    public void getGameHistory_Exception() throws Exception {
        when(gameEventService.getFormattedGameHistory(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/games/1/events"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getPlayerEvents_Success() throws Exception {
        when(gameEventService.getPlayerEventsInGame(1L, 2L)).thenReturn(sampleEventEntityList);

        mockMvc.perform(get("/api/games/1/events/player/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].turnNumber").value(1))
                .andExpect(jsonPath("$[0].type").value("ATTACK_PERFORMED"));
    }

    @Test
    public void getPlayerEvents_Exception() throws Exception {
        when(gameEventService.getPlayerEventsInGame(1L, 2L))
                .thenThrow(new RuntimeException("Player not found"));

        mockMvc.perform(get("/api/games/1/events/player/2"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getEventsByType_Success() throws Exception {
        when(gameEventService.getEventsByType(1L, EventType.ATTACK_PERFORMED))
                .thenReturn(sampleEventEntityList);

        mockMvc.perform(get("/api/games/1/events/type/ATTACK_PERFORMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("ATTACK_PERFORMED"));
    }

    @Test
    public void getEventsByType_Exception() throws Exception {
        when(gameEventService.getEventsByType(1L, EventType.ATTACK_PERFORMED))
                .thenThrow(new RuntimeException("Invalid event type"));

        mockMvc.perform(get("/api/games/1/events/type/ATTACK_PERFORMED"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getEventsByTurn_Success() throws Exception {
        when(gameEventService.getEventsByTurn(1L, 5)).thenReturn(sampleEventEntityList);

        mockMvc.perform(get("/api/games/1/events/turn/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].turnNumber").value(1));
    }

    @Test
    public void getEventsByTurn_Exception() throws Exception {
        when(gameEventService.getEventsByTurn(1L, 5))
                .thenThrow(new RuntimeException("Turn not found"));

        mockMvc.perform(get("/api/games/1/events/turn/5"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getRecentEvents_Success() throws Exception {
        when(gameEventService.getRecentGameEvents(1L, 24)).thenReturn(sampleEventEntityList);

        mockMvc.perform(get("/api/games/1/events/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void getRecentEvents_WithCustomHours_Success() throws Exception {
        when(gameEventService.getRecentGameEvents(1L, 48)).thenReturn(sampleEventEntityList);

        mockMvc.perform(get("/api/games/1/events/recent?hours=48"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void getRecentEvents_Exception() throws Exception {
        when(gameEventService.getRecentGameEvents(1L, 24))
                .thenThrow(new RuntimeException("Game not found"));

        mockMvc.perform(get("/api/games/1/events/recent"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getGameEventStats_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttacks", 15L);
        stats.put("totalConquests", 8L);
        stats.put("conquestsByPlayer", Map.of("Player1", 5L, "Player2", 3L));

        when(gameEventService.getGameEventStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/games/1/events/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttacks").value(15))
                .andExpect(jsonPath("$.totalConquests").value(8))
                .andExpect(jsonPath("$.conquestsByPlayer.Player1").value(5))
                .andExpect(jsonPath("$.conquestsByPlayer.Player2").value(3));
    }

    @Test
    public void getGameEventStats_Exception() throws Exception {
        when(gameEventService.getGameEventStats(1L))
                .thenThrow(new RuntimeException("Stats calculation error"));

        mockMvc.perform(get("/api/games/1/events/stats"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getLastEventByType_Success() throws Exception {
        when(gameEventService.getLastEventByType(1L, EventType.TURN_STARTED))
                .thenReturn(Optional.of(sampleEventEntity));

        mockMvc.perform(get("/api/games/1/events/last/TURN_STARTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("ATTACK_PERFORMED"));
    }

    @Test
    public void getLastEventByType_NotFound() throws Exception {
        when(gameEventService.getLastEventByType(1L, EventType.TURN_STARTED))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/games/1/events/last/TURN_STARTED"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getLastEventByType_Exception() throws Exception {
        when(gameEventService.getLastEventByType(1L, EventType.TURN_STARTED))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/games/1/events/last/TURN_STARTED"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void countEventsByType_Success() throws Exception {
        when(gameEventService.countEventsByType(1L, EventType.TERRITORY_CONQUERED))
                .thenReturn(12L);

        mockMvc.perform(get("/api/games/1/events/count/TERRITORY_CONQUERED"))
                .andExpect(status().isOk())
                .andExpect(content().string("12"));
    }

    @Test
    public void countEventsByType_Exception() throws Exception {
        when(gameEventService.countEventsByType(1L, EventType.TERRITORY_CONQUERED))
                .thenThrow(new RuntimeException("Count error"));

        mockMvc.perform(get("/api/games/1/events/count/TERRITORY_CONQUERED"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getPlayerAttacks_Success() throws Exception {
        when(gameEventService.getPlayerAttacks(1L, 2L)).thenReturn(sampleEventEntityList);

        mockMvc.perform(get("/api/games/1/events/player/2/attacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("ATTACK_PERFORMED"));
    }

    @Test
    public void getPlayerAttacks_Exception() throws Exception {
        when(gameEventService.getPlayerAttacks(1L, 2L))
                .thenThrow(new RuntimeException("Player attacks error"));

        mockMvc.perform(get("/api/games/1/events/player/2/attacks"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getPlayerConquests_Success() throws Exception {
        GameEventEntity conquestEvent = new GameEventEntity();
        conquestEvent.setId(2L);
        conquestEvent.setType(EventType.TERRITORY_CONQUERED);
        conquestEvent.setTurnNumber(3);

        when(gameEventService.getPlayerConquests(1L, 2L))
                .thenReturn(Arrays.asList(conquestEvent));

        mockMvc.perform(get("/api/games/1/events/player/2/conquests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].type").value("TERRITORY_CONQUERED"));
    }

    @Test
    public void getPlayerConquests_Exception() throws Exception {
        when(gameEventService.getPlayerConquests(1L, 2L))
                .thenThrow(new RuntimeException("Player conquests error"));

        mockMvc.perform(get("/api/games/1/events/player/2/conquests"))
                .andExpect(status().isInternalServerError());
    }

    // ===== POST ENDPOINTS TESTS =====

    @Test
    public void recordTerritoryConquest_Success() throws Exception {
        GameEventController.TerritoryConquestRequest request = new GameEventController.TerritoryConquestRequest();
        request.setConquererPlayerId(2L);
        request.setConqueredTerritory("Brasil");
        request.setFromPlayer("Player1");
        request.setTurnNumber(5);

        when(gameEventService.recordTerritoryConquest(1L, 2L, "Brasil", "Player1", 5))
                .thenReturn(sampleEventEntity);

        mockMvc.perform(post("/api/games/1/events/territory-conquest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("ATTACK_PERFORMED"));
    }

    @Test
    public void recordTerritoryConquest_Exception() throws Exception {
        GameEventController.TerritoryConquestRequest request = new GameEventController.TerritoryConquestRequest();
        request.setConquererPlayerId(2L);
        request.setConqueredTerritory("Brasil");
        request.setFromPlayer("Player1");
        request.setTurnNumber(5);

        when(gameEventService.recordTerritoryConquest(1L, 2L, "Brasil", "Player1", 5))
                .thenThrow(new RuntimeException("Recording error"));

        mockMvc.perform(post("/api/games/1/events/territory-conquest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void recordAttack_Success() throws Exception {
        GameEventController.AttackRequest request = new GameEventController.AttackRequest();
        request.setAttackerPlayerId(2L);
        request.setFromTerritory("Argentina");
        request.setToTerritory("Brasil");
        request.setTurnNumber(3);
        request.setSuccessful(true);

        when(gameEventService.recordAttack(1L, 2L, "Argentina", "Brasil", 3, true))
                .thenReturn(sampleEventEntity);

        mockMvc.perform(post("/api/games/1/events/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void recordAttack_Exception() throws Exception {
        GameEventController.AttackRequest request = new GameEventController.AttackRequest();
        request.setAttackerPlayerId(2L);
        request.setFromTerritory("Argentina");
        request.setToTerritory("Brasil");
        request.setTurnNumber(3);
        request.setSuccessful(false);

        when(gameEventService.recordAttack(1L, 2L, "Argentina", "Brasil", 3, false))
                .thenThrow(new RuntimeException("Attack recording error"));

        mockMvc.perform(post("/api/games/1/events/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void recordTurnStart_Success() throws Exception {
        GameEventController.TurnEventRequest request = new GameEventController.TurnEventRequest();
        request.setPlayerId(2L);
        request.setTurnNumber(4);

        when(gameEventService.recordTurnStart(1L, 2L, 4)).thenReturn(sampleEventEntity);

        mockMvc.perform(post("/api/games/1/events/turn-start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void recordTurnStart_Exception() throws Exception {
        GameEventController.TurnEventRequest request = new GameEventController.TurnEventRequest();
        request.setPlayerId(2L);
        request.setTurnNumber(4);

        when(gameEventService.recordTurnStart(1L, 2L, 4))
                .thenThrow(new RuntimeException("Turn start error"));

        mockMvc.perform(post("/api/games/1/events/turn-start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void recordTurnEnd_Success() throws Exception {
        GameEventController.TurnEventRequest request = new GameEventController.TurnEventRequest();
        request.setPlayerId(2L);
        request.setTurnNumber(4);

        when(gameEventService.recordTurnEnd(1L, 2L, 4)).thenReturn(sampleEventEntity);

        mockMvc.perform(post("/api/games/1/events/turn-end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void recordTurnEnd_Exception() throws Exception {
        GameEventController.TurnEventRequest request = new GameEventController.TurnEventRequest();
        request.setPlayerId(2L);
        request.setTurnNumber(4);

        when(gameEventService.recordTurnEnd(1L, 2L, 4))
                .thenThrow(new RuntimeException("Turn end error"));

        mockMvc.perform(post("/api/games/1/events/turn-end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void recordReinforcementsPlaced_Success() throws Exception {
        GameEventController.ReinforcementsRequest request = new GameEventController.ReinforcementsRequest();
        request.setPlayerId(2L);
        request.setTerritory("Argentina");
        request.setReinforcements(5);
        request.setTurnNumber(3);

        when(gameEventService.recordReinforcementsPlaced(1L, 2L, "Argentina", 5, 3))
                .thenReturn(sampleEventEntity);

        mockMvc.perform(post("/api/games/1/events/reinforcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void recordReinforcementsPlaced_Exception() throws Exception {
        GameEventController.ReinforcementsRequest request = new GameEventController.ReinforcementsRequest();
        request.setPlayerId(2L);
        request.setTerritory("Argentina");
        request.setReinforcements(5);
        request.setTurnNumber(3);

        when(gameEventService.recordReinforcementsPlaced(1L, 2L, "Argentina", 5, 3))
                .thenThrow(new RuntimeException("Reinforcements error"));

        mockMvc.perform(post("/api/games/1/events/reinforcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void recordFortification_Success() throws Exception {
        GameEventController.FortificationRequest request = new GameEventController.FortificationRequest();
        request.setPlayerId(2L);
        request.setFromTerritory("Argentina");
        request.setToTerritory("Brasil");
        request.setArmies(3);
        request.setTurnNumber(5);

        when(gameEventService.recordFortification(1L, 2L, "Argentina", "Brasil", 3, 5))
                .thenReturn(sampleEventEntity);

        mockMvc.perform(post("/api/games/1/events/fortification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void recordFortification_Exception() throws Exception {
        GameEventController.FortificationRequest request = new GameEventController.FortificationRequest();
        request.setPlayerId(2L);
        request.setFromTerritory("Argentina");
        request.setToTerritory("Brasil");
        request.setArmies(3);
        request.setTurnNumber(5);

        when(gameEventService.recordFortification(1L, 2L, "Argentina", "Brasil", 3, 5))
                .thenThrow(new RuntimeException("Fortification error"));

        mockMvc.perform(post("/api/games/1/events/fortification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ===== REQUEST DTO TESTS =====

    @Test
    public void territoryConquestRequest_GettersAndSetters() {
        GameEventController.TerritoryConquestRequest request = new GameEventController.TerritoryConquestRequest();

        request.setConquererPlayerId(1L);
        request.setConqueredTerritory("Argentina");
        request.setFromPlayer("TestPlayer");
        request.setTurnNumber(5);

        assert request.getConquererPlayerId().equals(1L);
        assert request.getConqueredTerritory().equals("Argentina");
        assert request.getFromPlayer().equals("TestPlayer");
        assert request.getTurnNumber().equals(5);
    }

    @Test
    public void attackRequest_GettersAndSetters() {
        GameEventController.AttackRequest request = new GameEventController.AttackRequest();

        request.setAttackerPlayerId(1L);
        request.setFromTerritory("Argentina");
        request.setToTerritory("Brasil");
        request.setTurnNumber(3);
        request.setSuccessful(true);

        assert request.getAttackerPlayerId().equals(1L);
        assert request.getFromTerritory().equals("Argentina");
        assert request.getToTerritory().equals("Brasil");
        assert request.getTurnNumber().equals(3);
        assert request.isSuccessful();
    }

    @Test
    public void turnEventRequest_GettersAndSetters() {
        GameEventController.TurnEventRequest request = new GameEventController.TurnEventRequest();

        request.setPlayerId(1L);
        request.setTurnNumber(4);

        assert request.getPlayerId().equals(1L);
        assert request.getTurnNumber().equals(4);
    }

    @Test
    public void reinforcementsRequest_GettersAndSetters() {
        GameEventController.ReinforcementsRequest request = new GameEventController.ReinforcementsRequest();

        request.setPlayerId(1L);
        request.setTerritory("Argentina");
        request.setReinforcements(5);
        request.setTurnNumber(3);

        assert request.getPlayerId().equals(1L);
        assert request.getTerritory().equals("Argentina");
        assert request.getReinforcements().equals(5);
        assert request.getTurnNumber().equals(3);
    }

    @Test
    public void fortificationRequest_GettersAndSetters() {
        GameEventController.FortificationRequest request = new GameEventController.FortificationRequest();

        request.setPlayerId(1L);
        request.setFromTerritory("Argentina");
        request.setToTerritory("Brasil");
        request.setArmies(3);
        request.setTurnNumber(5);

        assert request.getPlayerId().equals(1L);
        assert request.getFromTerritory().equals("Argentina");
        assert request.getToTerritory().equals("Brasil");
        assert request.getArmies().equals(3);
        assert request.getTurnNumber().equals(5);
    }
}
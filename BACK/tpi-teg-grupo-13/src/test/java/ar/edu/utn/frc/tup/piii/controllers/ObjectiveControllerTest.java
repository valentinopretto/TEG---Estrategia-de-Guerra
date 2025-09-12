package ar.edu.utn.frc.tup.piii.controllers;


import ar.edu.utn.frc.tup.piii.dtos.objective.WinnerDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Objective;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ObjectiveControllerTest {

    @InjectMocks
    private ObjectiveController controller;

    @Mock
    private ObjectiveService objectiveService;

    @Mock
    private GameService gameService;

    @Mock
    private PlayerService playerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Objective objective;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objective = Objective.builder()
                .id(1L)
                .type(ObjectiveType.COMMON)
                .description("Foo")
                .isCommon(true)
                .isAchieved(false)
                .build();

        player = Player.builder()
                .id(10L)
                .displayName("Jugador1")
                .objective(objective)
                .build();

        game = Game.builder()
                .id(100L)
                .players(List.of(player))
                .build();
    }


    @Test
    void getAllObjectives_success_andServerError() throws Exception {
        // success
        when(objectiveService.findAll()).thenReturn(List.of(objective));

        mockMvc.perform(get("/api/objectives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));


        reset(objectiveService);
        when(objectiveService.findAll()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/objectives"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getObjectiveById_found_notFound_badRequest() throws Exception {

        when(objectiveService.findById(1L)).thenReturn(Optional.of(objective));
        mockMvc.perform(get("/api/objectives/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));


        reset(objectiveService);
        when(objectiveService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/objectives/{id}", 1L))
                .andExpect(status().isNotFound());


        reset(objectiveService);
        when(objectiveService.findById(1L)).thenThrow(new RuntimeException("oops"));
        mockMvc.perform(get("/api/objectives/{id}", 1L))
                .andExpect(status().isBadRequest());
    }


    @Test
    void createObjective_success_andBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(objective);

        when(objectiveService.save(any(Objective.class))).thenReturn(objective);
        mockMvc.perform(post("/api/objectives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));


        reset(objectiveService);
        when(objectiveService.save(any(Objective.class))).thenThrow(new RuntimeException());
        mockMvc.perform(post("/api/objectives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteObjective_invokesService() throws Exception {
        doNothing().when(objectiveService).deleteById(5L);
        mockMvc.perform(delete("/api/objectives/{id}", 5L))
                .andExpect(status().isOk());
        verify(objectiveService).deleteById(5L);
    }


    @Test
    void assignObjectives_success_notFound_badRequest() throws Exception {

        when(gameService.findById(100L)).thenReturn(game);
        doNothing().when(objectiveService).assignObjectivesToPlayers(game);

        mockMvc.perform(post("/api/objectives/assign/{gameId}", 100L))
                .andExpect(status().isOk());


        reset(gameService, objectiveService);
        when(gameService.findById(100L)).thenReturn(null);
        mockMvc.perform(post("/api/objectives/assign/{gameId}", 100L))
                .andExpect(status().isNotFound());


        reset(gameService, objectiveService);
        when(gameService.findById(100L)).thenReturn(game);
        doThrow(new RuntimeException()).when(objectiveService).assignObjectivesToPlayers(game);
        mockMvc.perform(post("/api/objectives/assign/{gameId}", 100L))
                .andExpect(status().isBadRequest());
    }


    @Test
    void validateObjective_success_notFound_badRequest() throws Exception {
        // success
        when(gameService.findById(100L)).thenReturn(game);
        when(playerService.findById(10L)).thenReturn(Optional.of(player));
        when(objectiveService.isObjectiveAchieved(1L, game, player)).thenReturn(true);

        mockMvc.perform(get("/api/objectives/validate")
                        .param("objectiveId", "1")
                        .param("gameId", "100")
                        .param("playerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));


        reset(gameService, playerService, objectiveService);
        when(gameService.findById(100L)).thenReturn(null);
        mockMvc.perform(get("/api/objectives/validate")
                        .param("objectiveId", "1")
                        .param("gameId", "100")
                        .param("playerId", "10"))
                .andExpect(status().isNotFound());

        reset(gameService, playerService, objectiveService);
        when(gameService.findById(100L)).thenReturn(game);
        when(playerService.findById(10L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/objectives/validate")
                        .param("objectiveId", "1")
                        .param("gameId", "100")
                        .param("playerId", "10"))
                .andExpect(status().isNotFound());


        reset(gameService, playerService, objectiveService);
        when(gameService.findById(100L)).thenReturn(game);
        when(playerService.findById(10L)).thenReturn(Optional.of(player));
        when(objectiveService.isObjectiveAchieved(1L, game, player))
                .thenThrow(new RuntimeException());
        mockMvc.perform(get("/api/objectives/validate")
                        .param("objectiveId", "1")
                        .param("gameId", "100")
                        .param("playerId", "10"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getWinner_success_noContent_notFound_badRequest() throws Exception {

        when(gameService.findById(100L)).thenReturn(game);
        when(objectiveService.findWinner(game)).thenReturn(Optional.of(player));

        mockMvc.perform(get("/api/objectives/winner")
                        .param("gameId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(10))
                .andExpect(jsonPath("$.playerName").value("Jugador1"));


        reset(objectiveService, gameService);
        when(gameService.findById(100L)).thenReturn(game);
        when(objectiveService.findWinner(game)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/objectives/winner")
                        .param("gameId", "100"))
                .andExpect(status().isNoContent());


        reset(gameService);
        when(gameService.findById(100L)).thenReturn(null);
        mockMvc.perform(get("/api/objectives/winner")
                        .param("gameId", "100"))
                .andExpect(status().isNotFound());


        reset(gameService, objectiveService);
        when(gameService.findById(100L)).thenReturn(game);
        when(objectiveService.findWinner(game)).thenThrow(new RuntimeException());
        mockMvc.perform(get("/api/objectives/winner")
                        .param("gameId", "100"))
                .andExpect(status().isBadRequest());
    }
}

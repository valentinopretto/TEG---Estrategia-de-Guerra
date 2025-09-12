package ar.edu.utn.frc.tup.piii;

import ar.edu.utn.frc.tup.piii.controllers.AuthController;
import ar.edu.utn.frc.tup.piii.controllers.UserController;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.AuthService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SmokeTest {

    @Autowired
    private ApplicationContext applicationContext;

    // ======================== CONTROLLERS ========================

    @Autowired
    private AuthController authController;

    @Autowired
    private UserController userController;


    // ======================== SERVICES ========================

    @Autowired
    private UserService userService;


    @Autowired
    private AuthService authService;

    // ======================== REPOSITORIES ========================

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private BotProfileRepository botProfileRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ContinentRepository continentRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private GameSnapshotRepository gameSnapshotRepository;

    @Autowired
    private GameTerritoryRepository gameTerritoryRepository;

    @Autowired
    private ObjectiveRepository objectiveRepository;

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot se carga correctamente
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void controllersAreLoaded() {
        // Verifica que todos los controladores están cargados
        assertThat(authController).isNotNull();
        assertThat(userController).isNotNull();
    }

    @Test
    void servicesAreLoaded() {
        // Verifica que todos los servicios están cargados
        assertThat(userService).isNotNull();
        assertThat(authService).isNotNull();
    }

    @Test
    void repositoriesAreLoaded() {
        // Verifica que todos los repositorios están cargados
        assertThat(userRepository).isNotNull();
        assertThat(gameRepository).isNotNull();
        assertThat(playerRepository).isNotNull();
        assertThat(botProfileRepository).isNotNull();
        assertThat(cardRepository).isNotNull();
        assertThat(chatMessageRepository).isNotNull();
        assertThat(continentRepository).isNotNull();
        assertThat(countryRepository).isNotNull();
        assertThat(gameEventRepository).isNotNull();
        assertThat(gameSnapshotRepository).isNotNull();
        assertThat(gameTerritoryRepository).isNotNull();
        assertThat(objectiveRepository).isNotNull();
    }

    @Test
    void beansCanBeRetrievedFromContext() {
        // Verifica que los beans pueden ser obtenidos del contexto
        assertThat(applicationContext.getBean(UserService.class)).isNotNull();
        assertThat(applicationContext.getBean(AuthService.class)).isNotNull();
        assertThat(applicationContext.getBean(UserRepository.class)).isNotNull();
        assertThat(applicationContext.getBean(GameRepository.class)).isNotNull();
    }

    @Test
    void mappersAreLoaded() {
        // Verifica que los mappers están cargados
        assertThat(applicationContext.containsBean("cardMapper")).isTrue();
        assertThat(applicationContext.containsBean("chatMessageMapper")).isTrue();
        assertThat(applicationContext.containsBean("gameEventMapper")).isTrue();
        assertThat(applicationContext.containsBean("gameMapper")).isTrue();
        assertThat(applicationContext.containsBean("objectiveMapper")).isTrue();
        assertThat(applicationContext.containsBean("playerMapper")).isTrue();
    }

    @Test
    void configurationBeansAreLoaded() {
        // Verifica que las configuraciones están cargadas
        assertThat(applicationContext.containsBean("modelMapper")).isTrue();
        assertThat(applicationContext.containsBean("mergerMapper")).isTrue();
        assertThat(applicationContext.containsBean("objectMapper")).isTrue();
    }

    @Test
    void utilityBeansAreLoaded() {
        // Verifica que las utilidades están cargadas (sin jwtUtils)
        assertThat(applicationContext.containsBean("codeGenerator")).isTrue();
        assertThat(applicationContext.containsBean("colorManager")).isTrue();
        // jwtUtils eliminado ya que no se usa JWT
    }
}
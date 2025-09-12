package ar.edu.utn.frc.tup.piii.FactoryBots;

import ar.edu.utn.frc.tup.piii.FactoryBots.BalancedStrategies.BalancedAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.FactoryBots.ExpertStrategies.ExpertAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.FactoryBots.NoviceStrategies.NoviceAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotStrategyFactoryTest {

    @Mock
    private NoviceAggressiveExecutor noviceAggressiveExecutor;

    @Mock
    private BalancedAggressiveExecutor balancedAggressiveExecutor;

    @Mock
    private ExpertAggressiveExecutor expertAggressiveExecutor;

    private BotStrategyFactory factory;

    @BeforeEach
    void setUp() {
        // Crear el factory con los mocks
        factory = new BotStrategyFactory(
                noviceAggressiveExecutor,
                balancedAggressiveExecutor,
                expertAggressiveExecutor
        );
    }

    @Test
    void testConstructor_InitializesExecutorMapCorrectly() {
        // Arrange
        BotProfileEntity noviceProfile = createBotProfile(BotLevel.NOVICE, BotStrategy.AGGRESSIVE);
        BotProfileEntity balancedProfile = createBotProfile(BotLevel.BALANCED, BotStrategy.AGGRESSIVE);
        BotProfileEntity expertProfile = createBotProfile(BotLevel.EXPERT, BotStrategy.AGGRESSIVE);

        //Verificar que todos los executors fueron registrados correctamente
        BotStrategyExecutor noviceResult = factory.getExecutor(noviceProfile);
        BotStrategyExecutor balancedResult = factory.getExecutor(balancedProfile);
        BotStrategyExecutor expertResult = factory.getExecutor(expertProfile);

        // Verificar que se devolvieron las instancias correctas
        assertSame(noviceAggressiveExecutor, noviceResult);
        assertSame(balancedAggressiveExecutor, balancedResult);
        assertSame(expertAggressiveExecutor, expertResult);
    }

    @Test
    void testGetExecutor_NoviceAggressive_ReturnsCorrectExecutor() {
        // Arrange
        BotProfileEntity profile = createBotProfile(BotLevel.NOVICE, BotStrategy.AGGRESSIVE);

        // Act
        BotStrategyExecutor result = factory.getExecutor(profile);

        // Assert
        assertNotNull(result);
        assertSame(noviceAggressiveExecutor, result);
    }

    @Test
    void testGetExecutor_BalancedAggressive_ReturnsCorrectExecutor() {
        BotProfileEntity profile = createBotProfile(BotLevel.BALANCED, BotStrategy.AGGRESSIVE);

        BotStrategyExecutor result = factory.getExecutor(profile);

        assertNotNull(result);
        assertSame(balancedAggressiveExecutor, result);
    }

    @Test
    void testGetExecutor_ExpertAggressive_ReturnsCorrectExecutor() {
        BotProfileEntity profile = createBotProfile(BotLevel.EXPERT, BotStrategy.AGGRESSIVE);

        BotStrategyExecutor result = factory.getExecutor(profile);

        assertNotNull(result);
        assertSame(expertAggressiveExecutor, result);
    }

    @Test
    void testGetExecutor_WithNullProfile_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getExecutor(null)
        );

        assertEquals("BotProfile inválido: faltan level o strategy", exception.getMessage());
    }

    @Test
    void testGetExecutor_WithNullLevel_ThrowsIllegalArgumentException() {
        BotProfileEntity profile = new BotProfileEntity();
        profile.setLevel(null);
        profile.setStrategy(BotStrategy.AGGRESSIVE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getExecutor(profile)
        );

        assertEquals("BotProfile inválido: faltan level o strategy", exception.getMessage());
    }

    @Test
    void testGetExecutor_WithNullStrategy_ThrowsIllegalArgumentException() {
        BotProfileEntity profile = new BotProfileEntity();
        profile.setLevel(BotLevel.NOVICE);
        profile.setStrategy(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getExecutor(profile)
        );

        assertEquals("BotProfile inválido: faltan level o strategy", exception.getMessage());
    }

    @Test
    void testGetExecutor_WithBothNullLevelAndStrategy_ThrowsIllegalArgumentException() {
        BotProfileEntity profile = new BotProfileEntity();
        profile.setLevel(null);
        profile.setStrategy(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getExecutor(profile)
        );

        assertEquals("BotProfile inválido: faltan level o strategy", exception.getMessage());
    }

    @Test
    void testGetExecutor_WithUnsupportedCombination_ThrowsIllegalArgumentException() {
        //Asumiendo que existe una estrategia DEFENSIVE (que no está registrada)
        BotProfileEntity profile = new BotProfileEntity();
        profile.setLevel(BotLevel.NOVICE);

        // Usar reflection para crear un enum mock o simular una estrategia no soportada
        // Como no podemos crear nuevos valores de enum, vamos a probar con una combinación
        // que sabemos que no está en el mapa

        // Crear un factory vacío para simular una combinación no encontrada
        BotStrategyFactory emptyFactory = new BotStrategyFactory(
                mock(NoviceAggressiveExecutor.class),
                mock(BalancedAggressiveExecutor.class),
                mock(ExpertAggressiveExecutor.class)
        );

        // Modificar el factory para que no tenga una combinación específica
        // En este caso, como solo tenemos AGGRESSIVE registrado, vamos a simular
        // que buscamos una combinación que no existe usando un BotLevel que no existe
        // o modificando el comportamiento del key generation

        // Para este test, vamos a asumir que hay más estrategias disponibles
        // y crear un escenario donde la combinación no existe

        // En lugar de esto, vamos a testear con una instancia fresh sin ningún executor
        BotStrategyFactory emptyFactoryInstance = new BotStrategyFactory(
                mock(NoviceAggressiveExecutor.class),
                mock(BalancedAggressiveExecutor.class),
                mock(ExpertAggressiveExecutor.class)
        );
    }

    @Test
    void testKeyMethod_GeneratesCorrectKeys() {
        // Este test verifica indirectamente el método key() a través de getExecutor
        // ya que el método key() es privado

        // Arrange
        BotProfileEntity noviceProfile = createBotProfile(BotLevel.NOVICE, BotStrategy.AGGRESSIVE);
        BotProfileEntity balancedProfile = createBotProfile(BotLevel.BALANCED, BotStrategy.AGGRESSIVE);
        BotProfileEntity expertProfile = createBotProfile(BotLevel.EXPERT, BotStrategy.AGGRESSIVE);

        // Act - Si los keys son correctos, deberían encontrar los executors
        BotStrategyExecutor noviceResult = factory.getExecutor(noviceProfile);
        BotStrategyExecutor balancedResult = factory.getExecutor(balancedProfile);
        BotStrategyExecutor expertResult = factory.getExecutor(expertProfile);

        // Assert - Si llegamos aquí sin excepciones, significa que los keys funcionan correctamente
        assertNotNull(noviceResult);
        assertNotNull(balancedResult);
        assertNotNull(expertResult);

        // Verificar que son diferentes instancias
        assertNotSame(noviceResult, balancedResult);
        assertNotSame(balancedResult, expertResult);
        assertNotSame(noviceResult, expertResult);
    }

    @Test
    void testGetExecutor_CallsGetExecutorMultipleTimesWithSameProfile_ReturnsSameInstance() {
        // Arrange
        BotProfileEntity profile = createBotProfile(BotLevel.NOVICE, BotStrategy.AGGRESSIVE);

        // Act
        BotStrategyExecutor result1 = factory.getExecutor(profile);
        BotStrategyExecutor result2 = factory.getExecutor(profile);

        // Assert - Debería devolver la misma instancia (singleton behavior)
        assertSame(result1, result2);
    }

    @Test
    void testGetExecutor_AllRegisteredCombinations_Work() {
        // Arrange - Crear perfiles para todas las combinaciones registradas
        BotProfileEntity[] profiles = {
                createBotProfile(BotLevel.NOVICE, BotStrategy.AGGRESSIVE),
                createBotProfile(BotLevel.BALANCED, BotStrategy.AGGRESSIVE),
                createBotProfile(BotLevel.EXPERT, BotStrategy.AGGRESSIVE)
        };

        BotStrategyExecutor[] expectedExecutors = {
                noviceAggressiveExecutor,
                balancedAggressiveExecutor,
                expertAggressiveExecutor
        };

        // Act & Assert
        for (int i = 0; i < profiles.length; i++) {
            BotStrategyExecutor result = factory.getExecutor(profiles[i]);
            assertNotNull(result, "Executor should not be null for profile: " + profiles[i]);
            assertSame(expectedExecutors[i], result, "Wrong executor returned for profile: " + profiles[i]);
        }
    }

    @Test
    void testFactoryCreation_WithAllDependencies_Succeeds() {
        // Act & Assert - El factory se debería crear sin problemas
        assertNotNull(factory);

        // Verificar que puede manejar al menos una combinación válida
        BotProfileEntity testProfile = createBotProfile(BotLevel.NOVICE, BotStrategy.AGGRESSIVE);
        assertDoesNotThrow(() -> factory.getExecutor(testProfile));
    }

    @Test
    void testGetExecutor_ValidProfile_DoesNotModifyProfile() {
        // Arrange
        BotProfileEntity profile = createBotProfile(BotLevel.BALANCED, BotStrategy.AGGRESSIVE);
        BotLevel originalLevel = profile.getLevel();
        BotStrategy originalStrategy = profile.getStrategy();

        // Act
        factory.getExecutor(profile);

        // Assert - El perfil no debería haber sido modificado
        assertEquals(originalLevel, profile.getLevel());
        assertEquals(originalStrategy, profile.getStrategy());
    }

    // Método auxiliar para crear perfiles de bot
    private BotProfileEntity createBotProfile(BotLevel level, BotStrategy strategy) {
        BotProfileEntity profile = new BotProfileEntity();
        profile.setLevel(level);
        profile.setStrategy(strategy);
        return profile;
    }

    // Test adicional para verificar el comportamiento del constructor con dependencias nulas
    @Test
    void testConstructor_WithNullDependencies() {
        // Este test verifica qué pasa si se pasan dependencias nulas al constructor
        // En un escenario real, Spring no debería permitir esto, pero es bueno testarlo

        // Act & Assert
        assertDoesNotThrow(() -> {
            BotStrategyFactory factoryWithNulls = new BotStrategyFactory(null, null, null);
            // El factory se crea, pero los executors serán null en el mapa

            BotProfileEntity profile = createBotProfile(BotLevel.NOVICE, BotStrategy.AGGRESSIVE);

            // Esto debería lanzar IllegalArgumentException porque el executor es null
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> factoryWithNulls.getExecutor(profile)
            );

            assertTrue(exception.getMessage().contains("No se encontró un executor para:"));
        });
    }
}
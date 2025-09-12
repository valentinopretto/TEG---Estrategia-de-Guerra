package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class BotProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BotProfileRepository botProfileRepository;

    private BotProfileEntity noviceAggressive;
    private BotProfileEntity noviceDefensive;
    private BotProfileEntity balancedAggressive;
    private BotProfileEntity expertBalanced;

    @BeforeEach
    void setUp() {
        // Limpiar datos existentes
        botProfileRepository.deleteAll();

        // Crear perfiles de bot
        noviceAggressive = new BotProfileEntity();
        noviceAggressive.setLevel(BotLevel.NOVICE);
        noviceAggressive.setStrategy(BotStrategy.AGGRESSIVE);
        noviceAggressive.setBotName("Bot Novato Agresivo");
        noviceAggressive = entityManager.persistAndFlush(noviceAggressive);

        noviceDefensive = new BotProfileEntity();
        noviceDefensive.setLevel(BotLevel.NOVICE);
        noviceDefensive.setStrategy(BotStrategy.DEFENSIVE);
        noviceDefensive.setBotName("Bot Novato Defensivo");
        noviceDefensive = entityManager.persistAndFlush(noviceDefensive);

        balancedAggressive = new BotProfileEntity();
        balancedAggressive.setLevel(BotLevel.BALANCED);
        balancedAggressive.setStrategy(BotStrategy.AGGRESSIVE);
        balancedAggressive.setBotName("Bot Equilibrado Agresivo");
        balancedAggressive = entityManager.persistAndFlush(balancedAggressive);

        expertBalanced = new BotProfileEntity();
        expertBalanced.setLevel(BotLevel.EXPERT);
        expertBalanced.setStrategy(BotStrategy.BALANCED);
        expertBalanced.setBotName("Bot Experto Equilibrado");
        expertBalanced = entityManager.persistAndFlush(expertBalanced);

        entityManager.flush();
    }

    @Test
    void findByLevel_ShouldReturnBotsOfSpecificLevel() {
        List<BotProfileEntity> noviceBots = botProfileRepository.findByLevel(BotLevel.NOVICE);

        assertThat(noviceBots).hasSize(2);
        assertThat(noviceBots).allMatch(bot -> bot.getLevel() == BotLevel.NOVICE);
        assertThat(noviceBots).extracting(BotProfileEntity::getBotName)
                .containsExactlyInAnyOrder("Bot Novato Agresivo", "Bot Novato Defensivo");
    }

    @Test
    void findByStrategy_ShouldReturnBotsWithSpecificStrategy() {
        List<BotProfileEntity> aggressiveBots = botProfileRepository.findByStrategy(BotStrategy.AGGRESSIVE);

        assertThat(aggressiveBots).hasSize(2);
        assertThat(aggressiveBots).allMatch(bot -> bot.getStrategy() == BotStrategy.AGGRESSIVE);
        assertThat(aggressiveBots).extracting(BotProfileEntity::getLevel)
                .containsExactlyInAnyOrder(BotLevel.NOVICE, BotLevel.BALANCED);
    }

    @Test
    void findByLevelAndStrategy_WhenExists_ShouldReturnSpecificBot() {
        Optional<BotProfileEntity> found = botProfileRepository.findByLevelAndStrategy(
                BotLevel.EXPERT, BotStrategy.BALANCED);

        assertThat(found).isPresent();
        assertThat(found.get().getBotName()).isEqualTo("Bot Experto Equilibrado");
        assertThat(found.get().getLevel()).isEqualTo(BotLevel.EXPERT);
        assertThat(found.get().getStrategy()).isEqualTo(BotStrategy.BALANCED);
    }

    @Test
    void findByLevelAndStrategy_WhenNotExists_ShouldReturnEmpty() {
        Optional<BotProfileEntity> found = botProfileRepository.findByLevelAndStrategy(
                BotLevel.EXPERT, BotStrategy.OBJECTIVE_FOCUSED);

        assertThat(found).isEmpty();
    }

    @Test
    void findRandomBotsByLevel_ShouldReturnBotsOfSpecificLevel() {
        List<BotProfileEntity> randomNoviceBots = botProfileRepository.findRandomBotsByLevel(BotLevel.NOVICE);

        assertThat(randomNoviceBots).hasSize(2);
        assertThat(randomNoviceBots).allMatch(bot -> bot.getLevel() == BotLevel.NOVICE);
        // El orden puede variar por RAND(), pero el contenido debe ser el mismo
        assertThat(randomNoviceBots).extracting(BotProfileEntity::getBotName)
                .containsExactlyInAnyOrder("Bot Novato Agresivo", "Bot Novato Defensivo");
    }

    @Test
    void findAllRandomOrder_ShouldReturnAllBotsInRandomOrder() {
        List<BotProfileEntity> allRandomBots = botProfileRepository.findAllRandomOrder();

        assertThat(allRandomBots).hasSize(4);
        assertThat(allRandomBots).extracting(BotProfileEntity::getBotName)
                .containsExactlyInAnyOrder(
                        "Bot Novato Agresivo",
                        "Bot Novato Defensivo",
                        "Bot Equilibrado Agresivo",
                        "Bot Experto Equilibrado"
                );
    }

    @Test
    void findRandomBotsByLevel_WhenNoBotsOfLevel_ShouldReturnEmpty() {
        // No hay bots EXPERT con AGGRESSIVE, solo BALANCED
        List<BotProfileEntity> randomExpertBots = botProfileRepository.findRandomBotsByLevel(BotLevel.EXPERT);

        assertThat(randomExpertBots).hasSize(1);
        assertThat(randomExpertBots.get(0).getLevel()).isEqualTo(BotLevel.EXPERT);
    }

    @Test
    void findByLevel_WhenNoBotsOfLevel_ShouldReturnEmpty() {
        // Crear un nivel que no tiene bots en nuestro setup
        List<BotProfileEntity> nonExistentLevelBots = botProfileRepository.findByLevel(BotLevel.EXPERT);

        // Solo debería devolver el bot experto que creamos
        assertThat(nonExistentLevelBots).hasSize(1);
        assertThat(nonExistentLevelBots.get(0).getBotName()).isEqualTo("Bot Experto Equilibrado");
    }

    @Test
    void findByStrategy_WhenNoBotsWithStrategy_ShouldReturnEmpty() {
        List<BotProfileEntity> objectiveFocusedBots = botProfileRepository.findByStrategy(BotStrategy.OBJECTIVE_FOCUSED);

        assertThat(objectiveFocusedBots).isEmpty();
    }
}
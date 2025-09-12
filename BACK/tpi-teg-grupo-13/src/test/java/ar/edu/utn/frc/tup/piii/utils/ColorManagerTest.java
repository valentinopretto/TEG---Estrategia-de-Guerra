package ar.edu.utn.frc.tup.piii.utils;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ColorManagerTest {

    @InjectMocks
    private ColorManager colorManager;

    private GameEntity gameEntity;

    @BeforeEach
    void setUp() {
        gameEntity = new GameEntity();
        // Inicialmente, no hay jugadores
        gameEntity.setPlayers(Collections.emptyList());
    }

    @Test
    void getAvailableRandomColor_WhenNoPlayersInGame_ShouldReturnAnyColor() {
        PlayerColor result = colorManager.getAvailableRandomColor(gameEntity);

        // Debe devolver algún color no nulo, y estar dentro del enum completo
        assertThat(result).isNotNull();
        assertThat(result).isIn(PlayerColor.values());
    }

    @Test
    void getAvailableRandomColor_WhenSomeColorsUsed_ShouldReturnAvailableColor() {
        // Creamos dos PlayerEntity con colores específicos
        PlayerEntity player1 = new PlayerEntity();
        player1.setId(1L);
        player1.setColor(PlayerColor.RED);

        PlayerEntity player2 = new PlayerEntity();
        player2.setId(2L);
        player2.setColor(PlayerColor.BLUE);

        // Asignamos la lista de jugadores al GameEntity
        gameEntity.setPlayers(Arrays.asList(player1, player2));

        PlayerColor result = colorManager.getAvailableRandomColor(gameEntity);

        // Debe devolver un color distinto de RED y BLUE
        assertThat(result).isNotNull();
        assertThat(result).isNotIn(PlayerColor.RED, PlayerColor.BLUE);
    }

}

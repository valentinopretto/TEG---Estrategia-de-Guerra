package ar.edu.utn.frc.tup.piii.model.enums;

import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;

import java.util.*;
import java.util.stream.Collectors;

public enum PlayerColor {
    RED("#FF0000"),
    BLUE("#0000FF"),
    GREEN("#008000"),
    YELLOW("#FFFF00"),
    BLACK("#000000"),
    PURPLE("#800080");

    private final String hexColor;
    private static final List<PlayerColor> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final Random RANDOM = new Random();


    PlayerColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public String getHexColor() {
        return hexColor;
    }

    public static PlayerColor randomColor() {
        return VALUES.get(RANDOM.nextInt(VALUES.size()));
    }

}

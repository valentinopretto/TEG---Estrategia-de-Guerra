package ar.edu.utn.frc.tup.piii.dtos.player;

import lombok.Data;

@Data
public class PlayerRequestDto {
    private Long userId;
    private Long gameId;
    private Boolean isBot;
    private String botLevel;
    private String status;
    private String color;
    private Integer seatOrder;
}

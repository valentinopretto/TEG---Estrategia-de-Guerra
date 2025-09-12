package ar.edu.utn.frc.tup.piii.dtos.player;

import ar.edu.utn.frc.tup.piii.dtos.card.CardResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.objective.ObjectiveResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerResponseDto {
    private Long id;
    private String username;
    private String displayName;
    private String status;
    private String color;
    private Boolean isBot;
    private String botLevel;
    private Integer armiesToPlace;
    private Integer seatOrder;
    private LocalDateTime joinedAt;
    private LocalDateTime eliminatedAt;
    private List<CardResponseDto> hand;
    private List<Long> territoryIds;
    private ObjectiveResponseDto objective;
    private Integer territoryCount;
    private Integer totalArmies;
}
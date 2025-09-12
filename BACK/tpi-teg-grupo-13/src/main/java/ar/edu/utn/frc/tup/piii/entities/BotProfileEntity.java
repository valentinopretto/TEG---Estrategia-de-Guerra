package ar.edu.utn.frc.tup.piii.entities;

import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;

@Entity
@Table(name = "bot_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotStrategy strategy;

    @Column(name = "bot_name", nullable = false)
    private String botName;
}
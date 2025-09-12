package ar.edu.utn.frc.tup.piii.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "game_territories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameTerritoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private CountryEntity  country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerEntity  owner; // null si es neutral

//rastrear en qué turno un territorio fue conquistado.
/*
    @Column(name = "last_conquered_turn")
    private Integer lastConqueredTurn; // Nuevo campo
*/

    @Column(nullable = false)
    private Integer armies = 1;

    @PrePersist
    @PreUpdate
    protected void validateArmies() {
        if (armies != null && armies < 0) {
            armies = 0;
        }
    }
}


package ar.edu.utn.frc.tup.piii.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;

@Entity
@Table(name = "objectives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjectiveType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "target_data", length = 1000)
    private String targetData; // JSON con datos específicos del objetivo

    @Column(name = "is_common")
    private Boolean isCommon = false;
}
package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.objective.ObjectiveResponseDto;
import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.model.Objective;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import org.springframework.stereotype.Component;

@Component
public class ObjectiveMapper {

    public Objective toModel(ObjectiveEntity entity) {
        if (entity == null) return null;

        return Objective.builder()
                .id(entity.getId())
                .type(entity.getType())
                .description(entity.getDescription())
                .targetData(entity.getTargetData())
                .isCommon(entity.getIsCommon())
                .isAchieved(false) // Se calculará dinámicamente
                .build();
    }

    public ObjectiveEntity toEntity(Objective model) {
        if (model == null) return null;

        ObjectiveEntity entity = new ObjectiveEntity();
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setDescription(model.getDescription());
        entity.setTargetData(model.getTargetData());
        entity.setIsCommon(model.getIsCommon());

        return entity;
    }
    public ObjectiveResponseDto toResponseDto(Objective model) {
        if (model == null) return null;

        return ObjectiveResponseDto.builder()
                .id(model.getId())
                .description(model.getDescription())
                .isAchieved(model.getIsAchieved())
                .isCommon(model.getIsCommon())
                .type(model.getType() != null ? model.getType().name() : null)
                .build();
    }

}

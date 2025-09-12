package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.entities.ChatMessageEntity;
import ar.edu.utn.frc.tup.piii.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessage toModel(ChatMessageEntity entity) {
        if (entity == null) return null;

        String senderName = "Sistema";
        if (!entity.getIsSystemMessage() && entity.getSender() != null) {
            if (entity.getSender().getUser() != null) {
                senderName = entity.getSender().getUser().getUsername();
            } else if (entity.getSender().getBotProfile() != null) {
                senderName = entity.getSender().getBotProfile().getBotName();
            }
        }

        return ChatMessage.builder()
                .id(entity.getId())
                .senderName(senderName)
                .content(entity.getContent())
                .sentAt(entity.getSentAt())
                .isSystemMessage(entity.getIsSystemMessage())
                .build();
    }

    public ChatMessageEntity toEntity(ChatMessage model) {
        if (model == null) return null;

        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(model.getId());
        entity.setContent(model.getContent());
        entity.setSentAt(model.getSentAt());
        entity.setIsSystemMessage(model.getIsSystemMessage());

        return entity;
    }
    public ChatMessageResponseDto toResponseDto(ChatMessage model) {
        if (model == null) return null;

        return ChatMessageResponseDto.builder()
                .id(model.getId())
                .senderName(model.getSenderName())
                .content(model.getContent())
                .sentAt(model.getSentAt())  // Asegurate que el modelo tenga `sentAt`
                .isSystemMessage(model.getIsSystemMessage())
                .build();
    }
}
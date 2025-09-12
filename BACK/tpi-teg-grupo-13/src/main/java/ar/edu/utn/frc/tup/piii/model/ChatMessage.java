package ar.edu.utn.frc.tup.piii.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private Long id;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isSystemMessage;

    public static ChatMessage createSystemMessage(String content) {
        return ChatMessage.builder()
                .content(content)
                .sentAt(LocalDateTime.now())
                .isSystemMessage(true)
                .senderName("Sistema")
                .build();
    }

    public static ChatMessage createPlayerMessage(String senderName, String content) {
        return ChatMessage.builder()
                .senderName(senderName)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isSystemMessage(false)
                .build();
    }
}
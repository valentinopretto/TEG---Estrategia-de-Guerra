package ar.edu.utn.frc.tup.piii.dtos.chat;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ChatMessageResponseDto {
    private Long id;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isSystemMessage;
}

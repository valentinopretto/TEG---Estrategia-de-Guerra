package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageDto;
import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.model.ChatMessage;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import java.util.List;
import java.util.Optional;

public interface ChatService {


    /**
     * Enviar mensaje de chat
     */
    ChatMessageResponseDto sendMessage(String gameCode, ChatMessageDto messageDto);

    /**
     * Obtener mensajes nuevos desde un mensaje específico
     */
    List<ChatMessageResponseDto> getNewMessages(String gameCode, String sinceMessageId);

}

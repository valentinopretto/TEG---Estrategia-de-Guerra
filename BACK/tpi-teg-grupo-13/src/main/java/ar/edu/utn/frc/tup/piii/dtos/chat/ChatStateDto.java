package ar.edu.utn.frc.tup.piii.dtos.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatStateDto {
    private List<ChatMessageResponseDto> mensajes = new ArrayList<>();

    public List<ChatMessageResponseDto> getMensajes() {
        return this.mensajes;
    }

    public void setMensajes(List<ChatMessageResponseDto> mensajes) {
        this.mensajes = mensajes;
    }
}

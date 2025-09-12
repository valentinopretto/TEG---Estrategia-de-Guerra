package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageDto;
import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.entities.ChatMessageEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.mappers.ChatMessageMapper;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.repository.ChatMessageRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.ChatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceIml implements ChatService {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    PlayerServiceImpl playerService;

    @Autowired
    GameService gameService;

    //mappers
    @Autowired
    PlayerMapper playerMapper;

    @Autowired
    GameMapper gameMapper;

    @Autowired
    ChatMessageMapper chatMessageMapper;

    /**
     * Envia mensaje de chat y lo setea a la lista de mensajes del juego.
     * @param gameCode
     * @param messageDto
     * @return
     */
    @Override
    public ChatMessageResponseDto sendMessage(String gameCode, ChatMessageDto messageDto) {
        Game gameFind = gameService.findByGameCode(gameCode);//busco juego por codigo
        GameEntity gameEntity = gameMapper.toEntity(gameFind); //convierto a entity
        if(gameEntity == null){
            throw new IllegalArgumentException("Game not found with code: " + gameCode);
        }
        //busco el player que envia el mensaje
        Optional<Player> sender = playerService.findById(messageDto.getSenderId());
        if(!sender.isPresent()){
            throw new IllegalArgumentException("Sender not found with id: " + messageDto.getSenderId());
        }

        //armo el ChatEntity
        ChatMessageEntity chatEntity = new ChatMessageEntity();
        chatEntity.setContent(messageDto.getContent());
        chatEntity.setSender(playerMapper.toEntity(sender.get()));
        chatEntity.setIsSystemMessage(false);
        //agrego el mensaje a la lista de mensajes del juego
        gameEntity.getChatMessages().add(chatEntity);
        List<ChatMessageEntity> lst = gameEntity.getChatMessages();
        gameEntity.setChatMessages(lst);

        //recien ahi le seteo el juego al mensaje
        chatEntity.setGame(gameEntity);

        //mapeo a ChatMessageResponseDto
        ChatMessageResponseDto responseDto = chatMessageMapper.toResponseDto(chatMessageMapper.toModel(chatEntity));
        chatMessageRepository.save(chatEntity);
        return responseDto;
    }

     /**
     * Obtiene los mensajes nuevos de chat de un juego específico.
     *
     * Este método implementa un sistema de polling incremental para recuperar mensajes de chat.
     * En la primera llamada (sin sinceMessageId), devuelve todos los mensajes del juego.
     * En llamadas posteriores, devuelve solo los mensajes posteriores al ID especificado.
     *
     * @param gameCode El código único del juego del cual obtener los mensajes
     * @param sinceMessageId ID del último mensaje conocido. Si es null o vacío,
     *                       devuelve todos los mensajes del juego. Si tiene valor,
     *                       devuelve solo los mensajes con ID mayor a este valor.
     *
     * @return Lista de ChatMessageResponseDto con los mensajes solicitados,
     *         ordenados cronológicamente (más antiguos primero)
     *         */
        @Override
    public List<ChatMessageResponseDto> getNewMessages(String gameCode, String sinceMessageId) {
        // Buscar el juego
        Game gameFind = gameService.findByGameCode(gameCode);
        if (gameFind == null) {
            throw new IllegalArgumentException("Game not found with code: " + gameCode);
        }

        List<ChatMessageEntity> messages;

        if (sinceMessageId == null || sinceMessageId.isEmpty()) {
            // Primera llamada: devolver TODOS los mensajes del juego
            messages = chatMessageRepository.findByGameIdOrderBySentAtAsc(gameFind.getId());
        } else {
            // Siguientes llamadas: devolver mensajes DESPUÉS del sinceMessageId
            Long messageIdLong = Long.parseLong(sinceMessageId);
            messages = chatMessageRepository.findByGameIdAndIdGreaterThanOrderBySentAtAsc(
                    gameFind.getId(),
                    messageIdLong
            );
        }

        // Mapear a DTOs
        return messages.stream()
                .map(entity -> chatMessageMapper.toResponseDto(chatMessageMapper.toModel(entity)))
                .collect(Collectors.toList());
    }
}

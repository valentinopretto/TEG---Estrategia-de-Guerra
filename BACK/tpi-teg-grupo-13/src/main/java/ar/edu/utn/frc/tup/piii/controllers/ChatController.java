package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageDto;
import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.service.interfaces.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
@Tag(name = "Chat", description = "Sistema de chat en tiempo real para las partidas")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/{gameCode}/chat")
    @Operation(
            summary = "Enviar mensaje de chat",
            description = "Envía un mensaje de chat a todos los participantes de una partida específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Mensaje enviado exitosamente",
                    content = @Content(schema = @Schema(implementation = ChatMessageResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del mensaje inválidos o partida no permite chat"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @PathVariable String gameCode,
            @Valid @RequestBody ChatMessageDto messageDto) {

        try {
            ChatMessageResponseDto response = chatService.sendMessage(gameCode, messageDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{gameCode}/chat/new")
    @Operation(
            summary = "Obtener mensajes nuevos",
            description = "Obtiene mensajes de chat nuevos desde un punto específico. Soporta polling incremental para actualizaciones en tiempo real."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Mensajes obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ChatMessageResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetro 'since' inválido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida no encontrada"
            )
    })
    public ResponseEntity<List<ChatMessageResponseDto>> getNewMessages(
            @PathVariable String gameCode,
            @RequestParam(value = "since", required = false) String since) {

        try {
            List<ChatMessageResponseDto> messages = chatService.getNewMessages(gameCode, since);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{gameCode}/chat")
    @Operation(
            summary = "Obtener historial completo de chat",
            description = "Obtiene todos los mensajes de chat de una partida. Útil para cargar el historial completo al unirse a una partida."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial de chat obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = ChatMessageResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Partida no encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<List<ChatMessageResponseDto>> getAllMessages(
            @PathVariable String gameCode) {

        try {
            List<ChatMessageResponseDto> messages = chatService.getNewMessages(gameCode, null);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
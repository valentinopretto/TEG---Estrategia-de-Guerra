package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.user.PasswordChangeDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Management", description = "Gestión de perfiles y configuraciones de usuario")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtiene una lista de todos los usuarios registrados en el sistema. Endpoint administrativo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado - permisos insuficientes"
            )
    })
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Recupera la información completa de un usuario específico usando su ID único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado exitosamente",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado - solo puede ver su propio perfil"
            )
    })
    public ResponseEntity<User> getUserById (@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar perfil de usuario",
            description = "Permite actualizar la información del perfil de un usuario (username, email, avatar). No incluye cambio de contraseña."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de actualización inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username o email ya está en uso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado - solo puede modificar su propio perfil"
            )
    })
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDto dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/change-password")
    @Operation(
            summary = "Cambiar contraseña",
            description = "Permite a un usuario cambiar su contraseña actual por una nueva. Requiere verificación de la contraseña actual."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña cambiada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de cambio de contraseña inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Contraseña actual incorrecta"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado - solo puede cambiar su propia contraseña"
            )
    })
    public ResponseEntity<?> changePassword(@PathVariable Long id,
                                            @RequestBody @Valid PasswordChangeDto dto) {
        userService.changePassword(id, dto);
        return ResponseEntity.ok().build();
    }





}

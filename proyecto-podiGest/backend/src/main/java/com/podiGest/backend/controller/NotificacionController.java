package com.podiGest.backend.controller;

import com.podiGest.backend.model.Notificacion;
import com.podiGest.backend.model.Usuario;
import com.podiGest.backend.service.NotificacionService;
import com.podiGest.backend.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final PerfilService perfilService;

    public NotificacionController(NotificacionService notificacionService, PerfilService perfilService) {
        this.notificacionService = notificacionService;
        this.perfilService = perfilService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerNotificaciones() {
        try {
            // Obtener el usuario activo de la sesión
            Optional<Usuario> usuarioActivo = perfilService.obtenerPerfilActivo();
            
            if (usuarioActivo.isEmpty()) {
                System.err.println("ERROR: No hay sesión activa al intentar obtener notificaciones");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("No hay una sesión activa. Por favor, inicie sesión.");
            }
            
            // Obtener solo las notificaciones del usuario activo
            String correoUsuario = usuarioActivo.get().getCorreoElectronico();
            System.out.println("INFO: Obteniendo notificaciones para el usuario: " + correoUsuario);
            
            List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesPorUsuario(correoUsuario);
            System.out.println("INFO: Se encontraron " + notificaciones.size() + " notificaciones para el usuario");
            
            return ResponseEntity.ok(notificaciones);
        } catch (IOException e) {
            System.err.println("Error al obtener notificaciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerNotificacion(@PathVariable String id) {
        try {
            // Obtener el usuario activo de la sesión
            Optional<Usuario> usuarioActivo = perfilService.obtenerPerfilActivo();
            
            if (usuarioActivo.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("No hay una sesión activa. Por favor, inicie sesión.");
            }
            
            // Buscar la notificación
            Optional<Notificacion> notificacion = notificacionService.obtenerNotificacionPorId(id);
            
            if (notificacion.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que la notificación pertenezca al usuario activo
            String correoUsuario = usuarioActivo.get().getCorreoElectronico();
            if (notificacion.get().getCorreoDestinatario() != null 
                    && notificacion.get().getCorreoDestinatario().equalsIgnoreCase(correoUsuario)) {
                return ResponseEntity.ok(notificacion.get());
            } else {
                // La notificación no pertenece al usuario activo
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("No tiene permiso para acceder a esta notificación.");
            }
        } catch (IOException e) {
            System.err.println("Error al obtener notificación: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
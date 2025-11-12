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

    @PutMapping("/{id}/silenciar")
    public ResponseEntity<?> silenciarNotificacion(@PathVariable String id) {
        try {
            // Obtener el usuario activo de la sesión
            Optional<Usuario> usuarioActivo = perfilService.obtenerPerfilActivo();
            
            if (usuarioActivo.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("No hay una sesión activa. Por favor, inicie sesión.");
            }
            
            // Verificar que la notificación existe y pertenece al usuario
            Optional<Notificacion> notificacion = notificacionService.obtenerNotificacionPorId(id);
            
            if (notificacion.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String correoUsuario = usuarioActivo.get().getCorreoElectronico();
            if (notificacion.get().getCorreoDestinatario() == null 
                    || !notificacion.get().getCorreoDestinatario().equalsIgnoreCase(correoUsuario)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("No tiene permiso para silenciar esta notificación.");
            }
            
            // Silenciar la notificación
            boolean silenciada = notificacionService.silenciarNotificacion(id);
            
            if (silenciada) {
                System.out.println("INFO: Notificación " + id + " silenciada exitosamente para el usuario " + correoUsuario);
                return ResponseEntity.ok().body("Notificación silenciada exitosamente");
            } else {
                return ResponseEntity.internalServerError().body("Error al silenciar la notificación");
            }
        } catch (IOException e) {
            System.err.println("Error al silenciar notificación: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/dessilenciar")
    public ResponseEntity<?> dessilenciarNotificacion(@PathVariable String id) {
        try {
            // Obtener el usuario activo de la sesión
            Optional<Usuario> usuarioActivo = perfilService.obtenerPerfilActivo();
            
            if (usuarioActivo.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("No hay una sesión activa. Por favor, inicie sesión.");
            }
            
            // Verificar que la notificación existe y pertenece al usuario
            Optional<Notificacion> notificacion = notificacionService.obtenerNotificacionPorId(id);
            
            if (notificacion.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String correoUsuario = usuarioActivo.get().getCorreoElectronico();
            if (notificacion.get().getCorreoDestinatario() == null 
                    || !notificacion.get().getCorreoDestinatario().equalsIgnoreCase(correoUsuario)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("No tiene permiso para dessilenciar esta notificación.");
            }
            
            // Dessilenciar la notificación
            boolean dessilenciada = notificacionService.dessilenciarNotificacion(id);
            
            if (dessilenciada) {
                System.out.println("INFO: Notificación " + id + " dessilenciada exitosamente para el usuario " + correoUsuario);
                return ResponseEntity.ok().body("Notificación dessilenciada exitosamente");
            } else {
                return ResponseEntity.internalServerError().body("Error al dessilenciar la notificación");
            }
        } catch (IOException e) {
            System.err.println("Error al dessilenciar notificación: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> contarNotificacionesNoSilenciadas() {
        try {
            // Obtener el usuario activo de la sesión
            Optional<Usuario> usuarioActivo = perfilService.obtenerPerfilActivo();
            
            if (usuarioActivo.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("No hay una sesión activa. Por favor, inicie sesión.");
            }
            
            String correoUsuario = usuarioActivo.get().getCorreoElectronico();
            long count = notificacionService.contarNotificacionesNoSilenciadas(correoUsuario);
            
            System.out.println("INFO: Usuario " + correoUsuario + " tiene " + count + " notificaciones no silenciadas");
            
            return ResponseEntity.ok(count);
        } catch (IOException e) {
            System.err.println("Error al contar notificaciones: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
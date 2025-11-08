

package com.podiGest.backend.controller;

import com.podiGest.backend.model.Usuario;
import com.podiGest.backend.service.ConsultarPerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfil")
public class ConsultarPerfilController {

    @Autowired
    private ConsultarPerfilService perfilService;

    @GetMapping
    public ResponseEntity<?> consultarPerfil() {
        try {

            Optional<Usuario> usuarioSesion = perfilService.obtenerPerfilActivo();

            if (usuarioSesion.isPresent()) {
                return ResponseEntity.ok(usuarioSesion.get());
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("No hay una sesión activa.");
            }
        } catch (IOException e) {
            System.err.println("Error al leer datos del perfil: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al leer los datos del perfil.");
        }
    }
}

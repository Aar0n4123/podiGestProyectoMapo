package com.podiGest.backend.controller;

import com.podiGest.backend.model.Usuario;
import com.podiGest.backend.model.LoginRequest;
import com.podiGest.backend.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class PerfilController {

    @Autowired
    private PerfilService usuarioService;

    /**
     * Maneja las peticiones POST para registrar un nuevo usuario.
     * @param nuevoUsuario El objeto Usuario enviado desde el frontend.
     */
    @PostMapping
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {

        if (nuevoUsuario.getCorreoElectronico() == null || nuevoUsuario.getContrasenia() == null) {
            return ResponseEntity.badRequest().body("El correo y la contraseña son obligatorios.");
        }

        if (usuarioService.existeUsuario(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getCedula())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Ya existe un usuario registrado con esa cédula o correo electrónico.");
        }

        try {

            Usuario usuarioGuardado = usuarioService.guardarUsuario(nuevoUsuario);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(usuarioGuardado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor al procesar el registro.");
        }
    }


    /**
     * Maneja las peticiones POST a /api/usuarios/login.
     * Verifica la existencia del usuario y la contraseña.
     * @param loginRequest El objeto con correo y contraseña enviado desde el frontend.
     */
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest loginRequest) {

        String correoAcceso = loginRequest.getCorreo();
        String contrasenaAcceso = loginRequest.getContrasenia();

        Optional<Usuario> usuarioEncontrado = usuarioService.validarUsuarioExiste(correoAcceso, contrasenaAcceso);

        if (usuarioEncontrado.isPresent()) {

            Usuario usuario = usuarioEncontrado.get();
            usuarioService.guardarUsuarioSesion(usuario);
            return ResponseEntity.ok(usuario);

        } else {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales incorrectas. Verifique su correo y contraseña.");
        }
    }

    @GetMapping("/especialistas")
    public ResponseEntity<List<Usuario>> obtenerEspecialistas() {
        try {
            List<Usuario> especialistas = usuarioService.obtenerEspecialistas();
            return ResponseEntity.ok(especialistas);
        } catch (Exception e) {
            System.err.println("Error al obtener especialistas: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> consultarPerfil() {
        try {

            Optional<Usuario> usuarioSesion = usuarioService.obtenerPerfilActivo();

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


    // -------------------------------------------------------------------
    // NUEVO ENDPOINT: SPRINT 2 - MODIFICAR PERFIL
    // -------------------------------------------------------------------
    @PutMapping
    public ResponseEntity<?> actualizarPerfil(@RequestBody Usuario usuarioModificado) {
        try {
            // Llamamos al servicio que acabamos de crear
            Usuario actualizado = usuarioService.actualizarPerfil(usuarioModificado);
            return ResponseEntity.ok(actualizado);

        } catch (IOException e) {
            System.err.println("Error al actualizar perfil: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar los datos en el servidor.");
        }
    }

    // -------------------------------------------------------------------
    // NUEVO ENDPOINT: ELIMINAR CUENTA
    // -------------------------------------------------------------------
    @DeleteMapping
    public ResponseEntity<?> eliminarPerfil() {
        try {
            usuarioService.eliminarPerfilActual();
            return ResponseEntity.ok("{\"mensaje\": \"Perfil eliminado correctamente\"}");
        } catch (IOException e) {
            System.err.println("Error al eliminar perfil: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el perfil: " + e.getMessage());
        }
    }
}
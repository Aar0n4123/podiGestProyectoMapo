package com.podiGest.backend.controller;

import com.podiGest.backend.model.Usuario;
import com.podiGest.backend.model.LoginRequest;
import com.podiGest.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;



    /**
     * Maneja las peticiones POST para registrar un nuevo usuario.
     * Guarda el usuario en usuarios.json y, si es especialista, también en especialistas.json.
     * @param nuevoUsuario El objeto Usuario enviado desde el frontend.
     * @return 201 Created si es exitoso, 409 Conflict si el usuario ya existe.
     */
    @PostMapping
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        // Validación básica (se podría mejorar)
        if (nuevoUsuario.getCorreoElectronico() == null || nuevoUsuario.getContrasenia() == null) {
            return ResponseEntity.badRequest().body("El correo y la contraseña son obligatorios.");
        }

        // CORRECCIÓN: Se llama al método existente 'existeUsuario'
        if (usuarioService.existeUsuario(nuevoUsuario.getCorreoElectronico(), nuevoUsuario.getCedula())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Ya existe un usuario registrado con esa cédula o correo electrónico.");
        }

        try {
            // Guardar el nuevo usuario y manejar la lógica de especialista
            Usuario usuarioGuardado = usuarioService.guardarUsuario(nuevoUsuario);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(usuarioGuardado);

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
     * @return 200 OK con el objeto Usuario si es exitoso, 401 Unauthorized si falla.
     */
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest loginRequest) {

        String correoAcceso = loginRequest.getCorreo();
        String contrasenaAcceso = loginRequest.getContrasenia();

        // Llamar al servicio para validar las credenciales
        Optional<Usuario> usuarioEncontrado = usuarioService.validarUsuarioExiste(correoAcceso, contrasenaAcceso);

        if (usuarioEncontrado.isPresent()) {
            // Si las credenciales son correctas, devuelve el usuario (incluyendo el rol)
            return ResponseEntity.ok(usuarioEncontrado.get());
        } else {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales incorrectas. Verifique su correo y contraseña.");
        }
    }
}

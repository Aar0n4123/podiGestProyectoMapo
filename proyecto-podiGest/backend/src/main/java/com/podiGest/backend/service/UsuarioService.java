package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Usuario;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private List<Usuario> listaUsuarios;
    private List<Usuario> listaEspecialistas;

    private static final String USUARIOS_JSON_FILE = "usuarios.json";
    private static final String ESPECIALISTAS_JSON_FILE = "especialistas.json";

    private final ObjectMapper mapper;

    public UsuarioService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.listaUsuarios = cargarUsuariosDesdeJson(USUARIOS_JSON_FILE);
        this.listaEspecialistas = cargarUsuariosDesdeJson(ESPECIALISTAS_JSON_FILE);
    }

    private List<Usuario> cargarUsuariosDesdeJson(String fileName) {
        Path path = resolveWritablePath(fileName);
        try {
            if (Files.exists(path) && Files.size(path) > 0) {
                return mapper.readValue(path.toFile(), new TypeReference<List<Usuario>>() {});
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo JSON en: " + path);
        }
        List<Usuario> usuarios = cargarDesdeClasspath(fileName);
        if (!usuarios.isEmpty()) {
            try {
                guardarUsuariosAJson(usuarios, fileName);
            } catch (IOException e) {
                System.err.println("ERROR: No se pudo inicializar el archivo JSON en: " + path);
            }
        }
        return usuarios;
    }

    private List<Usuario> cargarDesdeClasspath(String resourceName) {
        try (InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
            return mapper.readValue(inputStream, new TypeReference<List<Usuario>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void guardarUsuariosAJson(List<Usuario> usuarios, String fileName) throws IOException {
        Path path = resolveWritablePath(fileName);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), usuarios);
    }

    private Path resolveWritablePath(String fileName) {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        if (workingDir.getFileName() != null && workingDir.getFileName().toString().equals("backend")) {
            return workingDir.resolve("src").resolve("main").resolve("resources").resolve(fileName);
        }
        Path backendDir = workingDir.resolve("backend");
        if (Files.exists(backendDir)) {
            return backendDir.resolve("src").resolve("main").resolve("resources").resolve(fileName);
        }
        return workingDir.resolve(fileName);
    }


    public boolean existeUsuario(String correo, String cedula) {
        return listaUsuarios.stream()
                .anyMatch(u -> u.getCorreoElectronico() != null && u.getCorreoElectronico().equalsIgnoreCase(correo) ||
                        u.getCedula() != null && u.getCedula().equalsIgnoreCase(cedula));
    }

    public Usuario guardarUsuario(Usuario nuevoUsuario) throws IOException {

        listaUsuarios.add(nuevoUsuario);
        guardarUsuariosAJson(listaUsuarios, USUARIOS_JSON_FILE);

        if ("especialista".equalsIgnoreCase(nuevoUsuario.getRol())) {
            listaEspecialistas.add(nuevoUsuario);
            guardarUsuariosAJson(listaEspecialistas, ESPECIALISTAS_JSON_FILE);
        }

        return nuevoUsuario;
    }


    // =========================================================
    // LÓGICA DE LOGIN
    // =========================================================

    public Optional<Usuario> validarUsuarioExiste(String correo, String contrasena) {
        Optional<Usuario> usuarioEncontrado = listaUsuarios.stream()
                .filter(u -> u.getCorreoElectronico() != null && u.getCorreoElectronico().equalsIgnoreCase(correo))
                .findFirst();

        if (usuarioEncontrado.isPresent()) {
            Usuario usuario = usuarioEncontrado.get();

            if (usuario.getContrasenia() != null && usuario.getContrasenia().equals(contrasena)) {
                return Optional.of(usuario);
            }
        }

        return Optional.empty();
    }
}
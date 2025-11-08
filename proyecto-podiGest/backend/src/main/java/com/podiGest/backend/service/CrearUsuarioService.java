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
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CrearUsuarioService {

    private List<Usuario> listaUsuarios;
    private static final String USUARIOS_JSON_FILE = "usuarios.json";
    private static final String USUARIO_SESION_JSON_FILE = "usuarioInicioSesion.json";
    private final ObjectMapper mapper;

    public CrearUsuarioService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.listaUsuarios = cargarUsuariosDesdeJson(USUARIOS_JSON_FILE);

    }

    //metodo para sbaer cuales son los usuarios registrados en el JSON
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

    //metodo para escribir el JSON y guardar el nuevo usuario
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
            workingDir = workingDir.getParent();
        }
        Path dataDir = workingDir.resolve("base_de_datos");

        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo crear la carpeta 'base_de_datos': " + e.getMessage());
        }
        return dataDir.resolve(fileName);
    }


    // metodo para verificar si el usuario ya existe
    public boolean existeUsuario(String correo, String cedula) {
        return listaUsuarios.stream()
                .anyMatch(u -> u.getCorreoElectronico() != null && u.getCorreoElectronico().equalsIgnoreCase(correo) ||
                        u.getCedula() != null && u.getCedula().equalsIgnoreCase(cedula));
    }

    //metodo para verificar si el usuario es o no mayor de edad
    public boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return false;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }

    // metodo guarda en el archivo JSON
    public Usuario guardarUsuario(Usuario nuevoUsuario) throws IOException {
        if (!esMayorDeEdad(nuevoUsuario.getFechaNacimiento())) {

            throw new IllegalArgumentException("El usuario debe ser mayor de edad (18 años) para registrarse.");
        }
        listaUsuarios.add(nuevoUsuario);
        guardarUsuariosAJson(listaUsuarios, USUARIOS_JSON_FILE);

        return nuevoUsuario;
    }

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

    /**
     * Guarda el usuario que acaba de iniciar sesión en un archivo JSON separado.
     * Este método SOBRESCRIBE el contenido del archivo cada vez que se llama.
     * @param usuario El usuario que ha iniciado sesión.
     */
    public void guardarUsuarioSesion(Usuario usuario) {
        List<Usuario> listaUsuarioSesion = new ArrayList<>();
        listaUsuarioSesion.add(usuario);

        try {

            Path path = resolveWritablePath(USUARIO_SESION_JSON_FILE);

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), listaUsuarioSesion);

            System.out.println("INFO: Usuario de sesión guardado en " + USUARIO_SESION_JSON_FILE);

        } catch (IOException e) {
            System.err.println("ERROR: No se pudo guardar el archivo de sesión del usuario: " + e.getMessage());
        }
    }

}
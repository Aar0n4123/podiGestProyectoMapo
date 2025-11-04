package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Usuario;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private List<Usuario> listaUsuarios;
    private List<Usuario> listaEspecialistas;

    private static final String USUARIOS_JSON_PATH = "src/main/resources/usuarios.json";
    private static final String ESPECIALISTAS_JSON_PATH = "src/main/resources/especialistas.json";

    private final ObjectMapper mapper;

    public UsuarioService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.listaUsuarios = cargarUsuariosDesdeJson(USUARIOS_JSON_PATH);
        this.listaEspecialistas = cargarUsuariosDesdeJson(ESPECIALISTAS_JSON_PATH);
    }

    private List<Usuario> cargarUsuariosDesdeJson(String path) {
        try {
            File file = new File(path);
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            List<Usuario> usuarios = mapper.readValue(file, new TypeReference<List<Usuario>>() {});
            return usuarios;
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo cargar el archivo JSON desde: " + path);
            return new ArrayList<>();
        }
    }

    private void guardarUsuariosAJson(List<Usuario> usuarios, String path) throws IOException {
        File file = new File(path);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, usuarios);
    }


    public boolean existeUsuario(String correo, String cedula) {
        return listaUsuarios.stream()
                .anyMatch(u -> u.getCorreoElectronico() != null && u.getCorreoElectronico().equalsIgnoreCase(correo) ||
                        u.getCedula() != null && u.getCedula().equalsIgnoreCase(cedula));
    }

    public Usuario guardarUsuario(Usuario nuevoUsuario) throws IOException {

        listaUsuarios.add(nuevoUsuario);
        guardarUsuariosAJson(listaUsuarios, USUARIOS_JSON_PATH);

        if ("especialista".equalsIgnoreCase(nuevoUsuario.getRol())) {
            listaEspecialistas.add(nuevoUsuario);
            guardarUsuariosAJson(listaEspecialistas, ESPECIALISTAS_JSON_PATH);
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
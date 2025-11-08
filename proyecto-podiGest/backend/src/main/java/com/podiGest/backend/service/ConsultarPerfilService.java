package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultarPerfilService {

    private final Path sesionPath;
    private final ObjectMapper objectMapper;

    /**
     * Constructor que inyecta la ruta del archivo de usuarioInicioSesion desde application.properties
     */
    public ConsultarPerfilService(@Value("${sesion.file.path}") String sesionPath) {
        this.sesionPath = Paths.get(sesionPath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Metodo para leer los datos del usuario activo desde el archivo JSON
     */
    public Optional<Usuario> obtenerPerfilActivo() throws IOException {

        if (!Files.exists(sesionPath)) {
            return Optional.empty();
        }
        String jsonContent = Files.readString(sesionPath);
        if (jsonContent.isBlank()) {
            return Optional.empty();
        }
        List<Usuario> sesion = objectMapper.readValue(jsonContent, new TypeReference<List<Usuario>>() {});
        if (sesion.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(sesion.get(0));
    }
}
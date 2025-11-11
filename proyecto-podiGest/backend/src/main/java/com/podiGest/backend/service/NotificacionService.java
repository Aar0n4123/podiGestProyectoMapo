package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Notificacion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionService {

    private final Path notificacionesPath;
    private final ObjectMapper objectMapper;
    private static final String NOTIFICACIONES_JSON_FILE = "notificaciones.json";

    public NotificacionService() {
        this.notificacionesPath = PathConfigService.getFilePath(NOTIFICACIONES_JSON_FILE);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Carga las notificaciones iniciales si el archivo no existe
        inicializarNotificaciones();
    }

    /**
     * Inicializa el archivo de notificaciones si no existe o está vacío
     */
    private void inicializarNotificaciones() {
        try {
            if (!Files.exists(notificacionesPath) || Files.size(notificacionesPath) == 0) {
                List<Notificacion> notificacionesIniciales = cargarDesdeClasspath();
                if (!notificacionesIniciales.isEmpty()) {
                    guardarNotificacionesAJson(notificacionesIniciales);
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudieron inicializar las notificaciones: " + e.getMessage());
        }
    }

    /**
     * Carga las notificaciones iniciales desde la carpeta base_de_datos
     */
    private List<Notificacion> cargarDesdeClasspath() {
        try {
            Path seedPath = PathConfigService.getSeedFilePath(NOTIFICACIONES_JSON_FILE);
            if (Files.exists(seedPath)) {
                String jsonContent = Files.readString(seedPath);
                return objectMapper.readValue(jsonContent, new TypeReference<List<Notificacion>>() {});
            }
        } catch (IOException e) {
            System.err.println("ADVERTENCIA: No se encontraron notificaciones iniciales en base_de_datos");
        }
        return new ArrayList<>();
    }

    public List<Notificacion> obtenerNotificaciones() throws IOException {
        if (!Files.exists(notificacionesPath)) {
            return List.of();
        }
        String jsonContent = Files.readString(notificacionesPath);
        if (jsonContent.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(jsonContent, new TypeReference<>() {});
    }

    public Optional<Notificacion> obtenerNotificacionPorId(String id) throws IOException {
        return obtenerNotificaciones()
                .stream()
                .filter(notificacion -> notificacion.getId().equals(id))
                .findFirst();
    }

    /**
     * Guarda las notificaciones en el archivo JSON (sobrescribiendo el contenido anterior)
     * 
     * @param notificaciones Lista de notificaciones a guardar
     * @throws IOException si hay error al escribir el archivo
     */
    private void guardarNotificacionesAJson(List<Notificacion> notificaciones) throws IOException {
        PathConfigService.ensureDataDirectoryExists();
        // Sobrescribe el archivo con los datos actuales (sin duplicar)
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(notificacionesPath.toFile(), notificaciones);
    }
}
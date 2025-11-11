package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.podiGest.backend.model.Cita;
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
public class CitasService {

    private final Path citasPath;
    private final ObjectMapper objectMapper;
    private static final String CITAS_JSON_FILE = "citas.json";

    public CitasService() {
        this.citasPath = PathConfigService.getSeedFilePath(CITAS_JSON_FILE);
        this.objectMapper = new ObjectMapper();
        
        // Inicializa el archivo de citas si no existe
        inicializarCitas();
    }

    /**
     * Inicializa el archivo de citas si no existe o está vacío
     */
    private void inicializarCitas() {
        try {
            if (!Files.exists(citasPath) || Files.size(citasPath) == 0) {
                List<Cita> citasIniciales = cargarDesdeClasspath();
                guardarCitasAJson(citasIniciales);
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudieron inicializar las citas: " + e.getMessage());
        }
    }

    /**
     * Carga las citas iniciales desde la carpeta base_de_datos
     */
    private List<Cita> cargarDesdeClasspath() {
        try {
            Path seedPath = PathConfigService.getSeedFilePath(CITAS_JSON_FILE);
            if (Files.exists(seedPath)) {
                String jsonContent = Files.readString(seedPath);
                return objectMapper.readValue(jsonContent, new TypeReference<List<Cita>>() {});
            }
        } catch (IOException e) {
            System.err.println("ADVERTENCIA: No se encontraron citas iniciales en base_de_datos");
        }
        return new ArrayList<>();
    }

    public List<Cita> obtenerCitas() throws IOException {
        if (!Files.exists(citasPath)) {
            return new ArrayList<>();
        }
        String jsonContent = Files.readString(citasPath);
        if (jsonContent.isBlank()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(jsonContent, new TypeReference<List<Cita>>() {});
    }

    public Optional<Cita> obtenerCitaPorId(String id) throws IOException {
        return obtenerCitas()
                .stream()
                .filter(cita -> cita.getId().equals(id))
                .findFirst();
    }

    public Cita guardarCita(Cita nuevaCita) throws IOException {
        List<Cita> citas = obtenerCitas();
        citas.add(nuevaCita);
        guardarCitasAJson(citas);
        return nuevaCita;
    }

    public void guardarCitasAJson(List<Cita> citas) throws IOException {
        // Sobrescribe el archivo con los datos actuales en base_de_datos/citas.json
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(citasPath.toFile(), citas);
    }

    public boolean cancelarCita(String citaId) throws IOException {
        List<Cita> citas = obtenerCitas();
        boolean removed = citas.removeIf(cita -> cita.getId().equals(citaId));
        if (removed) {
            guardarCitasAJson(citas);
        }
        return removed;
    }

    public List<Cita> obtenerCitasPorPaciente(String correoElectronico) throws IOException {
        return obtenerCitas()
                .stream()
                .filter(cita -> cita.getPacienteCorreo().equals(correoElectronico))
                .toList();
    }

    public List<Cita> obtenerCitasPorEspecialista(String especialista) throws IOException {
        return obtenerCitas()
                .stream()
                .filter(cita -> cita.getEspecialista().equals(especialista))
                .toList();
    }
}


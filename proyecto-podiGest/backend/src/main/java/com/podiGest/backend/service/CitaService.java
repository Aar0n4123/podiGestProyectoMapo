package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Cita;
import com.podiGest.backend.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private List<Cita> listaCitas;
    private static final String CITAS_JSON_FILE = "cita.json";
    private final ObjectMapper mapper;

    @Autowired
    private ConsultarPerfilService perfilService;

    // --- Lógica de Carga/Guardado (Adaptada de CrearUsuarioService) ---

    public CitaService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.listaCitas = cargarCitasDesdeJson(CITAS_JSON_FILE);
        System.out.println("CitaService inicializado. Citas cargadas: " + listaCitas.size());
    }

    private List<Cita> cargarCitasDesdeJson(String fileName) {
        Path path = resolveWritablePath(fileName);
        System.out.println("Intentando cargar citas desde: " + path.toAbsolutePath());
        try {
            if (Files.exists(path) && Files.size(path) > 0) {
                System.out.println("Archivo encontrado. Leyendo...");
                return mapper.readValue(path.toFile(), new TypeReference<List<Cita>>() {
                });
            } else {
                System.out.println("Archivo no existe o está vacío.");
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo JSON en: " + path);
            e.printStackTrace();
        }
        List<Cita> citas = cargarDesdeClasspath(fileName);
        if (!citas.isEmpty()) {
            try {
                guardarCitasAJson(citas, fileName);
            } catch (IOException e) {
                System.err.println("ERROR: No se pudo inicializar el archivo JSON en: " + path);
                e.printStackTrace();
            }
        }
        return citas;
    }

    private List<Cita> cargarDesdeClasspath(String resourceName) {
        try (InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
            return mapper.readValue(inputStream, new TypeReference<List<Cita>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void guardarCitasAJson(List<Cita> citas, String fileName) throws IOException {
        Path path = resolveWritablePath(fileName);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), citas);
        System.out.println("Citas guardadas en: " + path.toAbsolutePath());
    }

    private Path resolveWritablePath(String fileName) {
        // (Este método es idéntico al de CrearUsuarioService)
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

    // --- Lógica de Negocio (Lo que implementa el Sprint 1) ---

    // (Añadiremos los métodos para HU 1.1.3, 1.1.4, 1.1.2 aquí)

    //* Metodo para obtener todas las citas (para pruebas)
    public List<Cita> obtenerTodasLasCitas() {
        return listaCitas;
    }

    // Metodo para obtener citas de un especialista en un día específico
    public List<Cita> obtenerCitasPorEspecialistaYFecha(String especialistaCedula, LocalDate fecha) {
        return listaCitas.stream()
                .filter(cita -> cita.getEspecialistaCedula().equals(especialistaCedula) &&
                        cita.getFechaHoraInicio().toLocalDate().equals(fecha))
                .collect(Collectors.toList());
    }


    private static final LocalTime HORA_INICIO_CLINICA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_CLINICA = LocalTime.of(20, 0);
    private static final int DURACION_CITA_MINUTOS = 60;


    /**
     * HU 1.1.3: Consultar citas disponibles
     * Genera los slots de 1 hora (de 8am a 7pm) y filtra los que ya están ocupados.
     */
    public List<LocalTime> obtenerHorariosDisponibles(String especialistaCedula, LocalDate fecha) {

        // 1. Obtener todas las citas YA AGENDADAS para ese especialista ese día
        List<Cita> citasExistentes = obtenerCitasPorEspecialistaYFecha(especialistaCedula, fecha);

        // 2. Extraer las horas de inicio de esas citas
        List<LocalTime> horasOcupadas = citasExistentes.stream()
                .map(cita -> cita.getFechaHoraInicio().toLocalTime())
                .collect(Collectors.toList());

        // 3. Generar todos los slots posibles del día
        List<LocalTime> todosLosSlots = new ArrayList<>();
        LocalTime slotActual = HORA_INICIO_CLINICA;

        // Genera slots de 8:00, 9:00, ... hasta las 19:00 (la última cita)
        while (slotActual.isBefore(HORA_FIN_CLINICA)) {
            todosLosSlots.add(slotActual);
            slotActual = slotActual.plusMinutes(DURACION_CITA_MINUTOS);
        }

        // 4. Filtrar: Devolver solo los slots que NO estén en la lista de horasOcupadas
        return todosLosSlots.stream()
                .filter(slot -> !horasOcupadas.contains(slot))
                .collect(Collectors.toList());
    }

    public boolean estaDisponible(String especialistaCedula, LocalDateTime fechaHoraInicio) {
        return listaCitas.stream()
                .noneMatch(cita -> cita.getEspecialistaCedula().equals(especialistaCedula) &&
                        cita.getFechaHoraInicio().equals(fechaHoraInicio) &&
                        !cita.getEstado().equals("CANCELADA"));
    }

    private void validarHoraEnPunto(LocalDateTime fechaHora) throws IllegalArgumentException {
        if (fechaHora.getMinute() != 0 || fechaHora.getSecond() != 0) {
            throw new IllegalArgumentException("La cita debe ser programada en horas exactas (minutos y segundos en 00)");
        }
    }

    private void validarHorariosClinica(LocalDateTime fechaHora) throws IllegalArgumentException {
        LocalTime hora = fechaHora.toLocalTime();
        if (hora.isBefore(HORA_INICIO_CLINICA) || hora.isAfter(HORA_FIN_CLINICA) || hora.equals(HORA_FIN_CLINICA)) {
            throw new IllegalArgumentException("El horario debe estar entre " + HORA_INICIO_CLINICA + " y " + HORA_FIN_CLINICA.minusMinutes(1));
        }
    }

    public Cita agendarCita(String pacienteCedula, String especialistaCedula, LocalDateTime fechaHoraInicio, String descripcion) throws IOException {
        validarHoraEnPunto(fechaHoraInicio);
        validarHorariosClinica(fechaHoraInicio);

        if (!estaDisponible(especialistaCedula, fechaHoraInicio)) {
            throw new IllegalArgumentException("El horario " + fechaHoraInicio.toLocalTime() + " no está disponible para el especialista");
        }

        System.out.println("Agendando cita para: " + pacienteCedula + " con " + especialistaCedula + " en " + fechaHoraInicio);
        Cita nuevaCita = new Cita(
                UUID.randomUUID().toString(),
                pacienteCedula,
                especialistaCedula,
                fechaHoraInicio,
                descripcion
        );
        System.out.println("Nueva cita creada con ID: " + nuevaCita.getId());
        listaCitas.add(nuevaCita);
        System.out.println("Total de citas en memoria: " + listaCitas.size());
        guardarCitasAJson(listaCitas, CITAS_JSON_FILE);
        return nuevaCita;
    }

    public Cita agendarCitaConUsuarioAutenticado(String especialistaCedula, LocalDateTime fechaHoraInicio, String descripcion) throws IOException {
        Optional<Usuario> usuarioAutenticado = perfilService.obtenerPerfilActivo();
        if (!usuarioAutenticado.isPresent()) {
            throw new IllegalArgumentException("No hay usuario autenticado");
        }
        return agendarCita(usuarioAutenticado.get().getCedula(), especialistaCedula, fechaHoraInicio, descripcion);
    }
}
package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.podiGest.backend.model.Cita;
import com.podiGest.backend.model.Notificacion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CitasService {

    private final Path citasPath;
    private final ObjectMapper objectMapper;
    private final NotificacionService notificacionService;
    private static final String CITAS_JSON_FILE = "citas.json";

    public CitasService(NotificacionService notificacionService) {
        this.citasPath = PathConfigService.getSeedFilePath(CITAS_JSON_FILE);
        this.objectMapper = new ObjectMapper();
        this.notificacionService = notificacionService;
        
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
        
        // Generar notificación automáticamente al agendar la cita
        generarNotificacionCita(nuevaCita);
        
        return nuevaCita;
    }

    /**
     * Genera una notificación automática cuando se agenda una cita
     * 
     * @param cita La cita que se acaba de agendar
     */
    private void generarNotificacionCita(Cita cita) {
        try {
            System.out.println("INFO: Iniciando generación de notificación para la cita: " + cita.getId());
            System.out.println("INFO: Correo del paciente: " + cita.getPacienteCorreo());
            
            // Generar ID único para la notificación
            String notificacionId = "NOTIF-" + System.currentTimeMillis() + "-" + cita.getId().substring(5);
            
            // Obtener fecha y hora actual para la notificación
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String fechaEnvio = ahora.format(formatter);
            
            // Crear el asunto de la notificación
            String asunto = "Confirmación de cita agendada";
            
            // Crear el mensaje de la notificación con los detalles de la cita
            String mensaje = String.format(
                "Estimado/a %s,\n\n" +
                "Su cita ha sido agendada exitosamente con los siguientes detalles:\n\n" +
                "Especialista: %s\n" +
                "Fecha: %s\n" +
                "Hora: %s\n" +
                "Motivo: %s\n\n" +
                "Por favor, llegue 10 minutos antes de su cita.\n" +
                "Si necesita cancelar o reprogramar, comuníquese con nosotros con anticipación.\n\n" +
                "Gracias por confiar en nuestros servicios.",
                cita.getPacienteNombre(),
                cita.getEspecialista(),
                cita.getFecha(),
                cita.getHora(),
                cita.getRazonConsulta()
            );
            
            // Crear la notificación vinculada al correo del paciente
            Notificacion notificacion = new Notificacion(
                notificacionId,
                fechaEnvio,
                asunto,
                "Clínica Podológica",
                mensaje,
                cita.getPacienteCorreo() // Vincula la notificación al correo del paciente
            );
            
            System.out.println("INFO: Notificación creada con ID: " + notificacionId);
            System.out.println("INFO: Correo destinatario: " + notificacion.getCorreoDestinatario());
            
            // Guardar la notificación
            notificacionService.crearNotificacion(notificacion);
            
            System.out.println("INFO: Notificación guardada exitosamente para la cita: " + cita.getId());
        } catch (IOException e) {
            System.err.println("ERROR: Error al generar notificación para la cita " + cita.getId() + ": " + e.getMessage());
            e.printStackTrace();
            // No lanzamos la excepción para no interrumpir el proceso de creación de la cita
        }
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


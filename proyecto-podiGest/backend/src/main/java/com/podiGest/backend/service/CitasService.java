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
    private final PerfilService perfilService;
    private static final String CITAS_JSON_FILE = "citas.json";

    public CitasService(NotificacionService notificacionService, PerfilService perfilService) {
        this.citasPath = PathConfigService.getSeedFilePath(CITAS_JSON_FILE);
        this.objectMapper = new ObjectMapper();
        this.notificacionService = notificacionService;
        this.perfilService = perfilService;
        
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
        
        boolean citaExistente = citas.stream()
            .anyMatch(cita -> 
                cita.getFecha().equals(nuevaCita.getFecha()) &&
                cita.getHora().equals(nuevaCita.getHora()) &&
                cita.getEspecialista().equals(nuevaCita.getEspecialista()) &&
                !cita.getEstado().equals("cancelada")
            );
        
        if (citaExistente) {
            throw new IOException("Ya existe una cita agendada para este especialista en la fecha y hora seleccionadas");
        }
        
        citas.add(nuevaCita);
        guardarCitasAJson(citas);
        
        // Generar notificación automáticamente al agendar la cita (para el paciente)
        generarNotificacionCita(nuevaCita);
        
        // Generar notificación para el especialista
        generarNotificacionCitaParaEspecialista(nuevaCita);
        
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
            

            String notificacionId = "NOTIF-" + System.currentTimeMillis() + "-" + cita.getId().substring(5);
            

            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String fechaEnvio = ahora.format(formatter);
            

            String asunto = "Confirmación de cita agendada";
            

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
            

            Notificacion notificacion = new Notificacion(
                notificacionId,
                fechaEnvio,
                asunto,
                "Clínica Podológica",
                mensaje,
                cita.getPacienteCorreo()
            );
            
            System.out.println("INFO: Notificación creada con ID: " + notificacionId);
            System.out.println("INFO: Correo destinatario: " + notificacion.getCorreoDestinatario());
            

            notificacionService.crearNotificacion(notificacion);
            
            System.out.println("INFO: Notificación guardada exitosamente para la cita: " + cita.getId());
        } catch (IOException e) {
            System.err.println("ERROR: Error al generar notificación para la cita " + cita.getId() + ": " + e.getMessage());
            e.printStackTrace();
            // No lanzamos la excepción para no interrumpir el proceso de creación de la cita
        }
    }

    /**
     * Genera una notificación automática para el especialista cuando se agenda una cita con él
     * 
     * @param cita La cita que se acaba de agendar
     */
    private void generarNotificacionCitaParaEspecialista(Cita cita) {
        try {
            System.out.println("INFO: Iniciando generación de notificación para el especialista: " + cita.getEspecialista());
            
            // Obtener el correo del especialista
            Optional<String> correoEspecialista = perfilService.obtenerCorreoEspecialistaPorNombre(cita.getEspecialista());
            
            if (correoEspecialista.isEmpty()) {
                System.err.println("ADVERTENCIA: No se encontró el correo del especialista: " + cita.getEspecialista());
                return;
            }
            
            System.out.println("INFO: Correo del especialista: " + correoEspecialista.get());
            
            // Generar ID único para la notificación
            String notificacionId = "NOTIF-ESP-" + System.currentTimeMillis() + "-" + cita.getId().substring(5);
            
            // Obtener fecha y hora actual
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String fechaEnvio = ahora.format(formatter);
            
            // Asunto de la notificación
            String asunto = "Nueva cita agendada - " + cita.getPacienteNombre();
            
            // Mensaje de la notificación
            String mensaje = String.format(
                "Estimado/a Dr./Dra. %s,\n\n" +
                "Se ha agendado una nueva cita con usted:\n\n" +
                "Paciente: %s\n" +
                "Fecha: %s\n" +
                "Hora: %s\n" +
                "Motivo de consulta: %s\n" +
                "Teléfono del paciente: %s\n\n" +
                "Por favor, revise su agenda y prepárese para la consulta.\n\n" +
                "Saludos cordiales,\n" +
                "Sistema de Gestión de Citas",
                cita.getEspecialista(),
                cita.getPacienteNombre(),
                cita.getFecha(),
                cita.getHora(),
                cita.getRazonConsulta(),
                cita.getPacienteTelefono()
            );
            
            // Crear la notificación
            Notificacion notificacion = new Notificacion(
                notificacionId,
                fechaEnvio,
                asunto,
                "Sistema de Gestión de Citas",
                mensaje,
                correoEspecialista.get()
            );
            
            System.out.println("INFO: Notificación para especialista creada con ID: " + notificacionId);
            System.out.println("INFO: Correo destinatario: " + notificacion.getCorreoDestinatario());
            
            // Guardar la notificación
            notificacionService.crearNotificacion(notificacion);
            
            System.out.println("INFO: Notificación para especialista guardada exitosamente para la cita: " + cita.getId());
        } catch (IOException e) {
            System.err.println("ERROR: Error al generar notificación para el especialista " + cita.getEspecialista() + ": " + e.getMessage());
            e.printStackTrace();
            // No lanzamos la excepción para no interrumpir el proceso de creación de la cita
        }
    }

    /**
     * Genera una notificación automática cuando se modifica una cita
     * 
     * @param cita La cita que se acaba de modificar
     * @param fechaAnterior La fecha anterior de la cita
     * @param horaAnterior La hora anterior de la cita
     */
    private void generarNotificacionModificacionCita(Cita cita, String fechaAnterior, String horaAnterior) {
        try {
            System.out.println("INFO: Iniciando generación de notificación de modificación para la cita: " + cita.getId());
            System.out.println("INFO: Correo del paciente: " + cita.getPacienteCorreo());
            

            String notificacionId = "NOTIF-MOD-" + System.currentTimeMillis() + "-" + cita.getId().substring(5);
            

            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String fechaEnvio = ahora.format(formatter);
            

            String asunto = "Modificación de cita - Información actualizada";
            

            String mensaje = String.format(
                "Estimado/a %s,\n\n" +
                "Le informamos que su cita ha sido modificada.\n\n" +
                "INFORMACIÓN ANTERIOR:\n" +
                "Fecha: %s\n" +
                "Hora: %s\n\n" +
                "NUEVA INFORMACIÓN:\n" +
                "Especialista: %s\n" +
                "Fecha: %s\n" +
                "Hora: %s\n" +
                "Motivo: %s\n\n" +
                "Por favor, tome nota de los nuevos datos y llegue 10 minutos antes de su cita.\n" +
                "Si tiene alguna duda o necesita realizar cambios adicionales, comuníquese con nosotros.\n\n" +
                "Gracias por su comprensión.",
                cita.getPacienteNombre(),
                fechaAnterior,
                horaAnterior,
                cita.getEspecialista(),
                cita.getFecha(),
                cita.getHora(),
                cita.getRazonConsulta()
            );
            

            Notificacion notificacion = new Notificacion(
                notificacionId,
                fechaEnvio,
                asunto,
                "Clínica Podológica",
                mensaje,
                cita.getPacienteCorreo()
            );
            
            System.out.println("INFO: Notificación de modificación creada con ID: " + notificacionId);
            System.out.println("INFO: Correo destinatario: " + notificacion.getCorreoDestinatario());
            

            notificacionService.crearNotificacion(notificacion);
            
            System.out.println("INFO: Notificación de modificación guardada exitosamente para la cita: " + cita.getId());
        } catch (IOException e) {
            System.err.println("ERROR: Error al generar notificación de modificación para la cita " + cita.getId() + ": " + e.getMessage());
            e.printStackTrace();
            // No lanzamos la excepción para no interrumpir el proceso de modificación de la cita
        }
    }

    /**
     * Genera una notificación automática para el especialista cuando se modifica una cita
     * 
     * @param cita La cita que se acaba de modificar
     * @param fechaAnterior La fecha anterior de la cita
     * @param horaAnterior La hora anterior de la cita
     */
    private void generarNotificacionModificacionCitaParaEspecialista(Cita cita, String fechaAnterior, String horaAnterior) {
        try {
            System.out.println("INFO: Iniciando generación de notificación de modificación para el especialista: " + cita.getEspecialista());
            
            // Obtener el correo del especialista
            Optional<String> correoEspecialista = perfilService.obtenerCorreoEspecialistaPorNombre(cita.getEspecialista());
            
            if (correoEspecialista.isEmpty()) {
                System.err.println("ADVERTENCIA: No se encontró el correo del especialista: " + cita.getEspecialista());
                return;
            }
            
            System.out.println("INFO: Correo del especialista: " + correoEspecialista.get());
            
            // Generar ID único para la notificación
            String notificacionId = "NOTIF-ESP-MOD-" + System.currentTimeMillis() + "-" + cita.getId().substring(5);
            
            // Obtener fecha y hora actual
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String fechaEnvio = ahora.format(formatter);
            
            // Asunto de la notificación
            String asunto = "Modificación de cita - " + cita.getPacienteNombre();
            
            // Mensaje de la notificación
            String mensaje = String.format(
                "Estimado/a Dr./Dra. %s,\n\n" +
                "Se ha modificado una cita en su agenda:\n\n" +
                "INFORMACIÓN ANTERIOR:\n" +
                "Fecha: %s\n" +
                "Hora: %s\n\n" +
                "NUEVA INFORMACIÓN:\n" +
                "Paciente: %s\n" +
                "Fecha: %s\n" +
                "Hora: %s\n" +
                "Motivo de consulta: %s\n" +
                "Teléfono del paciente: %s\n\n" +
                "Por favor, tome nota de los cambios en su agenda.\n\n" +
                "Saludos cordiales,\n" +
                "Sistema de Gestión de Citas",
                cita.getEspecialista(),
                fechaAnterior,
                horaAnterior,
                cita.getPacienteNombre(),
                cita.getFecha(),
                cita.getHora(),
                cita.getRazonConsulta(),
                cita.getPacienteTelefono()
            );
            
            // Crear la notificación
            Notificacion notificacion = new Notificacion(
                notificacionId,
                fechaEnvio,
                asunto,
                "Sistema de Gestión de Citas",
                mensaje,
                correoEspecialista.get()
            );
            
            System.out.println("INFO: Notificación de modificación para especialista creada con ID: " + notificacionId);
            System.out.println("INFO: Correo destinatario: " + notificacion.getCorreoDestinatario());
            
            // Guardar la notificación
            notificacionService.crearNotificacion(notificacion);
            
            System.out.println("INFO: Notificación de modificación para especialista guardada exitosamente para la cita: " + cita.getId());
        } catch (IOException e) {
            System.err.println("ERROR: Error al generar notificación de modificación para el especialista " + cita.getEspecialista() + ": " + e.getMessage());
            e.printStackTrace();
            // No lanzamos la excepción para no interrumpir el proceso de modificación de la cita
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



    /**
     * Obtiene una cita específica por ID y la modifica solo en fecha y hora,
     * verificando antes la disponibilidad del nuevo horario.
     *
     * @param citaId ID de la cita a modificar.
     * @param citaActualizada Objeto Cita con la nueva fecha y hora.
     * @return La cita actualizada.
     * @throws IOException Si ocurre un error de lectura/escritura del archivo.
     * @throws IllegalStateException Si el nuevo horario choca con otra cita.
     */
    public Cita modificarCitaCompleta(String citaId, Cita citaActualizada) throws IOException, IllegalStateException {
        List<Cita> citas = obtenerCitas();

        for (int i = 0; i < citas.size(); i++) {
            if (citas.get(i).getId().equals(citaId)) {
                Cita citaOriginal = citas.get(i);

                // Guardar los valores anteriores para la notificación
                String fechaAnterior = citaOriginal.getFecha();
                String horaAnterior = citaOriginal.getHora();

                String especialistaNombre = citaOriginal.getEspecialista();
                String nuevaFecha = citaActualizada.getFecha();
                String nuevaHora = citaActualizada.getHora();


                if (!isHorarioDisponible(especialistaNombre, nuevaFecha, nuevaHora, citaId)) {

                    throw new IllegalStateException("El horario solicitado ya está ocupado por otra cita.");
                }


                citaOriginal.setFecha(nuevaFecha);
                citaOriginal.setHora(nuevaHora);


                guardarCitasAJson(citas);
                
                // Generar notificación de modificación para el paciente
                generarNotificacionModificacionCita(citaOriginal, fechaAnterior, horaAnterior);
                
                // Generar notificación de modificación para el especialista
                generarNotificacionModificacionCitaParaEspecialista(citaOriginal, fechaAnterior, horaAnterior);
                
                return citaOriginal;
            }
        }
        return null;
    }

    /**
     * Revisa si un horario específico ya está ocupado por un especialista,
     * excluyendo una cita específica (la que se está modificando).
     *
     * @param especialistaNombre El nombre del especialista.
     * @param nuevaFecha         La nueva fecha a verificar.
     * @param nuevaHora          La nueva hora a verificar.
     * @param citaIdExcluir      El ID de la cita que estamos intentando modificar (para no chocar con ella misma).
     * @return true si el horario está disponible, false si está ocupado.
     */
    public boolean isHorarioDisponible(String especialistaNombre, String nuevaFecha, String nuevaHora, String citaIdExcluir) throws IOException {
        List<Cita> citas = obtenerCitas();

        Optional<Cita> citaOcupada = citas.stream()
                .filter(cita ->
                        !cita.getId().equals(citaIdExcluir) &&
                                cita.getEspecialista().equals(especialistaNombre) &&
                                cita.getFecha().equals(nuevaFecha) &&
                                cita.getHora().equals(nuevaHora) &&
                                !cita.getEstado().equals("cancelada")
                )
                .findFirst();

        return citaOcupada.isEmpty();
    }


}


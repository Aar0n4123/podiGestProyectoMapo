package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Notificacion;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificacionService {

    private final Path notificacionesPath;
    private final ObjectMapper objectMapper;
    private static final String NOTIFICACIONES_JSON_FILE = "notificaciones.json";

    public NotificacionService() {
        this.notificacionesPath = PathConfigService.getSeedFilePath(NOTIFICACIONES_JSON_FILE);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        inicializarNotificaciones();
    }

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

    private void guardarNotificacionesAJson(List<Notificacion> notificaciones) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(notificacionesPath.toFile(), notificaciones);
    }

    public Notificacion crearNotificacion(Notificacion notificacion) throws IOException {
        System.out.println("INFO: Creando notificación con ID: " + notificacion.getId());
        System.out.println("INFO: Ruta del archivo: " + notificacionesPath.toAbsolutePath());
        
        List<Notificacion> notificaciones = new ArrayList<>(obtenerNotificaciones());
        System.out.println("INFO: Notificaciones existentes antes de agregar: " + notificaciones.size());
        
        notificaciones.add(notificacion);
        System.out.println("INFO: Notificaciones después de agregar: " + notificaciones.size());
        
        guardarNotificacionesAJson(notificaciones);
        System.out.println("INFO: Notificación guardada exitosamente en: " + notificacionesPath.toAbsolutePath());
        
        return notificacion;
    }

    public List<Notificacion> obtenerNotificacionesPorUsuario(String correoUsuario) throws IOException {
        List<Notificacion> todasLasNotificaciones = obtenerNotificaciones();
        System.out.println("INFO: Total de notificaciones en el archivo: " + todasLasNotificaciones.size());
        
        List<Notificacion> notificacionesFiltradas = todasLasNotificaciones
                .stream()
                .filter(notificacion -> {
                    boolean coincide = notificacion.getCorreoDestinatario() != null 
                            && notificacion.getCorreoDestinatario().equalsIgnoreCase(correoUsuario);
                    if (coincide) {
                        System.out.println("INFO: Notificación " + notificacion.getId() + " coincide con el usuario " + correoUsuario);
                    }
                    return coincide;
                })
                .toList();
        
        System.out.println("INFO: Notificaciones filtradas para " + correoUsuario + ": " + notificacionesFiltradas.size());
        return notificacionesFiltradas;
    }

    public boolean silenciarNotificacion(String id) throws IOException {
        System.out.println("INFO: Intentando silenciar notificación con ID: " + id);
        
        List<Notificacion> notificaciones = new ArrayList<>(obtenerNotificaciones());
        boolean encontrada = false;
        
        for (Notificacion notificacion : notificaciones) {
            if (notificacion.getId().equals(id)) {
                notificacion.setSilenciada(true);
                encontrada = true;
                System.out.println("INFO: Notificación " + id + " marcada como silenciada");
                break;
            }
        }
        
        if (encontrada) {
            guardarNotificacionesAJson(notificaciones);
            System.out.println("INFO: Cambios guardados exitosamente");
        } else {
            System.out.println("ADVERTENCIA: No se encontró la notificación con ID: " + id);
        }
        
        return encontrada;
    }

    public boolean dessilenciarNotificacion(String id) throws IOException {
        System.out.println("INFO: Intentando dessilenciar notificación con ID: " + id);
        
        List<Notificacion> notificaciones = new ArrayList<>(obtenerNotificaciones());
        boolean encontrada = false;
        
        for (Notificacion notificacion : notificaciones) {
            if (notificacion.getId().equals(id)) {
                notificacion.setSilenciada(false);
                encontrada = true;
                System.out.println("INFO: Notificación " + id + " marcada como NO silenciada");
                break;
            }
        }
        
        if (encontrada) {
            guardarNotificacionesAJson(notificaciones);
            System.out.println("INFO: Cambios guardados exitosamente");
        } else {
            System.out.println("ADVERTENCIA: No se encontró la notificación con ID: " + id);
        }
        
        return encontrada;
    }

    public long contarNotificacionesNoSilenciadas(String correoUsuario) throws IOException {
        return obtenerNotificacionesPorUsuario(correoUsuario)
                .stream()
                .filter(notificacion -> !notificacion.isSilenciada())
                .count();
    }

    public boolean eliminarTodasNotificacionesPorUsuario(String correoUsuario) throws IOException {
        System.out.println("INFO: Eliminando todas las notificaciones para el usuario: " + correoUsuario);
        
        List<Notificacion> todasLasNotificaciones = new ArrayList<>(obtenerNotificaciones());
        int notificacionesAntesDeEliminar = todasLasNotificaciones.size();
        
        List<Notificacion> notificacionesFiltradas = todasLasNotificaciones
                .stream()
                .filter(notificacion -> notificacion.getCorreoDestinatario() == null 
                        || !notificacion.getCorreoDestinatario().equalsIgnoreCase(correoUsuario))
                .toList();
        
        int notificacionesEliminadas = notificacionesAntesDeEliminar - notificacionesFiltradas.size();
        
        if (notificacionesEliminadas > 0) {
            guardarNotificacionesAJson(new ArrayList<>(notificacionesFiltradas));
            System.out.println("INFO: Se eliminaron " + notificacionesEliminadas + " notificaciones del usuario " + correoUsuario);
            return true;
        } else {
            System.out.println("ADVERTENCIA: No hay notificaciones para eliminar del usuario: " + correoUsuario);
            return false;
        }
    }

    public boolean establecerRecordatorio(String id, String fechaRecordatorio) throws IOException {
        System.out.println("INFO: Estableciendo recordatorio para notificación con ID: " + id + " en la fecha: " + fechaRecordatorio);
        
        List<Notificacion> notificaciones = new ArrayList<>(obtenerNotificaciones());
        boolean encontrada = false;
        
        for (Notificacion notificacion : notificaciones) {
            if (notificacion.getId().equals(id)) {
                notificacion.setTieneRecordatorio(true);
                notificacion.setFechaRecordatorio(fechaRecordatorio);
                notificacion.setRecordatorioActivo(true);
                encontrada = true;
                System.out.println("INFO: Recordatorio establecido para la notificación " + id);
                break;
            }
        }
        
        if (encontrada) {
            guardarNotificacionesAJson(notificaciones);
            System.out.println("INFO: Cambios guardados exitosamente");
        } else {
            System.out.println("ADVERTENCIA: No se encontró la notificación con ID: " + id);
        }
        
        return encontrada;
    }

    public boolean actualizarRecordatorio(String id, String nuevaFechaRecordatorio) throws IOException {
        System.out.println("INFO: Actualizando recordatorio para notificación con ID: " + id + " a la fecha: " + nuevaFechaRecordatorio);
        
        List<Notificacion> notificaciones = new ArrayList<>(obtenerNotificaciones());
        boolean encontrada = false;
        
        for (Notificacion notificacion : notificaciones) {
            if (notificacion.getId().equals(id)) {
                if (notificacion.isTieneRecordatorio()) {
                    notificacion.setFechaRecordatorio(nuevaFechaRecordatorio);
                    notificacion.setRecordatorioActivo(true);
                    encontrada = true;
                    System.out.println("INFO: Recordatorio actualizado para la notificación " + id);
                } else {
                    System.out.println("ADVERTENCIA: La notificación " + id + " no tiene recordatorio establecido");
                }
                break;
            }
        }
        
        if (encontrada) {
            guardarNotificacionesAJson(notificaciones);
            System.out.println("INFO: Cambios guardados exitosamente");
        }
        
        return encontrada;
    }

    public boolean desactivarRecordatorio(String id) throws IOException {
        System.out.println("INFO: Desactivando recordatorio para notificación con ID: " + id);
        
        List<Notificacion> notificaciones = new ArrayList<>(obtenerNotificaciones());
        boolean encontrada = false;
        
        for (Notificacion notificacion : notificaciones) {
            if (notificacion.getId().equals(id)) {
                notificacion.setRecordatorioActivo(false);
                encontrada = true;
                System.out.println("INFO: Recordatorio desactivado para la notificación " + id);
                break;
            }
        }
        
        if (encontrada) {
            guardarNotificacionesAJson(notificaciones);
            System.out.println("INFO: Cambios guardados exitosamente");
        } else {
            System.out.println("ADVERTENCIA: No se encontró la notificación con ID: " + id);
        }
        
        return encontrada;
    }

    public List<Notificacion> obtenerNotificacionesConRecordatorioPendiente() throws IOException {
        List<Notificacion> todasLasNotificaciones = obtenerNotificaciones();
        
        return todasLasNotificaciones
                .stream()
                .filter(notificacion -> notificacion.isTieneRecordatorio() && notificacion.isRecordatorioActivo())
                .toList();
    }

    private LocalDateTime parsearFecha(String fechaString) throws Exception {
        if (fechaString == null || fechaString.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha no puede estar vacía");
        }
        
        try {
            return LocalDateTime.parse(fechaString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(fechaString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e2) {
                try {
                    return LocalDateTime.parse(fechaString + ":00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e3) {
                    throw new IllegalArgumentException("Formato de fecha no válido: " + fechaString);
                }
            }
        }
    }

    public void procesarRecordatoriosPendientes() throws IOException {
        try {
            System.out.println("INFO: [SCHEDULER] Verificando recordatorios pendientes...");
            
            List<Notificacion> notificaciones = new ArrayList<>(obtenerNotificaciones());
            List<Notificacion> nuevosRecordatorios = new ArrayList<>();
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            boolean cambiosRealizados = false;
            
            System.out.println("INFO: [SCHEDULER] Hora actual: " + ahora.format(formatterOutput));
            System.out.println("INFO: [SCHEDULER] Total de notificaciones a verificar: " + notificaciones.size());
            
            for (int i = 0; i < notificaciones.size(); i++) {
                Notificacion notificacion = notificaciones.get(i);
                
                if (notificacion != null && notificacion.isTieneRecordatorio() && notificacion.isRecordatorioActivo()) {
                    System.out.println("INFO: [SCHEDULER] Notificación con recordatorio activo encontrada: " + notificacion.getId());
                    System.out.println("INFO: [SCHEDULER] Fecha del recordatorio: " + notificacion.getFechaRecordatorio());
                    
                    try {
                        LocalDateTime fechaRecordatorio = parsearFecha(notificacion.getFechaRecordatorio());
                        System.out.println("INFO: [SCHEDULER] Fecha parseada: " + fechaRecordatorio.format(formatterOutput));
                        System.out.println("INFO: [SCHEDULER] ¿Ha llegado la fecha? " + (ahora.isAfter(fechaRecordatorio) || ahora.isEqual(fechaRecordatorio)));
                        
                        if (ahora.isAfter(fechaRecordatorio) || ahora.isEqual(fechaRecordatorio)) {
                            System.out.println("INFO: [SCHEDULER] ¡¡¡ LA FECHA DEL RECORDATORIO HA LLEGADO para la notificación " + notificacion.getId() + " !!!");
                            
                            Notificacion recordatorio = new Notificacion();
                            recordatorio.setId("REMINDER-" + UUID.randomUUID().toString());
                            recordatorio.setFechaEnvio(ahora.format(formatterOutput));
                            recordatorio.setAsunto("[RECORDATORIO] " + (notificacion.getAsunto() != null ? notificacion.getAsunto() : "Sin asunto"));
                            recordatorio.setRemitente(notificacion.getRemitente() != null ? notificacion.getRemitente() : "Sistema");
                            recordatorio.setMensaje("RECORDATORIO: " + (notificacion.getMensaje() != null ? notificacion.getMensaje() : ""));
                            recordatorio.setCorreoDestinatario(notificacion.getCorreoDestinatario());
                            recordatorio.setSilenciada(false);
                            recordatorio.setTieneRecordatorio(false);
                            recordatorio.setRecordatorioActivo(false);
                            
                            nuevosRecordatorios.add(recordatorio);
                            System.out.println("INFO: [SCHEDULER] Nueva notificación recordatorio creada con ID: " + recordatorio.getId());
                            
                            notificacion.setRecordatorioActivo(false);
                            System.out.println("INFO: [SCHEDULER] Recordatorio de la notificación " + notificacion.getId() + " desactivado");
                            
                            cambiosRealizados = true;
                        }
                    } catch (Exception e) {
                        System.err.println("ERROR: [SCHEDULER] No se pudo procesar recordatorio para notificación " + notificacion.getId() + ": " + e.getMessage());
                    }
                }
            }
            
            if (cambiosRealizados) {
                notificaciones.addAll(nuevosRecordatorios);
                System.out.println("INFO: [SCHEDULER] Guardando " + notificaciones.size() + " notificaciones al archivo...");
                guardarNotificacionesAJson(notificaciones);
                System.out.println("INFO: [SCHEDULER] Cambios en notificaciones guardados exitosamente");
            } else {
                System.out.println("INFO: [SCHEDULER] No hay recordatorios pendientes para procesar");
            }
        } catch (Exception e) {
            System.err.println("ERROR: [SCHEDULER] Error general en procesarRecordatoriosPendientes: " + e.getMessage());
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}

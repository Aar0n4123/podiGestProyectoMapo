package com.podiGest.backend.controller;

import com.podiGest.backend.model.Cita;
import com.podiGest.backend.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    /**
     * Endpoint de prueba para ver todas las citas
     */
    @GetMapping
    public List<Cita> obtenerTodasLasCitas() {
        return citaService.obtenerTodasLasCitas();
    }

    /**
     * HU 1.1.3: Endpoint para Consultar citas disponibles
     * * Vue llamará a esta URL:
     * GET /api/citas/disponibles?especialista=V-123456&fecha=2025-11-10
     */
    @GetMapping("/disponibles")
    public List<LocalTime> getDisponibilidad(
            @RequestParam("especialista") String especialistaCedula,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return citaService.obtenerHorariosDisponibles(especialistaCedula, fecha);
    }

    /**
     * HU 1.1.4: Endpoint para Agendar Cita (con cédula del paciente)
     * Vue llamará a esta URL:
     * POST /api/citas
     * Body: {
     *   "pacienteCedula": "V-123456",
     *   "especialistaCedula": "V-654321",
     *   "fechaHoraInicio": "2025-11-10T14:00:00",
     *   "descripcion": "Revisión general"
     * }
     */
    @PostMapping
    public ResponseEntity<?> agendarCita(@RequestBody CitaRequest request) {
        try {
            if (request.getPacienteCedula() == null || request.getPacienteCedula().isEmpty()) {
                return ResponseEntity.badRequest().body("La cédula del paciente es requerida");
            }
            if (request.getEspecialistaCedula() == null || request.getEspecialistaCedula().isEmpty()) {
                return ResponseEntity.badRequest().body("La cédula del especialista es requerida");
            }
            if (request.getFechaHoraInicio() == null || request.getFechaHoraInicio().isEmpty()) {
                return ResponseEntity.badRequest().body("La fecha y hora es requerida");
            }

            LocalDateTime fechaHora = LocalDateTime.parse(request.getFechaHoraInicio());
            Cita citaCreada = citaService.agendarCita(
                    request.getPacienteCedula(),
                    request.getEspecialistaCedula(),
                    fechaHora,
                    request.getDescripcion()
            );
            return ResponseEntity.ok(citaCreada);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al guardar la cita: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint para Agendar Cita usando Usuario Autenticado
     * Vue llamará a esta URL:
     * POST /api/citas/agendar
     * Body: {
     *   "especialistaCedula": "V-654321",
     *   "fechaHoraInicio": "2025-11-10T14:00:00",
     *   "descripcion": "Revisión general"
     * }
     */
    @PostMapping("/agendar")
    public ResponseEntity<?> agendarCitaConUsuarioAutenticado(@RequestBody CitaRequestAutenticado request) {
        try {
            if (request.getEspecialistaCedula() == null || request.getEspecialistaCedula().isEmpty()) {
                return ResponseEntity.badRequest().body("La cédula del especialista es requerida");
            }
            if (request.getFechaHoraInicio() == null || request.getFechaHoraInicio().isEmpty()) {
                return ResponseEntity.badRequest().body("La fecha y hora es requerida");
            }

            LocalDateTime fechaHora = LocalDateTime.parse(request.getFechaHoraInicio());
            Cita citaCreada = citaService.agendarCitaConUsuarioAutenticado(
                    request.getEspecialistaCedula(),
                    fechaHora,
                    request.getDescripcion()
            );
            return ResponseEntity.ok(citaCreada);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al guardar la cita: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    public static class CitaRequest {
        private String pacienteCedula;
        private String especialistaCedula;
        private String fechaHoraInicio;
        private String descripcion;

        public String getPacienteCedula() {
            return pacienteCedula;
        }

        public void setPacienteCedula(String pacienteCedula) {
            this.pacienteCedula = pacienteCedula;
        }

        public String getEspecialistaCedula() {
            return especialistaCedula;
        }

        public void setEspecialistaCedula(String especialistaCedula) {
            this.especialistaCedula = especialistaCedula;
        }

        public String getFechaHoraInicio() {
            return fechaHoraInicio;
        }

        public void setFechaHoraInicio(String fechaHoraInicio) {
            this.fechaHoraInicio = fechaHoraInicio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }

    public static class CitaRequestAutenticado {
        private String especialistaCedula;
        private String fechaHoraInicio;
        private String descripcion;

        public String getEspecialistaCedula() {
            return especialistaCedula;
        }

        public void setEspecialistaCedula(String especialistaCedula) {
            this.especialistaCedula = especialistaCedula;
        }

        public String getFechaHoraInicio() {
            return fechaHoraInicio;
        }

        public void setFechaHoraInicio(String fechaHoraInicio) {
            this.fechaHoraInicio = fechaHoraInicio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }

}
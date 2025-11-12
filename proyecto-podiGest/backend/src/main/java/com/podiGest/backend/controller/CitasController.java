package com.podiGest.backend.controller;

import com.podiGest.backend.model.Cita;
import com.podiGest.backend.service.CitasService;
import com.podiGest.backend.model.Usuario;
import com.podiGest.backend.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
public class CitasController {

    private final CitasService citasService;
    private final PerfilService perfilService; // agrego viki

    public CitasController(CitasService citasService, PerfilService perfilService) {
        this.citasService = citasService;
        this.perfilService = perfilService;// agrego viki
    }

    @GetMapping
    public ResponseEntity<List<Cita>> obtenerCitas() {
        try {
            List<Cita> citas = citasService.obtenerCitas();
            return ResponseEntity.ok(citas);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable String id) {
        try {
            return citasService.obtenerCitaPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crearCita(@RequestBody Cita nuevaCita) {
        try {
            if (nuevaCita.getId() == null || nuevaCita.getId().isEmpty()) {
                return ResponseEntity.badRequest().body("El ID de la cita es requerido.");
            }

            if (nuevaCita.getPacienteCorreo() == null || nuevaCita.getPacienteCorreo().isEmpty()) {
                return ResponseEntity.badRequest().body("El correo del paciente es requerido.");
            }

            if (nuevaCita.getFecha() == null || nuevaCita.getFecha().isEmpty()) {
                return ResponseEntity.badRequest().body("La fecha de la cita es requerida.");
            }

            Cita citaGuardada = citasService.guardarCita(nuevaCita);
            return ResponseEntity.status(HttpStatus.CREATED).body(citaGuardada);
        } catch (IOException e) {
            System.err.println("Error al guardar cita: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la cita");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarCita(@PathVariable String id) {
        try {
            boolean cancelada = citasService.cancelarCita(id);
            if (cancelada) {
                return ResponseEntity.ok("Cita cancelada exitosamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/paciente/{correoElectronico}")
    public ResponseEntity<List<Cita>> obtenerCitasPorPaciente(@PathVariable String correoElectronico) {
        try {
            List<Cita> citas = citasService.obtenerCitasPorPaciente(correoElectronico);
            return ResponseEntity.ok(citas);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/especialista/{especialista}")
    public ResponseEntity<List<Cita>> obtenerCitasPorEspecialista(@PathVariable String especialista) {
        try {
            List<Cita> citas = citasService.obtenerCitasPorEspecialista(especialista);
            return ResponseEntity.ok(citas);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    //agregado viki
    /**
     * Endpoint para modificar la fecha y hora de una cita existente.
     * Incluye manejo de excepción para verificar la disponibilidad (409 Conflict).
     * @param citaId ID de la cita a modificar.
     * @param citaData Un objeto Cita que contiene la nueva 'fecha' y 'hora'.
     */
    @PutMapping("/{citaId}")
    public ResponseEntity<?> modificarCita(@PathVariable String citaId, @RequestBody Cita citaData) {
        try {
            if (citaData.getFecha() == null || citaData.getHora() == null) {
                return ResponseEntity.badRequest().body("La fecha y la hora son campos requeridos para la modificación.");
            }

            Cita citaActualizada = citasService.modificarCitaCompleta(
                    citaId,
                    citaData
            );

            if (citaActualizada != null) {
                return ResponseEntity.ok(citaActualizada);
            } else {

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró la cita con ID: " + citaId);
            }
        } catch (IllegalStateException e) {

            System.err.println("Conflicto al modificar cita: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al modificar cita: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error al modificar la cita.");
        }
    }


    @GetMapping("/propias")
    public ResponseEntity<?> obtenerCitasPropias() {
        try {

            Optional<Usuario> usuarioSesion = perfilService.obtenerPerfilActivo();

            if (usuarioSesion.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debe iniciar sesión para ver sus citas.");
            }

            Usuario usuario = usuarioSesion.get();

            List<Cita> citasFiltradas = citasService.obtenerCitasPorEspecialista(usuario.getNombre());

            return ResponseEntity.ok(citasFiltradas);

        } catch (IOException e) {
            System.err.println("Error al obtener citas del especialista: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener citas del especialista.");
        }
    }



}


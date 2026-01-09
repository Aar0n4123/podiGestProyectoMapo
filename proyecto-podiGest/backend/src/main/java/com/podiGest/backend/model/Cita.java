package com.podiGest.backend.model;

import lombok.Data;

@Data
public class Cita {
    private String id;
    private String pacienteNombre;
    private String pacienteCorreo;
    private String pacienteTelefono;
    private String especialista;
    private String cedulaEspecialista;
    private String especialidadBuscada;
    private String fecha;
    private String hora;
    private String razonConsulta;
    private String estado;
    private String fechaCreacion;

    public Cita() {
    }

    public Cita(String id, String pacienteNombre, String pacienteCorreo, String pacienteTelefono,
                String especialista, String cedulaEspecialista, String especialidadBuscada, String fecha, String hora,
                String razonConsulta, String estado, String fechaCreacion) {
        this.id = id;
        this.pacienteNombre = pacienteNombre;
        this.pacienteCorreo = pacienteCorreo;
        this.pacienteTelefono = pacienteTelefono;
        this.especialista = especialista;
        this.cedulaEspecialista = cedulaEspecialista;
        this.especialidadBuscada = especialidadBuscada;
        this.fecha = fecha;
        this.hora = hora;
        this.razonConsulta = razonConsulta;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }
}


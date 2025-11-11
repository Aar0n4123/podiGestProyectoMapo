package com.podiGest.backend.model;

import lombok.Data;

@Data
public class Notificacion {

    private String id;
    private String fechaEnvio;
    private String asunto;
    private String remitente;
    private String mensaje;
    private String correoDestinatario; // Correo del usuario al que pertenece la notificación

    public Notificacion() {
    }

    public Notificacion(String id, String fechaEnvio, String asunto, String remitente, String mensaje, String correoDestinatario) {
        this.id = id;
        this.fechaEnvio = fechaEnvio;
        this.asunto = asunto;
        this.remitente = remitente;
        this.mensaje = mensaje;
        this.correoDestinatario = correoDestinatario;
    }
}
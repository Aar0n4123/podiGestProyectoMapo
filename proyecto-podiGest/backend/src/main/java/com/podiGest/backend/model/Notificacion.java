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
    private boolean silenciada; // Indica si la notificación ha sido silenciada por el usuario

    public Notificacion() {
        this.silenciada = false; // Por defecto, las notificaciones no están silenciadas
    }

    public Notificacion(String id, String fechaEnvio, String asunto, String remitente, String mensaje, String correoDestinatario) {
        this.id = id;
        this.fechaEnvio = fechaEnvio;
        this.asunto = asunto;
        this.remitente = remitente;
        this.mensaje = mensaje;
        this.correoDestinatario = correoDestinatario;
        this.silenciada = false; // Por defecto, las notificaciones no están silenciadas
    }
}
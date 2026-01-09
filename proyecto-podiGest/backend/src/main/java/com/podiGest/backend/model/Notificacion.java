package com.podiGest.backend.model;

import lombok.Data;

@Data
public class Notificacion {

    private String id;
    private String fechaEnvio;
    private String asunto;
    private String remitente;
    private String mensaje;
    private String correoDestinatario;
    private boolean silenciada;
    private boolean tieneRecordatorio;
    private String fechaRecordatorio;
    private boolean recordatorioActivo;

    public Notificacion() {
        this.silenciada = false;
        this.tieneRecordatorio = false;
        this.recordatorioActivo = false;
    }

    public Notificacion(String id, String fechaEnvio, String asunto, String remitente, String mensaje, String correoDestinatario) {
        this.id = id;
        this.fechaEnvio = fechaEnvio;
        this.asunto = asunto;
        this.remitente = remitente;
        this.mensaje = mensaje;
        this.correoDestinatario = correoDestinatario;
        this.silenciada = false;
        this.tieneRecordatorio = false;
        this.recordatorioActivo = false;
    }
}
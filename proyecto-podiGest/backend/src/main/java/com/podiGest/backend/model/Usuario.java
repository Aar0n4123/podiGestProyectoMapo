package com.podiGest.backend.model;


import lombok.Data;

import java.time.LocalDate;

@Data

public class Usuario {
    private String cedula;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento; // ¡Cambiamos a LocalDate!
    private String correoElectronico;
    private String contrasenia;
    private String rol;

    public Usuario(String cedula, String nombre, String apellido, LocalDate fechaNacimiento, String correoElectronico, String contrasenia, String rol) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.correoElectronico = correoElectronico;
        this.contrasenia = contrasenia;
        this.rol= rol;
    }

    public Usuario() {
    }


    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
     }

    public String getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public String getRol() {
        return rol;
    }
}
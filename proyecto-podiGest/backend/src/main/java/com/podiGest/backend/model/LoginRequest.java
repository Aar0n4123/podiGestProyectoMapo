// Archivo: src/main/java/com/podiGest/backend/model/LoginRequest.java

package com.podiGest.backend.model;

import lombok.Data; // Si usas Lombok


@Data
public class LoginRequest {


    private String correo;
    private String contrasenia;


}
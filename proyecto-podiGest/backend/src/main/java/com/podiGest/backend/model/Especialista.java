
package com.podiGest.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Especialista extends Usuario {


    public Especialista() {
        super();
        this.setRol("especialista");
    }

    public Especialista(Usuario usuario) {
        super(
                usuario.getCedula(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getFechaNacimiento(),
                usuario.getCorreoElectronico(),
                usuario.getContrasenia(),
                "especialista" // Forzamos el rol
        );
    }
}
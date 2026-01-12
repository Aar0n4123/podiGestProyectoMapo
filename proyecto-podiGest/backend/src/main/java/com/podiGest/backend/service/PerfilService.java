package com.podiGest.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.podiGest.backend.model.Usuario;
import com.podiGest.backend.model.Cita;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PerfilService {

    private List<Usuario> listaUsuarios;
    private static final String USUARIOS_JSON_FILE = "usuarios.json";
    private static final String USUARIO_SESION_JSON_FILE = "usuarioInicioSesion.json";
    private final ObjectMapper mapper;
    
    private final ObjectProvider<CitasService> citasServiceProvider;

    public PerfilService(ObjectProvider<CitasService> citasServiceProvider) {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.citasServiceProvider = citasServiceProvider;

        this.listaUsuarios = cargarUsuariosDesdeJson(USUARIOS_JSON_FILE);

    }

    //metodo para sbaer cuales son los usuarios registrados en el JSON
    private List<Usuario> cargarUsuariosDesdeJson(String fileName) {
        Path path = PathConfigService.getSeedFilePath(fileName);
        try {
            if (Files.exists(path) && Files.size(path) > 0) {
                return mapper.readValue(path.toFile(), new TypeReference<List<Usuario>>() {});
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo JSON en: " + path);
        }
        // Intenta cargar desde el classpath (ej: src/main/resources) si el archivo externo no existe o está vacío
        List<Usuario> usuarios = cargarDesdeClasspath(fileName);
        if (!usuarios.isEmpty()) {
            try {
                // Si encontró usuarios en el classpath, los guarda en la carpeta externa
                guardarUsuariosAJson(usuarios, fileName);
            } catch (IOException e) {
                System.err.println("ERROR: No se pudo inicializar el archivo JSON en: " + path);
            }
        }
        return usuarios;
    }

    private List<Usuario> cargarDesdeClasspath(String resourceName) {
        try (InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
            return mapper.readValue(inputStream, new TypeReference<List<Usuario>>() {});
        } catch (IOException e) {
            // No es un error grave, solo significa que no hay un archivo de "semilla" en resources
            return new ArrayList<>();
        }
    }

    //metodo para escribir el JSON y guardar el nuevo usuario
    private void guardarUsuariosAJson(List<Usuario> usuarios, String fileName) throws IOException {
        Path path = PathConfigService.getSeedFilePath(fileName);
        // Sobrescribe el archivo con los datos actuales (sin duplicar)
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), usuarios);
    }



    // metodo para verificar si el usuario ya existe
    public boolean existeUsuario(String correo, String cedula) {
        return listaUsuarios.stream()
                .anyMatch(u -> u.getCorreoElectronico() != null && u.getCorreoElectronico().equalsIgnoreCase(correo) ||
                        u.getCedula() != null && u.getCedula().equalsIgnoreCase(cedula));
    }

    //metodo para verificar si el usuario es o no mayor de edad
    public boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return false;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }

    public boolean esNombreValido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        return nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚ\\s]+$");
    }

    public boolean esCedulaValida(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return false;
        }
        return cedula.matches("^[0-9]+$");
    }

    // metodo guarda en el archivo JSON
    public Usuario guardarUsuario(Usuario nuevoUsuario) throws IOException {
        if (!esMayorDeEdad(nuevoUsuario.getFechaNacimiento())) {

            throw new IllegalArgumentException("El usuario debe ser mayor de edad (18 años) para registrarse.");
        }

        if (!esNombreValido(nuevoUsuario.getNombre())) {
            throw new IllegalArgumentException("El nombre solo debe contener letras y espacios.");
        }

        if (!esNombreValido(nuevoUsuario.getApellido())) {
            throw new IllegalArgumentException("El apellido solo debe contener letras y espacios.");
        }

        if (!esCedulaValida(nuevoUsuario.getCedula())) {
            throw new IllegalArgumentException("La cédula solo debe contener números.");
        }

        nuevoUsuario.setRol("paciente");
        listaUsuarios.add(nuevoUsuario);
        guardarUsuariosAJson(listaUsuarios, USUARIOS_JSON_FILE);

        return nuevoUsuario;
    }

    public Optional<Usuario> validarUsuarioExiste(String correo, String contrasena) {
        Optional<Usuario> usuarioEncontrado = listaUsuarios.stream()
                .filter(u -> u.getCorreoElectronico() != null && u.getCorreoElectronico().equalsIgnoreCase(correo))
                .findFirst();

        if (usuarioEncontrado.isPresent()) {
            Usuario usuario = usuarioEncontrado.get();

            if (usuario.getContrasenia() != null && usuario.getContrasenia().equals(contrasena)) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    /**
     * Guarda el usuario que acaba de iniciar sesión en un archivo JSON separado.
     * Este método SOBRESCRIBE el contenido del archivo cada vez que se llama.
     * @param usuario El usuario que ha iniciado sesión.
     */
    public void guardarUsuarioSesion(Usuario usuario) {
        List<Usuario> listaUsuarioSesion = new ArrayList<>();
        listaUsuarioSesion.add(usuario);

        try {
            Path path = PathConfigService.getSeedFilePath(USUARIO_SESION_JSON_FILE);
            // Sobrescribe el archivo con los datos actuales (sin duplicar)
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), listaUsuarioSesion);
            System.out.println("INFO: Usuario de sesión guardado en " + USUARIO_SESION_JSON_FILE);
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo guardar el archivo de sesión del usuario: " + e.getMessage());
        }
    }

    public List<Usuario> obtenerEspecialistas() {
        return listaUsuarios.stream()
                .filter(u -> u.getRol() != null && u.getRol().equalsIgnoreCase("especialista"))
                .toList();
    }

    /**
     * Obtiene el correo electrónico de un especialista por su nombre completo o solo nombre
     * @param nombreBuscado El nombre del especialista (puede ser solo nombre o nombre completo)
     * @return Optional con el correo electrónico si se encuentra el especialista
     */
    public Optional<String> obtenerCorreoEspecialistaPorNombre(String nombreBuscado) {
        if (nombreBuscado == null || nombreBuscado.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String nombreBuscadoTrim = nombreBuscado.trim();
        
        return listaUsuarios.stream()
                .filter(u -> u.getRol() != null && u.getRol().equalsIgnoreCase("especialista"))
                .filter(u -> {
                    String nombreCompletoUsuario = (u.getNombre() + " " + u.getApellido()).trim();
                    String soloNombre = u.getNombre().trim();
                    
                    // Buscar por nombre completo o solo por nombre
                    return nombreCompletoUsuario.equalsIgnoreCase(nombreBuscadoTrim) ||
                           soloNombre.equalsIgnoreCase(nombreBuscadoTrim);
                })
                .map(Usuario::getCorreoElectronico)
                .findFirst();
    }

    /**
     * Metodo para leer los datos del usuario activo desde el archivo JSON
     * (Este es el método que movimos desde ConsultarPerfilService)
     */
    public Optional<Usuario> obtenerPerfilActivo() throws IOException {

        // ¡IMPORTANTE! Usamos la misma lógica de ruta que 'guardarUsuarioSesion'
        Path sesionPath = PathConfigService.getSeedFilePath(USUARIO_SESION_JSON_FILE);

        if (!Files.exists(sesionPath)) {
            System.err.println("ADVERTENCIA: No se encontró el archivo de sesión en: " + sesionPath);
            return Optional.empty();
        }
        String jsonContent = Files.readString(sesionPath);
        if (jsonContent.isBlank()) {
            return Optional.empty();
        }

        // Usamos el 'mapper' que ya existe en esta clase
        List<Usuario> sesion = mapper.readValue(jsonContent, new TypeReference<List<Usuario>>() {});

        if (sesion.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(sesion.get(0));
    }


    // -------------------------------------------------------------------
    // MÉTODO DEFINITIVO: ACTUALIZA DATOS (CON VALIDACIONES DE EDAD Y CORREO)
    // -------------------------------------------------------------------
    public Usuario actualizarPerfil(Usuario usuarioConDatosNuevos) throws IOException {

        // 1. Averiguamos quién es el usuario REAL consultando la sesión
        Optional<Usuario> sesionActual = obtenerPerfilActivo();

        if (sesionActual.isEmpty()) {
            throw new IOException("No se puede actualizar: No hay sesión activa.");
        }

        String cedulaFija = sesionActual.get().getCedula();
        usuarioConDatosNuevos.setCedula(cedulaFija); // Protegemos la cédula

        // --- VALIDACIÓN 1: MAYOR DE EDAD ---
        if (!esMayorDeEdad(usuarioConDatosNuevos.getFechaNacimiento())) {
            throw new IllegalArgumentException("Fecha inválida: Debes ser mayor de 18 años.");
        }

        // --- VALIDACIÓN 2: CORREO DUPLICADO ---
        // Buscamos si ALGUIEN MÁS (que no sea yo) ya tiene ese correo
        boolean correoOcupado = listaUsuarios.stream()
                .anyMatch(u -> u.getCorreoElectronico().equalsIgnoreCase(usuarioConDatosNuevos.getCorreoElectronico())
                        && !u.getCedula().equals(cedulaFija)); // Importante: Que no sea mi propia cédula

        if (correoOcupado) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado por otro usuario.");
        }

        boolean encontrado = false;
        int indiceEncontrado = -1;

        // 2. Buscamos al usuario en la lista general para actualizarlo
        for (int i = 0; i < listaUsuarios.size(); i++) {
            Usuario u = listaUsuarios.get(i);
            if (u.getCedula().equals(cedulaFija)) {
                indiceEncontrado = i;
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            throw new IOException("Error: El usuario no se encuentra en la base de datos.");
        }

        // 3. Reemplazamos SOLO el usuario encontrado
        listaUsuarios.set(indiceEncontrado, usuarioConDatosNuevos);

        // 4. Limpieza de duplicados por seguridad
        List<Usuario> sinDuplicados = new ArrayList<>();
        java.util.Set<String> cedulasVistas = new java.util.HashSet<>();
        for (Usuario u : listaUsuarios) {
            if (!cedulasVistas.contains(u.getCedula())) {
                sinDuplicados.add(u);
                cedulasVistas.add(u.getCedula());
            }
        }
        listaUsuarios = sinDuplicados;

        // 5. Guardamos cambios
        guardarUsuariosAJson(listaUsuarios, USUARIOS_JSON_FILE);
        guardarUsuarioSesion(usuarioConDatosNuevos);

        // 6. Actualizar citas si es especialista
        if (usuarioConDatosNuevos.getRol() != null && "especialista".equalsIgnoreCase(usuarioConDatosNuevos.getRol())) {
            actualizarCitasEspecialista(cedulaFija, usuarioConDatosNuevos);
        }

        return usuarioConDatosNuevos;
    }
    // -------------------------------------------------------------------
    // MÉTODO: ELIMINAR PERFIL ACTUAL (Seguro y Definitivo)
    // -------------------------------------------------------------------
    public void eliminarPerfilActual() throws IOException {

        // 1. Obtener quién está conectado
        Optional<Usuario> sesionActual = obtenerPerfilActivo();
        if (sesionActual.isEmpty()) {
            throw new IOException("No hay sesión activa para eliminar.");
        }

        String cedulaBorrar = sesionActual.get().getCedula();

        // 2. Borrar de la lista en memoria
        // (Esto elimina al usuario de la lista cargada en RAM)
        boolean borrado = listaUsuarios.removeIf(u -> u.getCedula().equals(cedulaBorrar));

        if (!borrado) {
            throw new IOException("Error: El usuario no se encuentra en la lista local.");
        }

        // 3. Sobrescribir el archivo usuarios.json con la lista actualizada
        // USAMOS TU MÉTODO EXISTENTE que ya funciona bien con las fechas
        guardarUsuariosAJson(listaUsuarios, USUARIOS_JSON_FILE);

        // 4. Borrar el archivo de sesión (Cerrar sesión forzosamente)
        Path pathSesion = PathConfigService.getSeedFilePath(USUARIO_SESION_JSON_FILE);
        Files.deleteIfExists(pathSesion);
    }
    
    // -------------------------------------------------------------------
    // MÉTODO: Actualizar citas de un especialista con su cedula
    // -------------------------------------------------------------------
    private void actualizarCitasEspecialista(String cedula, Usuario especialista) throws IOException {
        CitasService citasService = citasServiceProvider.getIfAvailable();
        if (citasService == null) {
            System.out.println("INFO: CitasService no disponible, saltando actualización de citas");
            return;
        }
        
        try {
            List<Cita> citas = citasService.obtenerCitas();
            String nombreCompleto = especialista.getNombre() + " " + especialista.getApellido();
            
            boolean actualizado = false;
            for (Cita cita : citas) {
                // Si la cita pertenece a este especialista (por nombre) y no tiene cedula asignada
                if (cita.getEspecialista() != null && cita.getEspecialista().equals(nombreCompleto) && 
                    (cita.getCedulaEspecialista() == null || cita.getCedulaEspecialista().isEmpty())) {
                    cita.setCedulaEspecialista(cedula);
                    actualizado = true;
                    System.out.println("INFO: Actualizada cita " + cita.getId() + " con cedula del especialista");
                }
            }
            
            if (actualizado) {
                citasService.guardarCitasAJson(citas);
                System.out.println("INFO: Citas del especialista " + nombreCompleto + " actualizadas con cedula: " + cedula);
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudieron actualizar las citas: " + e.getMessage());
            throw e;
        }
    }
}
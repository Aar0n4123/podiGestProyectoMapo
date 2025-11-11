package com.podiGest.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio centralizado para resolver rutas de archivos de configuración y datos.
 * Utiliza la carpeta home del usuario para garantizar portabilidad en cualquier sistema operativo.
 */
public class PathConfigService {

    private static final String DATA_DIR_NAME = "podiGest_data";
    private static final Path DATA_DIR;
    private static final Path SEED_DATA_DIR;

    static {
        // Obtiene la carpeta home del usuario (funciona en Windows, Mac, Linux)
        Path userHome = Paths.get(System.getProperty("user.home"));
        DATA_DIR = userHome.resolve(DATA_DIR_NAME);

        // Obtiene la carpeta de datos semilla (base_de_datos en el backend)
        Path tempSeedDataDir = resolveSeedDataDirectory();
        SEED_DATA_DIR = tempSeedDataDir;

        // Crea la carpeta de datos si no existe
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo crear la carpeta de datos en: " + DATA_DIR);
        }
    }

    /**
     * Retorna la ruta completa para un archivo específico en la carpeta de datos.
     * 
     * @param fileName Nombre del archivo (ej: "usuarios.json", "citas.json")
     * @return Path normalizado al archivo
     */
    public static Path getFilePath(String fileName) {
        return DATA_DIR.resolve(fileName);
    }

    /**
     * Retorna la carpeta de datos centralizada.
     * 
     * @return Path de la carpeta podiGest_data
     */
    public static Path getDataDirectory() {
        return DATA_DIR;
    }

    /**
     * Retorna la ruta de la carpeta de datos semilla (base_de_datos en el proyecto).
     * 
     * @return Path de la carpeta base_de_datos
     */
    public static Path getSeedDataDirectory() {
        return SEED_DATA_DIR;
    }

    /**
     * Retorna la ruta de un archivo en la carpeta de datos semilla.
     * 
     * @param fileName Nombre del archivo
     * @return Path del archivo en base_de_datos
     */
    public static Path getSeedFilePath(String fileName) {
        return SEED_DATA_DIR.resolve(fileName);
    }

    /**
     * Asegura que la carpeta de datos existe.
     * 
     * @throws IOException si hay error al crear la carpeta
     */
    public static void ensureDataDirectoryExists() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
    }

    /**
     * Resuelve la ruta de la carpeta base_de_datos buscando la estructura del proyecto.
     * Primero busca ascendiendo desde el directorio actual hasta encontrar "proyecto-podiGest/backend",
     * luego desciende a "base_de_datos".
     * 
     * @return Path a la carpeta base_de_datos, o el directorio actual si no se encuentra
     */
    private static Path resolveSeedDataDirectory() {
        // Comienza desde el directorio actual (normalmente donde se ejecuta Maven)
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        
        // Primero, chequea si estamos en el directorio backend directamente
        Path baseDbPath = currentDir.resolve("base_de_datos");
        if (Files.exists(baseDbPath) && Files.isDirectory(baseDbPath)) {
            return baseDbPath;
        }
        
        // Si no, busca ascendiendo hasta encontrar "backend"
        Path searchDir = currentDir;
        while (searchDir != null) {
            // Chequea si el directorio actual es "backend"
            if ("backend".equals(searchDir.getFileName().toString())) {
                Path possiblePath = searchDir.resolve("base_de_datos");
                if (Files.exists(possiblePath) && Files.isDirectory(possiblePath)) {
                    return possiblePath;
                }
            }
            
            // Chequea si "proyecto-podiGest/backend/base_de_datos" existe desde aquí
            Path proyectoPath = searchDir.resolve("proyecto-podiGest")
                    .resolve("backend")
                    .resolve("base_de_datos");
            if (Files.exists(proyectoPath) && Files.isDirectory(proyectoPath)) {
                return proyectoPath;
            }
            
            // Sube un nivel
            searchDir = searchDir.getParent();
        }
        
        // Último recurso: retorna base_de_datos en el directorio actual
        // (esto debería funcionar si se ejecuta correctamente desde el backend)
        return currentDir.resolve("base_de_datos");
    }
}
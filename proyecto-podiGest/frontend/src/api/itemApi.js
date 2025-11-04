// Archivo: frontend/src/api/itemApi.js

// Define la URL exacta de la ruta API que probaste en el navegador
const API_URL = 'http://localhost:8080/api/items';

// La palabra clave 'export' resuelve tu error 'does not provide an export named...'
export const getAllItems = async () => {
  try {
    // 1. Llama al Backend (Spring Boot)
    const response = await fetch(API_URL);
   
    // 2. Verifica si la respuesta fue exitosa (código 200)
    if (!response.ok) {
      // Lanza un error si hay problemas con la respuesta HTTP
      throw new Error(`HTTP error! status: ${response.status}`);
    }
   
    // 3. Convierte el resultado JSON en objetos JavaScript
    const data = await response.json();
    return data;

  } catch (error) {
    // Si hay un error de conexión (ej. Backend no está encendido), lo maneja
    console.error('Error al obtener ítems:', error);
    // Devuelve un array vacío para evitar que la aplicación se rompa
    return [];
  }
};
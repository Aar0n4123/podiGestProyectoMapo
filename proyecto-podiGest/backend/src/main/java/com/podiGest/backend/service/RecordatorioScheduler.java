package com.podiGest.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RecordatorioScheduler {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private CitasService citasService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(fixedRate = 30000)
    public void verificarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();
        System.out.println("\n=============================================================");
        System.out.println("⏰ [SCHEDULER] Ejecución programada - " + ahora.format(formatter));
        System.out.println("=============================================================");
        
        try {
            // Actualizar estados de citas pasadas
            citasService.actualizarEstadosCitas();
            
            notificacionService.procesarRecordatoriosPendientes();
            System.out.println("✓ [SCHEDULER] Proceso completado exitosamente");
        } catch (IOException e) {
            System.err.println("✗ [SCHEDULER] IOException al procesar recordatorios: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ [SCHEDULER] Error general al procesar recordatorios: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=============================================================\n");
    }
}

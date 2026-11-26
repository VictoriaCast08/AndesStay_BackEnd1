package cl.andesstay.report.service;

import cl.andesstay.report.kafka.dto.ReservationAnalyticsEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Lógica de dominio de analítica de reportes (separada de la infraestructura Kafka).
 * Procesa y registra las métricas de reservas recibidas por los consumidores.
 */
@Service
public class ReservationAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(ReservationAnalyticsService.class);

    private final Map<String, Long> reservasPorEstado = new ConcurrentHashMap<>();
    private final Map<String, Long> reservasPorUnidad = new ConcurrentHashMap<>();
    private long totalEventosProcesados = 0;

    public void procesarEvento(ReservationAnalyticsEventDto evento) {
        if (evento == null) {
            log.warn("[Analytics] Evento nulo recibido, se ignora");
            return;
        }

        String action = evento.action();
        String entityId = evento.entityId();
        String userId = evento.userId();
        Map<String, Object> metadata = evento.metadata();

        totalEventosProcesados++;
        String status = metadata != null ? String.valueOf(metadata.getOrDefault("status", "DESCONOCIDO")) : "DESCONOCIDO";
        String unitId = metadata != null ? String.valueOf(metadata.getOrDefault("unitId", "DESCONOCIDO")) : "DESCONOCIDO";

        reservasPorEstado.merge(status, 1L, Long::sum);
        reservasPorUnidad.merge(unitId, 1L, Long::sum);

        log.info("[Analytics] Evento procesado eventId={} action={} entityId={} userId={} status={} unitId={}",
                evento.eventId(), action, entityId, userId, status, unitId);
    }

    public Map<String, Long> getReservasPorEstado() {
        return Map.copyOf(reservasPorEstado);
    }

    public Map<String, Long> getReservasPorUnidad() {
        return Map.copyOf(reservasPorUnidad);
    }

    public long getTotalEventosProcesados() {
        return totalEventosProcesados;
    }
}

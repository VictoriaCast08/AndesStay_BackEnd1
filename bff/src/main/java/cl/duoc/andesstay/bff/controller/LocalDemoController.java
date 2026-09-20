package cl.duoc.andesstay.bff.controller;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo perfil local: simula identidad sin Entra para desarrollo y demo visual.
 * No se despliega en azure.
 */
@RestController
@Profile("local")
@RequestMapping("/api/bff")
public class LocalDemoController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "service", "ms-andesstay-bff",
                "status", "ok",
                "profile", "local",
                "user", "dev.local@andesstay.local"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        return ResponseEntity.ok(Map.of(
                "user", "dev.local@andesstay.local",
                "authorities", List.of("ROLE_Admin", "ROLE_Operador", "ROLE_Auditor"),
                "roles", List.of("Admin", "Operador", "Auditor"),
                "claims", Map.of(
                        "profile", "local-dev",
                        "iss", "local",
                        "aud", List.of("ms-andesstay-bff"),
                        "note", "Perfil local: JWT no validado contra Entra ID")));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(Map.of(
                "view", "admin",
                "title", "Panel de operaciones",
                "message", "Perfil local — KPIs mock de la red AndesStay",
                "kpis", Map.of(
                        "ocupacionActiva", 72,
                        "reservasHoy", 18,
                        "checkinsPendientes", 5,
                        "checkoutsHoy", 7),
                "llegadas", List.of(
                        Map.of("unidad", "Cabaña Ñuble", "huesped", "Camila Rojas", "estado", "CONFIRMADA"),
                        Map.of("unidad", "Hostal Sur", "huesped", "Diego Paredes", "estado", "CHECKIN_PENDIENTE")),
                "salidas", List.of(
                        Map.of("unidad", "Lodge Pucón", "huesped", "Ana Beltrán", "estado", "EN_ESTADÍA"))));
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<Map<String, Object>>> reservations() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "RSV-1042", "unidad", "Cabaña Ñuble", "huesped", "Camila Rojas",
                        "estado", "CREADA", "desde", "2026-04-12", "hasta", "2026-04-15"),
                Map.of("id", "RSV-1041", "unidad", "Hostal Sur", "huesped", "Diego Paredes",
                        "estado", "CONFIRMADA", "desde", "2026-04-11", "hasta", "2026-04-13"),
                Map.of("id", "RSV-1039", "unidad", "Cabaña Ñuble", "huesped", "María Fuentes",
                        "estado", "CHECKIN_PENDIENTE", "desde", "2026-04-10", "hasta", "2026-04-14"),
                Map.of("id", "RSV-1038", "unidad", "Lodge Pucón", "huesped", "Ana Beltrán",
                        "estado", "EN_ESTADÍA", "desde", "2026-04-09", "hasta", "2026-04-12"),
                Map.of("id", "RSV-1029", "unidad", "Hostal Valdivia", "huesped", "Luis Soto",
                        "estado", "CHECKOUT", "desde", "2026-04-01", "hasta", "2026-04-04"),
                Map.of("id", "RSV-1021", "unidad", "Hostal Sur", "huesped", "Pedro Lagos",
                        "estado", "CANCELADA", "desde", "2026-04-05", "hasta", "2026-04-07")));
    }

    @GetMapping("/catalog/units")
    public ResponseEntity<List<Map<String, Object>>> catalog() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "U-01", "nombre", "Cabaña Ñuble", "tipo", "cabaña", "tarifa", 62000, "disponible", true),
                Map.of("id", "U-02", "nombre", "Hostal Sur", "tipo", "habitación", "tarifa", 28000, "disponible", true),
                Map.of("id", "U-03", "nombre", "Lodge Pucón", "tipo", "lodge", "tarifa", 95000, "disponible", false),
                Map.of("id", "U-04", "nombre", "Hostal Valdivia", "tipo", "habitación", "tarifa", 31000, "disponible", true)));
    }

    @GetMapping("/audit/timeline")
    public ResponseEntity<List<Map<String, Object>>> audit() {
        return ResponseEntity.ok(List.of(
                Map.of("eventId", "EV-9", "reserva", "RSV-1042", "tipo", "RESERVA_CREADA",
                        "actor", "cliente@duoc.cl", "ts", "2026-04-10T15:02:00Z"),
                Map.of("eventId", "EV-8", "reserva", "RSV-1041", "tipo", "RESERVA_CONFIRMADA",
                        "actor", "operador@duoc.cl", "ts", "2026-04-10T11:40:00Z"),
                Map.of("eventId", "EV-7", "reserva", "RSV-1038", "tipo", "CHECK_IN",
                        "actor", "operador@duoc.cl", "ts", "2026-04-09T16:10:00Z"),
                Map.of("eventId", "EV-6", "reserva", "RSV-1029", "tipo", "CHECK_OUT",
                        "actor", "operador@duoc.cl", "ts", "2026-04-04T11:05:00Z")));
    }

    @GetMapping("/report/kpis")
    public ResponseEntity<Map<String, Object>> reportKpis() {
        return ResponseEntity.ok(Map.of(
                "range", "last24h",
                "reservasPorHora", Map.of("10", 2, "11", 3, "12", 1, "15", 4, "18", 2),
                "tiempoCicloHoras", 36,
                "ocupacionActivaPct", 72,
                "topUnidades", List.of(
                        Map.of("unidad", "Cabaña Ñuble", "reservas", 6),
                        Map.of("unidad", "Hostal Sur", "reservas", 4))));
    }
}

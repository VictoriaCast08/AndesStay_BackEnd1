package cl.duoc.andesstay.bff.controller;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                        "checkoutsHoy", 7)));
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<Map<String, Object>>> reservations() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "RSV-1042", "unidad", "Cabaña Ñuble", "huesped", "Camila Rojas",
                        "estado", "CREADA", "desde", "2026-04-12", "hasta", "2026-04-15"),
                Map.of("id", "RSV-1041", "unidad", "Hostal Sur", "huesped", "Diego Paredes",
                        "estado", "CONFIRMADA", "desde", "2026-04-11", "hasta", "2026-04-13")));
    }
}
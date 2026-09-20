package cl.duoc.andesstay.bff.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del BFF AndesStay (EP1). Devuelven payloads mock del caso
 * para demostrar autenticación Entra ID y autorización por rol.
 * En evaluaciones siguientes se enrutarán a los microservicios de dominio.
 */
@RestController
@Profile("azure")
@RequestMapping("/api/bff")
public class BffController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "service", "ms-andesstay-bff",
                "status", "ok",
                "timestamp", Instant.now().toString(),
                "user", username(authentication)));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        Map<String, Object> claims = Map.of();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            claims = Map.of(
                    "sub", nullSafe(jwt.getSubject()),
                    "preferred_username", nullSafe(jwt.getClaimAsString("preferred_username")),
                    "name", nullSafe(jwt.getClaimAsString("name")),
                    "iss", nullSafe(jwt.getIssuer() != null ? jwt.getIssuer().toString() : null),
                    "aud", jwt.getAudience() != null ? jwt.getAudience() : List.of(),
                    "roles", roles(authentication),
                    "exp", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : null);
        }
        return ResponseEntity.ok(Map.of(
                "user", username(authentication),
                "authorities", authorities(authentication),
                "roles", roles(authentication),
                "claims", claims));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        List<String> roles = roles(authentication);
        Map<String, Object> body;
        if (roles.contains("Admin")) {
            body = Map.of(
                    "view", "admin",
                    "title", "Panel de operaciones",
                    "kpis", Map.of(
                            "ocupacionActiva", 72,
                            "reservasHoy", 18,
                            "checkinsPendientes", 5,
                            "checkoutsHoy", 7),
                    "message", "Ocupación y KPIs de la red AndesStay");
        } else if (roles.contains("Operador")) {
            body = Map.of(
                    "view", "operador",
                    "title", "Llegadas y salidas",
                    "llegadas", List.of(
                            Map.of("unidad", "Cabaña Ñuble", "huesped", "Camila Rojas", "estado", "CONFIRMADA"),
                            Map.of("unidad", "Hostal Sur", "huesped", "Diego Paredes", "estado", "CHECKIN_PENDIENTE")),
                    "salidas", List.of(
                            Map.of("unidad", "Lodge Pucón", "huesped", "Ana Beltrán", "estado", "EN_ESTADÍA")),
                    "message", "Operación del día para recepción");
        } else if (roles.contains("Auditor")) {
            body = Map.of(
                    "view", "auditor",
                    "title", "Auditoría de hospedaje",
                    "eventosRecientes", 12,
                    "message", "Solo lectura: timeline de eventos");
        } else {
            body = Map.of(
                    "view", "cliente",
                    "title", "Mis reservas",
                    "activas", List.of(
                            Map.of("id", "RSV-1042", "unidad", "Cabaña Ñuble", "estado", "CREADA", "desde", "2026-04-12")),
                    "message", "Estado de tus reservas AndesStay");
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/reservations")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public ResponseEntity<List<Map<String, Object>>> reservations() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "RSV-1042", "unidad", "Cabaña Ñuble", "huesped", "Camila Rojas",
                        "estado", "CREADA", "desde", "2026-04-12", "hasta", "2026-04-15"),
                Map.of("id", "RSV-1041", "unidad", "Hostal Sur", "huesped", "Diego Paredes",
                        "estado", "CONFIRMADA", "desde", "2026-04-11", "hasta", "2026-04-13"),
                Map.of("id", "RSV-1038", "unidad", "Lodge Pucón", "huesped", "Ana Beltrán",
                        "estado", "EN_ESTADÍA", "desde", "2026-04-09", "hasta", "2026-04-12"),
                Map.of("id", "RSV-1029", "unidad", "Hostal Valdivia", "huesped", "Luis Soto",
                        "estado", "CHECKOUT", "desde", "2026-04-01", "hasta", "2026-04-04")));
    }

    @GetMapping("/catalog/units")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public ResponseEntity<List<Map<String, Object>>> catalog() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "U-01", "nombre", "Cabaña Ñuble", "tipo", "cabaña", "tarifa", 62000, "disponible", true),
                Map.of("id", "U-02", "nombre", "Hostal Sur", "tipo", "habitación", "tarifa", 28000, "disponible", true),
                Map.of("id", "U-03", "nombre", "Lodge Pucón", "tipo", "lodge", "tarifa", 95000, "disponible", false),
                Map.of("id", "U-04", "nombre", "Hostal Valdivia", "tipo", "habitación", "tarifa", 31000, "disponible", true)));
    }

    @GetMapping("/audit/timeline")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
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
    @PreAuthorize("hasRole('Admin')")
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

    private static String username(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String preferred = jwt.getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
            String name = jwt.getClaimAsString("name");
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        return authentication.getName();
    }

    private static List<String> roles(Authentication authentication) {
        if (authentication == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .sorted()
                .collect(Collectors.toList());
    }

    private static List<String> authorities(Authentication authentication) {
        if (authentication == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
    }

    private static Object nullSafe(String value) {
        return value == null ? "" : value;
    }
}

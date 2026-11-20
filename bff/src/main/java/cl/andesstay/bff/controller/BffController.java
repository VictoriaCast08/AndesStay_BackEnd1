package cl.duoc.andesstay.bff.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@Profile("azure")
@RequestMapping("/api/bff")
public class BffController {

    private final RestClient restClient;

    public BffController(@Value("${andesstay.services.reservations-url}") String reservationsUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(reservationsUrl)
                .build();
    }

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

    // LISTAR RESERVAS DESDE EL MICROSERVICIO / API MANAGER
    @GetMapping("/reservations")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public List<Object> getReservations(@RequestHeader(value = "Authorization", required = false) String token) {
        return restClient.get()
                .uri("/api/reservations")
                .header("Authorization", token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Object>>() {});
    }

    // CREAR RESERVA EN EL MICROSERVICIO / API MANAGER
    @PostMapping("/reservations")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public Object createReservation(@RequestBody Object reservationBody,
                                    @RequestHeader(value = "Authorization", required = false) String token) {
        return restClient.post()
                .uri("/api/reservations")
                .header("Authorization", token)
                .body(reservationBody)
                .retrieve()
                .body(Object.class);
    }

    // CAMBIAR ESTADO DE RESERVA EN EL MICROSERVICIO
    @PutMapping("/reservations/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public Object changeStatus(@PathVariable Long id,
                               @RequestBody Object statusBody,
                               @RequestHeader(value = "Authorization", required = false) String token) {
        return restClient.put()
                .uri("/api/reservations/{id}/status", id)
                .header("Authorization", token)
                .body(statusBody)
                .retrieve()
                .body(Object.class);
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
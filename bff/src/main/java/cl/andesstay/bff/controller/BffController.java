package cl.andesstay.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BffController {

    private final RestClient restClient;
    private final String catalogUrl;
    private final String reservationsUrl;
    private final String notifyUrl;
    private final String auditUrl;
    private final String reportUrl;

    public BffController(
            @Value("${andesstay.services.catalog-url:http://catalog:8081}") String catalogUrl,
            @Value("${andesstay.services.reservations-url:http://reservations:8082}") String reservationsUrl,
            @Value("${andesstay.services.notify-url:http://notify:8083}") String notifyUrl,
            @Value("${andesstay.services.audit-url:http://audit:8084}") String auditUrl,
            @Value("${andesstay.services.report-url:http://report:8085}") String reportUrl) {
        this.restClient = RestClient.create();
        this.catalogUrl = catalogUrl;
        this.reservationsUrl = reservationsUrl;
        this.notifyUrl = notifyUrl;
        this.auditUrl = auditUrl;
        this.reportUrl = reportUrl;
    }

    // =========================================================================
    // 1. ENDPOINTS PROPIOS DEL BFF (/api/bff/...)
    // =========================================================================

    @GetMapping("/bff/ping")
    public ResponseEntity<Object> ping(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "andesstay-bff",
                "timestamp", Instant.now().toString(),
                "user", username(authentication)
        ));
    }

    @GetMapping("/bff/me")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente','Auditor')")
    public ResponseEntity<Object> me(Authentication authentication) {
        Map<String, Object> claims = Map.of();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            claims = Map.of(
                    "sub", nullSafe(jwt.getSubject()),
                    "preferred_username", nullSafe(jwt.getClaimAsString("preferred_username")),
                    "name", nullSafe(jwt.getClaimAsString("name")),
                    "iss", nullSafe(jwt.getIssuer() != null ? jwt.getIssuer().toString() : null),
                    "aud", jwt.getAudience() != null ? jwt.getAudience() : List.of(),
                    "roles", roles(authentication),
                    "exp", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : ""
            );
        }
        return ResponseEntity.ok(Map.of(
                "user", username(authentication),
                "roles", roles(authentication),
                "claims", claims
        ));
    }

    @GetMapping("/bff/dashboard")
    @PreAuthorize("hasAnyRole('Admin','Operador','Auditor')")
    public ResponseEntity<Object> dashboard(Authentication authentication, HttpServletRequest request) {
        Object kpis = safeGet(reportUrl + "/api/report/kpis", request);
        Object topUnits = safeGet(reportUrl + "/api/report/top-units", request);
        return ResponseEntity.ok(Map.of(
                "user", username(authentication),
                "roles", roles(authentication),
                "kpis", kpis != null ? kpis : Map.of(),
                "topUnits", topUnits != null ? topUnits : List.of()
        ));
    }

    @GetMapping("/bff/reservations")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public ResponseEntity<Object> getBffReservations(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String uri = reservationsUrl + "/api/reservations" + (status != null ? "?status=" + status : "");
        return forward(HttpMethod.GET, uri, null, request);
    }

    @PostMapping("/bff/reservations")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public ResponseEntity<Object> createBffReservation(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.POST, reservationsUrl + "/api/reservations", body, request);
    }

    @PutMapping("/bff/reservations/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public ResponseEntity<Object> updateBffReservationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.PUT, reservationsUrl + "/api/reservations/" + id + "/status", body, request);
    }

    // =========================================================================
    // 2. CATALOG (/api/catalog/...) -> catalog:8081
    // =========================================================================

    @GetMapping("/catalog/units")
    public ResponseEntity<Object> getCatalogUnits(HttpServletRequest request) {
        return forward(HttpMethod.GET, catalogUrl + "/api/catalog/units", null, request);
    }

    @PostMapping("/catalog/units")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Object> createCatalogUnit(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.POST, catalogUrl + "/api/catalog/units", body, request);
    }

    @PutMapping("/catalog/units/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public ResponseEntity<Object> updateCatalogUnit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.PUT, catalogUrl + "/api/catalog/units/" + id, body, request);
    }

    // =========================================================================
    // 3. RESERVATIONS (/api/reservations/...) -> reservations:8082
    // =========================================================================

    @GetMapping("/reservations")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public ResponseEntity<Object> getReservations(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String uri = reservationsUrl + "/api/reservations" + (status != null ? "?status=" + status : "");
        return forward(HttpMethod.GET, uri, null, request);
    }

    @GetMapping("/reservations/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public ResponseEntity<Object> getReservationById(
            @PathVariable Long id,
            HttpServletRequest request) {
        return forward(HttpMethod.GET, reservationsUrl + "/api/reservations/" + id, null, request);
    }

    @PostMapping("/reservations")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public ResponseEntity<Object> createReservation(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.POST, reservationsUrl + "/api/reservations", body, request);
    }

    @PutMapping("/reservations/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public ResponseEntity<Object> updateReservationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.PUT, reservationsUrl + "/api/reservations/" + id + "/status", body, request);
    }

    // =========================================================================
    // 4. NOTIFY & RABBITMQ ADMIN -> notify:8083
    // =========================================================================

    @PostMapping("/notifications")
    public ResponseEntity<Object> sendNotification(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return forward(HttpMethod.POST, notifyUrl + "/api/notifications", body, request);
    }

    @GetMapping("/admin/rabbit/queues")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Object> getRabbitQueues(HttpServletRequest request) {
        return forward(HttpMethod.GET, notifyUrl + "/api/admin/rabbit/queues", null, request);
    }

    @DeleteMapping("/admin/rabbit/queues/{name}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Object> deleteRabbitQueue(
            @PathVariable String name,
            HttpServletRequest request) {
        return forward(HttpMethod.DELETE, notifyUrl + "/api/admin/rabbit/queues/" + name, null, request);
    }

    @GetMapping("/admin/rabbit/exchanges")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Object> getRabbitExchanges(HttpServletRequest request) {
        return forward(HttpMethod.GET, notifyUrl + "/api/admin/rabbit/exchanges", null, request);
    }

    @DeleteMapping("/admin/rabbit/exchanges/{name}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Object> deleteRabbitExchange(
            @PathVariable String name,
            HttpServletRequest request) {
        return forward(HttpMethod.DELETE, notifyUrl + "/api/admin/rabbit/exchanges/" + name, null, request);
    }

    // =========================================================================
    // 5. AUDIT & REPORT -> audit:8084 / report:8085
    // =========================================================================

    @GetMapping("/audit/timeline")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
    public ResponseEntity<Object> getAuditTimeline(HttpServletRequest request) {
        return forward(HttpMethod.GET, auditUrl + "/api/audit/timeline", null, request);
    }

    @GetMapping("/report/kpis")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Object> getReportKpis(HttpServletRequest request) {
        return forward(HttpMethod.GET, reportUrl + "/api/report/kpis", null, request);
    }

    @GetMapping("/report/top-units")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Object> getReportTopUnits(HttpServletRequest request) {
        return forward(HttpMethod.GET, reportUrl + "/api/report/top-units", null, request);
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE REENVÍO Y SEGURIDAD
    // =========================================================================

    private ResponseEntity<Object> forward(HttpMethod method, String uri, Object body, HttpServletRequest request) {
        try {
            RestClient.RequestBodySpec spec = restClient.method(method).uri(uri);
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && !auth.isBlank()) {
                spec.header(HttpHeaders.AUTHORIZATION, auth);
            }
            if (body != null) {
                spec.body(body);
            }
            return spec.retrieve().toEntity(Object.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    private Object safeGet(String uri, HttpServletRequest request) {
        try {
            return forward(HttpMethod.GET, uri, null, request).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    private String username(Authentication auth) {
        if (auth == null) return "anonymous";
        if (auth.getPrincipal() instanceof Jwt jwt) {
            String pref = jwt.getClaimAsString("preferred_username");
            return pref != null ? pref : jwt.getSubject();
        }
        return auth.getName();
    }

    private List<String> roles(Authentication auth) {
        if (auth == null) return List.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .toList();
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
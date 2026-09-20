package cl.duoc.andesstay.report.web;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@PreAuthorize("hasRole('Admin')")
public class ReportController {

    @GetMapping("/kpis")
    public Map<String, Object> kpis(@RequestParam(defaultValue = "last24h") String range) {
        return Map.of(
                "range", range,
                "reservasPorHora", Map.of("10", 2, "11", 3, "15", 4),
                "tiempoCicloHoras", 36,
                "ocupacionActivaPct", 72);
    }

    @GetMapping("/top-units")
    public List<Map<String, Object>> topUnits(@RequestParam(defaultValue = "last7d") String range) {
        return List.of(
                Map.of("unidad", "Cabana Nuble", "reservas", 6),
                Map.of("unidad", "Hostal Sur", "reservas", 4));
    }
}

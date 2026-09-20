package cl.duoc.andesstay.catalog.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.andesstay.catalog.domain.Unit;
import cl.duoc.andesstay.catalog.repo.UnitRepository;

@RestController
@RequestMapping("/api/catalog")
public class UnitController {

    private final UnitRepository repository;

    public UnitController(UnitRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/units")
    public List<Unit> list() {
        return repository.findAll();
    }

    @PostMapping("/units")
    @PreAuthorize("hasRole('Admin')")
    public Unit create(@RequestBody Unit body) {
        body.setId(null);
        return repository.save(body);
    }

    @PutMapping("/units/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public Unit update(@PathVariable Long id, @RequestBody Unit body) {
        Unit unit = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
        if (body.getName() != null) unit.setName(body.getName());
        if (body.getType() != null) unit.setType(body.getType());
        if (body.getRate() != null) unit.setRate(body.getRate());
        unit.setAvailable(body.isAvailable());
        return repository.save(unit);
    }
}

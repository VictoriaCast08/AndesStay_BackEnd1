package cl.andesstay.catalog.web;

import cl.andesstay.catalog.domain.Unit;
import cl.andesstay.catalog.repo.UnitRepository;
import cl.andesstay.catalog.kafka.KafkaEventProducerService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/catalog")
public class UnitController {

    private final UnitRepository repository;
    private final KafkaEventProducerService kafkaProducer;

    public UnitController(UnitRepository repository, KafkaEventProducerService kafkaProducer) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
    }

    @GetMapping("/units")
    public List<Unit> list() {
        return repository.findAll();
    }

    @PostMapping("/units")
    @PreAuthorize("hasRole('Admin')")
    public Unit create(@RequestBody Unit body, Authentication authentication) {
        body.setId(null);
        Unit saved = repository.save(body);
        String userId = authentication != null ? authentication.getName() : "system";
        kafkaProducer.publishUnitCreated(saved, userId);
        return saved;
    }

    @PutMapping("/units/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public Unit update(@PathVariable Long id, @RequestBody Unit body, Authentication authentication) {
        Unit unit = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
        if (body.getName() != null) unit.setName(body.getName());
        if (body.getType() != null) unit.setType(body.getType());
        if (body.getRate() != null) unit.setRate(body.getRate());
        unit.setAvailable(body.isAvailable());
        Unit updated = repository.save(unit);
        String userId = authentication != null ? authentication.getName() : "system";
        kafkaProducer.publishUnitUpdated(updated, userId);
        return updated;
    }
}

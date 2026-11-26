package cl.andesstay.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.andesstay.catalog.domain.Unit;

public interface UnitRepository extends JpaRepository<Unit, Long> {}

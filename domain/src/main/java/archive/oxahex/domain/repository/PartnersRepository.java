package archive.oxahex.domain.repository;

import archive.oxahex.domain.entity.Partners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnersRepository extends JpaRepository<Partners, Long> {

    boolean existsByName(String name);
    Optional<Partners> findByName(String name);
}

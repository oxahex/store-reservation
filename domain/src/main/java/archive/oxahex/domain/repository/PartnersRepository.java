package archive.oxahex.domain.repository;

import archive.oxahex.domain.entity.Partners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnersRepository extends JpaRepository<Partners, Long> {

    boolean existsByBusinessNumber(String businessNumber);
}

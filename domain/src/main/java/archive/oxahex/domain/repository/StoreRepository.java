package archive.oxahex.domain.repository;

import archive.oxahex.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByBusinessNumber(String businessNumber);
}

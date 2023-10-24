package archive.oxahex.domain.repository;

import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByBusinessNumber(String businessNumber);
    List<Store> findAllByOrderByRegisteredDateAsc();
    List<Store> findAllByOrderByRatingAsc();

    List<Store> findAllByPartners(Partners partners);

    Store findByBusinessNumber(String businessNumber);
}

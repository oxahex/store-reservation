package archive.oxahex.domain.repository;

import archive.oxahex.domain.entity.Review;
import archive.oxahex.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByStore(Store store);
}

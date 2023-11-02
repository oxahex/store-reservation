package archive.oxahex.domain.repository;

import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.type.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByStoreAndStatus(Store store, ReservationStatus status);
    List<Reservation> findAllByUser(User user);
    List<Reservation> findAllByUserAndStatus(User user, ReservationStatus status);
}

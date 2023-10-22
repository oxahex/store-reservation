package archive.oxahex.domain.entity;

import archive.oxahex.domain.type.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    @Column(name = "use_table_count")
    private Integer useTableCount;
}

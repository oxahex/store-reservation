package archive.oxahex.domain.entity;

import archive.oxahex.domain.type.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    @Column(name = "use_table_count")
    private Integer useTableCount;

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Builder
    private Reservation(
            User user,
            Store store,
            ReservationStatus status,
            LocalDateTime visitDate,
            Integer useTableCount
    ) {
        this.user = user;
        this.store = store;
        this.status = status;
        this.visitDate = visitDate;
        this.useTableCount = useTableCount;
    }
}

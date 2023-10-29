package archive.oxahex.domain.entity;

import archive.oxahex.domain.type.ReservationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private Integer rating;
    private String content;

    @Builder
    public Review(Reservation reservation, Integer rating, String content) {
        this.user = reservation.getUser();

        reservation.getStore().increaseReviewCount();
        this.store = reservation.getStore();

        this.rating = rating;
        this.content = content;

        reservation.setStatus(ReservationStatus.REVIEWED);
    }
}

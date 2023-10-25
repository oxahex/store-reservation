package archive.oxahex.domain.entity;

import archive.oxahex.domain.type.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private Integer rating;
    private String content;

    public void createReview(Reservation reservation, int rate, String content) {
        // 유저 정보
        this.user = reservation.getUser();
        // 별점
        this.rating = rate;
        // 리뷰 내용
        this.content = content;
        // 매장 리뷰 개수 증가 및 저장
        Store store = reservation.getStore();
        store.increaseReviewCount();
        this.store = store;
        // 예약 상태 REVIEWED로 변경
        reservation.setStatus(ReservationStatus.REVIEWED);
    }
}

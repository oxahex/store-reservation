package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Review;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.repository.ReviewRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.type.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;

    /**
     * 예약 건 당 하나의 리뷰 작성
     * 해당 유저의 예약 내역 중 예약 상태가 CONFIRMED인 경우 리뷰 작성 가능
     */
    @Transactional
    public Review addReview(Long reservationId, Integer rating, String content) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorType.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.REVIEWED) {
            throw new CustomException(ErrorType.ALREADY_REVIEW_STORE);
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorType.INVALID_REVIEW_REQUEST);
        }

        Review review = new Review();
        review.createReview(reservation, rating, content);

        reviewRepository.save(review);

        return review;
    }

    /**
     * 특정 매장의 리뷰 조회
     */
    public List<Review> getAllReviews(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorType.STORE_NOT_FOUND));

        return reviewRepository.findAllByStore(store);
    }
}

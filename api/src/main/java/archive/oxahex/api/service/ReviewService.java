package archive.oxahex.api.service;

import archive.oxahex.api.dto.request.ReviewModifyRequest;
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
import java.util.Objects;

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

        Review review = Review.builder()
                .reservation(reservation)
                .rating(rating)
                .content(content)
                .build();

        reviewRepository.save(review);

        return review;
    }

    /**
     * 리뷰 수정
     * @param user 리뷰 작성자
     * @param request 변경할 리뷰 내용
     */
    @Transactional
    public Review modifyReview(User user, Long reviewId, ReviewModifyRequest request) {

        // 리뷰
        Review review = validateReviewByUser(user, reviewId);

        review.modifyReview(request.getRating(), request.getContent());

        return reviewRepository.save(review);

    }

    @Transactional
    public Review deleteReview(User user, Long reviewId) {

        // 리뷰
        Review review = validateReviewByUser(user, reviewId);

        review.getStore().decreaseReviewCount();
        reviewRepository.delete(review);

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

    private Review validateReviewByUser(User user, Long reviewId) {

        // 리뷰
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorType.REVIEW_NOT_FOUND));

        // 리뷰 작성자와 유저가 일치하지 않는 경우
        if (!Objects.equals(review.getUser().getId(), user.getId())) {
            throw new CustomException(ErrorType.REVIEW_ACCESS_DENIED);
        }

        return review;
    }

}

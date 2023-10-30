package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Review;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.repository.ReviewRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.type.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    ReviewService reviewService;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    StoreRepository storeRepository;

    @Test
    @DisplayName("리뷰 작성 시 해당 예약 건이 없으면 리뷰를 작성할 수 없다.")
    void addReview_failure_reservation_not_found() {

        // given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.addReview(1L, 3, "리뷰 내용"));

        // then
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("리뷰 작성 시 이미 해당 예약건에 대해 리뷰를 작성한 경우 추가 작성할 수 없다.")
    void addReview_failure_already_review_store() {

        // given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .status(ReservationStatus.REVIEWED)
                        .build()));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.addReview(1L, 3, "리뷰 내용"));

        // then
        assertEquals(ErrorType.ALREADY_REVIEW_STORE.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.ALREADY_REVIEW_STORE.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("리뷰 작성은 방문이 확인된 경우에만 할 수 있다.")
    void addReview_failure_visited() {

        // given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .status(ReservationStatus.ALLOWED)
                        .build()));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.addReview(1L, 3, "리뷰 내용"));

        // then
        assertEquals(ErrorType.INVALID_REVIEW_REQUEST.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.INVALID_REVIEW_REQUEST.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("리뷰 작성 시 해당 매장의 전체 리뷰 카운트 증가한다.")
    void addReview_success() {

        // given
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .partners(partners)
                .reviewCount(1)
                .build();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .status(ReservationStatus.CONFIRMED)
                        .store(store)
                        .build()));

        // when
        Review review = reviewService.addReview(1L, 3, "리뷰 내용");

        // then
        assertEquals(review.getStore().getReviewCount(), 2);
    }
}
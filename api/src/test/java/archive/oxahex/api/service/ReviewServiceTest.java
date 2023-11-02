package archive.oxahex.api.service;

import archive.oxahex.api.dto.request.ReviewModifyRequest;
import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.*;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.repository.ReviewRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.type.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("해당 리뷰가 이미 삭제된 경우 리뷰를 수정할 수 없다.")
    void modifyReview_failure_review_not_found() {

        // given
        User user = User.builder().build();

        ReviewModifyRequest request = new ReviewModifyRequest();
        request.setRating(5);
        request.setContent("새로운 리뷰 내용으로 수정");

        Partners partners = Partners.builder().build();
        Store store = Store.builder().partners(partners).reviewCount(0).build();
        Reservation reservation = Reservation.builder().user(user).store(store).build();
        Review review = Review.builder().reservation(reservation).build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.modifyReview(user, 1L, request));

        // then
        assertEquals(ErrorType.REVIEW_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.REVIEW_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("리뷰 작성자가 아닌 경우 리뷰를 수정이 불가능합니다.")
    void modifyReview_failure_user_un_matched() {
        // given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();

        Partners partners = Partners.builder().build();
        Store store = Store.builder().partners(partners).reviewCount(1).build();
        Reservation reservation = Reservation.builder().user(user1).store(store).build();
        Review review = Review.builder()
                .reservation(reservation)
                .rating(3)
                .content("이전 리뷰 내용")
                .build();

        ReviewModifyRequest request = new ReviewModifyRequest();
        request.setRating(5);
        request.setContent("새로운 리뷰 내용으로 수정");


        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));

        // when: 저장된 리뷰와 다른 사용자가 요청
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.modifyReview(user2, 1L, request));

        // then
        assertEquals(ErrorType.REVIEW_ACCESS_DENIED.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.REVIEW_ACCESS_DENIED.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("리뷰 변경 시 변경된 리뷰 내용을 반환한다.")
    void modifyReview_success() {
        // given
        User user = User.builder().id(1L).build();

        Partners partners = Partners.builder().build();
        Store store = Store.builder().partners(partners).reviewCount(1).build();
        Reservation reservation = Reservation.builder().user(user).store(store).build();
        Review review = Review.builder()
                .reservation(reservation)
                .rating(3)
                .content("이전 리뷰 내용")
                .build();

        ReviewModifyRequest request = new ReviewModifyRequest();
        request.setRating(5);
        request.setContent("새로운 리뷰 내용으로 수정");


        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));

        given(reviewRepository.save(any(Review.class)))
                .willReturn(review);

        // when
        Review modifiedReview = reviewService.modifyReview(user, 1L, request);

        // then
        assertEquals(modifiedReview.getContent(), "새로운 리뷰 내용으로 수정");
        assertEquals(modifiedReview.getRating(), 5);
    }

    @Test
    @DisplayName("리뷰가 이미 삭제된 경우 리뷰를 삭제할 수 없습니다.")
    void deleteReview_failure_review_not_found() {

        // given
        User user = User.builder().build();

        Partners partners = Partners.builder().build();
        Store store = Store.builder().partners(partners).reviewCount(0).build();
        Reservation reservation = Reservation.builder().user(user).store(store).build();
        Review review = Review.builder().reservation(reservation).build();

        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.deleteReview(user, 1L));

        // then
        assertEquals(ErrorType.REVIEW_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.REVIEW_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("리뷰 작성자가 아닌 경우 리뷰를 삭제할 수 없습니다.")
    void deleteReview_failure_user_un_matched() {
        // given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();

        Partners partners = Partners.builder().build();
        Store store = Store.builder().partners(partners).reviewCount(1).build();
        Reservation reservation = Reservation.builder().user(user1).store(store).build();
        Review review = Review.builder()
                .reservation(reservation)
                .rating(3)
                .content("리뷰 내용")
                .build();


        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));

        // when: 저장된 리뷰와 다른 사용자가 요청
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.deleteReview(user2, 1L));

        // then
        assertEquals(ErrorType.REVIEW_ACCESS_DENIED.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.REVIEW_ACCESS_DENIED.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("리뷰를 삭제하는 경우 해당 리뷰가 삭제되고, 해당 매장의 리뷰 개수가 하나 줄어듭니다.")
    void deleteReview_success() {

        // given
        User user = User.builder().id(1L).build();

        Partners partners = Partners.builder().build();
        Store store = Store.builder().partners(partners).reviewCount(1).build();
        Reservation reservation = Reservation.builder().user(user).store(store).build();
        Review review = Review.builder()
                .reservation(reservation)
                .rating(3)
                .content("리뷰 내용")
                .build();

        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));

        // when
        reviewService.deleteReview(user, 1L);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        // then

        verify(reviewRepository, times(1)).delete(captor.capture());
        System.out.println(captor.getValue().getStore().getReviewCount());
    }
}
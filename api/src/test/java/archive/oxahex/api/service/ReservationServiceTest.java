package archive.oxahex.api.service;

import archive.oxahex.api.dto.request.ReservationRequest;
import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.repository.UserRepository;
import archive.oxahex.domain.type.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Transactional
class ReservationServiceTest {

    @InjectMocks
    ReservationService reservationService;

    @Mock
    UserRepository userRepository;

    @Mock
    StoreRepository storeRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약할 상점이 없는 경우 예약할 수 없다.")
    void requestReservation_failure_store_not_found() {

        // given
        User user = User.builder()
                .email("user@gmail.com").build();

        ReservationRequest request = new ReservationRequest();

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.requestReservation(user, 1L, request));

        // then
        assertEquals(ErrorType.STORE_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.STORE_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("예약 시 상점에 남아있는 자리가 없는 경우 예약할 수 없다.")
    void requestReservation_failure_table_sold_out() {

        // given
        User user = User.builder()
                .email("user@gmail.com").build();

        ReservationRequest request = new ReservationRequest();
        request.setUseTableCount(2);

        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .partners(partners)
                .tableCount(1)
                .build();

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.requestReservation(user, 1L, request));

        // then
        assertEquals(ErrorType.TABLE_SOLD_OUT.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.TABLE_SOLD_OUT.getErrorMessage(), exception.getErrorMessage());
    }


    @Test
    @DisplayName("예약 성공 시, PENDING 상태의 예약을 반환한다.")
    void requestReservation_success() {

        // given
        User user = User.builder()
                .email("user@gmail.com").build();

        ReservationRequest request = new ReservationRequest();
        request.setUseTableCount(1);

        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .partners(partners)
                .tableCount(1)
                .build();

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));

        given(reservationRepository.save(any(Reservation.class)))
                .willReturn(Reservation.builder()
                        .user(user)
                        .store(store)
                        .status(ReservationStatus.PENDING)
                        .useTableCount(request.getUseTableCount()).build());

        // when
        Reservation createdReservation =
                reservationService.requestReservation(user, 1L, request);

        // then
        assertEquals(createdReservation.getUseTableCount(), 1);
        assertEquals(createdReservation.getStatus(), ReservationStatus.PENDING);
        assertEquals(createdReservation.getStore().getTableCount(), 1);
    }

    @Test
    @DisplayName("예약 상태 변경 시 해당 예약 건이 없으면 예약 상태를 변경할 수 없다.")
    void changeReservationStatus_failure() {

        // given
        ReservationStatus changedStatus = ReservationStatus.REJECTED;
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () ->  reservationService.changeReservationStatus(changedStatus, 1L));

        // then
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("예약 상태를 승인으로 변경하는 경우, 해당 상점의 자리가 예약 자리만큼 줄어든다.")
    void changeReservationStatus_success() {

        // given
        ReservationStatus changedStatus = ReservationStatus.ALLOWED;
        Partners partners = Partners.builder().build();

        Store store = Store.builder()
                .partners(partners)
                .tableCount(2)
                .build();
        
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .status(ReservationStatus.PENDING)
                        .store(store)
                        .useTableCount(1)
                        .build()));


        // when
        Reservation reservation =
                reservationService.changeReservationStatus(changedStatus, 1L);

        // then
        assertEquals(reservation.getStore().getTableCount(), 1);
        assertEquals(reservation.getStatus(), ReservationStatus.ALLOWED);
    }

    @Test
    @DisplayName("예약 취소 시, 해당 예약 건이 존재하지 않는 경우 취소할 수 없다.")
    void cancelReservation_failure_reservation_not_found() {

        // given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.cancelReservation(1L));

        // then
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("예약 시간으로부터 8시간 이전의 예약만 취소할 수 있다.")
    void cancelReservation_failure_after_8_hour() {

        // given
        LocalDateTime now = LocalDateTime.now();
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .visitDate(now.plusHours(8))
                        .useTableCount(1)
                        .build()));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.cancelReservation(1L));

        // then
        assertEquals(ErrorType.CANCELLABLE_TIME_OUT.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.CANCELLABLE_TIME_OUT.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("예약을 취소하는 경우 해당 상점의 테이블 수가 원복된다.")
    void cancelReservation_success() {

        // given
        LocalDateTime now = LocalDateTime.now();
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .partners(partners)
                .tableCount(1)
                .build();
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .visitDate(now.plusHours(8).plusMinutes(1))
                        .useTableCount(1)
                        .store(store)
                        .build()));

        // when
        Reservation reservation = reservationService.cancelReservation(1L);

        // then
        assertEquals(reservation.getStatus(), ReservationStatus.CANCELLED);
        assertEquals(reservation.getStore().getTableCount(), 2);
    }
}
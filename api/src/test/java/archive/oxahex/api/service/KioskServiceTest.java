package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.type.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Transactional
class KioskServiceTest {

    @InjectMocks
    KioskService kioskService;

    @Mock
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 해당 매장에 예약 건이 없는 경우 입장이 불가하다.")
    void checkStoreEntry_failure_reservation_not_found() {

        // given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkStoreEntry(1L));

        // then
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 허가된 예약이 아니면 입장이 불가하다.")
    void checkStoreEntry_failure_invalid_reservation() {

        // given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .status(ReservationStatus.PENDING)
                        .build()));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkStoreEntry(1L));

        // then
        assertEquals(ErrorType.INVALID_RESERVATION.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.INVALID_RESERVATION.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 예약 시간 이후에 도착하는 경우 입장이 불가하다.")
    void checkStoreEntry_failure_too_late_to_user() {

        // given
        LocalDateTime now = LocalDateTime.now();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .status(ReservationStatus.ALLOWED)
                        .visitDate(now.minusMinutes(1))
                        .build()));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkStoreEntry(1L));

        // then
        assertEquals(ErrorType.TOO_LATE_TO_USE.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.TOO_LATE_TO_USE.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 유효한 시간에 도착하는 경우 입장이 가능하다.")
    void checkStoreEntry_success() {

        // given
        LocalDateTime now = LocalDateTime.now();
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .partners(partners)
                .tableCount(1)
                .build();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(Reservation.builder()
                        .status(ReservationStatus.ALLOWED)
                        .store(store)
                        .useTableCount(1)
                        .visitDate(now.plusMinutes(1))
                        .build()));

        // when
        Reservation reservation = kioskService.checkStoreEntry(1L);

        // then
        assertEquals(reservation.getStatus(), ReservationStatus.CONFIRMED);
        assertEquals(reservation.getStore().getTableCount(), 2);
    }


}
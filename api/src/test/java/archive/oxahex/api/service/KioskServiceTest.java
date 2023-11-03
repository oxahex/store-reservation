package archive.oxahex.api.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Transactional
class KioskServiceTest {

    @InjectMocks
    KioskService kioskService;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    StoreRepository storeRepository;

    @Test
    @DisplayName("이메일로 유저를 찾을 수 없는 경우 키오스크에서 예약 내역 확인이 불가합니다.")
    void getReservation_failure_user_not_found() {

        // given
        User user = User.builder()
                .email("user@email.com")
                .build();

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.getReservations(1L, "user@email.com"));

        // then
        assertEquals(ErrorType.USER_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.USER_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("해당 매장의 정보가 없는 경우 키오스크에서 예약 내역 확인이 불가합니다.")
    void getReservation_failure_store_not_found() {

        // given
        User user = User.builder()
                .email("user@email.com")
                .build();

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.of(user));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.getReservations(1L, "user@email.com"));

        // then
        assertEquals(ErrorType.STORE_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.STORE_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("예약 내역이 없는 경우 키오스크에서 예약 내역 확인이 불가합니다.")
    void getReservation_failure_reservation_not_found() {

        // given
        User user = User.builder()
                .email("user@email.com")
                .build();

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.of(user));

        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .partners(partners).build();
        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));

        given(reservationRepository.getReservationsOnKiosk(any(Store.class), any(User.class)))
                .willReturn(List.of());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.getReservations(1L, "user@email.com"));

        // then
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("키오스크에서 예약 내역 확인 시, 내역이 있는 경우 해당 예약 리스트 정보를 반환합니다.")
    void getReservation_success() {

        // given
        User user = User.builder()
                .email("user@email.com")
                .build();

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.of(user));

        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .partners(partners).build();
        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));

        Reservation reservation1 = Reservation.builder()
                .build();
        Reservation reservation2 = Reservation.builder().build();
        given(reservationRepository.getReservationsOnKiosk(any(Store.class), any(User.class)))
                .willReturn(List.of(reservation1, reservation2));

        // when
        List<Reservation> reservations =
                kioskService.getReservations(1L, "user@gmail.com");

        // then
        assertEquals(reservations.size(), 2);
    }

    @Test
    @DisplayName("해당 매장의 키오스크가 아닌 경우 예약 확인이 불가합니다.")
    void checkStoreEntry_failure_un_match_kiosk_store() {
        // given
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .id(1L)
                .partners(partners).build();

        Reservation reservation = Reservation.builder()
                .store(store)
                .build();


        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkStoreEntry(2L, 1L));

        // then
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 해당 매장에 예약 건이 없는 경우 입장이 불가하다.")
    void checkStoreEntry_failure_reservation_not_found() {

        // given
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .id(1L)
                .partners(partners).build();

        // 예약 건 없음
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkStoreEntry(1L, 1L));

        // then
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.RESERVATION_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 허가된 예약이 아니면 입장이 불가하다.")
    void checkStoreEntry_failure_invalid_reservation() {

        // given
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .id(1L)
                .partners(partners).build();

        Reservation reservation = Reservation.builder()
                .store(store)
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(reservation));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkStoreEntry(1L, 1L));

        // then
        assertEquals(ErrorType.INVALID_RESERVATION.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.INVALID_RESERVATION.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 예약 시간 이후에 도착하는 경우 입장이 불가하다.")
    void checkStoreEntry_failure_too_late_to_user() {

        // given
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .id(1L)
                .partners(partners).build();

        LocalDateTime now = LocalDateTime.now();

        // 예약 시간 = 현재 시간 9분 이후
        Reservation reservation = Reservation.builder()
                .store(store)
                .visitDate(now.plusMinutes(9))
                .status(ReservationStatus.ALLOWED)
                .build();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(reservation));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkStoreEntry(1L, 1L));

        // then
        assertEquals(ErrorType.TOO_LATE_TO_USE.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.TOO_LATE_TO_USE.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("키오스크에서 매장 도착 확인 시 예약 시간 10분 전 도착 시 입장이 가능하다.")
    void checkStoreEntry_success() {

        // given
        Partners partners = Partners.builder().build();
        Store store = Store.builder()
                .id(1L)
                .tableCount(10)
                .partners(partners).build();

        LocalDateTime now = LocalDateTime.now();

        // 예약 시간 = 현재 시간 11분 이후
        Reservation reservation = Reservation.builder()
                .store(store)
                .visitDate(now.plusMinutes(11))
                .useTableCount(1)
                .status(ReservationStatus.ALLOWED)
                .build();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(reservation));


        // when
        Reservation usedReservation = kioskService.checkStoreEntry(1L, 1L);

        // then
        assertEquals(usedReservation.getStatus(), ReservationStatus.CONFIRMED);
        assertEquals(usedReservation.getStore().getTableCount(), 11);
    }
}
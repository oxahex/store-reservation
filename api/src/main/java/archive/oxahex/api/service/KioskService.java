package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.repository.UserRepository;
import archive.oxahex.domain.type.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KioskService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 이메일로 예약 내역 조회
     * @param email 예약 시 사용한 이메일
     * @return 해당 이메일로 예약한 리스트 반환
     */
    public List<Reservation> getReservations(Long storeId, String email) {

        // email로 유저 검색
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        // store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorType.STORE_NOT_FOUND));

        List<Reservation> reservations =
                reservationRepository.getReservationsOnKiosk(store, user);

        // 예약이 없는 경우
        if (reservations.isEmpty()) {
            throw new CustomException(ErrorType.RESERVATION_NOT_FOUND);
        }

        return reservations;
    }

    /**
     * 키오스크에서 매장 이용 확인
     * <ol>
     *     <li>예약 시간보다 늦게 확인하는 경우 매장 이용 불가</li>
     *     <li>각 매장의 키오스크는 해당 매장의 ID 정보를 가지고 있는 것으로 정의(API에 매장 ID를 함께 요청)</li>
     *     <li>매장 이용 확인 시 매장 테이블 수 원복</li>
     * </ol>
     */
    @Transactional
    public Reservation checkStoreEntry(Long storeId, Long reservationId) {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorType.RESERVATION_NOT_FOUND));

        // 해당 매장의 키오스크인지 확인
        if (!Objects.equals(reservation.getStore().getId(), storeId)) {
            throw new CustomException(ErrorType.UN_MATCH_KIOSK_STORE);
        }
        // 승인된 예약인지 확인
        if (reservation.getStatus() != ReservationStatus.ALLOWED) {
            throw new CustomException(ErrorType.INVALID_RESERVATION);
        }
        // 시간 체크: 지정한 예약 시간 이후에 도착하는 경우
        if (now.isAfter(reservation.getVisitDate().minusMinutes(10))) {
            throw new CustomException(ErrorType.TOO_LATE_TO_USE);
        }

        // 유효 시간 내에 도착한 경우
        // 테이블 수 원복
        reservation.getStore().addTableCount(reservation.getUseTableCount());
        // 예약 상태 변경
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservation;
    }
}

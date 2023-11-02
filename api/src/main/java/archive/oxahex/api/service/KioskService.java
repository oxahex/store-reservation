package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.type.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KioskService {

    private final ReservationRepository reservationRepository;

    /**
     * 키오스크에서 매장 이용 확인
     * 예약 시간보다 늦게 확인하는 경우 매장 이용 불가
     * 각 매장의 키오스크는 해당 매장의 ID 정보를 가지고 있음
     * <p>테이블 수 원복
     */
    @Transactional
    public Reservation checkStoreEntry(Long reservationId) {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorType.RESERVATION_NOT_FOUND));

        // 유효한 예약인지 확인
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

package archive.oxahex.api.service;

import archive.oxahex.api.dto.ReservationDto;
import archive.oxahex.api.dto.ReservationSearchType;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 상점 예약 요청
     * <p>상점 ID로 등록된 상점을 찾아 남아 있는 자리를 확인
     * <p>(프론트단에서 처리하더라도, 값 변경 가능하므로 다시 확인)
     */
    @Transactional
    public Reservation requestReservation(
            User user, Long storeId, ReservationDto.Request request
    ) {

        // 해당 상점의 남아 있는 자리를 반환
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorType.STORE_NOT_FOUND));

        // 요청한 테이블 수 > 가용 테이블인 경우 예외
        if (request.getUseTableCount() > store.getTableCount()) {
            throw new CustomException(ErrorType.TABLE_SOLD_OUT);
        }

        // 예약 생성, 예약 상태는 PENDING
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStore(store);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setVisitDate(request.getVisitedDate());
        reservation.setUseTableCount(request.getUseTableCount());

        return reservationRepository.save(reservation);
    }

    // TODO: 같은 데이터를 조회하는데 조건이 다양한 경우 어떻게 처리하면 좋을지?

    /**
     * 유저 정보(user id)와 상태 정보로 예약 정보를 가져옴
     * 예약 대시/승인/거절 상태별로 조회 가능
     * status가 없는 경우 전체 조회 처리
     */
    public List<Reservation> getAllReservations(
            User user, ReservationSearchType searchType
    ) {

        List<Reservation> reservations;
        if (searchType == ReservationSearchType.ALL) {
            reservations = reservationRepository.findAllByUser(user);
        } else {
            ReservationStatus status = ReservationStatus.valueToEnum(searchType.getCondition())
                    .orElseThrow(() -> new CustomException(ErrorType.INVALID_SEARCH_CONDITION));
            reservations = reservationRepository.findAllByUserAndStatus(user, status);
        }

        return reservations;
    }

    /**
     * 특정 예약 건 정보 조회
     */
    public Reservation getReservation(Long reservationId) {

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorType.RESERVATION_NOT_FOUND));
    }

    /**
     * 예약 대기 상태 목록 조회(파트너스)
     * 등록한 파트너스로 매장 목록 반환 -> 전체 매장의 대기중 예약 목록 조회
     */
    public List<Reservation> getAllPendingReservations(Partners partners) {

        // 파트너스로 등록된 모든 매장의 아이디를 가져옴
        List<Store> stores = partners.getStores();
        log.info("partners stores={}", stores);
        List<Reservation> reservations = new ArrayList<>();
        for (Store store : stores) {

            // PENDING 예약건 모두 조회
            reservations.addAll(
                    reservationRepository.findAllByStoreAndStatus(
                            store, ReservationStatus.PENDING
                    )
            );
        }

        return reservations;
    }

    /**
     * 예약 거절 또는 승인
     * 해당 예약 건과, 변경해야 하는 타입을 받아 예약 상태 변경 처리
     */
    @Transactional
    public Reservation changeReservationStatus(
            ReservationStatus status,
            Long reservationId
    ) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorType.RESERVATION_NOT_FOUND));

        // 예약 상태 변경
        reservation.setStatus(status);

        // 예약 승인인 경우 해당 매장 좌석 감소
        if (status == ReservationStatus.ALLOWED){
            reservation.getStore().removeTableCount(reservation.getUseTableCount());
        }

        return reservation;
    }


    /**
     * 예약 취소 기능
     * 예약 일자로부터 8시간 이전의 예약만 취소 가능
     */
    @Transactional
    public Reservation cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorType.RESERVATION_NOT_FOUND));

        // 현재 시간에서 8시간을 뺀다 -> A
        // 예약에 명시된 예약 일자 시간이 A보다 과거이면 취소 가능

        LocalDateTime cancellableTime = LocalDateTime.now().minusHours(8);
        if (cancellableTime.isAfter(reservation.getVisitDate())) {

            reservation.setStatus(ReservationStatus.CANCELLED);
        } else {
            throw new CustomException(ErrorType.CANCELLABLE_TIME_OUT);
        }

        return reservation;
    }
}

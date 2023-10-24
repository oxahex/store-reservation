package archive.oxahex.api.service;

import archive.oxahex.api.dto.ReservationDto;
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
        reservation.setStore(store, request.getUseTableCount());    // 이렇게 하면 헷갈릴 것 같은데, 오버라이드 된 이 메서드가 기본 setter보다 우선하게 강제할 수는 없을지?
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setVisitDate(request.getVisitedDate());
        reservation.setUseTableCount(request.getUseTableCount());   // 해당 매장의 테이블 수 감소 처리(예약 생성 시 자동 감소)

        return reservationRepository.save(reservation);
    }

    // TODO: 같은 데이터를 조회하는데 조건이 다양한 경우 어떻게 처리하면 좋을지?

    /**
     * 유저 정보(user id)와 상태 정보로 예약 정보를 가져옴
     * 예약 대시/승인/거절 상태별로 조회 가능
     * status가 없는 경우 전체 조회 처리
     */
    public List<Reservation> getAllReservations(ReservationStatus status) {


        return null;
    }

    /**
     * 예약 대기 상태 목록 조회
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
}

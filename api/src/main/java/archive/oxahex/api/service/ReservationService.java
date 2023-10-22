package archive.oxahex.api.service;

import archive.oxahex.api.dto.ReservationDto;
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

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 상점 예약 요청
     * <p>상점 ID로 등록된 상점을 찾아 남아 있는 자리를 확인
     * <p>(프론트단에서 처리하더라도, 값 변경 가능하므로 다시 확인)
     */
    public ReservationDto.Detail requestReservation(
            String email, Long storeId, ReservationDto.Request request
    ) {

        // 요청 유저
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        // 해당 상점의 남아 있는 자리를 반환
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorType.STORE_NOT_FOUND));

        // 요청한 테이블 수 > 가용 테이블인 경우 예외
        if (request.getUseTableCount() > store.getTableCount()) {
            throw new CustomException(ErrorType.TABLE_SOLD_OUT);
        }

        // 예약 생성, 예약 상태는 PENDING
        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .status(ReservationStatus.PENDING)
                        .visitDate(request.getVisitedDate())
                        .useTableCount(request.getUseTableCount())
                        .build()
        );

        return ReservationDto.fromEntityToReservationDetail(reservation);
    }
}

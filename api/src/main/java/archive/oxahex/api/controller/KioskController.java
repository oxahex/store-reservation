package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReservationDto;
import archive.oxahex.api.dto.request.KioskRequest;
import archive.oxahex.api.service.KioskService;
import archive.oxahex.domain.entity.Reservation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/kiosk")
@RequiredArgsConstructor
public class KioskController {

    private final KioskService kioskService;

    /**
     * 키오스크에서 유저의 예약 리스트 조회
     * @param storeId 해당 키오스크에 저장된 매장 ID
     * @param request 유저 Email
     * @return 해당 유저 Email로 해당 매장에 등록된 예약건 리스트 반환
     */
    @GetMapping("/stores/{storeId}/reservations")
    public ResponseEntity<List<ReservationDto.Info>> getReservations(
            @PathVariable Long storeId,
            @RequestBody @Valid KioskRequest request
    ) {
        List<Reservation> reservations =
                kioskService.getReservations(storeId, request.getEmail());

        List<ReservationDto.Info> reservationInfos =
                reservations.stream()
                .map(ReservationDto::fromEntityToReservationInfo)
                .toList();

        return ResponseEntity.ok().body(reservationInfos);
    }

    /**
     * 키오스크 입장 확인
     * <ol>
     *     <li>도착 시간 10분 전인 경우만 입장 확인 가능</li>
     * </ol>
     */
    @PostMapping("/stores/{storeId}/reservations/{reservationId}")
    public ResponseEntity<ReservationDto.Info> confirmReservation(
            @PathVariable Long storeId,
            @PathVariable Long reservationId
    ) {
        Reservation reservation = kioskService.checkStoreEntry(storeId, reservationId);

        ReservationDto.Info reservationInfo =
                ReservationDto.fromEntityToReservationInfo(reservation);

        return ResponseEntity.ok().body(reservationInfo);
    }
}

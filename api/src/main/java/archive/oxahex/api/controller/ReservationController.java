package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReservationDto;
import archive.oxahex.api.dto.ReservationSearchType;
import archive.oxahex.api.dto.request.ReservationRequest;
import archive.oxahex.api.security.AuthUser;
import archive.oxahex.api.service.KioskService;
import archive.oxahex.api.service.ReservationService;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.User;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final KioskService kioskService;

    /**
     * 매장 예약 요청
     */
    @PostMapping("/stores/{storeId}")
    public ResponseEntity<ReservationDto.Detail> requestReservation(
            @PathVariable Long storeId,
            @RequestBody @Valid ReservationRequest request
    ) {

        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        Reservation reservation =
                reservationService.requestReservation(user, storeId, request);

        return ResponseEntity.ok().body(
                ReservationDto.fromEntityToReservationDetail(reservation)
        );
    }

    /**
     * 예약 내역 조회 기능
     * <ol>
     *     <li>검색 타입 PENDING, CANCELLED, ALLOWED, REJECTED, CONFIRMED, REVIEWED</li>
     *     <li>path param 미입력 시 전체 검색</li>
     * </ol>
     */
    @GetMapping
    public ResponseEntity<List<ReservationDto.Info>> getReservations(
            @PathParam("status") String status
    ) {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        ReservationSearchType searchType =
                ReservationSearchType.getReservationSearchType(status);

        List<Reservation> reservations =
                reservationService.getAllReservations(user, searchType);

        List<ReservationDto.Info> reservationsInfos = reservations.stream()
                .map(ReservationDto::fromEntityToReservationInfo)
                .toList();

        return ResponseEntity.ok().body(reservationsInfos);
    }

    /**
     * 특정 예약 내역 조회
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getReservation(
            @PathVariable Long reservationId
    ) {
        Reservation reservation = reservationService.getReservation(reservationId);
        ReservationDto.Detail reservationDetail =
                ReservationDto.fromEntityToReservationDetail(reservation);

        return ResponseEntity.ok().body(reservationDetail);
    }

    /**
     * 특정 예약 취소 기능
     * <ol>
     *     <li>PENDING 상태의 예약인 경우만 취소 가능</li>
     *     <li>예약 일자 8시간 이전에 예약된 건만 취소 가능</li>
     * </ol>
     */
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationDto.Detail> cancelReservation(
            @PathVariable Long reservationId
    ) {
        Reservation reservation =
                reservationService.cancelReservation(reservationId);

        ReservationDto.Detail reservationDetail =
                ReservationDto.fromEntityToReservationDetail(reservation);

        return ResponseEntity.ok().body(reservationDetail);
    }
}

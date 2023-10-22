package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReservationDto;
import archive.oxahex.api.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 매장 예약 요청 기능
     */
    @PostMapping("/{storeId}")
    public ResponseEntity<ReservationDto.Detail> requestReservation(
            Authentication auth,
            @PathVariable Long storeId,
            @RequestBody ReservationDto.Request request
    ) {

        String email = auth.getName();
        ReservationDto.Detail reservationDetail =
                reservationService.requestReservation(email, storeId, request);

        return ResponseEntity.ok().body(reservationDetail);
    }
}

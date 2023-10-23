package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReservationDto;
import archive.oxahex.api.service.ReservationService;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation")
@PreAuthorize("hasRole('USER') or hasRole('PARTNERS')")
@RequiredArgsConstructor
public class ReservationController {

    private final AuthService userService;
    private final ReservationService reservationService;

    /**
     * 매장 예약 요청 기능
     */
    @PostMapping("/{storeId}")
    public ResponseEntity<ReservationDto.Detail> requestReservation(
            Authentication auth,
            @PathVariable Long storeId,
            @RequestBody @Valid ReservationDto.Request request
    ) {

        User user = userService.loadUserByAuth(auth);
        Reservation reservation =
                reservationService.requestReservation(user, storeId, request);



        return ResponseEntity.ok().body(
                ReservationDto.fromEntityToReservationDetail(reservation)
        );
    }
}

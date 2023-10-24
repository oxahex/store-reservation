package archive.oxahex.api.controller;

import archive.oxahex.api.dto.PartnersDto;
import archive.oxahex.api.dto.ReservationDto;
import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.PartnersService;
import archive.oxahex.api.service.ReservationService;
import archive.oxahex.api.service.StoreService;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/partners")
@PreAuthorize("hasRole('PARTNERS')")
@RequiredArgsConstructor
public class PartnersController {

    private final AuthService userService;
    private final StoreService storeService;
    private final PartnersService partnersService;
    private final ReservationService reservationService;

    private final TokenProvider tokenProvider;

    /**
     * 파트너스 정보 조회
     * 유저가 생성한 파트너스 정보 조회
     */
    @GetMapping
    public ResponseEntity<PartnersDto.Detail> getPartners(Authentication auth) {
        User user = userService.loadUserByAuth(auth);

        Partners partners = partnersService.getPartners(user);
        PartnersDto.Detail partnersDetail =
                PartnersDto.fromEntityToPartnersDetail(partners, user);

        return ResponseEntity.ok().body(partnersDetail);
    }


    /**
     * PARTNERS 회원인 경우 매장 등록
     * 매장 정보를 입력값으로 받음
     * 매장 등록 성공 시 등록 정보 반환
     */
    @PostMapping("/store")
    public ResponseEntity<StoreDto.Info> registerStore(
            Authentication auth,
            @RequestBody @Valid StoreDto.Request request
    ) {

        // 유저
        User user = userService.loadUserByAuth(auth);

        // 등록한 매장 정보 데이터 받음
        Store store = storeService.registerStore(user, request);
        StoreDto.Info storeInfo = StoreDto.fromEntityToStoreInfo(store);

        // 매장 정보 반환
        return ResponseEntity.ok().body(storeInfo);
    }

    /**
     * 대기 상태 예약 목록 조회(매장 별 조회 x, 전체 조회)
     */
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto.Info>> getAllPendingReservation(Authentication auth) {

        User user = userService.loadUserByAuth(auth);
        Partners partners = partnersService.getPartners(user);

        // 예약 상태 타입으로 조회할 수 있는 메서드를 만들고, 재사용하면?
        List<Reservation> pendingReservations =
                reservationService.getAllPendingReservations(partners);

        List<ReservationDto.Info> reservationInfos =
                pendingReservations.stream()
                        .map(ReservationDto::fromEntityToReservationInfo)
                        .toList();

        return ResponseEntity.ok().body(reservationInfos);
    }
}

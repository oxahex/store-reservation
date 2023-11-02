package archive.oxahex.api.controller;

import archive.oxahex.api.dto.PartnersDto;
import archive.oxahex.api.dto.ReservationDto;
import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.dto.request.StoreModifyRequest;
import archive.oxahex.api.dto.request.StoreRegisterRequest;
import archive.oxahex.api.security.AuthUser;
import archive.oxahex.api.service.PartnersService;
import archive.oxahex.api.service.ReservationService;
import archive.oxahex.api.service.StoreService;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.type.ReservationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/partners")
@PreAuthorize("hasRole('PARTNERS')")
@RequiredArgsConstructor
public class PartnersController {

    private final StoreService storeService;
    private final PartnersService partnersService;
    private final ReservationService reservationService;

    /**
     * 파트너스 정보 조회
     * 유저가 생성한 파트너스 정보 조회
     */
    @GetMapping
    public ResponseEntity<PartnersDto.Detail> getPartners() {

        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

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
            @RequestBody @Valid StoreRegisterRequest request
    ) {

        // 유저
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        // 등록한 매장 정보 데이터 받음
        Store store = storeService.registerStore(user, request);
        StoreDto.Info storeInfo = StoreDto.fromEntityToStoreInfo(store);

        // 매장 정보 반환
        return ResponseEntity.ok().body(storeInfo);
    }

    /**
     * 저장한 매장 정보 변경
     * 매장 ID와 변경할 정보를 받음(사업자 번호는 변경 불가)
     */
    @PutMapping("/stores/{storeId}")
    public ResponseEntity<StoreDto.Detail> modifyStore(
            @PathVariable Long storeId,
            @RequestBody @Valid StoreModifyRequest request
    ) {
        Store store = storeService.modifyStore(storeId, request);
        StoreDto.Detail storeDetail = StoreDto.fromEntityToStoreDetail(store);

        return ResponseEntity.ok().body(storeDetail);
    }

    /**
     * 대기 상태 예약 목록 조회(매장 별 조회 x, 전체 조회)
     */
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto.Info>> getAllPendingReservation() {

        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

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

    /**
     * 특정 예약 거절
     */
    @PatchMapping("/reservations/reject/{reservationId}")
    public ResponseEntity<ReservationDto.Detail> rejectReservation(
            @PathVariable Long reservationId
    ){
        Reservation reservation =
                reservationService.changeReservationStatus(ReservationStatus.REJECTED, reservationId);

        ReservationDto.Detail reservationDetail =
                ReservationDto.fromEntityToReservationDetail(reservation);

        return ResponseEntity.ok().body(reservationDetail);
    }

    /**
     * 특정 예약 승인
     */
    @PatchMapping("/reservations/allow/{reservationId}")
    public ResponseEntity<ReservationDto.Detail> allowReservation(
            @PathVariable Long reservationId
    ) {
        Reservation reservation =
                reservationService.changeReservationStatus(ReservationStatus.ALLOWED, reservationId);

        ReservationDto.Detail reservationDetail =
                ReservationDto.fromEntityToReservationDetail(reservation);

        return ResponseEntity.ok().body(reservationDetail);
    }

}

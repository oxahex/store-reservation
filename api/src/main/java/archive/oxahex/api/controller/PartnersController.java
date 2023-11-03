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
     * <p>유저가 생성한 파트너스 정보 조회</p>
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
     * 매장 등록
     * <ol>
     *     <li>PARTNERS 권한이 있는 경우에만 접근 가능</li>
     *     <li>매장 정보를 입력값으로 받음</li>
     *     <li>하나의 파트너스에 여러 개의 매장 등록 가능</li>
     *     <li>매장 등록 성공 시 등록 정보 반환</li>
     * </ol>
     */
    @PostMapping("{partnersId}/store")
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
     * <ol>
     *     <li>파트너스 ID, 매장 ID와 변경할 정보를 받음(사업자 번호는 변경 불가)</li>
     * </ol>
     */
    @PutMapping("/{partnersId}/stores/{storeId}")
    public ResponseEntity<StoreDto.Detail> modifyStore(
            @PathVariable Long partnersId,
            @PathVariable Long storeId,
            @RequestBody @Valid StoreModifyRequest request
    ) {
        // 유저
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        Store store = storeService.modifyStore(user, partnersId, storeId, request);
        StoreDto.Detail storeDetail = StoreDto.fromEntityToStoreDetail(store);

        return ResponseEntity.ok().body(storeDetail);
    }

    /**
     * 등록 매장 삭제
     * <ol>
     *     <li>파트너스 ID와 매장 ID를 받아 삭제</li>
     * </ol>
     */
    @DeleteMapping("/{partnersId}/stores/{storeId}")
    public ResponseEntity<StoreDto.Detail> deleteStore(
            @PathVariable Long partnersId,
            @PathVariable Long storeId
    ) {
        // 유저
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        Store store = storeService.deleteStore(user, partnersId, storeId);

        StoreDto.Detail deletedStoreDetail = StoreDto.fromEntityToStoreDetail(store);

        return ResponseEntity.ok().body(deletedStoreDetail);
    }

    /**
     * 대기 상태 예약 목록 조회
     * <ol>
     *     <li>매장 별 조회, 파트너스 별 조회가 아니라 등록한 모든 매장에 대한 대기 상태 예약 전체 조회</li>
     * </ol>
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
    @PatchMapping("/reservations/{reservationId}/reject")
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
    @PatchMapping("/reservations/{reservationId}/allow")
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

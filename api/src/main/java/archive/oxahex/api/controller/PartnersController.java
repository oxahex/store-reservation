package archive.oxahex.api.controller;

import archive.oxahex.api.dto.PartnersDto;
import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.PartnersService;
import archive.oxahex.api.service.ReservationService;
import archive.oxahex.api.service.StoreService;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.domain.entity.Partners;
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
     * 생성한 파트너스 전체 조회
     * 유저가 생성한 파트너스 정보 전체 조회(매장 등록 시 사용)
     */
    @GetMapping
    public ResponseEntity<List<PartnersDto.Info>> getAllPartners(Authentication auth) {
        User user = userService.loadUserByAuth(auth);

        List<Partners> partnersList = partnersService.getAllPartners(user);
        List<PartnersDto.Info> partnersInfos =
                partnersList.stream().map(PartnersDto::fromEntityToPartnersInfo).toList();

        return ResponseEntity.ok().body(partnersInfos);
    }


    /**
     * PARTNERS 회원인 경우 매장 등록
     * 연결할 파트너스 정보와 매장 정보를 입력값으로 받음
     * 매장 등록 성공 시 등록 정보 반환
     */
    @PostMapping("/{partnersName}/store")
    public ResponseEntity<StoreDto.Info> registerStore(
            @PathVariable String partnersName,
            @RequestBody @Valid StoreDto.Request request
    ) {

        // 등록한 매장 정보 데이터 받음
        Store store = storeService.registerStore(partnersName, request);
        StoreDto.Info storeInfo = StoreDto.fromEntityToStoreInfo(store);

        // 매장 정보 반환
        return ResponseEntity.ok().body(storeInfo);
    }

    /**
     * 대기 상태 예약 목록 조회(파트너스 별 조회 x, 전체 조회)
     * TODO: provider에서 파트너스 리스트를 꺼내 와도 괜찮을듯 굳이 매번 유저를 찾지 말고
     */
    @GetMapping("/reservations")
    public ResponseEntity<?> getAllPendingReservation(Authentication auth) {

        User user = userService.loadUserByAuth(auth);

        // 예약 상태 타입으로 조회할 수 있는 메서드를 만들고, 재사용하면?

//        reservationService.getAllPendingReservations()

        return null;
    }
}

package archive.oxahex.api.controller;

import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/partners")
@PreAuthorize("hasRole('PARTNERS')")
@RequiredArgsConstructor
public class PartnersController {

    private final StoreService storeService;

    /**
     * PARTNERS 회원인 경우 매장 등록
     * 매장 등록 성공 시 등록 정보 반환
     */
    @PostMapping("/store")
    public ResponseEntity<StoreDto.Info> registerStore(
            Authentication auth,
            @RequestBody @Valid StoreDto.Request request
    ) {

        // 요청 유저 이메일
        String email = auth.getName();

        // 등록한 매장 정보 데이터 받음
        StoreDto.Info storeInfo =
                storeService.registerStore(email, request);

        // 매장 정보 반환
        return ResponseEntity.ok().body(storeInfo);
    }
}

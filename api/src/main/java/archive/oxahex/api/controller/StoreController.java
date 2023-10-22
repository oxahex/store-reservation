package archive.oxahex.api.controller;

import archive.oxahex.api.dto.SortType;
import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.service.StoreService;
import archive.oxahex.domain.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stores")
@PreAuthorize("hasRole('USER') or hasRole('PARTNERS')")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 등록된 상점 조회
     * <p> 기본값: 등록일 최신순(ASC)
     * <p> 별점 순(RATING)
     */
    @GetMapping
    public ResponseEntity<List<StoreDto.Info>> stores(
            @RequestParam(required = false) String sort
    ) {

        SortType sortType = SortType.getSortType(sort);
        List<Store> stores = storeService.getAllStore(sortType);
        List<StoreDto.Info> storeInfos = stores.stream()
                .map(StoreDto::fromEntityToStoreInfo).toList();

        return ResponseEntity.ok().body(storeInfos);
    }
}

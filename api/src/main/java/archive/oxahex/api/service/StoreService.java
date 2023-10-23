package archive.oxahex.api.service;

import archive.oxahex.api.dto.SortType;
import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.api.exception.CustomException;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final PartnersRepository partnersRepository;

    /**
     * 매장 정보를 받아 새로운 매장 등록
     * 권한이 있는 사용자만 등록 가능하도록 이전에 처리하였으므로, ROLE_PARTNERS 만 등록 가능
     * 매장을 등록할 파트너스와 매장 정보를 받아 매장을 저장
     */
    @Transactional
    public Store registerStore(String partnersName, StoreDto.Request request) {

        Partners partners = partnersRepository.findByName(partnersName)
                .orElseThrow(() -> new CustomException(ErrorType.PARTNERS_NOT_FOUND));

        // 이미 등록된 매장인 경우
        boolean exists =
                storeRepository.existsByBusinessNumber(request.getBusinessNumber());
        if (exists) {
            throw new CustomException(ErrorType.ALREADY_EXIST_STORE);
        }

        // 스토어
        return storeRepository.save(
                Store.builder()
                        .name(request.getName())
                        .address(request.getAddress())
                        .description(request.getDescription())
                        .businessNumber(request.getBusinessNumber())
                        .tableCount(request.getTableCount())
                        .registeredDate(LocalDateTime.now())
                        .partners(partners)
                        .build()
        );
    }

    /**
     * sortType 별로 등록된 모든 상점 리스트를 반환
     */
    public List<Store> getAllStore(SortType sortType) {

        List<Store> stores = null;
        switch (sortType) {
            case ASC -> stores = storeRepository.findAllByOrderByRegisteredDateAsc();
            case RATING -> stores = storeRepository.findAllByOrderByRatingAsc();
        }

        return stores;
    }
}
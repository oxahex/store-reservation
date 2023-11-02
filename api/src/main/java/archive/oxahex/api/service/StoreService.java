package archive.oxahex.api.service;

import archive.oxahex.api.dto.SortType;
import archive.oxahex.api.dto.request.StoreModifyRequest;
import archive.oxahex.api.dto.request.StoreRegisterRequest;
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
import java.util.Objects;

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
    public Store registerStore(User user, StoreRegisterRequest request) {

        Partners partners = partnersRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorType.PARTNERS_NOT_FOUND));

        // 이미 등록된 매장인 경우
        boolean exists =
                storeRepository.existsByBusinessNumber(request.getBusinessNumber());

        if (exists) {
            throw new CustomException(ErrorType.ALREADY_EXIST_STORE);
        }

        Store store = Store.builder()
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .businessNumber(request.getBusinessNumber())
                .tableCount(request.getTableCount())
                .partners(partners)
                .registeredDate(LocalDateTime.now())
                .build();

        // 스토어
        return storeRepository.save(store);
    }

    /**
     * sortType 별로 등록된 모든 상점 리스트를 반환
     */
    public List<Store> getAllStore(SortType sortType) {

        List<Store> stores = null;
        switch (sortType) {
            case ASC -> stores = storeRepository.findAllByOrderByRegisteredDateAsc();
            case REVIEW_COUNT -> stores = storeRepository.findAllByOrderByReviewCountAsc();
        }

        return stores;
    }

    /**
     * 매장 상세 정보 조회
     */
    public Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorType.STORE_NOT_FOUND));
    }

    /**
     * 매장 정보 수정
     * @param storeId 변경할 매장 ID
     */
    @Transactional
    public Store modifyStore(User user, Long partnersId, Long storeId, StoreModifyRequest request) {

        // 해당 유저의 매장인지 확인
        Partners partners = partnersRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorType.PARTNERS_NOT_FOUND));

        // 해당 매장 소유주가 아닌 경우
        if (!Objects.equals(partners.getId(), partnersId)) {
            throw new CustomException(ErrorType.STORE_ACCESS_DENIED);
        }

        // 매장 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorType.STORE_NOT_FOUND));


        System.out.println(request.getAddress());
        store.modifyStoreInfo(
                request.getName(),
                request.getAddress(),
                request.getDescription(),
                request.getTableCount()
        );

        return storeRepository.save(store);
    }

    /**
     * 매장 삭제
     * <ul>
     *     <li>진행 중인 예약이 있는 경우 삭제 불가</li>
     * </ul>
     * @param storeId
     */
    @Transactional
    public Store deleteStore(User user, Long partnersId, Long storeId) {
        // 해당 유저의 매장인지 확인
        Partners partners = partnersRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorType.PARTNERS_NOT_FOUND));

        // 해당 매장 소유주가 아닌 경우
        if (!Objects.equals(partners.getId(), partnersId)) {
            throw new CustomException(ErrorType.STORE_ACCESS_DENIED);
        }

        // 매장 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorType.STORE_NOT_FOUND));

        storeRepository.delete(store);

        return store;

    }

}
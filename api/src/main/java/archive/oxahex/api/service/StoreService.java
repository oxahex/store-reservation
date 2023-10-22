package archive.oxahex.api.service;

import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.api.exception.CustomException;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PartnersRepository partnersRepository;

    /**
     * 매장 정보를 받아 새로운 매장 등록
     * 권한이 있는 사용자만 등록 가능하도록 이전에 처리하였으므로, ROLE_PARTNERS 만 등록 가능
     * <p>등록된 파트너스 ID(사업자 등록 번호)와 요청 유저의 사업자 번호가 불일치하는 경우 등록 불가
     * <p>이미 등록된 매장인 경우 등록 불가(사업자 등록 번호와 매장은 1:1이므로 사업자 등록 번호로 매장 중복 확인 가능)
     */
    @Transactional
    public StoreDto.Info registerStore(String email, StoreDto.Request request) {

        // 요청 유저의 이메일로 해당 유저를 찾고
        User requestUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        // 파트너스에 등록된 사업자 번호의 userId를 가져옴
        Partners partners = partnersRepository.findByBusinessNumber(request.getBusinessNumber())
                .orElseThrow(() -> new CustomException(ErrorType.BUSINESS_NUMBER_NOT_FOUND));

        // 요청 유저와 파트너스 유저가 일치하지 않는 경우
        if (requestUser != partners.getUser()) {
            throw new CustomException(ErrorType.UN_MATCH_PARTNERS_USER);
        }

        // 이미 등록된 매장인 경우
        boolean exists = storeRepository.existsByBusinessNumber(request.getBusinessNumber());
        if (exists) {
            throw new CustomException(ErrorType.ALREADY_EXIST_STORE);
        }

        // 스토어
        Store store = storeRepository.save(
                Store.builder()
                        .name(request.getName())
                        .address(request.getAddress())
                        .description(request.getDescription())
                        .businessNumber(request.getBusinessNumber())
                        .tableCount(request.getTableCount())
                        .build()
        );

        return StoreDto.fromEntityToStoreInfo(store);
    }
}
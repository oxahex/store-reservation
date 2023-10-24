package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartnersService {

    private final UserRepository userRepository;
    private final PartnersRepository partnersRepository;
    private final StoreRepository storeRepository;

    /**
     * 사업자가 생성한 모든 파트너스 목록 조회
     * 파트너스 이름 반환(매장 등록 시 사용)
     */
    public Partners getPartners(User user) {

        return partnersRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorType.PARTNERS_NOT_FOUND));

    }
}

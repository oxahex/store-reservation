package archive.oxahex.api.service;

import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<Partners> getAllPartners(User user) {

        return partnersRepository.findAllByUser(user);

    }
}

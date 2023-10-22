package archive.oxahex.api.service;

import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnersService {

    private final UserRepository userRepository;
    private final PartnersRepository partnersRepository;
    private final StoreRepository storeRepository;

    /**
     * 파트너스 사용자가 등록한 매장 조회
     * 파트너스 등록 시 등록한 사업자 등록 번호(여러 개 등록 가능)로 소유한 매장 리스트 반환
     */
    public List<Store> getAllPartnersStore(User user) {

        List<Partners> partnersList = partnersRepository.findAllByUserId(user.getId());

        List<Store> stores = new ArrayList<>();
        for (Partners partners : partnersList) {
            stores.add(storeRepository.
                    findByBusinessNumber(partners.getBusinessNumber())
            );
        }

        return stores;
    }
}

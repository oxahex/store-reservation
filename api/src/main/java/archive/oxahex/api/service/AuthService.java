package archive.oxahex.api.service;

import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.api.exception.CustomException;

import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.UserRepository;
import archive.oxahex.domain.type.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PartnersRepository partnersRepository;

    /**
     *
     */
    public User loadUserByAuth(Authentication auth) {

        log.info("auth.email={}", auth.getName());
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));
    }


    /**
     * 유저 생성
     * <ol>
     *     <li>새 유저 정보를 DB에 저장합니다.</li>
     *     <li>유저 비밀번호는 암호화 되어 저장됩니다.</li>
     * </ol>
     * @param request username, password, email, phoneNumber
     * @return 생성된 User Entity 객체
     */
    @Transactional
    public User createUser(SignUpDto.Request request) {

        boolean exists = userRepository.existsByEmail(request.getEmail());
        if (exists) {
            throw new CustomException(ErrorType.ALREADY_EXIST_USER);
        }

        return userRepository.save(User.builder()
                .name(request.getUsername())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(RoleType.ROLE_USER)
                .registeredDate(LocalDateTime.now())
                .build()
        );
    }

    /**
     * 파트너스 등록
     * <ol>
     *     <li>ROLE_USER의 경우 파트너스로 등록 가능합니다.</li>
     *     <li>파트너스 이름은 중복될 수 없습니다.</li>
     * </ol>
     * @param user 로그인 유저 객체
     * @param partnersName 파트너스 이름
     * @return 생성된 파트너스 객체
     */
    @Transactional
    public Partners createPartners(User user, String partnersName) {

        // 이미 등록된 이름인지 검증
        boolean exists = partnersRepository.existsByName(partnersName);
        if (exists) {
            throw new CustomException(ErrorType.ALREADY_EXIST_PARTNERS_NAME);
        }

        // 유저 권한 업데이트
        user.setRole(RoleType.ROLE_PARTNERS);

        Partners partners = new Partners();
        partners.setName(partnersName);
        partners.setUser(user);

        // 파트너스 등록
        return partnersRepository.save(partners);
    }

    /**
     * 유저 email, password 일치 여부 확인
     * <ol>
     *     <li>유저가 입력한 email, password로 가입된 사용자인지 확인합니다.</li>
     *     <li>인증이 성공적으로 수행되면, 인증 완료 된 유저 정보를 반환합니다.</li>
     * </ol>
     * @param email 유저 이메일
     * @param password 유저 비밀번호
     * @return 인증된 유저 Entity 반환
     */
    public User authenticate(String email, String password) {

        // 인증 처리
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        // 비밀 번호 확인
        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorType.WRONG_PASSWORD);
        }

        return user;
    }
}

package archive.oxahex.api.service;

import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.api.exception.CustomException;

import archive.oxahex.api.security.AuthUser;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.UserRepository;
import archive.oxahex.domain.type.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final PartnersRepository partnersRepository;

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        log.info("AuthService.loadUserByUsername={}", username);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        return new AuthUser(user);
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

        log.info("[createUser] email={}, password={}", request.getEmail(), request.getPassword());
        boolean exists = userRepository.existsByEmail(request.getEmail());
        if (exists) {
            log.info("이미 있음");
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

        Partners partners = Partners.builder()
                .name(partnersName).build();
        partners.setUser(user);

        // 파트너스 등록
        return partnersRepository.save(partners);
    }
}

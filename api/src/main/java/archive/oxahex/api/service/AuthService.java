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
     * email로 유저를 찾음
     */
    public User loadUserByAuth(Authentication auth) {

        log.info("auth.name={}", auth.getName());
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));
    }



    /**
     * 유저 생성
     * <p>새 유저 정보를 DB에 저장하고, 유저의 정보를 반환합니다.
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
     * <p>로그인 사용자 정보와 요청 정보(사업자 등록 번호)를 받아
     * <p>사업자가 확인되는 경우 파트너스 테이블에 저장
     * <p>(사업자 확인 로직은 구현하지 않음, 사업자 번호 정상 입력 시 존재하는 사업자로 간주)
     * <p>파트너스 등록 시 해당 유저의 Role Type 변경 후 업데이트 된 권한 정보 반환
     */
    @Transactional
    public void createPartners(
            User user, String businessNumber
    ) {

        // 이미 등록된 사업자 번호인지 검증
        boolean exists =
                partnersRepository.existsByBusinessNumber(businessNumber);

        if (exists) {
            throw new CustomException(ErrorType.ALREADY_EXIST_PARTNERS);
        }

        partnersRepository.save(
                Partners.builder()
                        .businessNumber(businessNumber)
                        .user(user)
                        .build());

        // 유저 권한 업데이트
        user.setRole(RoleType.ROLE_PARTNERS);
    }

    /**
     * 유저가 입력한 email, password로 인증 작업
     * <p>인증이 완료되면 인증 완료 된 유저 정보 반환
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
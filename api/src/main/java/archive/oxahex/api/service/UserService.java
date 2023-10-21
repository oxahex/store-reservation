package archive.oxahex.api.service;

import archive.oxahex.api.dto.SignInDto;
import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.api.exception.UserException;

import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.UserRepository;
import archive.oxahex.domain.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * 유저 생성
     * <p>새 유저 정보를 DB에 저장하고, 유저의 정보를 반환합니다.
     */
    public UserDto.Info createUser(SignUpDto.Request request) {

        boolean exists = userRepository.existsByEmail(request.getEmail());
        if (exists) {
            throw new UserException(ErrorType.USER_NOT_FOUND);
        }

        User user = userRepository.save(User.builder()
                .name(request.getUsername())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(RoleType.ROLE_USER)
                .registeredDate(LocalDateTime.now())
                .build()
        );

        return UserDto.fromEntity(user);
    }

    public UserDto.Info createPartners() {
        return null;
    }

    /**
     * 유저가 입력한 email, password로 인증 작업
     * <p>인증이 완료되면 인증 완료 된 유저 정보 반환
     */
    public UserDto.Info authenticate(SignInDto.Request request) {

        // 인증 처리
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException(ErrorType.USER_NOT_FOUND));

        // 비밀 번호 확인
        if (!this.passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException(ErrorType.ACCESS_DENIED);
        }

        return UserDto.fromEntity(user);
    }
}

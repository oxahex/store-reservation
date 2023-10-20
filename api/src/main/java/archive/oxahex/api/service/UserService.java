package archive.oxahex.api.service;

import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.UserRepository;
import archive.oxahex.domain.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 유저 생성
     * <p>새 유저 정보를 DB에 저장하고, 유저의 정보를 반환합니다.
     */
    public UserDto.Info createUser(SignUpDto.Request request) {

        User user = userRepository.save(User.builder()
                .name(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(RoleType.ROLE_USER)
                .registeredDate(LocalDateTime.now())
                .build()
        );

        return UserDto.fromEntity(user);
    }
}

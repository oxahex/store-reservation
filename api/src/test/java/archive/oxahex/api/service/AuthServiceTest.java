package archive.oxahex.api.service;

import archive.oxahex.api.dto.form.JoinDto;
import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.UserRepository;
import archive.oxahex.domain.type.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartnersRepository partnersRepository;

    /**
     * Email(Spring Security - username) 기반으로 DB에서 해당 유저를 찾음.
     * UserDetails 객체로 감싸서 반환
     * DB에 해당 Email 유저가 없는 경우 USER_NOT_FOUND Exception Throw
     */
    @DisplayName("유저 Email(username)로 해당 유저를 찾는다.")
    @Test
    void loadByUsername_success() {
        // given
        User user = generateUserEntity("test1", RoleType.ROLE_USER);
        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.of(user));


        // when
        UserDetails authUser = authService.loadUserByUsername("test1@gmail.com");

        // then
        assertEquals("test1@gmail.com", authUser.getUsername());
        assertEquals("test1test1test1", authUser.getPassword());

        assertThat(authUser.getAuthorities()).hasSize(1)
                        .extracting("role").contains("ROLE_USER");
    }

    @Test
    @DisplayName("찾는 유저가 없는 경우 NOT FOUND 예외를 던진다.")
    void loadByUsername_failure() {
        // given
        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());


        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> authService.loadUserByUsername("test2@gmail.com"));

        // then
        assertEquals(ErrorType.USER_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.USER_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("회원 가입")
    void createUser_success() {
        // given
        JoinDto joinRequest = new JoinDto();
        joinRequest.setUsername("test1");
        joinRequest.setPassword("test1test1test1");
        joinRequest.setEmail("test1@gmail.com");
        joinRequest.setPhoneNumber("01011111111");

        User user = generateUserEntity("test1", RoleType.ROLE_USER);
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        User registeredUser = authService.createUser(joinRequest);

        // then
        assertEquals("test1", registeredUser.getName());
        assertEquals("test1@gmail.com", registeredUser.getEmail());

    }

    @Test
    @DisplayName("회원가입 시 기존에 존재하는 Email과 중복되면 안 된다.")
    void createUser_failure() {
        // given
        JoinDto joinRequest = new JoinDto();
        joinRequest.setUsername("test1");
        joinRequest.setPassword("test1test1test1");
        joinRequest.setEmail("test1@gmail.com");
        joinRequest.setPhoneNumber("01011111111");

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> authService.createUser(joinRequest));

        // then
        assertEquals(exception.getErrorMessage(), ErrorType.ALREADY_EXIST_USER.getErrorMessage());
        assertEquals(exception.getHttpStatus(), ErrorType.ALREADY_EXIST_USER.getHttpStatus());
    }


    @Test
    @DisplayName("일반 유저가 파트너 권한 신청 시 권한이 ROLE_PARTNERS 로 변경 된다.")
    void createPartners_success() {
        // given
        User user = generateUserEntity("user", RoleType.ROLE_USER);

        Partners partners = generatePartnersEntity("파트너스 이름");
        given(partnersRepository.save(any(Partners.class))).willReturn(partners);

        // when
        Partners generatedPartners = authService.createPartners(user, "파트너스 이름");

        // then
        assertEquals("파트너스 이름", generatedPartners.getName());
        assertEquals(RoleType.ROLE_PARTNERS, user.getRole());
    }

    @Test
    @DisplayName("이미 존재하는 파트너 이름으로는 파트너로 가입할 수 없다.")
    void createPartners_failure() {
        // given
        User user = generateUserEntity("user", RoleType.ROLE_USER);
        given(partnersRepository.existsByName(anyString())).willReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> authService.createPartners(user, "파트너스 이름"));

        // then
        assertEquals(exception.getHttpStatus(), ErrorType.ALREADY_EXIST_PARTNERS_NAME.getHttpStatus());
        assertEquals(exception.getErrorMessage(), ErrorType.ALREADY_EXIST_PARTNERS_NAME.getErrorMessage());
    }

    private User generateUserEntity(String name, RoleType role) {

        return User.builder()
                .name(name)
                .email(name + "@gmail.com")
                .password(name + name + name)
                .phoneNumber("01011111111")
                .role(RoleType.ROLE_USER)
                .registeredDate(LocalDateTime.now())
                .build();
    }

    private Partners generatePartnersEntity(String partnersName) {
        return Partners.builder()
                .name(partnersName)
                .build();
    }
}
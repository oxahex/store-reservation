package archive.oxahex.api.controller;

import archive.oxahex.api.dto.SignInDto;
import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenProvider tokenProvider;

    /**
     * 회원 가입 기능
     * <p>최초 회원 가입 시 모든 유저는 ROLE_USER 로 정의합니다.
     * <p>이후 파트너스 회원 가입 시 ROLE_PARTNERS 로 전환됩니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserDto.Info> signUp(
            @RequestBody @Valid SignUpDto.Request request
    ) {
        return ResponseEntity.ok().body(userService.createUser(request));
    }

    /**
     * 회원 로그인
     * <p>유저의 email, password 정보를 받음.
     * <p>가입된 유저인지 검증하고,
     * <p>정상적으로 인증된 유저의 경우 JWT Token 발급
     */
    @PostMapping("/signin")
    public ResponseEntity<SignInDto.Response> signIn(
            @RequestBody @Valid SignInDto.Request request
    ) {

        UserDto.Info verifiedUser = userService.authenticate(request);
        String token = tokenProvider.generateToken(verifiedUser.getEmail(), verifiedUser.getRole());

        SignInDto.Response response = new SignInDto.Response();
        response.setToken(token);

        return ResponseEntity.ok().body(response);
    }
}

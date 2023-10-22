package archive.oxahex.api.controller;

import archive.oxahex.api.dto.PartnersDto;
import archive.oxahex.api.dto.SignInDto;
import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService userService;
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
        User user = userService.createUser(request);
        return ResponseEntity.ok().body(UserDto.fromEntityToUserInfo(user));
    }

    /**
     * 회원 로그인
     * <p>유저의 email, password 정보를 받음.
     * <p>가입된 유저인지 검증하고,
     * <p>정상적으로 인증된 유저의 경우 JWT Token 발급
     * <p>유저 DB에 저장된 Role Type(권한)이 JWT 토큰에 포함된다.
     */
    @PostMapping("/signin")
    public ResponseEntity<SignInDto.Response> signIn(
            @RequestBody @Valid SignInDto.Request request
    ) {

        User verifiedUser = userService.authenticate(request.getEmail(), request.getPassword());
        String token = tokenProvider.generateToken(verifiedUser.getId(), verifiedUser.getEmail(), verifiedUser.getRole());

        SignInDto.Response signInResponse =
                SignInDto.fromEntityToSignInResponse(verifiedUser, token);


        return ResponseEntity.ok().body(signInResponse);
    }

    /**
     * 파트너스 등록(사업자 등록 번호 마다 파트너스 등록이 가능하다)
     * <p>로그인한 회원인 경우 파트너스 회원 신청을 할 수 있다.
     * <p>사업자 등록이 된 사람만 파트너스에 등록할 수 있는 것으로 정의, 사업자 등록 번호를 추가 정보로 받는다.
     * <p>파트너스 회원 등록 시, 응답으로 업데이트된 roleType이 포함된 유저 정보를 반환한다.
     */
    @PostMapping("/partners")
    @PreAuthorize("hasRole('USER') or hasRole('PARTNERS')")
    public ResponseEntity<PartnersDto.Response> joinPartners(
            Authentication auth, @RequestBody @Valid PartnersDto.Request request
    ) {

        User user = userService.loadUserByAuth(auth);
        userService.createPartners(user, request.getBusinessRegistrationNumber());

        String token = tokenProvider.generateToken(
                user.getId(), user.getEmail(), user.getRole()
        );

        PartnersDto.Response partnersResponse =
                PartnersDto.fromEntityToPartnersResponse(user, token);


        return ResponseEntity.ok().body(partnersResponse);
    }
}

package archive.oxahex.api.controller;

import archive.oxahex.api.dto.PartnersDto;
import archive.oxahex.api.dto.SignInDto;
import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.api.security.AuthUser;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.api.utils.RedisUtil;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
     * <ol>
     *     <li>최초 회원 가입 시 모든 유저는 ROLE_USER 권한을 갖습니다.</li>
     *     <li>이후 파트너스 회원 가입 시 ROLE_PARTNERS 권한으로 전환됩니다.</li>
     * </ol>
     * @param request username, password, email, phoneNumber
     * @return 해당 유저 요약 데이터
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
     * <ol>
     *     <li>비밀번호와 이메일로 가입된 유저인지 검증합니다.</li>
     *     <li>정상적으로 인증된 유저의 경우 JWT Token을 발급합니다.</li>
     *     <li>JWT Token은 Authrization Header로 전송되고, 응답으로도 전송됩니다.</li>
     * </ol>
     * @param request email, password
     * @return 유저 정보 요약(info), jwt token
     */
    @PostMapping("/signin")
    public ResponseEntity<SignInDto.Response> signIn(
            @RequestBody @Valid SignInDto.Request request
    ) {

        User verifiedUser = userService.authenticate(request.getEmail(), request.getPassword());

        // Access Token 발급
        String accessToken = tokenProvider.generateAccessToken(AuthUser.fromEntityToAuthUser(verifiedUser));
        // Refresh Token Redis 저장
        tokenProvider.generateRefreshToken(verifiedUser);

        SignInDto.Response signInResponse =
                SignInDto.fromEntityToSignInResponse(verifiedUser, accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", accessToken);

        return ResponseEntity.ok().headers(headers).body(signInResponse);
    }

    /**
     * 파트너스 등록
     * <ol>
     *     <li>로그인 회원인 경우 파트너스 회원 신청이 가능합니다.</li>
     *     <li>파트너스 회원 등록 시 응답으로 업데이트 된 RoleType이 포함된 유저 정보와 등록한 파트너스 정보를 반환합니다.</li>
     * </ol>
     * @param auth 로그인 유저 객체
     * @param request 파트너스 이름
     * @return 등록한 파트너스 정보, 새로 발급된 JWT Token
     */
    @PostMapping("/partners")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PartnersDto.Response> joinPartners(
            Authentication auth, @RequestBody @Valid PartnersDto.Request request
    ) {

        User user = userService.loadUserByAuth(auth);
        Partners partners = userService.createPartners(user, request.getName());

        // 새 Access Token 발급
        String accessToken = tokenProvider.generateAccessToken(AuthUser.fromEntityToAuthUser(user));
        // 기존 Refresh Token 업데이트
        tokenProvider.generateRefreshToken(user);

        PartnersDto.Response partnersResponse =
                PartnersDto.fromEntityToPartnersResponse(user, partners, accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", accessToken);

        return ResponseEntity.ok().headers(headers).body(partnersResponse);
    }
}

package archive.oxahex.api.controller;

import archive.oxahex.api.dto.request.JoinPartnersRequest;
import archive.oxahex.api.dto.request.LoginRequest;
import archive.oxahex.api.dto.request.JoinRequest;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.api.dto.response.JoinPartnersResponse;
import archive.oxahex.api.security.AuthUser;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
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
    @PostMapping("/join")
    public ResponseEntity<UserDto.Info> join(
            @RequestBody @Valid JoinRequest request
    ) {
        log.info("[회원가입] request={}", request.getEmail());
        User user = authService.createUser(request);
        return ResponseEntity.ok().body(UserDto.fromEntityToUserInfo(user));
    }

    /**
     * 회원 로그인(Security Filter에서 처리)
     * <ol>
     *     <li>비밀번호와 이메일로 가입된 유저인지 검증합니다.</li>
     *     <li>정상적으로 인증된 유저의 경우 JWT Token을 발급합니다.</li>
     *     <li>JWT Token은 Authrization Header로 전송되고, 응답은 200 OK 이외에 Body는 없습니다.</li>
     * </ol>
     * @param request email, password
     * @return Authorization Header, STATUS 200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok().body(null);
    }

    /**
     * 파트너스 등록
     * <ol>
     *     <li>로그인 회원인 경우 파트너스 회원 신청이 가능합니다.</li>
     *     <li>파트너스 회원 등록 시 응답으로 업데이트 된 RoleType이 포함된 유저 정보와 등록한 파트너스 정보를 반환합니다.</li>
     * </ol>
     * @param request 파트너스 이름
     * @return 등록한 파트너스 정보, 새로 발급된 JWT Token
     */
    @PostMapping("/partners")
    @PreAuthorize("hasRole('USER') or hasRole('PARTNERS')")
    public ResponseEntity<JoinPartnersResponse> joinPartners(
            @RequestBody @Valid JoinPartnersRequest request
    ) {

        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        Partners partners = authService.createPartners(user, request.getName());
        log.info("partners={}", partners.getName());
        System.out.println("user.getRole(): " + user.getRole());

        // 새 Access Token 발급
        String accessToken = tokenProvider.generateAccessToken(authUser);

        // 기존 Refresh Token 업데이트
        tokenProvider.generateRefreshToken(authUser);

        JoinPartnersResponse response =
                JoinPartnersResponse.fromEntityToResponse(user, partners, accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        return ResponseEntity.ok().headers(headers).body(response);
    }
}

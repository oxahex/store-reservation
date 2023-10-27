package archive.oxahex.api.security;

import archive.oxahex.api.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * JWT Access Token 유효성 확인 및 권한 인증
 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String[] EXCLUDE_PATH = {"/auth/join", "/auth/login"};

    AuthService authService;
    TokenProvider tokenProvider;

    public JwtAuthorizationFilter(
            AuthenticationManager authenticationManager,
            AuthService authService,
            TokenProvider tokenProvider
    ) {
        super(authenticationManager);
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 로그인, 회원가입의 경우 JWT 검증을 거치지 않도록 함
     */
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) throws ServletException {
        String path = request.getRequestURI();
        return Arrays.stream(EXCLUDE_PATH).anyMatch(path::startsWith);
    }

    /**
     * JWT Access Token 유효성 검증 및 Refresh Token 있는 경우 Access Token 갱신
     * @param request HTTP Request
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        log.info("JwtAuthorizationFilter.doFilterInternal Access Token={}", request.getHeader("Authorization"));

        String accessToken = resolveTokenFromRequest(request);

        // Access Token이 없으면 리턴
        if (accessToken == null) {
            log.info("[JwtAuthorizationFilter] 토큰이 없음");
            chain.doFilter(request, response);
        }

        // 유효하지 않음(기간 지남) 경우 따로 처리
        if (!tokenProvider.validateToken(accessToken)) {
            log.info("[JwtAuthorizationFilter] 유효하지 않은 토큰={}", accessToken);

            // Redis에서 username(email)로 저장된 Refresh Token이 있는지 확인
            String email = tokenProvider.getTokenSubject(accessToken);
            String refreshToken = tokenProvider.getRefreshToken(email);
            log.info("refreshToken from Redis={}", refreshToken);

            // Refresh Token 없는 경우 Authentication 없이 리턴(진행)
            // 재 로그인 필요
            if (refreshToken == null) {
                chain.doFilter(request, response);
            }

            // Refresh Token 있는 경우 검증하고, 새 Access Token 발급
            if (tokenProvider.validateToken(refreshToken)) {
                String username = tokenProvider.getTokenSubject(refreshToken);
                AuthUser authUser = (AuthUser) authService.loadUserByUsername(username);

                String reIssuedAccessToken = tokenProvider.generateAccessToken(authUser);
                log.info("[AccessToken 재발급] Access Token={}", reIssuedAccessToken);

                // 새로 발급된 Access Token 응답 Header에 삽입
                response.setHeader(TOKEN_HEADER, TOKEN_PREFIX + reIssuedAccessToken);

                // 새로 발급된 Access Token으로 기존 Access Token 교체
                accessToken = reIssuedAccessToken;
            }
        }

        // 유효한 경우 Authentication 객체 생성 후 Security Session에 객체를 등록
        String username = tokenProvider.getTokenSubject(accessToken);
        AuthUser authUser = (AuthUser) authService.loadUserByUsername(username);

        // 인증된 Authentication 객체
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        authUser, null, authUser.getAuthorities()
                );

        // SecurityContextHolder 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    /**
     * 요청으로부터 JWT Token 파싱
     * @param request 요청
     * @return JWT Token
     */
    private String resolveTokenFromRequest(HttpServletRequest request) {

        String token = request.getHeader(TOKEN_HEADER);
        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}

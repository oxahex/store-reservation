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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        log.info("JwtAuthorizationFilter.doFilterInternal Access Token={}", request.getHeader("Authorization"));

        String accessToken = resolveTokenFromRequest(request);

        // AccessToken이 없으면 리턴
        if (accessToken == null) {
            log.info("[JwtAuthorizationFilter] 토큰이 없음");
            chain.doFilter(request, response);
        }

        // 유효하지 않음(기간 지남) 경우 따로 처리
        if (!tokenProvider.validateToken(accessToken)) {
            log.info("[JwtAuthorizationFilter] 유효하지 않은 토큰={}", accessToken);
            // TODO: 일단 반환하고 후에 Refresh Token 이용 방식으로 전환
            chain.doFilter(request, response);
        }

        // 유효한 경우 Authentication 객체 생성 후 Security Session에 객체를 등록
        String email = tokenProvider.getTokenSubject(accessToken);
        AuthUser authUser = (AuthUser) authService.loadUserByUsername(email);

        // 인증된 Authentication 객체
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        authUser, null, authUser.getAuthorities()
                );

        // SecurityContextHolder 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {

        String token = request.getHeader(TOKEN_HEADER);
        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}

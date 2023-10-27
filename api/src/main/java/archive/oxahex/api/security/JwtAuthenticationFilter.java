package archive.oxahex.api.security;

import archive.oxahex.api.dto.SignInDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;


    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            TokenProvider tokenProvider,
            String loginPath
    ) {
        super.setFilterProcessesUrl(loginPath);
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 가입한 유저인지 검증
     * <ol>
     *     <li>Email, Password로 가입한 유저인지 검증</li>
     *     <li>검증 완료 시 Authentication 객체를 생성해 반환</li>
     * </ol>
     */
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        log.info("JwtAuthenticationFilter.attemptAuthentication={}", request.getRequestURI());

        ObjectMapper om = new ObjectMapper();

        try {
            SignInDto.Request signInRequest =
                    om.readValue(request.getInputStream(), SignInDto.Request.class);

            // 미인증된 Authentication 객체
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            signInRequest.getEmail(), signInRequest.getPassword()
                    );

            // 내부적으로 AuthService에 구현한 loadByUsername 메서드 호출
            // 검증 성공 시 Authentication 객체 반환
            return authenticationManager.authenticate(authenticationToken);

        } catch (IOException e) {
            log.error("JwtAuthenticationFilter.attemptAuthentication", e);
        }

        return null;
    }

    /**
     * JWT Token 발급
     * <ol>
     *     <li>JWT Access Token 발급</li>
     *     <li>Response Header에 Access Token 전송</li>
     * </ol>
     */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {

        AuthUser authUser = (AuthUser) authResult.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(authUser);

        response.addHeader(TOKEN_HEADER, TOKEN_PREFIX + accessToken);
    }
}

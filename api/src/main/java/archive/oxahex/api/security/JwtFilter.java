package archive.oxahex.api.security;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.api.utils.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;


/**
 * JWTFilter
 * <ul>
 *     <li>request 객체로부터 토큰을 받아와 정상 토큰인 경우 security context에 저장 </li>
 *     <li>Access Token 만료이나 Refresh Token이 있는 경우 Refresh Token 정보를 토대로 AccessToken 재발급</li>
 * </ul>
 */

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private static final List<String> WHITE_LIST = List.of("/auth/signin");

    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 요청 URI가 인증하지 않아도 되는 URI인 경우
        String requestURI = request.getRequestURI();
        boolean noNeedToCheck = WHITE_LIST.contains(requestURI);

        if (noNeedToCheck) {
            // Filter Chain 넘김
            filterChain.doFilter(request, response);
            return;
        }

        // Token에서 Email과 권한을 꺼냄
        String accessToken = this.resolveTokenFromRequest(request);

        AuthUser authUser = tokenProvider.getAuthUser(accessToken);

        // Access Token 있고 인증된 유저
         if (!tokenProvider.validateToken(accessToken)) {
             // Refresh Token 있으면 재발급
             String refreshToken = redisUtil.get(authUser.getEmail());

             if (tokenProvider.validateToken(refreshToken)) {
                 log.info("refreshToken={}", refreshToken);
                 AuthUser authUserFromRefreshToken = tokenProvider.getAuthUser(refreshToken);
                 String reIssuedAccessToken = tokenProvider.generateAccessToken(authUserFromRefreshToken);
                 response.setHeader("Authorization", reIssuedAccessToken);
             } else {
                 throw new CustomException(ErrorType.EXPIRED_JWT_TOKEN);
             }
        }


        // Filter Chain 넘김
        // Username 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        List.of(new SimpleGrantedAuthority(authUser.getRole()))
                );

        // 인증된 유저로 넘김
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    /**
     * request 객체에서 Authorization Header 값 파싱해 JWT String만 반환
     */
    private String resolveTokenFromRequest(HttpServletRequest request) {

        String token = request.getHeader(TOKEN_HEADER);
        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}

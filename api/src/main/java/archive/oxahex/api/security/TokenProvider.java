package archive.oxahex.api.security;

import archive.oxahex.api.utils.RedisUtil;
import archive.oxahex.domain.entity.User;
import io.jsonwebtoken.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final RedisUtil redisUtil;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;   // 1h
//    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60;   // 1분 test
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;   // 24h

    private static final String KEY_EMAIL = "email";
    private static final String KEY_ID = "id";
    private static final String KEY_ROLE = "role";

    @Value("${spring.jwt.secret}")
    private String key;

    /**
     * 토큰 생성(발급)
     * <p>id, email, role 정보 포함
     */
    public String generateAccessToken(AuthUser authUser) {
        log.info("TokenProvider generateAccessToken user.email = {}", authUser.getUsername());
        Claims claims = Jwts.claims().setSubject(authUser.getUsername());
        claims.put(KEY_ID, authUser.getUser().getId());

        Date now = new Date(System.currentTimeMillis());
        Date expiredDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   // 생성
                .setExpiration(expiredDate)     // 만료
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    };

    public String generateRefreshToken(AuthUser authUser) {

        log.info("TokenProvider generateRefreshToken user.email = {}", authUser.getUsername());

        Claims claims = Jwts.claims().setSubject(authUser.getUsername());
        claims.put(KEY_ID, authUser.getUser().getId());


        Date now = new Date();
        Date expireDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();

        // 새 Token Redis 저장
        redisUtil.set(authUser.getUsername(), refreshToken);

        return refreshToken;
    }

    public String getRefreshToken(String key) {
        return redisUtil.get(key);
    }

    /**
     * Token으로부터 Email(Username)을 꺼냄
     */
    public String getTokenSubject(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }


    /**
     * 토큰 유효기간 확인
     * @param token
     * @return 유효한 경우 true, 유효 기간 만료 토큰인 경우 false
     */
    public boolean validateToken(String token) {
        Claims claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

    /**
     * Claim의 Body를 파싱
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(this.key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}

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
//    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;   // 1h
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60;   // 1분 test
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
        Claims claims = Jwts.claims().setSubject(authUser.getEmail());
        claims.put(KEY_EMAIL, authUser.getEmail());
        claims.put(KEY_ID, authUser.getId());
        claims.put(KEY_ROLE, authUser.getRole());

        Date now = new Date(System.currentTimeMillis());
        Date expiredDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   // 생성
                .setExpiration(expiredDate)     // 만료
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    };

    public String generateRefreshToken(User user) {

        log.info("TokenProvider generateRefreshToken user.email = {}", user.getEmail());

        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put(KEY_EMAIL, user.getEmail());
        claims.put(KEY_ID, user.getId());
        claims.put(KEY_ROLE, user.getRole());

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();

        // 새 Token Redis 저장
        redisUtil.set(user.getEmail(), refreshToken);

        return refreshToken;
    }

    public String getRefreshToken(String key) {
        return redisUtil.get(key);
    }

    public AuthUser getAuthUser(String token) {
        Claims claims = parseClaims(token);

        String email = claims.get(KEY_EMAIL, String.class);
        Long id = claims.get(KEY_ID, Long.class);
        String role = claims.get(KEY_ROLE, String.class);

        return new AuthUser(id, email, role);
    }

    public boolean validateToken(String token) {
        log.info("token={}", token);
        if (!StringUtils.hasText(token)) return false;

        Claims claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

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

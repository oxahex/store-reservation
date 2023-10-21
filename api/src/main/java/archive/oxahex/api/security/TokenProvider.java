package archive.oxahex.api.security;

import archive.oxahex.api.dto.UserDto;
import archive.oxahex.domain.type.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
public class TokenProvider {

    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;   // 1h
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    @Value("${spring.jwt.secret}")
    private String key;

    /**
     * 토큰 생성(발급)
     * <p>email, role로 발급
     */
    public String generateToken(String email, RoleType role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put(KEY_EMAIL, email);
        claims.put(KEY_ROLE, role);

        Date now = new Date(System.currentTimeMillis());
        Date expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   // 생성
                .setExpiration(expiredDate)     // 만료
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    };


    public String getUserEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.get(KEY_EMAIL, String.class);
    }

    public String getUserRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get(KEY_ROLE, String.class);
    }

    public UserDto.Info getAuthUserInfo(String token) {
        Claims claims = parseClaims(token);

        UserDto.Info userInfo = new UserDto.Info();
        userInfo.setEmail(claims.get(KEY_EMAIL, String.class));
        userInfo.setRole(claims.get(KEY_ROLE, RoleType.class));

        return userInfo;
    }


    public boolean validateToken(String token) {
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

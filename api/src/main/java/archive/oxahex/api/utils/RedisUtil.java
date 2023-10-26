package archive.oxahex.api.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;   // 24h
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void set(String key, String value) {
        // 기존에 있으면 제거
        if (get(key) != null) delete(key);

        log.info("RedisUtil set key={}, value={}", key, value);
        redisTemplate.opsForValue().set(
                key, value, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS
        );
    }

    @Transactional(readOnly = true)
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Transactional
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
}

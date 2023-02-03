package site.lghtsg.api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void setValues(String key, String value, Duration duration){
        redisTemplate.opsForValue().set(key, value, duration);
    }

    // 만료시간 설정 -> 자동 삭제
    @Transactional
    public void setValuesWithTimeout(String key, String value, long timeout){
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    public String getValues(String key){
        return redisTemplate.opsForValue().get(key);
    }

    @Transactional
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
/*
    // token 유효성 검사
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(jwtToken);
            ValueOperations<String, String> logoutValueOperations = redisTemplate.opsForValue();
            if (logoutValueOperations.get(jwtToken) != null) {
                return false;
            }
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // 로그아웃
    public void logout(int userIdx, String accessToken) {
        String userIdxString = Integer.toString(userIdx);
        if(redisTemplate.opsForValue().get(userIdx) != null){
            redisTemplate.delete(userIdxString);
        }
        long expiration = jwtService.getExpiration(accessToken);
        redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
        setValues("blackList" + accessToken, userIdxString, Duration.ofMillis(expiration));
        deleteValues(userIdxString); // Redis에서 유저 리프레시 토큰 삭제
    }*/
}

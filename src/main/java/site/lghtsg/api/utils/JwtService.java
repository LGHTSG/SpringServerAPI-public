package site.lghtsg.api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.Secret.Secret;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static site.lghtsg.api.config.BaseResponseStatus.*;

@Service
public class JwtService {

    private final RedisService redisService;

    @Autowired
    public JwtService(RedisService redisService) {
        this.redisService = redisService;
    }

    /*
    JWT 생성
    @param userIdx
    @return String
     */
    public String createJwt(int userIdx, long option){
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userIdx",userIdx)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + option))
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_SECRET_KEY)
                .compact();
    }

    // access token
    public String createAccessToken(int userIdx) {
        long tokenInValidTime = 1*(1000*60*60*24*30);
        return this.createJwt(userIdx, tokenInValidTime);
    }

    // refresh token
    public String createRefreshToken(int userIdx) {
        long tokenInValidTime = 1*(1000*60*60*24*365);
        String userIdxString = Integer.toString(userIdx);
        String refresh = this.createJwt(userIdx, tokenInValidTime);
        redisService.setValuesWithTimeout(userIdxString, refresh, tokenInValidTime);
        return refresh;
    }

    /*
    Header에서 X-ACCESS-TOKEN 으로 JWT 추출
    @return String
     */
    public String getJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("X-ACCESS-TOKEN");
    }

    /*
    JWT에서 userIdx 추출
    @return int
    @throws BaseException
     */
    public int getUserIdx() throws BaseException{
        //1. JWT 추출
        String accessToken = getJwt();
        // jwt 오류 처리 부분
        if(accessToken == null || accessToken.length() == 0){
            throw new BaseException(EMPTY_JWT);
        }

        // 2. JWT parsing
        Jws<Claims> claims;
        try{
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new BaseException(EMPTY_JWT);
        }

        // 3. userIdx 추출
        return claims.getBody().get("userIdx",Integer.class);  // jwt 에서 userIdx를 추출합니다.
    }

}

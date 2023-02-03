package site.lghtsg.api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.users.UserDao;
import site.lghtsg.api.users.model.Token;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static site.lghtsg.api.config.BaseResponseStatus.*;
import static site.lghtsg.api.config.Secret.Secret.JWT_SECRET_KEY;

@Service
public class JwtService {

    private final RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    private final RedisService redisService;
    private final UserDao userDao;

    @Autowired
    public JwtService(RedisService redisService, UserDao userDao) {
        this.redisService = redisService;
        this.userDao = userDao;
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
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
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
                    .setSigningKey(JWT_SECRET_KEY)
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new BaseException(EMPTY_JWT);
        }

        // 3. userIdx 추출
        return claims.getBody().get("userIdx",Integer.class);  // jwt 에서 userIdx를 추출합니다.
    }

    // 토큰 만료
    public Long getExpiration(String accessToken) {
        Date expiration = Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(accessToken).getBody().getExpiration();
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    // 토큰 재발급
    public Token reIssueAccessToken(int userIdx, String refreshToken) {
        // String userIdxString = Integer.toString(userIdx);
        if(!validateToken(refreshToken)) {
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }
        String accessToken = createAccessToken(userIdx);
        return new Token(accessToken, refreshToken);
    }

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

}

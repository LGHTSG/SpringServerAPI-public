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
import java.time.Duration;
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
        long tokenInValidTime = 1*(1000*60*60*24*365);
        return this.createJwt(userIdx, tokenInValidTime);
    }

    // refresh token
    public String createRefreshToken(int userIdx) {
        long tokenInValidTime = 1*(1000*60*60*24*365);
        String userIdxString = Integer.toString(userIdx);
        String refresh = this.createJwt(userIdx, tokenInValidTime);
        redisService.setValues(userIdxString, refresh, Duration.ofMillis(tokenInValidTime));
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

        Token token = new Token();
        token.setAccessToken(accessToken);

        // access token의 유효성을 확인한다. 만료가 안되면 정상 진행,
        // 만료가 되었으면 refresh token의 유효성을 확인하고 유효하면 access token을 재발급
        // 혹은 로그아웃 및 만료로 다시 로그인을 한다.
        if(validateAccessToken(token)) {
            // JWT parsing
            Jws<Claims> claims;
            try{
                claims = Jwts.parser()
                        .setSigningKey(JWT_SECRET_KEY)  // key 입력
                        .parseClaimsJws(accessToken);   // value 입력
            } catch (Exception ignored) {
                throw new BaseException(JWT_ERROR);
            }
            // userIdx 추출
            token.setUserIdx(claims.getBody().get("userIdx",Integer.class)); // jwt 에서 userIdx를 추출합니다.
        } else if(!validateAccessToken(token)) {
            throw new BaseException(JWT_VALIDATE_ERROR);
        }
        return token.getUserIdx();
    }

    // 토큰 만료
    public Long getExpiration(String accessToken) {
        Date expiration = Jwts.parser()
                .setSigningKey(JWT_SECRET_KEY)
                .parseClaimsJws(accessToken)
                .getBody()
                .getExpiration();
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    // 토큰 재발급
    public String reIssueAccessToken(Token token) {
        String accessToken = createAccessToken(token.getUserIdx());
        return accessToken;
    }

    // access token 유효성 검사
    public boolean validateAccessToken(Token token) {
        try {
            // jwt parsing (access token)
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET_KEY)
                    .parseClaimsJws(token.getAccessToken());
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // refresh token 받아오기 및 유효성 검사
    public Token validateRefreshToken(Token token) throws BaseException {
        try {
            // jwt parsing (access token)
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET_KEY)
                    .parseClaimsJws(token.getAccessToken());

            // userIdx 추출
            int userIdx = claims.getBody().get("userIdx",Integer.class); // jwt 에서 userIdx를 추출합니다.
            token.setUserIdx(userIdx);
            String userIdxString = Integer.toString(userIdx);

            // refresh token : redis에 존재하는지 확인 (로그아웃이나 만료되면 존재 X)
            token.setRefreshToken(redisService.getValues(userIdxString));
            if(token.getRefreshToken() == null) {
                System.out.println("만료 혹은 로그아웃 된 token");
                throw new BaseException(JWT_VALIDATE_ERROR);
            }
            return token;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}

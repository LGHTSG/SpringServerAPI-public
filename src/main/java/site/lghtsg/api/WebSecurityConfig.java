package site.lghtsg.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * SpringSecurity 설정
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();  // CSRF 비활성화,
        // REST API 서버는 stateless하게 개발하기 때문에 사용자 정보를 Session에 저장 안함
        // jwt 토큰을 Cookie에 저장하지 않는다면, CSRF에 어느정도는 안전.
    }
}

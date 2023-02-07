package site.lghtsg.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ApiApplication2 {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}



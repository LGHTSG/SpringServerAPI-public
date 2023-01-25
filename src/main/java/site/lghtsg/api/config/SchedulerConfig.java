package site.lghtsg.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        int pool_size = 3;

        scheduler.setPoolSize(pool_size);
        scheduler.setThreadNamePrefix("데이터 업로드 쓰레드-"); // 로그에서만 적용됨.
        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);
    }
}

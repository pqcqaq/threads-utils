package online.zust.qcqcqc.utils.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qcqcqc
 */
@Configuration
public class ThreadsUtilsAutoInject {

    private static final Logger log = LoggerFactory.getLogger(ThreadsUtilsAutoInject.class);

    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        ThreadsUtilsAutoInject.applicationContext = applicationContext;
    }

    public static Object getBeanByName(String name) {
        if (applicationContext == null) {
            throw new RuntimeException("applicationContext is null");
        }
        return applicationContext.getBean(name);
    }

    @Bean(name = "promiseExecutor")
    public TaskExecutor threadPoolTaskExecutor() {
        log.info("start promiseExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(8);
        // 最大线程数
        executor.setMaxPoolSize(16);
        // 任务队列容量
        executor.setQueueCapacity(127);
        // 设置线程的最大空闲时间
        executor.setKeepAliveSeconds(60);
        // 线程名前缀
        executor.setThreadNamePrefix("promise-exec-");
        // 设置拒绝策略：当线程池达到最大线程数时，如何处理新任务
        // CALLER_RUNS：在添加到线程池失败时会由主线程自己来执行这个任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }
}

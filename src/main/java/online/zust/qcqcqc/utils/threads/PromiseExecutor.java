package online.zust.qcqcqc.utils.threads;

import online.zust.qcqcqc.utils.ThreadsUtils;
import online.zust.qcqcqc.utils.config.ThreadsUtilsAutoInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qcqcqc
 */
@Component
@ConfigurationProperties(prefix = "promise.executor")
public class PromiseExecutor implements DisposableBean {

    @Value("${promise.executor.core-pool-size:16}")
    private Integer corePoolSize;
    @Value("${promise.executor.max-pool-size:32}")
    private Integer maxPoolSize;
    @Value("${promise.executor.queue-capacity:127}")
    private Integer queueCapacity;
    @Value("${promise.executor.keep-alive-seconds:60}")
    private Integer keepAliveSeconds;
    @Value("${promise.executor.thread-name-prefix:promise}")
    private String threadNamePrefix;

    private static final Logger log = LoggerFactory.getLogger(PromiseExecutor.class);

    private static Executor promiseExecutor;

    /**
     * 创建线程池
     *
     * @return 线程池
     */
    @Bean(name = "promiseExecutor")
    @ConditionalOnMissingBean(name = "promiseExecutor")
    public Executor threadPoolTaskExecutor() {
        log.info("start promiseExecutor");
        Executor executor = ThreadsUtils.createExecutor(corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds, new ThreadPoolExecutor.CallerRunsPolicy(), threadNamePrefix);
        initExecutor(executor);
        return executor;
    }

    /**
     * 手动初始化线程池
     *
     * @param promiseExecutor 线程池
     */
    public static void initExecutor(Executor promiseExecutor) {
        PromiseExecutor.promiseExecutor = promiseExecutor;
    }

    /**
     * 获取线程池
     *
     * @return 线程池
     */
    public static Executor getPromiseExecutor() {
        if (promiseExecutor == null) {
            Executor promiseExecutor1;
            try {
                promiseExecutor1 = (Executor) ThreadsUtilsAutoInject.getBeanByName("promiseExecutor");
            } catch (Exception e) {
                throw new RuntimeException("promiseExecutor has not been initialized in env, please call PromiseExecutor.initExecutor() first.");
            }
            return promiseExecutor1;
        }
        return promiseExecutor;
    }

    @Override
    public void destroy() throws Exception {
        if (promiseExecutor instanceof ThreadPoolExecutor) {
            ((ThreadPoolExecutor) promiseExecutor).shutdown();
        } else {
            log.warn("promiseExecutor is not ThreadPoolExecutor, cannot shutdown.");
        }
    }
}

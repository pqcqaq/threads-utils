package online.zust.qcqcqc.utils.config;

import online.zust.qcqcqc.utils.threads.PromiseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author qcqcqc
 */
@Configuration
@Import({PromiseExecutor.class})
public class ThreadsUtilsAutoInject {

    private static final Logger log = LoggerFactory.getLogger(ThreadsUtilsAutoInject.class);

    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        ThreadsUtilsAutoInject.applicationContext = applicationContext;
    }

    /**
     * 通过类型来获取Bean
     * @param clazz 类型
     * @return Bean
     * @param <T> 类型
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new RuntimeException("applicationContext is null");
        }
        return applicationContext.getBean(clazz);
    }

    /**
     * 通过名称来获取Bean
     * @param name 名称
     * @return Bean
     */
    public static Object getBeanByName(String name) {
        if (applicationContext == null) {
            log.error("applicationContext is null");
            throw new RuntimeException("applicationContext is null");
        }
        return applicationContext.getBean(name);
    }
}

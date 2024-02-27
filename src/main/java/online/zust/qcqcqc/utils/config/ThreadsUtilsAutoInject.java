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

    public static <T> T getBean(Class<T> name) {
        if (applicationContext == null) {
            throw new RuntimeException("applicationContext is null");
        }
        return applicationContext.getBean(name);
    }

    public static Object getBeanByName(String name) {
        if (applicationContext == null) {
            throw new RuntimeException("applicationContext is null");
        }
        return applicationContext.getBean(name);
    }
}

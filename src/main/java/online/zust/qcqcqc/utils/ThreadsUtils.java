package online.zust.qcqcqc.utils;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qcqcqc
 */
public class ThreadsUtils {
    /**
     * 创建线程池
     *
     * @param poolSize         核心线程数
     * @param maxPoolSize      最大线程数
     * @param queueCapacity    任务队列容量
     * @param keepAliveSeconds 线程的最大空闲时间
     * @param policy           拒绝策略
     * @param prefix           线程名前缀
     * @return 线程池
     */
    public static Executor createExecutor(int poolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds, RejectedExecutionHandler policy, String prefix) {
//        new ThreadPoolExecutor.AbortPolicy() // 不执行新任务，直接抛出异常，提示线程池已满
//        new ThreadPoolExecutor.CallerRunsPolicy() // 哪来的去哪里！由调用线程处理该任务
//        new ThreadPoolExecutor.DiscardPolicy() //不执行新任务，也不抛出异常
//        new ThreadPoolExecutor.DiscardOldestPolicy() //丢弃队列最前面的任务，然后重新提交被拒绝的任务。
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(poolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 任务队列容量
        executor.setQueueCapacity(queueCapacity);
        // 设置线程的最大空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程名前缀
        executor.setThreadNamePrefix(getNamePrefix(prefix));
        // 设置拒绝策略：当线程池达到最大线程数时，如何处理新任务
        // CALLER_RUNS：在添加到线程池失败时会由主线程自己来执行这个任务
        executor.setRejectedExecutionHandler(policy);
        // 初始化
        executor.initialize();
        return executor;
    }

    /**
     * 创建线程池
     *
     * @param poolSize      核心线程数
     * @param maxPoolSize   最大线程数
     * @param queueCapacity 任务队列容量
     * @param policy        拒绝策略
     * @param prefix        线程名前缀
     * @return 线程池
     */
    public static Executor createExecutor(int poolSize, int maxPoolSize, int queueCapacity, RejectedExecutionHandler policy, String prefix) {
        return createExecutor(poolSize, maxPoolSize, queueCapacity, 60, policy, prefix);
    }

    /**
     * 创建线程池
     *
     * @param poolSize      核心线程数
     * @param maxPoolSize   最大线程数
     * @param queueCapacity 任务队列容量
     * @param prefix        线程名前缀
     * @return 线程池
     */
    public static Executor createExecutor(int poolSize, int maxPoolSize, int queueCapacity, String prefix) {
        return createExecutor(poolSize, maxPoolSize, queueCapacity, new ThreadPoolExecutor.CallerRunsPolicy(), prefix);
    }

    /**
     * 创建线程池
     *
     * @param poolSize 线程池大小
     * @param prefix   线程名前缀
     * @return 线程池
     */
    public static Executor createExecutor(int poolSize, String prefix) {
        return createExecutor(poolSize, poolSize * 2, 127, prefix);
    }

    /**
     * 创建线程池
     *
     * @param poolSize 线程池大小
     * @return 线程池
     */
    public static Executor createExecutor(int poolSize) {
        return createExecutor(poolSize, null);
    }

    /**
     * 获取线程名前缀
     *
     * @param name 名称
     * @return 线程名前缀
     */
    private static String getNamePrefix(String name) {
        if (name != null && !name.isEmpty()) {
            return name + "-exec-";
        }
        long l = System.currentTimeMillis();
        return l + "-exec-";
    }
}

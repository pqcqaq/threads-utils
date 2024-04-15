package online.zust.qcqcqc.utils.threads.tasks;

/**
 * @author qcqcqc
 * @date 2024/04
 * @time 11-00-49
 */
@FunctionalInterface
public interface SingleTask<T> {
    /**
     * 执行任务
     *
     * @param item 任务
     * @throws RuntimeException 异常
     */
    void execute(T item) throws RuntimeException;
}

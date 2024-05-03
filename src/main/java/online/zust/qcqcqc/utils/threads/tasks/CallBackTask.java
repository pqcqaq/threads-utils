package online.zust.qcqcqc.utils.threads.tasks;

/**
 * @author qcqcqc
 * @date 2024/04
 * @time 11-12-06
 */
@FunctionalInterface
public interface CallBackTask<P, R> {
    /**
     * 执行任务
     *
     * @param p 参数
     * @return 结果
     * @throws RuntimeException 异常
     */
    R execute(P p) throws RuntimeException;
}

package online.zust.qcqcqc.utils.threads.tasks;

import online.zust.qcqcqc.utils.threads.Promise;

/**
 * @author qcqcqc
 */
@FunctionalInterface
public interface PromisedTask<T> {
    /**
     * 执行任务
     * @param status 状态
     * @return 结果
     * @throws Exception 异常
     */
    T execute(Promise.Next status) throws Exception;
}

package online.zust.qcqcqc.utils.threads.tasks;


/**
 * @author qcqcqc
 * Date: 2024/2/27
 * Time: 18:13
 */
public interface ExceptionHandleTask<T> {
    /**
     * 执行任务
     * @param e 异常
     * @return 结果
     */
    T execute(Exception e);
}

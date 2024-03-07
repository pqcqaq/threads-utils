package online.zust.qcqcqc.utils.threads.tasks;

/**
 * 无返回值任务
 * @author pqcmm
 */
@FunctionalInterface
public interface VoidTask {
    /**
     * 任务
     */
    void run();
}

package online.zust.qcqcqc.utils.threads.tasks;

/**
 * @author qcqcqc
 * Date: 2024/3/13
 * Time: 21:45
 */
@FunctionalInterface
public interface ProgressHandler {
    /**
     * 进度处理
     *
     * @param done  已完成
     * @param total 总数
     */
    void handle(int done, int total);
}

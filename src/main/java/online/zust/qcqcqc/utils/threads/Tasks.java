package online.zust.qcqcqc.utils.threads;

import online.zust.qcqcqc.utils.threads.tasks.VoidTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qcqcqc
 */
public class Tasks {
    /**
     * 等待所有任务完成
     *
     * @param promises 任务列表
     * @param <T>      结果类型
     * @return 结果列表
     */
    public static <T> List<T> awaitAll(List<Promise<T>> promises) {
        List<T> results = new ArrayList<>();
        promises.forEach(Promise::startAsync);
        for (Promise<T> promise : promises) {
            results.add(promise.await());
        }
        return results;
    }

    /**
     * 等待所有任务完成
     *
     * @param promises 任务列表
     * @return 结果列表
     */
    public static List<Object> awaitAll(Promise<?>... promises) {
        List<Object> results = new ArrayList<>();
        for (Promise<?> promise : promises) {
            promise.startAsync();
        }
        for (Promise<?> promise : promises) {
            Object await = promise.await();
            results.add(await);
        }
        return results;
    }

    /**
     * 等待所有任务完成
     *
     * @param promises 任务列表
     */
    public static void awaitAllVoid(Promise<?>... promises) {
        for (Promise<?> promise : promises) {
            promise.startAsync();
        }
        for (Promise<?> promise : promises) {
            promise.waitFinish();
        }
    }

    /**
     * 设置定时任务
     *
     * @param task    任务
     * @param timeout 超时时间
     * @return Promise
     */
    public static Promise<?> setTimeout(VoidTask task, long timeout) {
        return Promise.resolve(() -> {
            Thread.sleep(timeout);
            return null;
        }).onSucceed((res) -> task.run());
    }
}

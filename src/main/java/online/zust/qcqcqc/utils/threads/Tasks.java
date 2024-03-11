package online.zust.qcqcqc.utils.threads;

import online.zust.qcqcqc.utils.threads.tasks.VoidTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author qcqcqc
 */
public class Tasks {
    /**
     * 任务列表
     *
     * @param <T> 结果类型
     */
    public static class TaskList<T> {
        private final List<Promise<T>> tasks = new ArrayList<>();

        /**
         * 添加任务
         *
         * @param task 任务
         * @return 任务列表
         */
        public TaskList<T> add(Promise<T> task) {
            tasks.add(task);
            return this;
        }

        /**
         * 添加任务
         *
         * @param callable 任务
         * @return 任务列表
         */
        public TaskList<T> add(Callable<T> callable) {
            tasks.add(Promise.resolve(callable));
            return this;
        }

        /**
         * 添加任务
         *
         * @return 任务列表
         */
        public List<T> awaitAll() {
            return Tasks.awaitAll(tasks);
        }

        /**
         * 开始所有任务
         */
        public void startAllAsync() {
            tasks.forEach(Promise::startAsync);
        }

        /**
         * 等待所有任务完成
         *
         * @return 结果列表
         */
        public int getTaskCount() {
            return tasks.size();
        }

        /**
         * 获取未完成任务数量
         *
         * @return 未完成任务数量
         */
        public int getUnfinishedTaskCount() {
            int count = 0;
            for (Promise<T> task : tasks) {
                if (!task.isDone()) {
                    count++;
                }
            }
            return count;
        }

        /**
         * 添加任务全部完成回调
         *
         * @param task 任务
         */
        public void onTasksFinish(VoidTask task) {
            Promise.resolve(() -> {
                Tasks.awaitAll(tasks);
                return null;
            }).onSucceed((res) -> task.run()).startAsync();
        }
    }

    /**
     * 创建任务列表
     *
     * @param <T> 结果类型
     * @return 任务列表
     */
    public static <T> TaskList<T> createTaskList() {
        return new TaskList<>();
    }

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

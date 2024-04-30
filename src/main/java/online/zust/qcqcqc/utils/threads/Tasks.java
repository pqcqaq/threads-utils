package online.zust.qcqcqc.utils.threads;

import online.zust.qcqcqc.utils.threads.tasks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        private boolean startedAll = false;
        private static final Logger log = LoggerFactory.getLogger(Tasks.class);

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
            List<T> results = new ArrayList<>();
            if (!startedAll) {
                for (Promise<T> tPromise : tasks) {
                    if (!tPromise.isDone()) {
                        tPromise.startAsync();
                    }
                }
            }
            startedAll = true;
            for (Promise<T> promise : tasks) {
                results.add(promise.waitForResult());
            }
            return results;
        }

        /**
         * 开始所有任务
         */
        public void startAllAsync() {
            tasks.forEach(Promise::startAsync);
            startedAll = true;
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
         * 等待所有任务完成
         */
        private void awaitAllForDone() {
            for (Promise<T> promise : tasks) {
                promise.waitForResult();
            }
        }

        /**
         * 添加任务全部完成回调
         *
         * @param task 任务
         */
        public void onTasksFinish(VoidTask task) {
            Promise.resolve(() -> {
                this.awaitAllForDone();
                return null;
            }).onSucceed((res) -> task.run()).startAsync();
        }

        /**
         * 添加单个任务完成回调
         *
         * @param task 任务
         */
        public void onProgressCallback(Runnable task) {
            tasks.forEach((promise) -> promise.addFinishCallBack(task));
        }

        /**
         * 是否所有任务已经开始
         *
         * @return 是否所有任务已经开始
         */
        public boolean isTasksStarted() {
            return startedAll;
        }

        /**
         * 添加进度显示
         *
         * @param task 任务
         */
        public void onShowProgress(ProgressHandler task) {
            tasks.forEach((promise) -> promise.addFinishCallBack(() -> {
                // 这里因为当前任务就算已经完成，但是还没有执行回调，所以需要+1
                task.handle(getTaskCount() - getUnfinishedTaskCount() + 1, getTaskCount());
            }));
        }

        /**
         * 添加进度显示
         */
        public void addShowProgress() {
            onShowProgress((done, total) -> log.info("当前执行的线程{}，任务进度：{}/{}", Thread.currentThread().getName(), done, total));
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
        for (Promise<T> tPromise : promises) {
            if (!tPromise.isDone()) {
                tPromise.startAsync();
            }
        }
        for (Promise<T> promise : promises) {
            results.add(promise.waitForResult());
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
            if (!promise.isDone()) {
                promise.startAsync();
            }
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

    /**
     * 使用多线程执行任务
     *
     * @param items 任务列表
     * @param task  任务
     * @param <T>   任务类型
     */
    public static <T> void startWithMultiThreadsSync(List<T> items, SingleTask<T> task) {
        TaskList<T> taskList = createTaskList();
        for (T item : items) {
            taskList.add(Promise.resolve(() -> {
                task.execute(item);
                return null;
            }));
        }
        taskList.awaitAll();
    }

    /**
     * 使用多线程执行任务
     *
     * @param items               任务列表
     * @param task                任务
     * @param exceptionHandleTask 异常处理任务
     * @param <T>                 任务类型
     */
    public static <T> void startWithMultiThreadsSync(List<T> items, SingleTask<T> task, SingleTask<RuntimeException> exceptionHandleTask) {
        TaskList<T> taskList = createTaskList();
        for (T item : items) {
            taskList.add(
                    Promise.resolve(() -> {
                        task.execute(item);
                        return item;
                    }).onException((e) -> {
                        exceptionHandleTask.execute(new RuntimeException(e));
                        return item;
                    })
            );
        }
        taskList.startAllAsync();
    }

    /**
     * 使用多线程执行任务
     *
     * @param items 任务列表
     * @param task  任务
     * @param <T>   任务类型
     * @param <R>   结果类型
     * @return 任务列表
     */
    public static <T, R> TaskList<R> startWithMultiThreadsAsync(List<T> items, CallBackTask<T, R> task) {
        TaskList<R> taskList = createTaskList();
        for (T item : items) {
            taskList.add(Promise.resolve(() -> task.execute(item)));
        }
        taskList.startAllAsync();
        return taskList;
    }

    /**
     * 使用多线程执行任务
     *
     * @param items               任务列表
     * @param task                任务
     * @param exceptionHandleTask 异常处理任务
     * @param <T>                 任务类型
     * @param <R>                 结果类型
     * @return 任务列表
     */
    public static <T, R> TaskList<R> startWithMultiThreadsAsync(List<T> items, CallBackTask<T, R> task, ExceptionHandleTask<R> exceptionHandleTask) {
        TaskList<R> taskList = createTaskList();
        for (T item : items) {
            taskList.add(
                    Promise.resolve(() -> task.execute(item))
                            .onException(exceptionHandleTask)
            );
        }
        taskList.startAllAsync();
        return taskList;
    }

    /**
     * 使用多线程执行任务
     *
     * @param items 任务列表
     * @param task  任务
     * @param <T>   任务类型
     * @param <R>   结果类型
     * @return 任务列表
     */
    public static <T, R> List<R> startWithMultiThreadsSync(List<T> items, CallBackTask<T, R> task) {
        TaskList<R> taskList = createTaskList();
        for (T item : items) {
            taskList.add(Promise.resolve(() -> task.execute(item)));
        }
        taskList.startAllAsync();
        return taskList.awaitAll();
    }

    /**
     * 使用多线程执行任务
     *
     * @param items               任务列表
     * @param task                任务
     * @param exceptionHandleTask 异常处理任务
     * @param <T>                 任务类型
     * @param <R>                 结果类型
     * @return 任务列表
     */
    public static <T, R> List<R> startWithMultiThreadsSync(List<T> items, CallBackTask<T, R> task, ExceptionHandleTask<R> exceptionHandleTask) {
        TaskList<R> taskList = createTaskList();
        for (T item : items) {
            taskList.add(
                    Promise.resolve(() -> task.execute(item))
                            .onException(exceptionHandleTask)
            );
        }
        taskList.startAllAsync();
        return taskList.awaitAll();
    }
}

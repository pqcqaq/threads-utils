package online.zust.qcqcqc.utils.threads;

import online.zust.qcqcqc.utils.threads.enums.PromiseStatus;
import online.zust.qcqcqc.utils.threads.tasks.ExceptionHandleTask;
import online.zust.qcqcqc.utils.threads.tasks.PromisedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author qcqcqc
 * Date: 2024/2/26
 * Time: 01:06
 */
public class Promise<T> {
    /**
     * 线程池
     * 在使用Promise之前需要设置线程池
     * 工具类中自动注入线程池
     */
    private static final Executor PROMISE_EXECUTOR = PromiseExecutor.getPromiseExecutor();
    /**
     * 设置执行状态，1表示未执行，0表示已执行
     */
    private final CountDownLatch countDownLatch;
    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(Promise.class);
    /**
     * 任务
     */
    private final PromisedTask<T> promisedTask;
    /**
     * 成功回调
     */
    private Consumer<T> success;
    /**
     * 失败回调
     */
    private Consumer<T> fail;
    /**
     * 最终回调
     */
    private Consumer<T> finallyCall;
    /**
     * 异常处理
     */
    private ExceptionHandleTask<T> handleException;
    /**
     * 结果
     */
    private T result;
    /**
     * 状态
     */
    private PromiseStatus status;
    /**
     * 下一步状态
     */
    private final NextStatus nextStatus;

    /**
     * 是否已经开始
     */
    private boolean started;

    /**
     * 下一步状态
     */
    public static class NextStatus {
        /**
         * 状态
         */
        private PromiseStatus status;

        /**
         * 构造函数
         */
        public NextStatus() {
            this.status = PromiseStatus.FULFILLED;
        }

        /**
         * 接受
         * 下一步进入成功回调
         */
        public void accept() {
            this.status = PromiseStatus.FULFILLED;
        }

        /**
         * 拒绝
         * 下一步进入异常回调
         */
        public void reject() {
            this.status = PromiseStatus.REJECTED;
        }

        /**
         * 取消
         * 下一步直接进入最终回调
         */
        public void terminated() {
            this.status = PromiseStatus.CANCELED;
        }
    }

    /**
     * 构造函数
     *
     * @param promisedTask 任务
     */
    private Promise(PromisedTask<T> promisedTask) {
        // init
        this.nextStatus = new NextStatus();
        this.promisedTask = promisedTask;
        this.countDownLatch = new CountDownLatch(1);
        this.status = PromiseStatus.PENDING;
        this.started = false;
    }

    /**
     * 创建一个Promise
     *
     * @param consumer 任务
     * @param <T>      泛型
     * @return Promise
     */
    public static <T> Promise<T> resolve(PromisedTask<T> consumer) {
        return new Promise<>(consumer);
    }

    /**
     * 创建一个Promise
     *
     * @param consumer 任务
     * @param <T>      泛型
     * @return Promise
     */
    public static <T> Promise<T> resolve(Callable<T> consumer) {
        return new Promise<>((status) -> consumer.call());
    }

    /**
     * 创建一个Promise
     *
     * @param consumer 任务
     * @return Promise
     */
    public static Promise<?> resolve(Runnable consumer) {
        return new Promise<>((status) -> {
            consumer.run();
            return null;
        });
    }

    /**
     * 成功回调
     *
     * @param consumer 回调
     * @return Promise
     */
    public Promise<T> onSucceed(Consumer<T> consumer) {
        this.success = consumer;
        return this;
    }

    /**
     * 失败回调
     *
     * @param consumer 回调
     * @return Promise
     */
    public Promise<T> onFail(Consumer<T> consumer) {
        this.fail = consumer;
        return this;
    }

    /**
     * 最终回调
     *
     * @param consumer 回调
     */
    public Promise<T> onFinally(Consumer<T> consumer) {
        this.finallyCall = consumer;
        return this;
    }

    /**
     * 异常处理
     *
     * @param exceptionExceptionHandleTask 回调
     * @return Promise
     */
    public Promise<T> onException(ExceptionHandleTask<T> exceptionExceptionHandleTask) {
        this.handleException = exceptionExceptionHandleTask;
        return this;
    }

    /**
     * 开始执行
     */
    public void startAsync() {
        if (started) {
            log.debug("尝试start重复启动一个Promise！");
            return;
        }
        Runnable promiseTaskLine = getExecutorTask();
        this.started = true;
        if (PROMISE_EXECUTOR == null) {
            log.error("Promise线程池未设置，使用当前线程执行任务，请检查线程池配置！");
            promiseTaskLine.run();
            return;
        }
        // 使用线程池执行任务
        PROMISE_EXECUTOR.execute(promiseTaskLine);
    }

    /**
     * 获取执行任务
     *
     * @return 任务
     */
    private Runnable getExecutorTask() {
        return () -> {
            try {
                this.result = promisedTask.execute(nextStatus);
                this.status = this.nextStatus.status;
                switch (this.status) {
                    case CANCELED:
                        break;
                    case FULFILLED:
                        handleSuccess();
                        break;
                    case REJECTED:
                        handleError(new RuntimeException("Promise rejected by user"));
                        break;
                    default:
                        handleSuccess();
                        break;
                }
            } catch (Exception e) {
                this.result = handleException(e);
                // 如果发生异常，就不需要执行错误回调了，因为object一定是null
//                handleError(e);
            } finally {
                handleFinally();
                countDownLatch.countDown();
            }
//            handleFinally();
//            countDownLatch.countDown();
        };
    }

    /**
     * 同步执行
     */
    public T startSync() {
        if (started) {
            log.warn("尝试start重复启动一个Promise！将等待异步完成...");
            return await();
        }
        this.started = true;
        getExecutorTask().run();
        return getResult();
    }

    /**
     * 处理异常
     *
     * @param e 异常
     */
    private T handleException(Exception e) {
        T execute = null;
        if (handleException == null) {
            log.error("Promise error Not captured：{}", e.getMessage());
            return execute;
        }
        try {
            execute = handleException.execute(e);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return execute;
    }

    /**
     * 处理最终回调
     */
    private void handleFinally() {
        if (finallyCall == null) {
            return;
        }
        try {
            finallyCall.accept(result);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 处理异常
     *
     * @param e 异常
     */
    private void handleError(Exception e) {
        log.error(e.getMessage());
        this.status = PromiseStatus.REJECTED;
        if (fail == null) {
            return;
        }
        try {
            fail.accept(result);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    /**
     * 处理成功
     */
    private void handleSuccess() {
        if (success == null) {
            return;
        }
        try {
            success.accept(result);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 获取结果
     *
     * @return 结果
     */
    public T getResult() {
        return result;
    }

    /**
     * 等待结果
     *
     * @return 结果
     */
    public T await() {
        if (!started) {
            startAsync();
        }
        // 等待线程池执行完毕
        waitForFinish();
        return getResult();
    }

    /**
     * 等待执行完毕
     */
    public void waitFinish() {
        if (!started) {
            startAsync();
        }
        // 等待线程池执行完毕
        waitForFinish();
    }

    /**
     * 等待执行完毕
     */
    private void waitForFinish() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public PromiseStatus getStatus() {
        return status;
    }

    /**
     * 重新构建
     *
     * @return Promise
     */
    public Promise<T> reBuild() {
        if (!started) {
            log.debug("try to reBuild a promise that has not started!");
            return this;
        }
        return new Promise<>(promisedTask).onSucceed(success).onFail(fail).onFinally(finallyCall).onException(handleException);
    }

    /**
     * 更改任务
     *
     * @param task 任务
     * @return Promise
     */
    public Promise<T> changeTask(PromisedTask<T> task) {
        return new Promise<>(task).onSucceed(success).onFail(fail).onFinally(finallyCall).onException(handleException);
    }

    /**
     * 是否已经开始
     *
     * @return 是否已经开始
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * 是否已经完成
     *
     * @return 是否已经完成
     */
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }
}

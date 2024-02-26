package online.zust.qcqcqc.utils.threads;

import online.zust.qcqcqc.utils.config.ThreadsUtilsAutoInject;
import online.zust.qcqcqc.utils.threads.enums.PromiseStatus;
import online.zust.qcqcqc.utils.threads.tasks.PromisedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author qcqcqc
 */
public class Promise<T> {
    /**
     * 线程池
     * 在使用Promise之前需要设置线程池
     * 工具类中自动注入线程池
     */
    private static final Executor PROMISE_EXECUTOR = ThreadsUtilsAutoInject.getPromiseExecutor();
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
    private final Next next;

    /**
     * 下一步状态
     */
    public static class Next {
        /**
         * 状态
         */
        private PromiseStatus status;

        /**
         * 构造函数
         */
        public Next() {
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
         * 未决
         * 下一步直接进入最终回调
         */
        public void terminated() {
            this.status = PromiseStatus.PENDING;
        }
    }

    /**
     * 构造函数
     *
     * @param promisedTask 任务
     */
    private Promise(PromisedTask<T> promisedTask) {
        this.next = new Next();
        this.promisedTask = promisedTask;
        this.countDownLatch = new CountDownLatch(1);
        this.status = PromiseStatus.PENDING;
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
     * 开始执行
     */
    public void start() {
        Runnable promiseTaskLine = () -> {
            try {
                this.result = promisedTask.execute(next);
                this.status = this.next.status;
                switch (this.status) {
                    case PENDING:
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
                handleError(e);
            }
            handleFinally();
            countDownLatch.countDown();
        };
        if (PROMISE_EXECUTOR == null) {
            log.error("Promise线程池未设置，使用当前线程执行任务，请检查线程池配置！");
            promiseTaskLine.run();
            return;
        }
        // 使用线程池执行任务
        PROMISE_EXECUTOR.execute(promiseTaskLine);
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
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    /**
     * 等待结果
     *
     * @return 结果
     */
    public T await() {
        start();
        // 等待线程池执行完毕
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return getResult();
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public PromiseStatus getStatus() {
        return status;
    }
}

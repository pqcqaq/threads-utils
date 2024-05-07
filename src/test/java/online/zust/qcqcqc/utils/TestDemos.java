package online.zust.qcqcqc.utils;


import online.zust.qcqcqc.utils.threads.Promise;
import online.zust.qcqcqc.utils.threads.PromiseExecutor;
import online.zust.qcqcqc.utils.threads.Tasks;
import online.zust.qcqcqc.utils.threads.enums.PromiseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author qcqcqc
 * Date: 2024/2/28
 * Time: 15:54
 */
public class TestDemos {

    @BeforeEach
    public void initExecutor() {
        PromiseExecutor.initExecutor(ThreadsUtils.createExecutor(127));
    }

    @Test
    public void testThreads() throws Exception {
        Promise<String> stringPromise = Promise.resolve((status) -> {
                    System.out.println("start wait.....");
                    Thread.sleep(3000);
                    // 拒绝状态，进入失败回调
                    status.reject();
                    return "123";
                })
                .onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"))
                .onFail((res) -> System.out.println("返回结果为" + res + "，进入失败回调"))
                .onFinally((res) -> System.out.println("返回结果为" + res + "，进入finally回调"));
        stringPromise.startAsync();
        System.out.println("主线程已完成，等待3秒钟");
        boolean started = stringPromise.isStarted();
        System.out.println("Promise是否已启动：" + started);
        Thread.sleep(3000);
        System.out.println("尝试获取Promise结果");
        boolean done = stringPromise.isDone();
        System.out.println("Promise是否已完成：" + done);
        String await = stringPromise.await();
        System.out.println("Promise结果为：" + await);

        System.out.println("尝试重启Promise");
        stringPromise.reBuild().startAsync();
        stringPromise.changeTask((status) -> {
            System.out.println("start wait.....");
            Thread.sleep(2000);
            // 接受状态，进入成功回调
            status.accept();
            return "456";
        }).startAsync();
        Thread.sleep(5000);
    }

    @Test
    public void testMultiTask() {
        List<Promise<Integer>> pool = new ArrayList<>();
        List<Integer> integers = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            Promise<Integer> integerPromise = Promise.resolve((status) -> {
                        System.out.println("start wait.....");
                        Thread.sleep(new Random().nextInt(500) + 500);
                        // 接受状态，进入成功回调
                        status.accept();
                        integers.add(finalI);
                        return finalI;
                    }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"))
                    .onFail((res) -> System.out.println("返回结果为" + res + "，进入失败回调"));
            pool.add(integerPromise);
        }
        List<Integer> integersResult = Tasks.awaitAll(pool);
        System.out.println("执行结果为：" + integers);
        System.out.println("Promise结果为：" + integersResult);
    }

    @Test
    public void testMultiReturnValue() {
        Promise<Integer> integerPromise = Promise.resolve((status) -> {
            System.out.println("start wait.....");
            Thread.sleep(1000);
            // 接受状态，进入成功回调
            status.accept();
            return 123;
        }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"));
        Promise<String> stringPromise = Promise.resolve((status) -> {
            System.out.println("start wait.....");
            Thread.sleep(1000);
            // 接受状态，进入成功回调
            status.accept();
            return "456";
        }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"));
        System.out.println("Promise已启动，等待结果");
        List<Object> promiseResults = Tasks.awaitAll(integerPromise, stringPromise);
        System.out.println("Promise结果为：" + promiseResults);
    }

    @Test
    public void testSync() {
        Promise<Integer> integerPromise = Promise.resolve((status) -> {
            System.out.println("start wait.....");
            Thread.sleep(1000);
            // 接受状态，进入成功回调
            status.accept();
            return 123;
        }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"));
        integerPromise.startSync();
        Promise<String> stringPromise = Promise.resolve((status) -> {
            System.out.println("start wait.....");
            Thread.sleep(1000);
            // 接受状态，进入成功回调
            status.terminated();
            return "456";
        }).onFinally((res) -> System.out.println("返回结果为" + res + "，进入结束回调"));
        Tasks.awaitAllVoid(integerPromise.reBuild(), stringPromise);
        System.out.println("Promise结果为：" + integerPromise.getResult() + " " + stringPromise.getResult());
    }

    @Test
    public void testCreateThreadPool() {
        new ThreadPoolExecutor(50,
                100,
                60,
                SECONDS,
                new LinkedBlockingQueue<>(127),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Test
    public void testSimple() {
        Promise<Integer> resolve = Promise.resolve((status) -> {
                    System.out.println("start promise....");
                    Thread.sleep(1000);
                    return 123;
                }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"))
                .onFail((res) -> System.out.println("返回结果为" + res + "，进入失败回调"))
                .onException((e) -> {
                    System.out.println("返回结果为" + e + "，进入异常回调");
                    return 456;
                })
                .onFinally((res) -> System.out.println("返回结果为" + res + "，进入finally回调"));
        Integer i = resolve.startSync();
        System.out.println("Promise结果为：" + i);
    }


    @Test
    public void testSimple2() {
        Promise<?> resolve = Promise.resolve(() -> {
                    System.out.println("start promise....");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"))
                .onFail((res) -> System.out.println("返回结果为" + res + "，进入失败回调"))
                .onException((e) -> {
                    System.out.println("返回结果为" + e + "，进入异常回调");
                    return null;
                })
                .onFinally((res) -> System.out.println("返回结果为" + res + "，进入finally回调"));
        resolve.startAsync();
        PromiseStatus status = resolve.getStatus();
        System.out.println(status.getCode());
        System.out.println(status.getDesc());
        resolve.startAsync();
        resolve.startSync();
        resolve.await();
    }


    @Test
    public void testException() {
        Promise<Integer> resolve = Promise.resolve((status) -> {
                    System.out.println("start promise....");
                    Thread.sleep(1000);
                    throwException();
                    return 123;
                }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"))
                .onFail((res) -> System.out.println("返回结果为" + res + "，进入失败回调"))
                .onException((e) -> {
                    System.out.println("返回结果为" + e.getMessage() + "，进入异常回调");
                    return 456;
                })
                .onFinally((res) -> System.out.println("返回结果为" + res + "，进入finally回调"));
        Integer i = resolve.startSync();
        System.out.println("Promise结果为：" + i);
    }

    public void throwException() {
        throw new RuntimeException() {
            @Serial
            private static final long serialVersionUID = -129037843615610725L;

            public String getMessage() {
                return "运行出错啦";
            }
        };
    }

    @Test
    public void testTimeout() {
        Promise<?> timeout = Tasks.setTimeout(() -> System.out.println("timeout"), 3000);
        timeout.startAsync();
        timeout.await();
    }

    @Test
    public void testNullTask() throws Exception {
        Promise<Object> resolve = Promise.resolve(() -> null);
        Tasks.TaskList<Object> taskList = Tasks.createTaskList();
        taskList.add(resolve);
        taskList.add(() -> {
            Thread.sleep(1000);
            return null;
        });
        taskList.onTasksFinish(() -> System.out.println("任务全部完成"));
        taskList.startAllAsync();
        Thread.sleep(100);
        int unfinishedTaskCount = taskList.getUnfinishedTaskCount();
        System.out.println("未完成任务数量：" + unfinishedTaskCount);
        taskList.awaitAll();
    }

    @Test
    public void testProgressCallback() {
        Tasks.TaskList<Integer> taskList = Tasks.createTaskList();
        for (int i = 0; i < 10; i++) {
            taskList.add(() -> {
                int millis = new Random().nextInt(1000);
                Thread.sleep(millis);
                return millis;
            });
        }
        taskList.addShowProgress();
        taskList.onTasksFinish(() -> System.out.println("任务全部完成"));
        taskList.startAllAsync();
        List<Integer> integers = taskList.awaitAll();
        System.out.println("任务结果：" + integers);
    }

    @Test
    public void testStartWithMultiThreads1() {
        List<Integer> integers = new ArrayList<>();
        Tasks.startWithMultiThreadsSync(List.of(1, 2, 3, 4, 5), (item) -> {
            System.out.println("执行任务：" + item);
            integers.add(item);
        });
        System.out.println("任务结果：" + integers);
    }

    @Test
    public void testStartWithMultiThreads2() {
        List<String> strings = Tasks.startWithMultiThreadsSync(List.of(1, 2, 3, 4, 5), (item) -> {
            System.out.println("执行任务：" + item);
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(item);
        });
        System.out.println("任务结果：" + strings);
    }

    @Test
    public void testStartWithMultiThreads3() {
        Tasks.TaskList<String> stringTaskList = Tasks.startWithMultiThreadsAsync(List.of(1, 2, 3, 4, 5), (item) -> {
            System.out.println("执行任务：" + item);
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(item);
        });
        stringTaskList.onShowProgress((done, total) -> System.out.println("已完成任务数：" + done + "，总任务数：" + total));
        stringTaskList.onTasksFinish(() -> System.out.println("任务全部完成"));
        stringTaskList.awaitAll();
    }

    @Test
    public void testStartWithMultiThreadsWithExceptionHandler() {
        Tasks.TaskList<String> stringTaskList = Tasks.startWithMultiThreadsAsync(List.of(1, 2, 3, 4, 5), (item) -> {
            System.out.println("执行任务：" + item);
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(item);
        }, (e) -> {
            System.out.println("任务执行出错：" + e.getMessage());
            return "error";
        });
        stringTaskList.onShowProgress((done, total) -> System.out.println("已完成任务数：" + done + "，总任务数：" + total));
        stringTaskList.onTasksFinish(() -> System.out.println("任务全部完成"));
        stringTaskList.awaitAll();
    }

    @Test
    public void testStartWithMultiThreadsWithExceptionHandler2() {
        List<String> strings = Tasks.startWithMultiThreadsSync(List.of(1, 2, 3, 4, 5), (item) -> {
            System.out.println("执行任务：" + item);
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(item);
        }, (e) -> {
            System.out.println("任务执行出错：" + e.getMessage());
            return "error";
        });
        System.out.println("任务结果：" + strings);
    }

    @Test
    public void testCompletableFuture() {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 123;
        });
        future.thenAccept(System.out::println);
        CompletableFuture.allOf(future).join();
    }
}

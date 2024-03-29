# 更优雅的多线程调用工具



## 在Maven中导入依赖

```xml
<dependency>
    <groupId>online.zust.qcqcqc.utils</groupId>
    <artifactId>threads-utils-spring-boot-starter</artifactId>
    <version>1.2.2</version>
</dependency>
```

上一个版本是：1.2.1



## 配置异步线程池

- #### Promise异步线程池配置

    - spring环境中，仅需修改配置文件（已提供默认配置）

        - ![](https://cdn.jsdelivr.net/gh/pqcqaq/imageSource/upload/20240227172305.png)

        - ```yaml
            promise:
              executor:
                core-pool-size: 16
                max-pool-size: 32
                queue-capacity: 1000
                keep-alive-seconds: 60
                thread-name-prefix: promise
            ```

    - #### 非spring环境中，需要手动初始化线程池

        - ```java
            PromiseExecutor.initExecutor(ThreadsUtils.createExecutor(50));
            ```





## Promise使用

- ### PromisedTask接口

    - ```java
        	/**
             * 执行任务
             * @param status 状态
             * @return 结果
             * @throws Exception 异常
             */
            T execute(Promise.NextStatus status) throws Exception;
        ```

    - promise中运行的任务需要实现这个接口，**传入一个参数，返回一个参数**

    - NextStatus status：下一状态，可以调用其中的方法来指定Promise的下一步操作

    - T：返回类型，也是Promise的泛型

- ### 创建一个Promise

    - ```java
        Promise<Integer> resolve = Promise.resolve((status) -> {
                    System.out.println("start promise....");
                    Thread.sleep(1000);
                    return 123;
                });
        ```

        - 此时这个Promise还未执行，处于待执行状态

    - ##### 指定回调方法

        - ```java
                    Promise<Integer> resolve = Promise.resolve((status) -> {
                                System.out.println("start promise....");
                                Thread.sleep(1000);
                                return 123;
                            }).onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"))
                            .onFail((res) -> System.out.println("返回结果为" + res + "，进入失败回调"))
                            .onException((e) -> System.out.println("返回结果为" + e + "，进入异常回调"))
                            .onFinally((res) -> System.out.println("返回结果为" + res + "，进入finally回调"));
            ```

        - ##### 有四种回调：

            - succeed回调：任务执行成功时进行回调，传入参数为任务的执行结果
            - fail回调：任务执行失败时进行回调，传入参数为任务执行结果
            - exception回调：在任务出现异常时进行回调，传入参数为异常Exception类
            - finally回调：直接到最后进行回调，传入参数为执行结果

            

- ### 开始执行一个Promise

    - 有两种执行方式

        - 交由线程池进行运行

            ```java
            resolve.startAsync();
            ```

        - 由当前线程直接执行任务

            ```java
            resolve.startSync();
            ```

        

- ### 手动确定下一步运行状态

    - 在任务中，我们传入了status作为下一状态信息，我们可以调用其中的方法，直接进行状态转移

        - ```java
                                status.accept();
                                status.reject();
                                status.terminated();
            ```

        - accept()：

            - 默认状态，在该任务直接完成后进入成功回调。

            ![](https://cdn.jsdelivr.net/gh/pqcqaq/imageSource/upload/20240227174439.png)

        - reject()：

            - 拒绝状态，在完成任务之后进入失败回调，进行结果处理。

            ![](https://cdn.jsdelivr.net/gh/pqcqaq/imageSource/upload/20240227174552.png)

        - terminated()：

            - 终止状态，在完成任务后直接进入结束回调，跳过成功或失败回调。

            ![](https://cdn.jsdelivr.net/gh/pqcqaq/imageSource/upload/20240227174631.png)

        - #### 你也可以借由抛出异常来进入异常回调

            - ![](https://cdn.jsdelivr.net/gh/pqcqaq/imageSource/upload/20240227175627.png)

            - ##### 在新版中，异常回调必须有返回值，**作为这次任务的返回结果**

            

- ### 等待Promise执行结束并获取返回值

    ```java
    public T startSync()
    ```

    - 由当前线程直接执行该任务，并获取任务的返回值

    ```java
    public void startAsync()
    ```

    - 开始异步执行任务，此时结果还未产出，所以返回值为void

    ```java
    public T await()
    ```

    - 等待异步任务执行完毕，并获取返回值

    ```java
    public void waitFinish()
    ```

    - 等待异步任务执行完毕，忽略返回值



- ### 批量获取Promise返回值

    - #### Tasks类

        - 提供了三个方法，用于等待执行结束

            ```java
            public static <T> List<T> awaitAll(List<Promise<T>> promises)
            ```

            - 等待同一种返回类型的Promise执行结束，并批量获取他们的返回值
            - 返回结果的顺序和传入Promise的顺序是一致的

            ```java
            public static List<Object> awaitAll(Promise<?>... promises)
            ```

            - 等待不同种类型的Promise执行结束，并批量获取他们的返回值
                - 因为返回类型的不同，所以统一返回Object数组作为结果
                - 顺序也与传入顺序一致

            ```java
            public static void awaitAllVoid(Promise<?>... promises)
            ```

            - 只是等待执行结束并不关心返回值
                - 也就是返回值已经在成功回调中处理完成，后续不需要关心返回值的情况

        ![](https://cdn.jsdelivr.net/gh/pqcqaq/imageSource/upload/20240227174849.png)



- ### 重启Promise

    - ##### 出于安全考虑，一个Promise对象只能被执行一次，但是可以多次获取返回值，如果需要多次运行一个任务，这里也提供了两个方法

    ```java
    public Promise<T> reBuild()
    ```

    - 在任务执行完成后，重构一个完全相同的任务。
    - 如果当前任务还未被执行，则返回自身。

    ```java
    public Promise<T> changeTask(PromisedTask<T> task)
    ```

    - 修改任务，但不修改回调函数，返回一个新的任务。



## 使用示例

- ```java
        @Test
        public void testThreads() throws Exception {
            PromiseExecutor.initExecutor(ThreadsUtils.createExecutor(50));
            Promise<String> stringPromise = Promise.resolve((status) -> {
                        System.out.println("start wait.....");
                        Thread.sleep(5000);
                        // 拒绝状态，进入失败回调
                        status.reject();
                        return "123";
                    })
                    .onSucceed((res) -> System.out.println("返回结果为" + res + "，进入成功回调"))
                    .onFail((res) -> System.out.println("返回结果为" + res + "，进入失败回调"))
                    .onFinally((res) -> System.out.println("返回结果为" + res + "，进入finally回调"));
            stringPromise.startAsync();
            System.out.println("主线程已完成，等待3秒钟");
            Thread.sleep(3000);
            System.out.println("尝试获取Promise结果");
            String await = stringPromise.await();
            System.out.println("Promise结果为：" + await);
    
            System.out.println("尝试重启Promise");
            stringPromise.reBuild().startAsync();
            stringPromise.changeTask((status) -> {
                System.out.println("start wait.....");
                Thread.sleep(5000);
                // 接受状态，进入成功回调
                status.accept();
                return "456";
            }).startAsync();
            Thread.sleep(10000);
        }
    ```

- ```bash
    18:07:28.724 [main] DEBUG org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor - Initializing ExecutorService
    主线程已完成，等待3秒钟
    start wait.....
    尝试获取Promise结果
    18:07:33.739 [1709028448721-exec-1] ERROR online.zust.qcqcqc.utils.threads.Promise - Promise rejected by user
    返回结果为123，进入失败回调
    返回结果为123，进入finally回调
    Promise结果为：123
    尝试重启Promise
    start wait.....
    start wait.....
    18:07:38.744 [1709028448721-exec-2] ERROR online.zust.qcqcqc.utils.threads.Promise - Promise rejected by user
    返回结果为123，进入失败回调
    返回结果为123，进入finally回调
    返回结果为456，进入成功回调
    返回结果为456，进入finally回调
    ```



- ```java
        @Test
        public void testException() {
            PromiseExecutor.initExecutor(ThreadsUtils.createExecutor(50));
            Promise<Integer> resolve = Promise.resolve((status) -> {
                        System.out.println("start promise....");
                        Thread.sleep(1000);
                        throwException();
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
    
        public void throwException() {
            throw new RuntimeException("运行出错啦");
        }
    ```

- ```bash
    18:17:44.345 [main] DEBUG org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor - Initializing ExecutorService
    start promise....
    返回结果为java.lang.RuntimeException: 运行出错啦，进入异常回调
    返回结果为456，进入finally回调
    Promise结果为：456
    ```

- 


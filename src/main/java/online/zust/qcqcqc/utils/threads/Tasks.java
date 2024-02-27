package online.zust.qcqcqc.utils.threads;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qcqcqc
 */
public class Tasks {
    public static <T> List<T> awaitAll(List<Promise<T>> promises) {
        List<T> results = new ArrayList<>();
        for (Promise<T> promise : promises) {
            results.add(promise.await());
        }
        return results;
    }

    public static List<Object> awaitAll(Promise<?>... promises) {
        List<Object> results = new ArrayList<>();
        for (Promise<?> promise : promises) {
            Object await = promise.await();
            results.add(await);
        }
        return results;
    }

    public static void awaitAllVoid(Promise<?>... promises) {
        for (Promise<?> promise : promises) {
            promise.await();
        }
    }
}

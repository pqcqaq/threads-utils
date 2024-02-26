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
}

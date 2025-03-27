package io.hhplus.tdd.common;

import io.hhplus.tdd.point.UserPoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {
    private static final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    public static ReentrantLock getLock(Long id) {
        lockMap.putIfAbsent(id, new ReentrantLock());
        return lockMap.get(id);
    }

    public static void releaseLock(Long id, ReentrantLock lock) {
        lockMap.remove(id, lock);
    }
}

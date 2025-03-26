package io.hhplus.tdd.point;

import io.hhplus.tdd.common.TransactionManager;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final Long MAXIMUM_POINT = 999999L;

    private final ReentrantLock lock = new ReentrantLock();

    public UserPoint point(Long id) {
        return userPointTable.selectById(id);
    }

    public List<PointHistory> history(Long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint charge(Long id, Long amount) {
        return runTransactionWithLock(id, amount, TransactionType.CHARGE);
    }

    public UserPoint use(Long id, Long amount) {
        return runTransactionWithLock(id, amount, TransactionType.USE);
    }

    private UserPoint runTransactionWithLock(Long id, Long amount, TransactionType transactionType) {
        lock.lock();
        try {
            UserPoint userPoint = userPointTable.selectById(id);
            validation(userPoint.point(), amount, transactionType);
            long sum = getSum(userPoint.point(), amount, transactionType);
            // 트랜잭션 실행
            AtomicReference<UserPoint> updatedUserPoint = new AtomicReference<>();
            TransactionManager.runTransaction(userPoint.point(), (originalPoint) -> {
                // 액션 실행
                UserPoint result = userPointTable.insertOrUpdate(id, sum);
                pointHistoryTable.insert(id, sum, transactionType, System.currentTimeMillis());
                userPointTable.selectById(id);
                updatedUserPoint.set(result);
            }, () -> {
                // 롤백 처리
                userPointTable.insertOrUpdate(id, userPoint.point());
                pointHistoryTable.insert(id, sum, TransactionType.FAIL, System.currentTimeMillis());
            });
            return updatedUserPoint.get();
        } finally {
            lock.unlock();
        }
    }

    private void validation(long point, Long amount, TransactionType transactionType) {
        switch (transactionType) {
            case USE:
                if (point - amount < 0) {
                    throw new CustomException("-202", "잔고가 부족합니다.");
                }
                break;
            case CHARGE:
                if (point + amount > MAXIMUM_POINT) {
                    throw new CustomException("-201", "최대 잔고를 초과했습니다.");
                }
                break;
        }
    }

    public Long getSum(Long point, Long amount, TransactionType transactionType) {
        return transactionType == TransactionType.USE ? point - amount : point + amount;
    }
}


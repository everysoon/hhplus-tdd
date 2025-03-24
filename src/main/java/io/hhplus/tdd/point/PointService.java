package io.hhplus.tdd.point;

import io.hhplus.tdd.common.TransactionManager;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final Long MAXIMUM_POINT = 100000L;

    public UserPoint point(Long id) {
        return userPointTable.selectById(id);
    }

    public List<PointHistory> history(Long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint charge(Long id, Long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint.point() + amount < MAXIMUM_POINT) {
            throw new CustomException("-201", "최대 잔고를 초과했습니다.");
        }
        final AtomicReference<UserPoint> updatedUserPoint = new AtomicReference<>();
        TransactionManager.runTransaction(userPoint.point(),(originalPoint)->{
            // 포인트 충전
            UserPoint result = userPointTable.insertOrUpdate(id, originalPoint + amount);
            pointHistoryTable.insert(id,amount,TransactionType.CHARGE, System.currentTimeMillis());
            updatedUserPoint.set(result);
        },()->{
            // rollback
            userPointTable.insertOrUpdate(id, userPoint.point());
            pointHistoryTable.insert(id,amount,TransactionType.FAIL, System.currentTimeMillis());
        });
        return updatedUserPoint.get();
    }

    public UserPoint use(Long id, Long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint.point() - amount < 0) {
            throw new CustomException("-202", "잔고가 부족합니다.");
        }
        final AtomicReference<UserPoint> updatedUserPoint = new AtomicReference<>();
        TransactionManager.runTransaction(userPoint.point(),(originalPoint)->{
            // 포인트 사용
            UserPoint result = userPointTable.insertOrUpdate(id, originalPoint - amount);
            pointHistoryTable.insert(id,amount,TransactionType.USE, System.currentTimeMillis());
            updatedUserPoint.set(result);
        },()->{
            // rollback
            userPointTable.insertOrUpdate(id, userPoint.point());
            pointHistoryTable.insert(id,amount,TransactionType.FAIL, System.currentTimeMillis());
        });
        return updatedUserPoint.get();
    }
}

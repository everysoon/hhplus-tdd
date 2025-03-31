package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.CustomException;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;

import static io.hhplus.tdd.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

// 기본 과제 테스트 - 통합 테스트
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class BasicTest {

    @Autowired
    PointService pointService;
    @Autowired
    UserPointTable userPointTable;
    @Autowired
    PointHistoryTable historyTable;


    @BeforeEach
    public void setup() {
        isUnitTest = false;
        createMockPoint(userPointTable,1L);
        createMockPoint(userPointTable,2L);
        createMockHistory(historyTable,1L);
        createMockHistory(historyTable,2L);
    }
    @AfterEach
    public void shutdown() {
        userPointTable.insertOrUpdate(1L, 0);
        userPointTable.insertOrUpdate(2L, 0);
    }
    @Test
    @DisplayName("유저 포인트 조회")
    // 유저 포인트를 조회했을 때, 잔액이 예상과 같아야 한다.
    public void getUserPoint() {
        UserPoint userPoint = pointService.point(userId);
        assertEquals(baseAmount, userPoint.point());
    }

    @Test
    @DisplayName("point 기록 조회")
    // point 기록을 조회했을 때 예상 수와 같아야 한다.
    public void getHistory() {
        List<PointHistory> histories = pointService.history(userId);
        assertEquals(1, histories.size());
        assertEquals(baseAmount, histories.get(0).amount());
    }

    @Test
    @DisplayName("point 충전 테스트 - 최대 포인트 초과")
    // 최대 포인트가 초과했을때 CustomException 을 반환해야 한다.
    public void chargePointWhenOverCharging() {
        assertThrows(CustomException.class, () -> {
            pointService.charge(userId, 99999999L);
        });
    }

    @Test
    @DisplayName("point 충전 테스트")
    // 포인트를 충전 했을때 , 잔액이 예상과 같아야 한다.
    public void chargePoint() {
        UserPoint userPoint = pointService.charge(userId, baseAmount);
        assertEquals(baseAmount*2, userPoint.point());
    }

    @Test
    @DisplayName("point 사용 테스트")
    // 포인트를 사용했을때, 잔액이 예상과 같아야 한다.
    public void usePoint() {
        UserPoint userPoint = pointService.use(userId, baseAmount);
        assertEquals(0, userPoint.point());
    }

    @Test
    @DisplayName("point 사용 테스트 - 더 많이 사용할 경우")
    // 잔고 포인트보다 초과량을 사용했을 때 CustomException 을 반환해야 한다.
    public void usePointOverPoint() {
        assertThrows(CustomException.class, () -> {
            pointService.use(userId, 100000L);
        });
    }
}

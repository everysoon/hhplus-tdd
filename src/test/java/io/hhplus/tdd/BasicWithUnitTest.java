package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.CustomException;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static io.hhplus.tdd.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// 기본 과제 테스트 - 단위 테스트
@ExtendWith(MockitoExtension.class)
public class BasicWithUnitTest {

    @InjectMocks
    PointService pointService;
    @Mock
    UserPointTable userPointTable;
    @Mock
    PointHistoryTable historyTable;

    @BeforeEach
    public void setup() {
        // Given (Mock 설정)
        isUnitTest = true;
        createMockPoint(userPointTable, userId);
        createMockHistory(historyTable, userId);

    }

    @Test
    @DisplayName("유저 포인트 조회")
    public void getUserPoint() {
        // When
        UserPoint result = pointService.point(userId);
        // Then
        assertEquals(baseAmount, result.point());
        verify(userPointTable).selectById(userId);
    }

    @Test
    @DisplayName("point 기록 조회")
    public void getHistory() {
        // When
        List<PointHistory> history = pointService.history(userId);
        // Then
        assertEquals(1, history.size());
        assertEquals(baseAmount, history.get(0).amount());
        assertEquals(TransactionType.CHARGE, history.get(0).type());

    }

    @Test
    @DisplayName("point 충전 테스트 - 최대 포인트 초과")
    public void chargePointWhenOverCharging() {
        // 충전 초과시 에러 잘 나나 확인
        assertThrows(CustomException.class, () -> {
            pointService.charge(userId, 20000000L);
        });
        // 그 후 로직 실행 안 됐는지 확인
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(historyTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    @DisplayName("point 충전 테스트")
    public void chargePoint() {
        long addAmount = 100L;
        UserPoint mockPoint = userPointTable.selectById(userId);

        pointService.charge(mockPoint.id(), addAmount);

        assertEquals(200L, mockPoint.point() + addAmount);

        verify(userPointTable).insertOrUpdate(userId, mockPoint.point() + addAmount);
//        verify(historyTable).insert(userId, mockPoint.point() + addAmount, TransactionType.CHARGE, fixedTimestamp);
    }

    @Test
    @DisplayName("point 사용 테스트")
    public void usePoint() {
        long useAmount = 80L;
        pointService.use(userId, useAmount);
        // Then
        verify(userPointTable).insertOrUpdate(userId, baseAmount - useAmount);
//        verify(historyTable).insert(userId, useAmount, TransactionType.USE, anyLong());
    }

    @Test
    @DisplayName("point 사용 테스트 - 더 많이 사용할 경우")
    public void usePointOverPoint() {
        assertThrows(CustomException.class, () -> {
            pointService.use(userId, 100000L);
        });

        // 그 후 로직 실행 안 됐는지 확인
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(historyTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }


}

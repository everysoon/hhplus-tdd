package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static io.hhplus.tdd.TestUtils.userId;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


public class TestUtils {
    public static long userId = 1L;
    public static long baseAmount = 100L;
    public static long fixedTimestamp = 1700000000000L;
    public static boolean isUnitTest = false;
    public static long cursor = 1;

    public static void createMockPoint(UserPointTable userPointTable, long userId) {
        if (isUnitTest) {
            UserPoint userPoint = new UserPoint(userId, userId * baseAmount, System.currentTimeMillis());
            lenient().when(userPointTable.selectById(userId)).thenReturn(userPoint);
        } else {
            userPointTable.insertOrUpdate(userId, userId * baseAmount);
        }
    }
    public static void createMockHistory(PointHistoryTable pointHistoryTable, long userId) {
        List<PointHistory> historyList = new ArrayList<>();
        if (isUnitTest) {
            PointHistory history = new PointHistory(cursor++, userId, userId * baseAmount, TransactionType.CHARGE, System.currentTimeMillis());
            historyList.add(history);
            lenient().when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(historyList);
        } else {
            historyList.add(pointHistoryTable.insert(userId, userId * baseAmount, TransactionType.CHARGE, fixedTimestamp));
        }
    }
}

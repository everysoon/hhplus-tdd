package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint point(Long id) {
        return userPointTable.selectById(id);
    }
    public List<PointHistory> history(Long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }
    public UserPoint charge(Long id,Long amount){
        return userPointTable.insertOrUpdate(id,amount);
    }

    public UserPoint use(Long id,Long amount) throws BadRequestException {
        UserPoint userPoint = userPointTable.selectById(id);
        if(userPoint.point() < amount){
            throw new BadRequestException("잔고가 부족합니다.");
        }
        return userPointTable.insertOrUpdate(id,amount);
    }
}

package io.hhplus.tdd;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.hhplus.tdd.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// 심화 과제 테스트
@SpringBootTest
public class InDepthTest {
    private static final Logger log = LoggerFactory.getLogger(InDepthTest.class);
    @Autowired
    PointService pointService;

    @Autowired
    UserPointTable userPointTable;

    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    @BeforeEach
    public void setup() {
        createMockPoint(userPointTable,userId); // userId : 1L , amount : 100
    }
    @Test
    // 잔액이 100인 user의 포인트를 1000원씩 10번 충전 할 예정으로 결과는 10100원이 되어야 한다.
    public void 동시_충전(){
        long chargeAmount = 1000L; // 각 스레드가 충전할 금액
        long expectedFinalAmount = baseAmount + (threadCount * chargeAmount); // 100 + (10 * 1000) = 10100
        try{
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                         pointService.charge(userId, chargeAmount);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executor.shutdown();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        UserPoint finalPoint = pointService.point(userId);
        assertEquals(expectedFinalAmount, finalPoint.point()); // 최종 포인트 검증
    }
    @Test
    // 잔액이 100인 user의 포인트를 1원씩 10번 차감할 예정으로 결과는 90원이 되어야 한다.
    public void 동시_사용(){
        long chargeAmount = 1L; // 각 스레드가 충전할 금액
        long expectedFinalAmount = baseAmount - (threadCount * chargeAmount); // 100 + (10 * 1) = 90L
        try{
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        pointService.use(userId, chargeAmount);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executor.shutdown();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        UserPoint finalPoint = pointService.point(userId);
        assertEquals(expectedFinalAmount, finalPoint.point()); // 최종 포인트 검증
    }
}

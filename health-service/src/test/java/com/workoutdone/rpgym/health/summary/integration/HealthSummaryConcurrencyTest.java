package com.workoutdone.rpgym.health.summary.integration;

import com.workoutdone.rpgym.health.activity.application.SyncedActivity;
import com.workoutdone.rpgym.health.summary.adapter.out.UserHealthContextResponse;
import com.workoutdone.rpgym.health.summary.adapter.out.UserServiceClient;
import com.workoutdone.rpgym.health.summary.adapter.out.persistence.DailyHealthSummaryJpaRepository;
import com.workoutdone.rpgym.health.summary.application.HealthSummarySyncService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS health_service",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false"
})
class HealthSummaryConcurrencyTest {

    @Autowired
    private HealthSummarySyncService syncService;

    @Autowired
    private DailyHealthSummaryJpaRepository summaryJpaRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    private UUID userId;
    private LocalDate activityDate;

    @BeforeEach
    void setUp() {
        summaryJpaRepository.deleteAll();
        userId = UUID.randomUUID();
        activityDate = LocalDate.of(2026, 10, 19);

        // 💡 NPE 방지를 위해 RETURNS_DEEP_STUBS 모드로 가짜 Response 객체 주입
        UserHealthContextResponse mockResponse = Mockito.mock(
                UserHealthContextResponse.class,
                Mockito.RETURNS_DEEP_STUBS
        );
        given(userServiceClient.getHealthContext(any())).willReturn(mockResponse);
    }

    @Test
    @DisplayName("동시 요청 시 애플리케이션 존재 조회를 우회하여 DB Unique 제약조건에서 409 예외가 터짐을 증명한다")
    void validateConcurrencyLayer() throws InterruptedException {
        int concurrentRequests = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);

        CountDownLatch readyLatch = new CountDownLatch(concurrentRequests);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrentRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger appBlockedCount = new AtomicInteger(0);
        AtomicInteger dbExceptionCount = new AtomicInteger(0);

        SyncedActivity sampleActivity = createSampleSyncedActivity(userId, activityDate);

        for (int i = 0; i < concurrentRequests; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startSignal.await();

                    syncService.sync(sampleActivity);
                    successCount.incrementAndGet();

                } catch (DataIntegrityViolationException e) {
                    dbExceptionCount.incrementAndGet();
                } catch (IllegalArgumentException e) {
                    appBlockedCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startSignal.countDown();
        doneLatch.await();

        long savedCount = summaryJpaRepository.count();

        System.out.println("=========================================");
        System.out.println("[동시성 테스트 정량적 측정 결과]");
        System.out.println("총 동시 요청 수: " + concurrentRequests);
        System.out.println("성공한 요청 수: " + successCount.get());
        System.out.println("앱 레이어(exists) 차단 건수: " + appBlockedCount.get());
        System.out.println("DB 레이어(Unique Constraint) 차단 건수: " + dbExceptionCount.get());
        System.out.println("최종 DB 저장된 레코드 수: " + savedCount);
        System.out.println("=========================================");

        assertThat(savedCount).isEqualTo(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(dbExceptionCount.get()).isEqualTo(9);
        assertThat(appBlockedCount.get()).isZero();
    }

    private SyncedActivity createSampleSyncedActivity(UUID userId, LocalDate date) {
        return new SyncedActivity(
                UUID.randomUUID(),
                userId,
                date,
                Instant.now(),
                1000,
                30,
                200
        );
    }
}
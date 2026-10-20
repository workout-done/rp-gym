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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_hold;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS health_service",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false",
        // 💡 HikariCP 커넥션 풀 크기를 제한하여 커넥션 점유 영향을 명확히 측정
        "spring.datasource.hikari.maximum-pool-size=5"
})
class HealthSummaryConnectionHoldTimeTest {

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
    }

    @Test
    @DisplayName("외부 API에 200ms 네트워크 지연이 발생할 때 트랜잭션/커넥션 점유 시간을 정량적으로 측정한다")
    void measureConnectionHoldTimeWithExternalApiDelay() {
        // [Given] 외부 API 호출 시 200ms 의도적 지연(Latency) 주입
        int simulatedApiDelayMs = 200;

        UserHealthContextResponse mockResponse = Mockito.mock(
                UserHealthContextResponse.class,
                Mockito.RETURNS_DEEP_STUBS
        );

        given(userServiceClient.getHealthContext(any())).willAnswer(invocation -> {
            Thread.sleep(simulatedApiDelayMs); // 외부 네트워크 지연 모의
            return mockResponse;
        });

        SyncedActivity sampleActivity = new SyncedActivity(
                UUID.randomUUID(),
                userId,
                activityDate,
                Instant.now(),
                2000,
                45,
                350
        );

        // [When] sync() 수행 시간(커넥션 점유 시간) 측정
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        syncService.sync(sampleActivity);

        stopWatch.stop();
        long totalExecutionTimeMs = stopWatch.getTotalTimeMillis();

        // [Then] 결과 출력 및 정량적 측정 수치 확인
        System.out.println("=========================================");
        System.out.println("[DB Connection Hold Time 측정 결과]");
        System.out.println("주입된 외부 API Network Latency: " + simulatedApiDelayMs + " ms");
        System.out.println("총 sync() 실행 및 커넥션 점유 시간: " + totalExecutionTimeMs + " ms");
        System.out.println("순수 DB 작업 및 로직 소요 시간(추정): " + (totalExecutionTimeMs - simulatedApiDelayMs) + " ms");
        System.out.println("=========================================");

        // 검증: 정상 저장 완료 확인 및 소요 시간이 API 지연 시간(200ms) 이상인지 검증
        assertThat(summaryJpaRepository.count()).isEqualTo(1);
        assertThat(totalExecutionTimeMs).isGreaterThanOrEqualTo(simulatedApiDelayMs);
    }
}
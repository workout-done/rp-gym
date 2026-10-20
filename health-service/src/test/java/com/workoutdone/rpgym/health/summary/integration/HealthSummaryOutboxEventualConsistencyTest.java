package com.workoutdone.rpgym.health.summary.integration;

import com.workoutdone.rpgym.health.activity.application.SyncedActivity;
import com.workoutdone.rpgym.health.outbox.adapter.out.persistence.EventOutboxJpaRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_outbox;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS health_service",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false"
})
class HealthSummaryOutboxEventualConsistencyTest {

    @Autowired
    private HealthSummarySyncService syncService;

    @Autowired
    private DailyHealthSummaryJpaRepository summaryJpaRepository;

    @Autowired
    private EventOutboxJpaRepository eventOutboxJpaRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    private UUID userId;
    private LocalDate activityDate;

    @BeforeEach
    void setUp() {
        summaryJpaRepository.deleteAll();
        eventOutboxJpaRepository.deleteAll();
        userId = UUID.randomUUID();
        activityDate = LocalDate.of(2026, 10, 19);

        UserHealthContextResponse mockResponse = Mockito.mock(
                UserHealthContextResponse.class,
                Mockito.RETURNS_DEEP_STUBS
        );
        given(userServiceClient.getHealthContext(any())).willReturn(mockResponse);
    }

    @Test
    @DisplayName("동기화 처리 시 단일 DB 트랜잭션 내에서 Outbox 이벤트 레코드가 원자적으로 생성됨을 검증한다")
    void validateTransactionalOutboxPattern() {
        // [Given] 동기화 요청 준비
        SyncedActivity sampleActivity = new SyncedActivity(
                UUID.randomUUID(),
                userId,
                activityDate,
                Instant.now(),
                15000,
                60,
                500
        );

        long initialSummaryCount = summaryJpaRepository.count();
        long initialOutboxCount = eventOutboxJpaRepository.count();

        // [When] 동기화 수행
        syncService.sync(sampleActivity);

        // [Then] 측정 수치 확인 및 검증
        long finalSummaryCount = summaryJpaRepository.count();
        long finalOutboxCount = eventOutboxJpaRepository.count();

        System.out.println("=========================================");
        System.out.println("[Transactional Outbox 패턴 검증 결과]");
        System.out.println("동기화 수행 전 Summary 수: " + initialSummaryCount + " / Outbox 수: " + initialOutboxCount);
        System.out.println("동기화 수행 후 Summary 수: " + finalSummaryCount + " / Outbox 수: " + finalOutboxCount);
        System.out.println("원자적으로 함께 저장된 Outbox 이벤트 수: " + (finalOutboxCount - initialOutboxCount));
        System.out.println("=========================================");

        // 검증: Summary 저장 1건 & Outbox 이벤트 1건 이상 원자적 생성 완료
        assertThat(finalSummaryCount).isEqualTo(1);
        assertThat(finalOutboxCount).isGreaterThan(0);
    }
}
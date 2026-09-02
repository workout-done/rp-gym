package com.workoutdone.rpgym.game.domain.vo;

/**
 * {Quest.applySnapshot()}의 판정 결과.
 *
 * <p>void로 두고 상태만 바꾸면 "완료됐는가"를 호출부가 다시 물어야 하고,
 * XP 지급 · QuestCompleted 발행의 근거가 흐려진다. 결과를 타입으로 돌려주면
 * 애플리케이션 서비스가 분기 하나로 끝난다.
 *
 *
 * switch (quest.applySnapshot(snapshot)) {
 *     case ApplyResult.Completed c -> { XP 지급 + Outbox 적재 }
 *     case ApplyResult.Progressed p -> { 진행도만 갱신 — 발행 없음 }
 *     case ApplyResult.Ignored i -> { 아무것도 안 함 + DEBUG 로그 }
 * }
 *
 *
 * sealed이므로 케이스를 빠뜨리면 컴파일이 실패한다. 판정 결과가 늘어날 때
 * 처리를 누락할 수 없게 만드는 것이 이 타입의 목적이다.
 */
public sealed interface ApplyResult {

    // 스냅샷을 반영하지 않았다. 상태 변화 없음
    record Ignored(Reason reason) implements ApplyResult {}

    // 진행도는 올랐지만 목표에 못 미쳤다. 이벤트 발행 없음
    record Progressed(int achievedDelta) implements ApplyResult {}

    // 목표를 채웠다. 이 결과에서만 XP가 지급되고 QuestCompleted가 발행
    record Completed(int achievedDelta) implements ApplyResult {}

    enum Reason {
        // 이미 COMPLETED이거나 EXPIRED. 완료된 Quest에 이벤트가 더 와도 보상은 한 번뿐
        NOT_ACTIVE,
        // measuredAt이 마지막 반영 시각 이하 — 중복 도착 또는 순서 역전
        STALE_SNAPSHOT,
        // measuredAt이 만료 시각을 넘었다. 만료 후 활동은 인정하지 않음
        AFTER_EXPIRY,
        // 누적값이 baseline보다 작다 — 자정 리셋 또는 Health 측 데이터 정정. 롤백하지 않고 무시
        NEGATIVE_DELTA
    }
}

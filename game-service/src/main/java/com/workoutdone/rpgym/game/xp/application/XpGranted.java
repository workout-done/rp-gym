package com.workoutdone.rpgym.game.xp.application;

import java.util.UUID;

/**
 * XP가 지급됐다는 사실만 알리는 이벤트.
 *
 * 이 record가 xp/application에 있는 것이 의존 방향을 정한다 --
 * 발행하는 쪽이 타입을 소유하고 구독하는 쪽이 import한다. xp는 누가 듣는지 모른다.
 * 구독자가 늘어나도 이 패키지의 코드는 바뀌지 않는다.
 *
 * userId 하나만 담는다. 구독자는 XpQueryService.totalXp()로 절대값을 다시 읽어
 * 통째로 덮어쓰면 되고, 그래야 멱등하다. amount를 실으면 "받은 델타를 더하는" 구현을
 * 유도하는데 그건 이벤트 하나가 유실되면 영구히 어긋난다.
 */
public record XpGranted(UUID userId) {
}

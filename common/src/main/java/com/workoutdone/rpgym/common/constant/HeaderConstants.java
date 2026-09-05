package com.workoutdone.rpgym.common.constant;

public final class HeaderConstants {

    // Gateway에서 인증된 사용자 정보를 전달하는 Header
    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLE = "X-User-Role";

    // 상수 클래스의 객체 생성을 막기 위해 추가
    private HeaderConstants() {
    }
}
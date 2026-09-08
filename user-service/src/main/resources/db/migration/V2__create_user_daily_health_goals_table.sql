-- TO-DO: feature/50-register-body-profile 브랜치(V2__create_user_health_profiles_table.sql)가
-- 이 브랜치보다 먼저 develop에 머지되면 버전 번호가 겹치므로, 이 파일명을
-- V3__create_user_daily_health_goals_table.sql로 바꿔야 한다.
CREATE TABLE user_service.user_daily_health_goals
(
    id                    UUID    PRIMARY KEY,
    user_id               UUID    NOT NULL,
    step_goal             INTEGER NOT NULL DEFAULT 3000,
    active_minutes_goal   INTEGER NOT NULL DEFAULT 30,
    active_calories_goal  INTEGER NOT NULL DEFAULT 300,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at            TIMESTAMP NULL
);

CREATE UNIQUE INDEX ux_user_daily_health_goals_user_id
    ON user_service.user_daily_health_goals (user_id)
    WHERE deleted_at IS NULL;

CREATE TABLE user_service.user_daily_health_goals
(
    id                    UUID    PRIMARY KEY,
    user_id               UUID    NOT NULL,
    step_goal             INTEGER NOT NULL DEFAULT 5000,
    active_minutes_goal   INTEGER NOT NULL DEFAULT 60,
    active_calories_goal  INTEGER NOT NULL DEFAULT 300,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at            TIMESTAMP NULL
);

CREATE UNIQUE INDEX ux_user_daily_health_goals_user_id
    ON user_service.user_daily_health_goals (user_id)
    WHERE deleted_at IS NULL;

-- ============================================================
-- daily_health_summaries: 사용자 일일 건강 요약
-- ============================================================
CREATE TABLE health_service.daily_health_summaries (
    summary_id             UUID PRIMARY KEY,
    user_id                UUID NOT NULL,
    activity_date          DATE NOT NULL,
    total_steps            INTEGER NOT NULL DEFAULT 0,
    total_active_minutes   INTEGER NOT NULL DEFAULT 0,
    total_active_calories  INTEGER NOT NULL DEFAULT 0,
    is_all_goals_achieved  BOOLEAN NOT NULL DEFAULT FALSE,
    achieved_at            TIMESTAMP,
    last_synced_at         TIMESTAMPTZ NOT NULL,
    calculated_at          TIMESTAMP NOT NULL,
    version                BIGINT NOT NULL DEFAULT 0,
    created_at             TIMESTAMP NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT ux_daily_health_summaries_user_date UNIQUE (user_id, activity_date)
);

-- ============================================================
-- daily_goal_progress: 지표별(걸음수/활동시간/칼로리) 목표 진행상황
-- ============================================================
CREATE TABLE health_service.daily_goal_progress (
    progress_id      UUID PRIMARY KEY,
    summary_id       UUID NOT NULL REFERENCES health_service.daily_health_summaries(summary_id),
    user_id          UUID NOT NULL,
    activity_date    DATE NOT NULL,
    metric_type      VARCHAR(30) NOT NULL,
    target_value     NUMERIC(10,2) NOT NULL,
    achieved_value   NUMERIC(10,2) NOT NULL DEFAULT 0,
    shortage_value   NUMERIC(10,2) NOT NULL DEFAULT 0,
    is_achieved      BOOLEAN NOT NULL DEFAULT FALSE,
    unit             VARCHAR(10) NOT NULL,
    version          BIGINT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT ux_daily_goal_progress_summary_metric UNIQUE (summary_id, metric_type)
);

CREATE INDEX ix_daily_goal_progress_user_date_metric
    ON health_service.daily_goal_progress (user_id, activity_date, metric_type);
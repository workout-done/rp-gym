CREATE SCHEMA IF NOT EXISTS health_service;

CREATE TABLE health_service.health_activities (
                                                  activity_id       UUID         NOT NULL,
                                                  user_id           UUID         NOT NULL,
                                                  activity_date     DATE         NOT NULL,
                                                  measured_at       TIMESTAMPTZ  NOT NULL,
                                                  steps             INT          NOT NULL DEFAULT 0,
                                                  active_minutes    INT          NOT NULL DEFAULT 0,
                                                  active_calories   INT          NOT NULL DEFAULT 0,
                                                  source            VARCHAR(20)  NOT NULL,
                                                  created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  CONSTRAINT pk_health_activities PRIMARY KEY (activity_id)
);

CREATE UNIQUE INDEX uk_health_activities_user_measured
    ON health_service.health_activities (user_id, measured_at);

CREATE INDEX idx_health_activities_user_date
    ON health_service.health_activities (user_id, activity_date, measured_at DESC);


CREATE TABLE health_service.event_outbox (
                                             outbox_id           UUID         NOT NULL,
                                             event_id            UUID         NOT NULL,
                                             event_type          VARCHAR(50)  NOT NULL,
                                             source_activity_id  UUID         NOT NULL,
                                             dedup_key           VARCHAR(150) NOT NULL,
                                             partition_key       VARCHAR(50)  NOT NULL,
                                             payload             JSONB        NOT NULL,
                                             status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                                             retry_count         INT          NOT NULL DEFAULT 0,
                                             created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             published_at        TIMESTAMP,
                                             CONSTRAINT pk_event_outbox PRIMARY KEY (outbox_id)
);

CREATE UNIQUE INDEX uk_health_activity_outbox_dedup
    ON health_service.event_outbox (dedup_key);

CREATE UNIQUE INDEX uk_health_activity_outbox_event_id
    ON health_service.event_outbox (event_id);

CREATE INDEX idx_health_activity_outbox_status
    ON health_service.event_outbox (status, created_at);
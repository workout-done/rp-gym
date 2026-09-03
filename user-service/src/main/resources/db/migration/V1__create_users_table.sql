CREATE SCHEMA IF NOT EXISTS user_service;

CREATE TABLE user_service.users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(50)  NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    slack_id   VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ  NULL
);

CREATE UNIQUE INDEX ux_users_email_active
    ON user_service.users (email)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_users_nickname_active
    ON user_service.users (nickname)
    WHERE deleted_at IS NULL;

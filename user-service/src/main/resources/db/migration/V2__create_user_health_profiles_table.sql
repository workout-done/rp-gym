CREATE TABLE user_service.user_health_profiles
(
    id         UUID PRIMARY KEY,
    user_id    UUID          NOT NULL,
    height     NUMERIC(5, 2) NOT NULL,
    weight     NUMERIC(5, 2) NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP     NULL
);

CREATE UNIQUE INDEX ux_user_health_profiles_user_id
    ON user_service.user_health_profiles (user_id)
    WHERE deleted_at IS NULL;

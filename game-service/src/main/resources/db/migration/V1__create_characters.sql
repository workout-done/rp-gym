CREATE TABLE characters (
id UUID PRIMARY KEY,
user_id UUID NOT NULL,
level INTEGER NOT NULL DEFAULT 1,
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uk_characters_user_id ON characters (user_id);
CREATE INDEX idx_characters_level ON characters (level DESC);

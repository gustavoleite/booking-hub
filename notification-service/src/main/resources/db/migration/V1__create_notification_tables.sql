CREATE TABLE booking_snapshots (
    booking_id      UUID         PRIMARY KEY,
    client_id       VARCHAR(255) NOT NULL,
    professional_id UUID         NOT NULL,
    start_datetime  TIMESTAMPTZ  NOT NULL,
    end_datetime    TIMESTAMPTZ  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_snapshots_client       ON booking_snapshots(client_id);
CREATE INDEX idx_snapshots_professional ON booking_snapshots(professional_id);

CREATE TABLE calendar_feeds (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    VARCHAR(255) NOT NULL UNIQUE,
    feed_token VARCHAR(64)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

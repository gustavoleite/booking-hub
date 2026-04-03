CREATE TABLE tb_bookings (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id            VARCHAR(255) NOT NULL,
    professional_id      UUID         NOT NULL,
    establishment_id     UUID         NOT NULL,
    provided_service_id  UUID         NOT NULL,
    start_datetime       TIMESTAMP    NOT NULL,
    end_datetime         TIMESTAMP    NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    price                DECIMAL(19, 2) NOT NULL,
    duration_minutes     INT          NOT NULL,
    notes                TEXT,
    cancel_reason        TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    cancelled_at         TIMESTAMP
);

-- Anti-double-booking: unique active slot per professional
CREATE UNIQUE INDEX uk_booking_active_slot
    ON tb_bookings (professional_id, start_datetime)
    WHERE status NOT IN ('CANCELLED', 'NO_SHOW');

-- Performance indexes
CREATE INDEX idx_booking_professional_date ON tb_bookings (professional_id, start_datetime);
CREATE INDEX idx_booking_client            ON tb_bookings (client_id, start_datetime DESC);
CREATE INDEX idx_booking_establishment     ON tb_bookings (establishment_id, start_datetime);

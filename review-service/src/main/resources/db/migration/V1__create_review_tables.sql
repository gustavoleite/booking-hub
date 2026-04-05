-- Bookings eligible for review (consumed from booking.completed events)
CREATE TABLE tb_eligible_bookings (
    booking_id        UUID         PRIMARY KEY,
    client_id         VARCHAR(255) NOT NULL,
    professional_id   UUID         NOT NULL,
    establishment_id  UUID         NOT NULL,
    completed_at      TIMESTAMP    NOT NULL
);

-- Reviews submitted by clients
CREATE TABLE tb_reviews (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id           UUID         NOT NULL UNIQUE,
    client_id            VARCHAR(255) NOT NULL,
    professional_id      UUID         NOT NULL,
    establishment_id     UUID         NOT NULL,
    professional_rating  INTEGER      CHECK (professional_rating BETWEEN 1 AND 5),
    establishment_rating INTEGER      CHECK (establishment_rating BETWEEN 1 AND 5),
    comment              TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_at_least_one_rating
        CHECK (professional_rating IS NOT NULL OR establishment_rating IS NOT NULL)
);

CREATE UNIQUE INDEX uk_review_booking     ON tb_reviews (booking_id);
CREATE INDEX idx_review_professional      ON tb_reviews (professional_id, created_at DESC);
CREATE INDEX idx_review_establishment     ON tb_reviews (establishment_id, created_at DESC);

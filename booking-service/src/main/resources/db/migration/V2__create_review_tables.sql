CREATE TABLE tb_eligible_bookings (
    booking_id       UUID         PRIMARY KEY,
    client_id        VARCHAR(255) NOT NULL,
    professional_id  UUID         NOT NULL,
    establishment_id UUID         NOT NULL,
    completed_at     TIMESTAMP    NOT NULL
);

CREATE TABLE tb_reviews (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id           UUID         NOT NULL UNIQUE REFERENCES tb_eligible_bookings(booking_id),
    client_id            VARCHAR(255) NOT NULL,
    professional_id      UUID         NOT NULL,
    establishment_id     UUID         NOT NULL,
    professional_rating  SMALLINT,
    establishment_rating SMALLINT,
    comment              TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_review_professional   ON tb_reviews (professional_id);
CREATE INDEX idx_review_establishment  ON tb_reviews (establishment_id);

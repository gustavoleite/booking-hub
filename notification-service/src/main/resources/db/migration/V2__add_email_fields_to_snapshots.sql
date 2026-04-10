ALTER TABLE booking_snapshots
    ADD COLUMN IF NOT EXISTS client_email       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS professional_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS reminder_sent      BOOLEAN NOT NULL DEFAULT FALSE;

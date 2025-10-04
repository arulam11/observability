CREATE TABLE IF NOT EXISTS outbox_events (
  id              BIGSERIAL PRIMARY KEY,
  aggregate_type  TEXT        NOT NULL,       -- e.g., 'ride'
  aggregate_id    TEXT        NOT NULL,       -- e.g., ride id
  type            TEXT        NOT NULL,       -- e.g., 'ride.created'
  payload         JSONB       NOT NULL,       -- event data
  status          TEXT        NOT NULL DEFAULT 'PENDING', -- PENDING|PUBLISHED|FAILED
  attempts        INTEGER     NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  available_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at    TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_available
  ON outbox_events (status, available_at);
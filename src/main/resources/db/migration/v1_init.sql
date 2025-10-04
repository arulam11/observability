CREATE TABLE IF NOT EXISTS rides (
  id BIGSERIAL PRIMARY KEY,
  origin TEXT NOT NULL,
  destination TEXT NOT NULL,
  departure_time TIMESTAMPTZ NOT NULL,
  seats_available INT NOT NULL CHECK (seats_available >= 0),
  price_cents INT NOT NULL CHECK (price_cents >= 0),
  driver_name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rides_departure_time ON rides(departure_time);
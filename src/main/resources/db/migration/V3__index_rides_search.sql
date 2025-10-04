-- If you’re on Postgres and expect large tables in prod,
-- you might consider CONCURRENTLY, but that requires running outside a tx.
-- For dev-sized tables, a simple index is fine:
CREATE INDEX IF NOT EXISTS idx_rides_od_time
  ON rides (origin, destination, departure_time);

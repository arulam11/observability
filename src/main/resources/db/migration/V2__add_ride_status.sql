-- Add a new required column with a safe default
ALTER TABLE rides
  ADD COLUMN status TEXT;

-- Backfill existing rows (so nothing is NULL)
UPDATE rides SET status = 'OPEN' WHERE status IS NULL;

-- Enforce NOT NULL going forward
ALTER TABLE rides
  ALTER COLUMN status SET NOT NULL;

-- Optional: set a default for new rows
ALTER TABLE rides
  ALTER COLUMN status SET DEFAULT 'OPEN';

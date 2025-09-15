CREATE TABLE IF NOT EXISTS orders (
  id           BIGSERIAL PRIMARY KEY,
  customer_id  BIGINT      NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL,
  total_amount NUMERIC(12,2) NOT NULL,
  note         TEXT
);

-- 検索条件: customer_id + created_at 範囲 + 並び替えに効く複合Index
CREATE INDEX IF NOT EXISTS idx_orders_customer_created
  ON orders (customer_id, created_at DESC);

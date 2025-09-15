-- データ投入（例）
INSERT INTO orders (customer_id, created_at, total_amount, note)
SELECT (1 + floor(random() * 5000))::bigint,
       NOW() - (floor(random() * 365) || ' days')::interval,
       round((random() * 1000)::numeric, 2),
       'note-' || gs
FROM generate_series(1, 200000) AS gs;

-- 統計を即時作成（重要）
ANALYZE orders;


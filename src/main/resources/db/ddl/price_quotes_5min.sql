CREATE TABLE IF NOT EXISTS public.price_quotes_5min (
    id          BIGSERIAL PRIMARY KEY,
    symbol      VARCHAR(255)     NOT NULL,
    timestamp   TIMESTAMPTZ      NOT NULL,
    open        NUMERIC(12, 4),
    high        NUMERIC(12, 4),
    low         NUMERIC(12, 4),
    close_price NUMERIC(12, 4),
    volume      BIGINT,
    created_at  TIMESTAMPTZ      NOT NULL,
    CONSTRAINT uq_price_quotes_5min_symbol_ts UNIQUE (symbol, timestamp)
);

CREATE INDEX IF NOT EXISTS idx_price_quotes_5min_symbol_ts
    ON public.price_quotes_5min (symbol, timestamp DESC);
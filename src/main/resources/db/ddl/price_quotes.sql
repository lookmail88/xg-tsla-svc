CREATE TABLE IF NOT EXISTS public.price_quotes (
    id          BIGSERIAL PRIMARY KEY,
    symbol      VARCHAR(255)     NOT NULL,
    timestamp   TIMESTAMPTZ      NOT NULL,
    open        NUMERIC(12, 4),
    high        NUMERIC(12, 4),
    low         NUMERIC(12, 4),
    close_price NUMERIC(12, 4),
    volume      BIGINT,
    vwap        NUMERIC(12, 4)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_price_quotes_symbol_ts
    ON public.price_quotes (symbol, timestamp);

CREATE INDEX IF NOT EXISTS idx_price_quotes_symbol_ts
    ON public.price_quotes (symbol, timestamp DESC);
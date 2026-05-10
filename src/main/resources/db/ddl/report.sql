-- 开启扩展（如果需要进行全文检索优化）
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1. 创建分析报告主表
CREATE TABLE stock_analysis_reports (
                                        report_id SERIAL PRIMARY KEY,
                                        symbol VARCHAR(20) NOT NULL,                -- 标的代码 (如 'TSLA')
                                        report_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- 报告生成时间

    -- 核心结论 (结构化数据)
                                        trend_sentiment VARCHAR(20) CHECK (trend_sentiment IN ('Bullish', 'Bear落地', 'Neutral', 'Bearish')),
                                        summary_conclusion TEXT NOT NULL,           -- 报告摘要/一句话总结

    -- 关键技术位 (用于后续量化回测)
                                        support_level_primary NUMERIC(12, 4),      -- 第一支撑位
                                        support_level_secondary NUMERIC(12, 4),    -- 第二支撑位
                                        resistance_level_primary NUMERIC(12, 4),  -- 第一压力位
                                        resistance_level_secondary NUMERIC(12, 4),-- 第二压力位

    -- 深度分析内容 (非结构化数据)
                                        detailed_analysis TEXT,                     -- 完整的深度分析报告全文
                                        volume_observation TEXT,                   -- 关于成交量的观察结论
                                        price_action_observation TEXT,             -- 关于价格走势的观察结论

    -- 风险评估
                                        risk_level VARCHAR(10) CHECK (risk_level IN ('Low', 'Medium', 'High', 'Extreme')),

    -- 元数据
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 为 symbol 和 summary 建立索引，方便快速检索
CREATE INDEX idx_reports_symbol ON stock_analysis_reports(symbol);
CREATE INDEX idx_reports_sentiment ON stock_analysis_reports(trend_sentiment);
CREATE INDEX idx_reports_summary_trgm ON stock_analysis_reports USING gin (summary_conclusion gin_trgm_ops);


-- 2. 创建指标快照表 (用于存储报告产生时的瞬时技术指标)
CREATE TABLE analysis_metrics_snapshots (
                                            snapshot_id SERIAL PRIMARY KEY,
                                            report_id INTEGER REFERENCES stock_analysis_reports(report_id) ON DELETE CASCADE,

    -- 指标数值 (用于量化计算)
                                            rsi_value NUMERIC(5, 2),                    -- RSI 指标值
                                            macd_line NUMERIC(12, 4),                   -- MACD 快线
                                            macd_signal_line NUMERIC(12, 4),            -- MACD 慢线
                                            macd_histogram NUMERIC(12, 4),             -- MACD 柱状图
                                            volume_change_pct NUMERIC(10, 4),          -- 成交量变化率 (对比前期)
                                            price_volatility_stddev NUMERIC(12, 	4),  -- 价格波动率 (标准差)

                                            captured_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. 创建视图：方便直接查看“标的 + 结论 + 关键位”的综合视图
CREATE VIEW view_trading_summary AS
SELECT
    r.symbol,
    r.report_timestamp,
    r.trend_sentiment,
    r.summary_conclusion,
    r.support_level_primary,
    r.resistance_level_primary,
    s.rsi_value,
    s.volume_change_pct
FROM stock_analysis_reports r
         LEFT JOIN analysis_metrics_snapshots s ON r.report_id = s.report_id;
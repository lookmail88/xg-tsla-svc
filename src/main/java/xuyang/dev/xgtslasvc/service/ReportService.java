package xuyang.dev.xgtslasvc.service;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xuyang.dev.xgtslasvc.entity.AnalysisMetricsSnapshot;
import xuyang.dev.xgtslasvc.entity.PriceQuote;
import xuyang.dev.xgtslasvc.entity.PriceQuote5Min;
import xuyang.dev.xgtslasvc.entity.StockAnalysisReport;
import xuyang.dev.xgtslasvc.repository.AnalysisMetricsSnapshotRepository;
import xuyang.dev.xgtslasvc.repository.PriceQuote5MinRepository;
import xuyang.dev.xgtslasvc.repository.PriceQuoteRepository;
import xuyang.dev.xgtslasvc.repository.StockAnalysisReportRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final ZoneId LA = ZoneId.of("America/Los_Angeles");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(LA);
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(LA);
    // Lenient JSON parser — LLMs occasionally emit single quotes, unquoted keys,
    // trailing commas, or // comments. We accept all of them rather than fail the request.
    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .build();

    private final OllamaService ollamaService;
    private final PriceQuoteRepository priceQuoteRepository;
    private final PriceQuote5MinRepository priceQuote5MinRepository;
    private final StockAnalysisReportRepository reportRepository;
    private final AnalysisMetricsSnapshotRepository metricsRepository;

    public ReportService(OllamaService ollamaService,
                         PriceQuoteRepository priceQuoteRepository,
                         PriceQuote5MinRepository priceQuote5MinRepository,
                         StockAnalysisReportRepository reportRepository,
                         AnalysisMetricsSnapshotRepository metricsRepository) {
        this.ollamaService = ollamaService;
        this.priceQuoteRepository = priceQuoteRepository;
        this.priceQuote5MinRepository = priceQuote5MinRepository;
        this.reportRepository = reportRepository;
        this.metricsRepository = metricsRepository;
    }

    public Map<String, Object> getLatestSavedReport(String ticker) {
        return reportRepository.findFirstBySymbolOrderByReportTimestampDesc(ticker.toUpperCase())
                .map(this::toMap)
                .orElse(Map.of("status", "not_found", "ticker", ticker));
    }

    public Map<String, Object> generateNewReport(String ticker) {
        try {
            List<PriceQuote> dailyPrices = priceQuoteRepository
                    .findBySymbolOrderByTimestampDesc(ticker)
                    .stream().limit(20).toList();

            if (dailyPrices.isEmpty()) {
                log.warn("No price data found for {}", ticker);
                return Map.of("status", "no_price_data", "ticker", ticker);
            }

            Instant tenDaysAgo = Instant.now().minus(1, ChronoUnit.DAYS);
            List<PriceQuote5Min> prices5Min = priceQuote5MinRepository
                    .findBySymbolAndTimestampBetweenOrderByTimestampAsc(ticker, tenDaysAgo, Instant.now());

            String prompt = buildPrompt(ticker, dailyPrices, prices5Min);
            String raw = ollamaService.generateAnalysis(prompt);

            if (raw == null || raw.isBlank()) {
                log.warn("Ollama returned no content for {}", ticker);
                return Map.of("status", "ollama_no_response", "ticker", ticker);
            }

            JsonNode json = parseJson(raw);
            StockAnalysisReport report = buildReport(ticker, json);
            StockAnalysisReport saved = reportRepository.save(report);
            log.info("Saved report id={} for {} sentiment={}", saved.getReportId(), ticker, saved.getTrendSentiment());

            saveMetrics(saved, json);

            return toMap(saved);

        } catch (Exception e) {
            log.error("Failed to generate report for {}", ticker, e);
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    private String buildPrompt(String ticker, List<PriceQuote> dailyPrices, List<PriceQuote5Min> prices5Min) {
        StringBuilder sb = new StringBuilder();

        // dailyPrices arrives newest-first from the repository; present chronologically
        // (oldest-first) so the model reads price action left-to-right like a chart.
        List<PriceQuote> dailyAsc = new ArrayList<>(dailyPrices);
        Collections.reverse(dailyAsc);

        PriceQuote latest = dailyPrices.get(0);
        PriceQuote prior = dailyPrices.size() > 1 ? dailyPrices.get(1) : null;

//        sb.append("=== Analysis Context ===%n".formatted());
//        sb.append("Ticker:           %s%n".formatted(ticker));
//        sb.append("As-of date:       %s (America/Los_Angeles)%n".formatted(DATE_FMT.format(latest.getTimestamp())));
//        sb.append("Latest close:     %s%n".formatted(latest.getClose()));
//        sb.append("Latest intraday:  low %s / high %s%n".formatted(latest.getLow(), latest.getHigh()));
//        sb.append("Latest volume:    %d%n".formatted(latest.getVolume()));
        if (prior != null && prior.getClose() != null
                && latest.getClose() != null && prior.getClose().signum() != 0) {
            BigDecimal pct = latest.getClose().subtract(prior.getClose())
                    .divide(prior.getClose(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            sb.append("Day %% change:     %s%%%n".formatted(pct.toPlainString()));
        }
        sb.append("Daily bars:       %d sessions provided%n".formatted(dailyAsc.size()));
        sb.append("Intraday bars:    %d five-minute bars over the last 5 trading days%n".formatted(prices5Min.size()));
        sb.append("%n");

        sb.append("=== Daily OHLCV — last %d sessions (oldest first) ===%n".formatted(dailyAsc.size()));
        sb.append("Date         | Open     | High     | Low      | Close    | Volume      | VWAP%n");
        for (PriceQuote q : dailyAsc) {
            sb.append("%-12s | %-8s | %-8s | %-8s | %-8s | %-11d | %s%n".formatted(
                    DATE_FMT.format(q.getTimestamp()),
                    q.getOpen(), q.getHigh(), q.getLow(), q.getClose(),
                    q.getVolume(), q.getVwap()));
        }

        sb.append("%n=== Intraday 5-minute OHLCV — last 5 trading days (oldest first) ===%n".formatted());
        if (prices5Min.isEmpty()) {
            sb.append("(no 5-minute data available — base intraday observations on the daily bars)%n".formatted());
        } else {
            sb.append("Datetime          | Open     | High     | Low      | Close    | Volume%n");
            for (PriceQuote5Min q : prices5Min) {
                sb.append("%-17s | %-8s | %-8s | %-8s | %-8s | %d%n".formatted(
                        DATETIME_FMT.format(q.getTimestamp()),
                        q.getOpen(), q.getHigh(), q.getLow(), q.getClose(),
                        q.getVolume()));
            }
        }

//        sb.append("%n=== Task ===%n".formatted());
//        sb.append("Produce a single valid JSON object that exactly matches the schema in the system prompt.%n".formatted());
//        sb.append("Use the daily bars for trend, moving-average alignment, momentum (RSI/MACD), and volume regime;%n".formatted());
//        sb.append("use the 5-minute bars for intraday confirmation, short-term volatility, and the latest price action.%n".formatted());
//        sb.append("Anchor support_level_primary/secondary and resistance_level_primary/secondary to actual swing lows and highs visible in the data above.%n".formatted());
//        sb.append("All bilingual fields must follow the format \"<Chinese> | <English>\" defined in the system prompt.%n".formatted());
//        sb.append("Return JSON only — no markdown fences, no commentary, no trailing text.%n".formatted());

        return sb.toString();
    }

    /**
     * Parses the model's raw response into JSON. Robust to common LLM output quirks:
     * Markdown code fences, narrative preamble/postamble, and the lenient features
     * enabled on OBJECT_MAPPER (single quotes, unquoted keys, trailing commas, comments).
     */
    JsonNode parseJson(String raw) throws Exception {
        if (raw == null) throw new IllegalArgumentException("raw response is null");
        String cleaned = stripFences(raw.strip());
        String jsonObject = extractFirstJsonObject(cleaned);
        if (jsonObject == null) {
            log.error("Could not locate a JSON object in the model response. Raw (truncated): {}",
                    raw.substring(0, Math.min(raw.length(), 2000)));
            throw new IllegalStateException("Model response did not contain a JSON object");
        }
        try {
            return OBJECT_MAPPER.readTree(jsonObject);
        } catch (Exception e) {
            log.error("Failed to parse extracted JSON. Extracted (truncated): {} | Raw (truncated): {}",
                    jsonObject.substring(0, Math.min(jsonObject.length(), 2000)),
                    raw.substring(0, Math.min(raw.length(), 2000)));
            throw e;
        }
    }

    /** Strip all ```...``` code-fence markers (including the language hint), wherever they appear. */
    private static String stripFences(String s) {
        String out = s;
        // Leading fence with optional language hint, e.g. ```json
        out = out.replaceFirst("^```(?:json|JSON)?\\s*\\R?", "");
        // Trailing fence
        out = out.replaceFirst("\\R?```\\s*$", "");
        return out.strip();
    }

    /**
     * Returns the substring starting at the first '{' and ending at the matching '}',
     * respecting string literals and escape sequences. Returns null if no balanced object exists.
     */
    static String extractFirstJsonObject(String s) {
        int start = s.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) { escape = false; continue; }
            if (c == '\\' && inString) { escape = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return s.substring(start, i + 1);
            }
        }
        return null;
    }

    private StockAnalysisReport buildReport(String ticker, JsonNode j) {
        StockAnalysisReport r = new StockAnalysisReport();
        r.setSymbol(ticker);
        r.setTrendSentiment(text(j, "trend_sentiment"));
        r.setSummaryConclusion(text(j, "summary_conclusion"));
        r.setSupportLevelPrimary(decimal(j, "support_level_primary"));
        r.setSupportLevelSecondary(decimal(j, "support_level_secondary"));
        r.setResistanceLevelPrimary(decimal(j, "resistance_level_primary"));
        r.setResistanceLevelSecondary(decimal(j, "resistance_level_secondary"));
        r.setDetailedAnalysis(text(j, "detailed_analysis"));
        r.setVolumeObservation(text(j, "volume_observation"));
        r.setPriceActionObservation(text(j, "price_action_observation"));
        r.setRiskLevel(text(j, "risk_level"));
        return r;
    }

    private void saveMetrics(StockAnalysisReport saved, JsonNode j) {
        if (!j.hasNonNull("rsi_value")) return;
        AnalysisMetricsSnapshot m = new AnalysisMetricsSnapshot();
        m.setReport(saved);
        m.setRsiValue(decimal(j, "rsi_value"));
        m.setMacdLine(decimal(j, "macd_line"));
        m.setMacdSignalLine(decimal(j, "macd_signal_line"));
        m.setMacdHistogram(decimal(j, "macd_histogram"));
        m.setVolumeChangePct(decimal(j, "volume_change_pct"));
        m.setPriceVolatilityStddev(decimal(j, "price_volatility_stddev"));
        metricsRepository.save(m);
    }

    private Map<String, Object> toMap(StockAnalysisReport r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("reportId", r.getReportId());
        m.put("symbol", r.getSymbol());
        m.put("reportTimestamp", r.getReportTimestamp());
        m.put("trendSentiment", r.getTrendSentiment());
        m.put("summaryConclusion", r.getSummaryConclusion());
        m.put("supportLevelPrimary", r.getSupportLevelPrimary());
        m.put("supportLevelSecondary", r.getSupportLevelSecondary());
        m.put("resistanceLevelPrimary", r.getResistanceLevelPrimary());
        m.put("resistanceLevelSecondary", r.getResistanceLevelSecondary());
        m.put("detailedAnalysis", r.getDetailedAnalysis());
        m.put("volumeObservation", r.getVolumeObservation());
        m.put("priceActionObservation", r.getPriceActionObservation());
        m.put("riskLevel", r.getRiskLevel());
        return m;
    }

    private String text(JsonNode j, String field) {
        JsonNode n = j.get(field);
        return (n != null && !n.isNull()) ? n.asText() : null;
    }

    private BigDecimal decimal(JsonNode j, String field) {
        JsonNode n = j.get(field);
        return (n != null && !n.isNull()) ? n.decimalValue() : null;
    }
}
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
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

    public Map<String, Object> getLatestReport(String ticker) {
        try {
            List<PriceQuote> dailyPrices = priceQuoteRepository
                    .findBySymbolOrderByTimestampDesc(ticker)
                    .stream().limit(20).toList();

            if (dailyPrices.isEmpty()) {
                log.warn("No price data found for {}", ticker);
                return Map.of("status", "no_price_data", "ticker", ticker);
            }

            Instant tenDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);
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

        sb.append("Recent %s daily prices (newest first):%n".formatted(ticker));
        sb.append("Date         | Open     | High     | Low      | Close    | Volume      | VWAP%n");
        for (PriceQuote q : dailyPrices) {
            sb.append("%-12s | %-8s | %-8s | %-8s | %-8s | %-11d | %s%n".formatted(
                    DATE_FMT.format(q.getTimestamp()),
                    q.getOpen(), q.getHigh(), q.getLow(), q.getClose(),
                    q.getVolume(), q.getVwap()));
        }

        sb.append("%nRecent %s 5-minute prices — last 10 days (oldest first):%n".formatted(ticker));
        sb.append("Datetime          | Open     | High     | Low      | Close    | Volume%n");
        for (PriceQuote5Min q : prices5Min) {
            sb.append("%-17s | %-8s | %-8s | %-8s | %-8s | %d%n".formatted(
                    DATETIME_FMT.format(q.getTimestamp()),
                    q.getOpen(), q.getHigh(), q.getLow(), q.getClose(),
                    q.getVolume()));
        }

        return sb.toString();
    }

    private JsonNode parseJson(String raw) throws Exception {
        // strip markdown code fences if present
        String cleaned = raw.strip();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("```(?:json)?\\s*", "").replaceFirst("```\\s*$", "").strip();
        }
        return OBJECT_MAPPER.readTree(cleaned);
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
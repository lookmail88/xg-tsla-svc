package xuyang.dev.xgtslasvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import xuyang.dev.xgtslasvc.dto.YahooChartResponse;
import xuyang.dev.xgtslasvc.entity.PriceQuote5Min;
import xuyang.dev.xgtslasvc.repository.PriceQuote5MinRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class YahooFinanceService {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceService.class);

    private final RestClient restClient;
    private final PriceQuote5MinRepository repository;

    public YahooFinanceService(
            @Value("${yahoo.finance.base-url:https://query1.finance.yahoo.com}") String baseUrl,
            PriceQuote5MinRepository repository) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .build();
        this.repository = repository;
    }

    public void fetchAndSave5MinBars(String symbol) {
        try {
            YahooChartResponse response = restClient.get()
                    .uri("/v8/finance/chart/{symbol}?interval=5m&range=1d", symbol)
                    .retrieve()
                    .body(YahooChartResponse.class);

            if (response == null || response.getChart() == null
                    || response.getChart().getResult() == null
                    || response.getChart().getResult().isEmpty()) {
                log.warn("Yahoo Finance returned no data for {}", symbol);
                return;
            }

            YahooChartResponse.Result result = response.getChart().getResult().getFirst();
            List<Long> timestamps = result.getTimestamp();

            if (timestamps == null || timestamps.isEmpty()) {
                log.warn("No timestamp data for {}", symbol);
                return;
            }

            YahooChartResponse.Quote quote = result.getIndicators().getQuote().getFirst();
            int saved = 0;

            for (int i = 0; i < timestamps.size(); i++) {
                Instant ts = Instant.ofEpochSecond(timestamps.get(i));

                if (repository.existsBySymbolAndTimestamp(symbol, ts)) {
                    continue;
                }

                Double close = safeGet(quote.getClose(), i);
                if (close == null) continue;

                PriceQuote5Min bar = new PriceQuote5Min(
                        symbol, ts,
                        toDecimal(safeGet(quote.getOpen(), i)),
                        toDecimal(safeGet(quote.getHigh(), i)),
                        toDecimal(safeGet(quote.getLow(), i)),
                        BigDecimal.valueOf(close),
                        safeGet(quote.getVolume(), i)
                );
                repository.save(bar);
                saved++;
            }

            log.info("Saved {} new 5-min bars for {}", saved, symbol);
        } catch (Exception e) {
            log.error("Failed to fetch 5-min bars for {}", symbol, e);
        }
    }

    private <T> T safeGet(List<T> list, int index) {
        if (list == null || index >= list.size()) return null;
        return list.get(index);
    }

    private BigDecimal toDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
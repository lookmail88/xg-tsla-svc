package xuyang.dev.xgtslasvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xuyang.dev.xgtslasvc.dto.PolygonPrevDayResponse;
import xuyang.dev.xgtslasvc.dto.PolygonPrevDayResponse.AggResult;
import xuyang.dev.xgtslasvc.entity.PriceQuote;
import xuyang.dev.xgtslasvc.repository.PriceQuoteRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceService.class);

    private final MassiveApiService massiveApiService;
    private final PriceQuoteRepository priceQuoteRepository;

    public PriceService(MassiveApiService massiveApiService,
                        PriceQuoteRepository priceQuoteRepository) {
        this.massiveApiService = massiveApiService;
        this.priceQuoteRepository = priceQuoteRepository;
    }

    public PriceQuote fetchAndSaveTslaPrice() {
        try {
            PolygonPrevDayResponse response = massiveApiService.fetchPreviousDayBar("TSLA");

            if (response == null || !"OK".equals(response.getStatus())
                    || response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("PrevDay API returned no results: {}",
                        response != null ? response.getStatus() : "null");
                return null;
            }

            AggResult bar = response.getResults().getFirst();

            Instant timestamp = bar.getTimestamp() > 0
                    ? Instant.ofEpochMilli(bar.getTimestamp())
                    : Instant.now();

            return savePriceQuote("TSLA", timestamp,
                    bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(),
                    bar.getVolume(), bar.getVwap());
        } catch (Exception e) {
            log.error("Failed to fetch TSLA price", e);
            return null;
        }
    }

    private PriceQuote savePriceQuote(String symbol, Instant timestamp,
                                       double open, double high, double low, double close,
                                       long volume, double vwap) {
        if (priceQuoteRepository.existsBySymbolAndTimestamp(symbol, timestamp)) {
            log.debug("Skipping duplicate {} price at {}", symbol, timestamp);
            return null;
        }

        PriceQuote quote = new PriceQuote(
                symbol,
                timestamp,
                BigDecimal.valueOf(open),
                BigDecimal.valueOf(high),
                BigDecimal.valueOf(low),
                BigDecimal.valueOf(close),
                volume,
                BigDecimal.valueOf(vwap)
        );

        PriceQuote saved = priceQuoteRepository.save(quote);
        log.info("Saved {} price: close={} volume={} at {}",
                symbol, saved.getClose(), saved.getVolume(), saved.getTimestamp());
        return saved;
    }
}

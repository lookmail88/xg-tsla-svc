package xuyang.dev.xgtslasvc.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xuyang.dev.xgtslasvc.entity.PriceQuote;
import xuyang.dev.xgtslasvc.repository.PriceQuoteRepository;
import xuyang.dev.xgtslasvc.service.PriceService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/price")
public class PriceController {

    private final PriceQuoteRepository priceQuoteRepository;
    private final PriceService priceService;

    public PriceController(PriceQuoteRepository priceQuoteRepository, PriceService priceService) {
        this.priceQuoteRepository = priceQuoteRepository;
        this.priceService = priceService;
    }

    @GetMapping("/latest")
    public ResponseEntity<List<PriceQuote>> getLatestPrices(
            @RequestParam(defaultValue = "60") int limit) {
        List<PriceQuote> prices = priceQuoteRepository
                .findBySymbolOrderByTimestampDesc("TSLA");
        if (prices.size() > limit) {
            prices = prices.subList(0, limit);
        }
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/today")
    public ResponseEntity<List<PriceQuote>> getTodayPrices() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        List<PriceQuote> prices = priceQuoteRepository
                .findBySymbolAndTimestampBetweenOrderByTimestampAsc("TSLA", since, Instant.now());
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/fetch")
    public ResponseEntity<PriceQuote> fetchNow() {
        PriceQuote quote = priceService.fetchAndSaveTslaPrice();
        if (quote != null) {
            return ResponseEntity.ok(quote);
        }
        return ResponseEntity.internalServerError().build();
    }
}

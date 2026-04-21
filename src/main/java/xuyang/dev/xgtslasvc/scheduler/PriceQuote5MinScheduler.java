package xuyang.dev.xgtslasvc.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xuyang.dev.xgtslasvc.service.YahooFinanceService;

@Component
public class PriceQuote5MinScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceQuote5MinScheduler.class);

    private final YahooFinanceService yahooFinanceService;

    public PriceQuote5MinScheduler(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    @Scheduled(cron = "${yahoo.finance.price-5min-cron:0 */5 * * * MON-FRI}")
    public void fetchTsla5MinBars() {
        log.info("Fetching TSLA 5-min bars from Yahoo Finance");
        yahooFinanceService.fetchAndSave5MinBars("TSLA");
    }
}
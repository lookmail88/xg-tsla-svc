package xuyang.dev.xgtslasvc.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xuyang.dev.xgtslasvc.service.PriceService;

@Component
public class PriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceScheduler.class);

    private final PriceService priceService;

    public PriceScheduler(PriceService priceService) {
        this.priceService = priceService;
    }

    /**
     * Fetch TSLA price snapshot every minute.
     * Cron: second=0, every minute, every hour, every day
     * Configurable via massive.api.price-cron property.
     */
    @Scheduled(cron = "${massive.api.price-cron:0 15 * * * *}")
    public void fetchTslaPrice() {
        log.info("Fetching TSLA price snapshot");
        try {
            priceService.fetchAndSaveTslaPrice();
        } catch (Exception e) {
            log.error("Failed to fetch TSLA price", e);
        }
    }
}

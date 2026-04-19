package xuyang.dev.xgtslasvc.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xuyang.dev.xgtslasvc.service.MassiveApiService;

@Component
public class DividendScheduler {

    private static final Logger log = LoggerFactory.getLogger(DividendScheduler.class);

    private final MassiveApiService massiveApiService;

    public DividendScheduler(MassiveApiService massiveApiService) {
        this.massiveApiService = massiveApiService;
    }

    @Scheduled(cron = "${massive.api.cron:0 0 0 * * *}")
    public void fetchDividends() {
        log.info("Fetching dividends from Massive API");
        try {
            String response = massiveApiService.fetchDividends();
            log.info("Dividends response: {}", response);
        } catch (Exception e) {
            log.error("Failed to fetch dividends from Massive API", e);
        }
    }
}

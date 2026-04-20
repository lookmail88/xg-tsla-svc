package xuyang.dev.xgtslasvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import xuyang.dev.xgtslasvc.dto.PolygonPrevDayResponse;

@Service
public class MassiveApiService {

    private static final Logger log = LoggerFactory.getLogger(MassiveApiService.class);

    private final RestClient restClient;

    public MassiveApiService(
            @Value("${massive.api.base-url}") String baseUrl,
            @Value("${massive.api.token}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    public String fetchDividends() {
        log.debug("Calling GET /v3/reference/dividends");
        return restClient.get()
                .uri("/v3/reference/dividends")
                .retrieve()
                .body(String.class);
    }

    public String fetchPreviousDayBarRaw(String ticker) {
        log.debug("Calling GET /v2/aggs/ticker/{}/prev", ticker);
        return restClient.get()
                .uri("/v2/aggs/ticker/{ticker}/prev", ticker)
                .retrieve()
                .body(String.class);
    }

    public PolygonPrevDayResponse fetchPreviousDayBar(String ticker) {
        log.debug("Calling GET /v2/aggs/ticker/{}/prev", ticker);
        return restClient.get()
                .uri("/v2/aggs/ticker/{ticker}/prev", ticker)
                .retrieve()
                .body(PolygonPrevDayResponse.class);
    }
}

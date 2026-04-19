package xuyang.dev.xgtslasvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import xuyang.dev.xgtslasvc.dto.PolygonPrevDayResponse;
import xuyang.dev.xgtslasvc.dto.PolygonSnapshotResponse;

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

    /**
     * Fetch dividend data from Polygon.io
     * GET /v3/reference/dividends
     */
    public String fetchDividends() {
        log.debug("Calling GET /v3/reference/dividends");
        return restClient.get()
                .uri("/v3/reference/dividends")
                .retrieve()
                .body(String.class);
    }

    /**
     * Fetch previous day's OHLCV bar for a ticker
     * GET /v2/aggs/ticker/{ticker}/prev
     *
     * Response format:
     * {
     *   "ticker": "TSLA",
     *   "status": "OK",
     *   "results": [{
     *     "T": "TSLA",
     *     "o": 230.12,    // open
     *     "h": 235.50,    // high
     *     "l": 228.80,    // low
     *     "c": 234.00,    // close
     *     "v": 89000000,  // volume
     *     "vw": 232.45,   // vwap
     *     "t": 1713484800000, // timestamp (Unix ms)
     *     "n": 650000     // number of transactions
     *   }]
     * }
     */
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

    /**
     * Fetch snapshot for a single ticker (real-time)
     * GET /v2/snapshot/locale/us/markets/stocks/tickers/{ticker}
     *
     * Response format:
     * {
     *   "status": "OK",
     *   "ticker": {
     *     "ticker": "TSLA",
     *     "day": { "o": 230.12, "h": 235.50, "l": 228.80, "c": 234.00, "v": 89000000, "vw": 232.45 },
     *     "min": { "o": 233.80, "h": 234.20, "l": 233.50, "c": 234.00, "v": 120000, "vw": 233.90, "t": 1713484800000 },
     *     "prevDay": { "o": 228.00, "h": 231.00, "l": 227.50, "c": 230.12, "v": 75000000, "vw": 229.50 },
     *     "lastTrade": { "p": 234.05, "s": 100, "t": 1713484860000 },
     *     "lastQuote": { "P": 234.10, "S": 200, "p": 234.00, "s": 300, "t": 1713484860000 },
     *     "todaysChange": 3.88,
     *     "todaysChangePerc": 1.686,
     *     "updated": 1713484860000000000
     *   }
     * }
     */
    public String fetchSnapshotRaw(String ticker) {
        log.debug("Calling GET /v2/snapshot/locale/us/markets/stocks/tickers/{}", ticker);
        return restClient.get()
                .uri("/v2/snapshot/locale/us/markets/stocks/tickers/{ticker}", ticker)
                .retrieve()
                .body(String.class);
    }

    public PolygonSnapshotResponse fetchSnapshot(String ticker) {
        log.debug("Calling GET /v2/snapshot/locale/us/markets/stocks/tickers/{}", ticker);
        return restClient.get()
                .uri("/v2/snapshot/locale/us/markets/stocks/tickers/{ticker}", ticker)
                .retrieve()
                .body(PolygonSnapshotResponse.class);
    }
}

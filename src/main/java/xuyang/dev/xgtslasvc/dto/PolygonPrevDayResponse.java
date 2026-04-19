package xuyang.dev.xgtslasvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps the Polygon.io /v2/aggs/ticker/{ticker}/prev response.
 *
 * Example:
 * {
 *   "ticker": "TSLA",
 *   "status": "OK",
 *   "resultsCount": 1,
 *   "results": [{
 *     "T": "TSLA",
 *     "o": 230.12,
 *     "h": 235.50,
 *     "l": 228.80,
 *     "c": 234.00,
 *     "v": 89000000,
 *     "vw": 232.45,
 *     "t": 1713484800000,
 *     "n": 650000
 *   }]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolygonPrevDayResponse {

    private String ticker;
    private String status;
    private int resultsCount;
    private List<AggResult> results;

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getResultsCount() { return resultsCount; }
    public void setResultsCount(int resultsCount) { this.resultsCount = resultsCount; }

    public List<AggResult> getResults() { return results; }
    public void setResults(List<AggResult> results) { this.results = results; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AggResult {

        @JsonProperty("T")
        private String ticker;

        @JsonProperty("o")
        private double open;

        @JsonProperty("h")
        private double high;

        @JsonProperty("l")
        private double low;

        @JsonProperty("c")
        private double close;

        @JsonProperty("v")
        private long volume;

        @JsonProperty("vw")
        private double vwap;

        @JsonProperty("t")
        private long timestamp;

        @JsonProperty("n")
        private long transactions;

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public double getOpen() { return open; }
        public void setOpen(double open) { this.open = open; }

        public double getHigh() { return high; }
        public void setHigh(double high) { this.high = high; }

        public double getLow() { return low; }
        public void setLow(double low) { this.low = low; }

        public double getClose() { return close; }
        public void setClose(double close) { this.close = close; }

        public long getVolume() { return volume; }
        public void setVolume(long volume) { this.volume = volume; }

        public double getVwap() { return vwap; }
        public void setVwap(double vwap) { this.vwap = vwap; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public long getTransactions() { return transactions; }
        public void setTransactions(long transactions) { this.transactions = transactions; }
    }
}

package xuyang.dev.xgtslasvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the Polygon.io /v2/snapshot/locale/us/markets/stocks/tickers/{ticker} response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolygonSnapshotResponse {

    private String status;
    private TickerSnapshot ticker;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public TickerSnapshot getTicker() { return ticker; }
    public void setTicker(TickerSnapshot ticker) { this.ticker = ticker; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TickerSnapshot {
        private String ticker;
        private AggBar day;
        private AggBar min;
        private AggBar prevDay;
        private LastTrade lastTrade;
        private double todaysChange;
        private double todaysChangePerc;
        private long updated;

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public AggBar getDay() { return day; }
        public void setDay(AggBar day) { this.day = day; }

        public AggBar getMin() { return min; }
        public void setMin(AggBar min) { this.min = min; }

        public AggBar getPrevDay() { return prevDay; }
        public void setPrevDay(AggBar prevDay) { this.prevDay = prevDay; }

        public LastTrade getLastTrade() { return lastTrade; }
        public void setLastTrade(LastTrade lastTrade) { this.lastTrade = lastTrade; }

        public double getTodaysChange() { return todaysChange; }
        public void setTodaysChange(double todaysChange) { this.todaysChange = todaysChange; }

        public double getTodaysChangePerc() { return todaysChangePerc; }
        public void setTodaysChangePerc(double todaysChangePerc) { this.todaysChangePerc = todaysChangePerc; }

        public long getUpdated() { return updated; }
        public void setUpdated(long updated) { this.updated = updated; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AggBar {
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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LastTrade {
        @JsonProperty("p")
        private double price;

        @JsonProperty("s")
        private int size;

        @JsonProperty("t")
        private long timestamp;

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}

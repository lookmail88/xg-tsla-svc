package xuyang.dev.xgtslasvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooChartResponse {

    private Chart chart;

    public Chart getChart() { return chart; }
    public void setChart(Chart chart) { this.chart = chart; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chart {
        private List<Result> result;

        public List<Result> getResult() { return result; }
        public void setResult(List<Result> result) { this.result = result; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<Long> timestamp;
        private Indicators indicators;

        public List<Long> getTimestamp() { return timestamp; }
        public void setTimestamp(List<Long> timestamp) { this.timestamp = timestamp; }

        public Indicators getIndicators() { return indicators; }
        public void setIndicators(Indicators indicators) { this.indicators = indicators; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Indicators {
        private List<Quote> quote;

        public List<Quote> getQuote() { return quote; }
        public void setQuote(List<Quote> quote) { this.quote = quote; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quote {
        private List<Double> open;
        private List<Double> high;
        private List<Double> low;
        private List<Double> close;
        private List<Long> volume;

        public List<Double> getOpen() { return open; }
        public void setOpen(List<Double> open) { this.open = open; }

        public List<Double> getHigh() { return high; }
        public void setHigh(List<Double> high) { this.high = high; }

        public List<Double> getLow() { return low; }
        public void setLow(List<Double> low) { this.low = low; }

        public List<Double> getClose() { return close; }
        public void setClose(List<Double> close) { this.close = close; }

        public List<Long> getVolume() { return volume; }
        public void setVolume(List<Long> volume) { this.volume = volume; }
    }
}
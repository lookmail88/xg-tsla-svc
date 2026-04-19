package xuyang.dev.xgtslasvc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_quotes")
public class PriceQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(precision = 12, scale = 4)
    private BigDecimal open;

    @Column(precision = 12, scale = 4)
    private BigDecimal high;

    @Column(precision = 12, scale = 4)
    private BigDecimal low;

    @Column(name = "close_price", precision = 12, scale = 4)
    private BigDecimal close;

    private Long volume;

    @Column(precision = 12, scale = 4)
    private BigDecimal vwap;

    public PriceQuote() {}

    public PriceQuote(String symbol, Instant timestamp, BigDecimal open, BigDecimal high,
                      BigDecimal low, BigDecimal close, Long volume, BigDecimal vwap) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.vwap = vwap;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }

    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }

    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }

    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public BigDecimal getVwap() { return vwap; }
    public void setVwap(BigDecimal vwap) { this.vwap = vwap; }
}

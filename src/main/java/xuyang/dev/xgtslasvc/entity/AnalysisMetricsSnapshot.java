package xuyang.dev.xgtslasvc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "analysis_metrics_snapshots")
public class AnalysisMetricsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long snapshotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private StockAnalysisReport report;

    @Column(name = "rsi_value", precision = 5, scale = 2)
    private BigDecimal rsiValue;

    @Column(name = "macd_line", precision = 12, scale = 4)
    private BigDecimal macdLine;

    @Column(name = "macd_signal_line", precision = 12, scale = 4)
    private BigDecimal macdSignalLine;

    @Column(name = "macd_histogram", precision = 12, scale = 4)
    private BigDecimal macdHistogram;

    @Column(name = "volume_change_pct", precision = 10, scale = 4)
    private BigDecimal volumeChangePct;

    @Column(name = "price_volatility_stddev", precision = 12, scale = 4)
    private BigDecimal priceVolatilityStddev;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @PrePersist
    private void prePersist() {
        if (capturedAt == null) capturedAt = Instant.now();
    }

    public AnalysisMetricsSnapshot() {}

    public Long getSnapshotId() { return snapshotId; }
    public StockAnalysisReport getReport() { return report; }
    public void setReport(StockAnalysisReport report) { this.report = report; }
    public BigDecimal getRsiValue() { return rsiValue; }
    public void setRsiValue(BigDecimal rsiValue) { this.rsiValue = rsiValue; }
    public BigDecimal getMacdLine() { return macdLine; }
    public void setMacdLine(BigDecimal macdLine) { this.macdLine = macdLine; }
    public BigDecimal getMacdSignalLine() { return macdSignalLine; }
    public void setMacdSignalLine(BigDecimal macdSignalLine) { this.macdSignalLine = macdSignalLine; }
    public BigDecimal getMacdHistogram() { return macdHistogram; }
    public void setMacdHistogram(BigDecimal macdHistogram) { this.macdHistogram = macdHistogram; }
    public BigDecimal getVolumeChangePct() { return volumeChangePct; }
    public void setVolumeChangePct(BigDecimal volumeChangePct) { this.volumeChangePct = volumeChangePct; }
    public BigDecimal getPriceVolatilityStddev() { return priceVolatilityStddev; }
    public void setPriceVolatilityStddev(BigDecimal priceVolatilityStddev) { this.priceVolatilityStddev = priceVolatilityStddev; }
    public Instant getCapturedAt() { return capturedAt; }
}
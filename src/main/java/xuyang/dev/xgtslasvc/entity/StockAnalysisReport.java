package xuyang.dev.xgtslasvc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_analysis_reports")
public class StockAnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(name = "report_timestamp")
    private Instant reportTimestamp;

    @Column(name = "trend_sentiment")
    private String trendSentiment;

    @Column(name = "summary_conclusion", nullable = false, columnDefinition = "TEXT")
    private String summaryConclusion;

    @Column(name = "support_level_primary", precision = 12, scale = 4)
    private BigDecimal supportLevelPrimary;

    @Column(name = "support_level_secondary", precision = 12, scale = 4)
    private BigDecimal supportLevelSecondary;

    @Column(name = "resistance_level_primary", precision = 12, scale = 4)
    private BigDecimal resistanceLevelPrimary;

    @Column(name = "resistance_level_secondary", precision = 12, scale = 4)
    private BigDecimal resistanceLevelSecondary;

    @Column(name = "detailed_analysis", columnDefinition = "TEXT")
    private String detailedAnalysis;

    @Column(name = "volume_observation", columnDefinition = "TEXT")
    private String volumeObservation;

    @Column(name = "price_action_observation", columnDefinition = "TEXT")
    private String priceActionObservation;

    @Column(name = "risk_level" )
    private String riskLevel;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        if (reportTimestamp == null) reportTimestamp = now;
        if (createdAt == null) createdAt = now;
    }

    public StockAnalysisReport() {}

    public Long getReportId() { return reportId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Instant getReportTimestamp() { return reportTimestamp; }
    public void setReportTimestamp(Instant reportTimestamp) { this.reportTimestamp = reportTimestamp; }
    public String getTrendSentiment() { return trendSentiment; }
    public void setTrendSentiment(String trendSentiment) { this.trendSentiment = trendSentiment; }
    public String getSummaryConclusion() { return summaryConclusion; }
    public void setSummaryConclusion(String summaryConclusion) { this.summaryConclusion = summaryConclusion; }
    public BigDecimal getSupportLevelPrimary() { return supportLevelPrimary; }
    public void setSupportLevelPrimary(BigDecimal supportLevelPrimary) { this.supportLevelPrimary = supportLevelPrimary; }
    public BigDecimal getSupportLevelSecondary() { return supportLevelSecondary; }
    public void setSupportLevelSecondary(BigDecimal supportLevelSecondary) { this.supportLevelSecondary = supportLevelSecondary; }
    public BigDecimal getResistanceLevelPrimary() { return resistanceLevelPrimary; }
    public void setResistanceLevelPrimary(BigDecimal resistanceLevelPrimary) { this.resistanceLevelPrimary = resistanceLevelPrimary; }
    public BigDecimal getResistanceLevelSecondary() { return resistanceLevelSecondary; }
    public void setResistanceLevelSecondary(BigDecimal resistanceLevelSecondary) { this.resistanceLevelSecondary = resistanceLevelSecondary; }
    public String getDetailedAnalysis() { return detailedAnalysis; }
    public void setDetailedAnalysis(String detailedAnalysis) { this.detailedAnalysis = detailedAnalysis; }
    public String getVolumeObservation() { return volumeObservation; }
    public void setVolumeObservation(String volumeObservation) { this.volumeObservation = volumeObservation; }
    public String getPriceActionObservation() { return priceActionObservation; }
    public void setPriceActionObservation(String priceActionObservation) { this.priceActionObservation = priceActionObservation; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Instant getCreatedAt() { return createdAt; }
}
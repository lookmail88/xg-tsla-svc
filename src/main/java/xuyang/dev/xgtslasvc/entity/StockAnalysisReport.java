package xuyang.dev.xgtslasvc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_analysis_reports")
@Data
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

    @Column(name = "support_level_primary", nullable = false, columnDefinition = "NUMERIC(12,4) DEFAULT 0")
    private BigDecimal supportLevelPrimary;

    @Column(name = "support_level_secondary", nullable = false, columnDefinition = "NUMERIC(12,4) DEFAULT 0")
    private BigDecimal supportLevelSecondary;

    @Column(name = "resistance_level_primary", nullable = false, columnDefinition = "NUMERIC(12,4) DEFAULT 0")
    private BigDecimal resistanceLevelPrimary;

    @Column(name = "resistance_level_secondary", nullable = false, columnDefinition = "NUMERIC(12,4) DEFAULT 0")
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
    @PreUpdate
    private void applyDefaults() {
        Instant now = Instant.now();
        if (reportTimestamp == null)       reportTimestamp       = now;
        if (createdAt == null)             createdAt             = now;
        if (trendSentiment == null)        trendSentiment        = "";
        if (summaryConclusion == null)     summaryConclusion     = "";
        if (detailedAnalysis == null)      detailedAnalysis      = "";
        if (volumeObservation == null)     volumeObservation     = "";
        if (priceActionObservation == null) priceActionObservation = "";
        if (riskLevel == null)             riskLevel             = "";
        if (supportLevelPrimary == null)   supportLevelPrimary   = BigDecimal.ZERO;
        if (supportLevelSecondary == null) supportLevelSecondary = BigDecimal.ZERO;
        if (resistanceLevelPrimary == null)  resistanceLevelPrimary  = BigDecimal.ZERO;
        if (resistanceLevelSecondary == null) resistanceLevelSecondary = BigDecimal.ZERO;
    }
}
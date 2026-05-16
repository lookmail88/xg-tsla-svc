package xuyang.dev.xgtslasvc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PolygonFinancialsResponse {

    private String status;

    @JsonProperty("request_id")
    private String requestId;

    private List<FinancialResult> results;

    public String getStatus() { return status; }
    public String getRequestId() { return requestId; }
    public List<FinancialResult> getResults() { return results; }

    public static class FinancialResult {

        @JsonProperty("company_name")
        private String companyName;

        @JsonProperty("fiscal_period")
        private String fiscalPeriod;

        @JsonProperty("fiscal_year")
        private String fiscalYear;

        @JsonProperty("period_of_report_date")
        private String periodOfReportDate;

        @JsonProperty("filing_date")
        private String filingDate;

        private Financials financials;

        public String getCompanyName() { return companyName; }
        public String getFiscalPeriod() { return fiscalPeriod; }
        public String getFiscalYear() { return fiscalYear; }
        public String getPeriodOfReportDate() { return periodOfReportDate; }
        public String getFilingDate() { return filingDate; }
        public Financials getFinancials() { return financials; }
    }

    public static class Financials {

        @JsonProperty("income_statement")
        private IncomeStatement incomeStatement;

        public IncomeStatement getIncomeStatement() { return incomeStatement; }
    }

    public static class IncomeStatement {

        private FinancialItem revenues;

        @JsonProperty("gross_profit")
        private FinancialItem grossProfit;

        @JsonProperty("operating_income_loss")
        private FinancialItem operatingIncomeLoss;

        @JsonProperty("net_income_loss")
        private FinancialItem netIncomeLoss;

        @JsonProperty("basic_earnings_per_share")
        private FinancialItem basicEarningsPerShare;

        @JsonProperty("diluted_earnings_per_share")
        private FinancialItem dilutedEarningsPerShare;

        public FinancialItem getRevenues() { return revenues; }
        public FinancialItem getGrossProfit() { return grossProfit; }
        public FinancialItem getOperatingIncomeLoss() { return operatingIncomeLoss; }
        public FinancialItem getNetIncomeLoss() { return netIncomeLoss; }
        public FinancialItem getBasicEarningsPerShare() { return basicEarningsPerShare; }
        public FinancialItem getDilutedEarningsPerShare() { return dilutedEarningsPerShare; }
    }

    public static class FinancialItem {

        private Double value;
        private String unit;
        private String label;

        public Double getValue() { return value; }
        public String getUnit() { return unit; }
        public String getLabel() { return label; }
    }
}
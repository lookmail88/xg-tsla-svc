package xuyang.dev.xgtslasvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xuyang.dev.xgtslasvc.entity.StockAnalysisReport;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAnalysisReportRepository extends JpaRepository<StockAnalysisReport, Long> {

    List<StockAnalysisReport> findBySymbolOrderByReportTimestampDesc(String symbol);

    Optional<StockAnalysisReport> findFirstBySymbolOrderByReportTimestampDesc(String symbol);
}
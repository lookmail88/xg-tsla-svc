package xuyang.dev.xgtslasvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xuyang.dev.xgtslasvc.entity.AnalysisMetricsSnapshot;

@Repository
public interface AnalysisMetricsSnapshotRepository extends JpaRepository<AnalysisMetricsSnapshot, Long> {
}

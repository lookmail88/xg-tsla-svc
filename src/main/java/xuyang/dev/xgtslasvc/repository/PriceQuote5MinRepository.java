package xuyang.dev.xgtslasvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xuyang.dev.xgtslasvc.entity.PriceQuote5Min;

import java.time.Instant;
import java.util.List;

@Repository
public interface PriceQuote5MinRepository extends JpaRepository<PriceQuote5Min, Long> {

    List<PriceQuote5Min> findBySymbolOrderByTimestampDesc(String symbol);

    List<PriceQuote5Min> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
            String symbol, Instant start, Instant end);

    boolean existsBySymbolAndTimestamp(String symbol, Instant timestamp);
}
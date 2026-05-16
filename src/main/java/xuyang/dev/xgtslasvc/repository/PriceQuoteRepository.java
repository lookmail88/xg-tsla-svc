package xuyang.dev.xgtslasvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xuyang.dev.xgtslasvc.entity.PriceQuote;

import java.time.Instant;
import java.util.List;

@Repository
public interface PriceQuoteRepository extends JpaRepository<PriceQuote, Long> {

    List<PriceQuote> findBySymbolOrderByTimestampDesc(String symbol);

    List<PriceQuote> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
            String symbol, Instant start, Instant end);

    boolean existsBySymbolAndTimestamp(String symbol, Instant timestamp);
}

package xuyang.dev.xgtslasvc.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xuyang.dev.xgtslasvc.service.ReportService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
public class WebController {

    @Value("${app.env}")
    private String apiEnv;
    private final BuildProperties buildProperties;
    private final DataSource dataSource;
    private final ReportService reportService;

    public WebController(Optional<BuildProperties> buildProperties, DataSource dataSource,
                         ReportService reportService) {
        this.buildProperties = buildProperties.orElse(null);
        this.dataSource = dataSource;
        this.reportService = reportService;
    }

    @GetMapping(value="/sayhello")
    public ResponseEntity<String> sayHello(){
        return ResponseEntity.ok("Hello,This is for xg-tsla-svc v0.1.0");
    }
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss_SSS");


    @GetMapping(value="/health")
    public ResponseEntity<String> getHealth(){
        ZonedDateTime nowLA = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
        return ResponseEntity.ok( nowLA.format(formatter));
    }

    @GetMapping(value = "/version")
    public ResponseEntity<String> getVersion(){
        String version = buildProperties != null ? buildProperties.getVersion() : "unknown";
        return ResponseEntity.ok(apiEnv + ":" + version);
    }

    @GetMapping(value = "/db/status")
    public ResponseEntity<Map<String, String>> getDbStatus() {
        try (Connection conn = dataSource.getConnection()) {
            String dbProduct = conn.getMetaData().getDatabaseProductName();
            String dbVersion = conn.getMetaData().getDatabaseProductVersion();
            String url = conn.getMetaData().getURL();
            return ResponseEntity.ok(Map.of(
                    "status", "connected",
                    "database", dbProduct,
                    "version", dbVersion,
                    "url", url
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "disconnected",
                    "error", e.getMessage()
            ));
        }
    }
}

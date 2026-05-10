package xuyang.dev.xgtslasvc.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xuyang.dev.xgtslasvc.service.ReportService;

import java.util.Map;

@RestController
@CrossOrigin
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping (value = "/report/generateNew")
    public ResponseEntity<Map<String, Object>> generateNewReport() {
        return ResponseEntity.ok(reportService.generateNewReport("TSLA"));
    }

    @GetMapping(value = "/report/getLatest")
    public ResponseEntity<Map<String, Object>> getLatestReport() {
        return ResponseEntity.ok(reportService.getLatestSavedReport("TSLA"));
    }
}
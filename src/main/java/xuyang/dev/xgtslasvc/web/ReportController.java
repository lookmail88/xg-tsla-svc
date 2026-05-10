package xuyang.dev.xgtslasvc.web;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xuyang.dev.xgtslasvc.service.ReportService;


import java.util.Map;

@RestController
@CrossOrigin
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/getlatestreport")
    public ResponseEntity<Map<String, Object>> getLatestReport() {
        return ResponseEntity.ok(reportService.getLatestReport("TSLA"));
    }


}

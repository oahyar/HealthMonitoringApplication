package com.example.HealthMonitoringApp.Controller;

import com.example.HealthMonitoringApp.Entity.ApiStatusLog;
import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.Service.ApiHealthService;
import com.example.HealthMonitoringApp.Service.JobMonitorService;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.Wiremock.properties.MonitorProperties;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class APIController {

    @Autowired
    private ServerMetricService serverMetricService;

    @Autowired
    private TableSpaceService tableSpaceService;
    @Autowired
    private JobLogRepository jobLogRepository;
    @Autowired
    private MonitorProperties monitorProperties;
    @Autowired
    private ApiStatusLogRepository apiStatusLogRepository;

    @Autowired
    private Scheduler scheduler;

    // Matches fetch('/api/status/recent')
    @GetMapping("/api/status/recent")
    @ResponseBody
    public List<ApiStatusLog> recentChecks() {
        return apiStatusLogRepository.findTop10ByOrderByTimestampDesc();
    }

    @GetMapping("/api-summary")
    public String apiSummaryPage(
            @RequestParam(name="apiName", required=false) String apiName, Model model)
    {
        model.addAttribute("warnThreshold", monitorProperties.getWarnThreshold());
        model.addAttribute("critThreshold", monitorProperties.getCritThreshold());
        model.addAttribute("apiName", apiName);             // pass it through
        return "api_status";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ApiStatusLog> statuses = apiStatusLogRepository.findLatestPerApi();
        model.addAttribute("apiStatuses", statuses);
        return "dashboard";
    }

    @GetMapping("/dashboard/apimetrics")
    @ResponseBody
    public Map<String,Object> getApiMetrics(
            @RequestParam(name="apiName", required=false) String apiName
    ) {
        // 1) fetch the last 20 logs, newest first
        List<ApiStatusLog> recent;
        if (apiName != null && !apiName.isBlank()) {
            recent = apiStatusLogRepository
                    .findTop20ByApiNameOrderByTimestampDesc(apiName);
        } else {
            recent = apiStatusLogRepository
                    .findTop20ByOrderByTimestampDesc();
        }
        // reverse so oldest → newest
        Collections.reverse(recent);

        // 2) build the label (time) list
        DateTimeFormatter fmt = DateTimeFormatter
                .ofPattern("HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        List<String> labels = recent.stream()
                .map(log -> fmt.format(log.getTimestamp()))
                .collect(Collectors.toList());

        // 3) build the latency series
        List<Long> latencySeries = recent.stream()
                .map(ApiStatusLog::getLatencyMillis)
                .collect(Collectors.toList());

        // 4) build the uptime series (1 = up, 0 = down)
        List<Integer> uptimeSeries = recent.stream()
                .map(log -> log.isUp() ? 1 : 0)
                .collect(Collectors.toList());

        // 5) return as JSON
        return Map.of(
                "labels",        labels,
                "latencySeries", latencySeries,
                "uptimeSeries",  uptimeSeries
        );
    }

    @GetMapping("/dashboard/apistatus")
    @ResponseBody
    public List<ApiStatusLog> latestPerApi() {
        return apiStatusLogRepository.findLatestPerApi();
    }

}

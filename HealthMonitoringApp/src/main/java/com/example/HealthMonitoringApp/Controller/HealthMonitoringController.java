package com.example.HealthMonitoringApp.Controller;

import com.example.HealthMonitoringApp.Entity.ApiStatusLog;
import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.Service.ApiHealthService;
import com.example.HealthMonitoringApp.Service.JobMonitorService;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.Wiremock.properties.MonitorProperties;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import com.example.HealthMonitoringApp.dto.JobDependencyDTO;
import com.example.HealthMonitoringApp.dto.JobStatusDTO;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HealthMonitoringController {

    @Autowired
    private ServerMetricService serverMetricService;

    @Autowired
    private TableSpaceService tableSpaceService;

    @Autowired
    private JobMonitorService jobMonitorService;

    @Autowired
    private JobLogRepository jobLogRepository;
    @Autowired
    private ApiHealthService apiHealthService;
    @Autowired
    private MonitorProperties monitorProperties;
    @Autowired
    private ApiStatusLogRepository apiStatusLogRepository;

    @Autowired
    private Scheduler scheduler;

    /**
     * Render the main dashboard page.
     * Adds overall and high‐usage metrics for disks and tablespaces to the model.
     */
    @GetMapping("/disk")
    public String showDiskStatus(String hostname, String sid, Model model) {
        // Add all aggregated disk metrics
        model.addAttribute("diskUsages", serverMetricService.getAggregatedSpaceMetrics());
        // Add all aggregated tablespace metrics
        model.addAttribute("tableUsages", tableSpaceService.getAggregatedTableSpaceMetrics());

        // Filter servers to only those with at least one high‐usage filesystem
        List<AggregatedSpaceMetrics> allServers = serverMetricService.getAggregatedSpaceMetrics();
        List<AggregatedSpaceMetrics> filteredServers = new ArrayList<>();
        for (AggregatedSpaceMetrics server : allServers) {
            List<ServerDiskPartition> highUsageFiles =
                    serverMetricService.getHighUsageFilesystems(server.getHostname());
            if (!highUsageFiles.isEmpty()) {
                filteredServers.add(server);
            }
        }
        model.addAttribute("highUsageServers", filteredServers);

        // Filter tablespaces to only those with high‐usage partitions
        List<AggregatedTableSpaceMetrics> allTableSpaces =
                tableSpaceService.getAggregatedTableSpaceMetrics();
        List<AggregatedTableSpaceMetrics> filteredTableSpaces = new ArrayList<>();
        for (AggregatedTableSpaceMetrics ts : allTableSpaces) {
            List<TableSpace> highUsageTables =
                    tableSpaceService.getHighUsageTablespaces(ts.getSid());
            if (!highUsageTables.isEmpty()) {
                filteredTableSpaces.add(ts);
            }
        }
        model.addAttribute("highUsageDb", filteredTableSpaces);

        return "disk_status";
    }

    /**
     * AJAX endpoint: fetch detailed disk partitions for a specific host.
     */
    @GetMapping("/disk/getDetailsByHostname")
    @ResponseBody
    public List<ServerDiskPartition> getDetailsByHostname(@RequestParam String hostname) {
        return serverMetricService.getDiskDetailByHostname(hostname);
    }

    /**
     * API endpoint: return all aggregated disk metrics.
     */
    @GetMapping("/aggregated-space")
    @ResponseBody
    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        return serverMetricService.getAggregatedSpaceMetrics();
    }

    /**
     * API endpoint: return latest filesystem details for a given hostname.
     */
    @GetMapping("/latest-filesystem/{hostname}")
    @ResponseBody
    public List<ServerDiskPartition> getLatestFilesystemByHostname(@PathVariable String hostname) {
        return serverMetricService.getDiskDetailByHostname(hostname);
    }

    /**
     * API endpoint: return all aggregated tablespace metrics.
     */
    @GetMapping("/aggregated-tablespace")
    @ResponseBody
    public List<AggregatedTableSpaceMetrics> getAggregatedTableSpaceMetrics() {
        return tableSpaceService.getAggregatedTableSpaceMetrics();
    }

    /**
     * API endpoint: return latest tablespace details for a given host and SID.
     */
    @GetMapping("/latest-tablespace/{hostname}/{sid}")
    @ResponseBody
    public List<TableSpace> getLatestTableSpaceByHostname(
            @PathVariable String hostname,
            @PathVariable String sid) {
        return tableSpaceService.getLatestTableSpaceDetails(hostname, sid);
    }

    /**
     * API endpoint: return high‐usage filesystems for a given hostname.
     */
    @GetMapping("/high-usage-filesystems/{hostname}")
    @ResponseBody
    public List<ServerDiskPartition> getHighUsageFilesystems(@PathVariable String hostname) {
        return serverMetricService.getHighUsageFilesystems(hostname);
    }

    /**
     * API endpoint: return high‐usage tablespaces for a given SID.
     */
    @GetMapping("/high-usage-db/{sid}")
    @ResponseBody
    public List<TableSpace> getHighUsageTablespaces(@PathVariable String sid) {
        return tableSpaceService.getHighUsageTablespaces(sid);
    }

    /**
     * API endpoint: get current status of a Quartz job by name (and optional group).
     */
    @GetMapping("/{jobName}/status")
    @ResponseBody
    public ResponseEntity<String> getJobStatus(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "DEFAULT") String group) {
        try {
            String status = jobMonitorService.getJobStatus(jobName, group);
            return ResponseEntity.ok(status);
        } catch (SchedulerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * API endpoint: fetch all log entries for a given job name.
     */
    @GetMapping("/{jobName}")
    @ResponseBody
    public List<JobLog> getLogs(@PathVariable String jobName) {
        return jobLogRepository.findByJobName(jobName);
    }

    /**
     * API endpoint: fetch the most recent log entry for a given job.
     */
    @GetMapping("/status/{jobName}")
    @ResponseBody
    public JobLog getLatestStatus(@PathVariable String jobName) {
        return jobLogRepository.findTopByJobNameOrderByStartTimeDesc(jobName);
    }

    /**
     * Render the job status summary page.
     * Shows each monitored job’s last run and its next scheduled fire time.
     */
    @GetMapping("/status-summary")
    public String getJobStatusSummary(
            @RequestParam(name="filter", required=false) String filter,
            Model model) throws SchedulerException {

        // 1️⃣ Gather all monitored job names
        List<String> allJobs = jobLogRepository.findDistinctJobNames();
        List<String> monitoredJobs = allJobs.stream()
                .filter(name -> !name.isEmpty() && Character.isUpperCase(name.charAt(0)))
                .collect(Collectors.toList());

        // 2️⃣ Build the DTOs
        List<JobStatusDTO> summaries = new ArrayList<>();
        for (String jobName : monitoredJobs) {
            JobLog latest = jobLogRepository
                    .findTopByJobNameOrderByStartTimeDesc(jobName);
            LocalDateTime nextRun = getNextFireTime(jobName);

            JobStatusDTO dto = new JobStatusDTO();
            dto.setJobName(jobName);
            dto.setLastStatus(latest != null ? latest.getStatus() : "UNKNOWN");
            dto.setLastRunTime(latest != null ? latest.getStartTime() : null);
            dto.setNextRunTime(nextRun);
            summaries.add(dto);
        }

        // 3️⃣ Apply the optional filter
        if (filter != null && !filter.isBlank()) {
            String up = filter.toUpperCase();
            summaries = summaries.stream()
                    .filter(dto -> up.equals(dto.getLastStatus()))
                    .collect(Collectors.toList());
        }

        // 4️⃣ Pass both the list and the active filter back to the view
        model.addAttribute("jobs", summaries);
        model.addAttribute("activeFilter", filter);

        return "job_status";
    }


    /**
     * Helper to look up the Quartz trigger’s next fire time for a job.
     */
    LocalDateTime getNextFireTime(String jobName) throws SchedulerException {
        List<? extends Trigger> triggers =
                scheduler.getTriggersOfJob(new JobKey(jobName));
        if (!triggers.isEmpty()) {
            Date nextFire = triggers.get(0).getNextFireTime();
            return nextFire != null
                    ? LocalDateTime.ofInstant(nextFire.toInstant(),
                    ZoneId.systemDefault())
                    : null;
        }
        return null;
    }

    /**
     * API endpoint: return job execution history including any dependent‐job retries.
     */
    @GetMapping("/history/{jobName}")
    @ResponseBody
    public List<JobLog> getJobHistory(@PathVariable String jobName) {
        return jobMonitorService.getHistoryIncludingDependent(jobName);
    }

    /**
     * API endpoint: return the job dependency graph data.
     */
    @GetMapping("/api/job-graph")
    @ResponseBody
    public List<JobDependencyDTO> getJobGraph(@RequestParam String jobName) {
        return jobMonitorService.getJobGraphElements(jobName);
    }

    /**
     * API endpoint: download history as a text file.
     * Includes both main and dependent‐job logs, formatted in single lines.
     */
    @GetMapping("/logs/download/{jobName}")
    public ResponseEntity<Resource> downloadLogs(@PathVariable String jobName) {
        List<JobLog> logs =
                jobMonitorService.getHistoryIncludingDependent(jobName);

        StringBuilder sb = new StringBuilder();
        for (JobLog log : logs) {
            sb.append("[")
                    .append(log.getStartTime())
                    .append(" - ")
                    .append(log.getEndTime() != null
                            ? log.getEndTime()
                            : "-")
                    .append("] ")
                    .append(log.getJobName())
                    .append(" ")
                    .append(log.getStatus())
                    .append(": ")
                    .append(log.getMessage())
                    .append("\n");
        }

        ByteArrayResource resource =
                new ByteArrayResource(sb.toString()
                        .getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + jobName + "_logs.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }

    // Matches fetch('/api/status/recent')
    @GetMapping("/api/status/recent")
    @ResponseBody
    public List<ApiStatusLog> recentChecks() {
        return apiStatusLogRepository.findTop10ByOrderByTimestampDesc();
    }

    // If you also want your “run” endpoint:
    @GetMapping("/api/status/run")
    @ResponseBody
    public List<ApiStatusLog> runChecksNow() {
        monitorProperties.getEndpoints().forEach(apiHealthService::check);
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

    // Returns only host‐rows where at least one filesystem is over threshold
    @GetMapping("/dashboard/server")
    @ResponseBody
    public List<AggregatedSpaceMetrics> getHighUsageServers() {
        List<AggregatedSpaceMetrics> all = serverMetricService.getAggregatedSpaceMetrics();
        return all.stream()
                .filter(s -> !serverMetricService
                        .getHighUsageFilesystems(s.getHostname())
                        .isEmpty())
                .collect(Collectors.toList());
    }

    // Returns only SID‐rows where at least one tablespace is over threshold
    @GetMapping("dashboard/db")
    @ResponseBody
    public List<AggregatedTableSpaceMetrics> getHighUsageDbs() {
        List<AggregatedTableSpaceMetrics> all = tableSpaceService.getAggregatedTableSpaceMetrics();
        return all.stream()
                .filter(ts -> !tableSpaceService
                        .getHighUsageTablespaces(ts.getSid())
                        .isEmpty())
                .collect(Collectors.toList());
    }

    @GetMapping("/dashboard/jobcounts")
    @ResponseBody
    public Map<String,Long> getJobStatusCounts() throws SchedulerException {
        // 1) Fetch all distinct, monitored job names
        List<String> allJobs = jobLogRepository.findDistinctJobNames().stream()
                .filter(name -> !name.isEmpty() && Character.isUpperCase(name.charAt(0)))
                .collect(Collectors.toList());

        // 2) Pull the latest status for each job
        List<String> statuses = allJobs.stream()
                .map(jobName -> {
                    JobLog latest = jobLogRepository
                            .findTopByJobNameOrderByStartTimeDesc(jobName);
                    return latest != null
                            ? latest.getStatus()
                            : "UNKNOWN";
                })
                .collect(Collectors.toList());

        // 3) Count how many of each
        long success = statuses.stream().filter(s -> "SUCCESS".equals(s)).count();
        long failed  = statuses.stream().filter(s -> "FAILED".equals(s)).count();
        long waiting = statuses.stream().filter(s -> "WAITING".equals(s)).count();

        // 4) Return as simple JSON
        return Map.of(
                "successCount", success,
                "failCount",    failed,
                "waitCount",    waiting
        );
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

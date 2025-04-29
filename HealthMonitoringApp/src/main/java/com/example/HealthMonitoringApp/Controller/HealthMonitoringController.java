package com.example.HealthMonitoringApp.Controller;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.Service.JobMonitorService;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
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
import org.springframework.data.domain.Sort;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private Scheduler scheduler;

    /**
     * Render the main dashboard page.
     * Adds overall and high‐usage metrics for disks and tablespaces to the model.
     */
    @GetMapping("/dashboard")
    public String showDashboard(String hostname, String sid, Model model) {
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

        return "dashboard";
    }

    /**
     * AJAX endpoint: fetch detailed disk partitions for a specific host.
     */
    @GetMapping("/dashboard/getDetailsByHostname")
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
    public String getJobStatusSummary(Model model) throws SchedulerException {
        // Gather distinct job names from logs
        List<String> allJobs = jobLogRepository.findDistinctJobNames();

        // Filter out any names that don’t start with uppercase (non‐monitored)
        List<String> monitoredJobs = allJobs.stream()
                .filter(name -> !name.isEmpty() && Character.isUpperCase(name.charAt(0)))
                .collect(Collectors.toList());

        // Build DTOs for each job
        List<JobStatusDTO> summaries = new ArrayList<>();
        for (String jobName : monitoredJobs) {
            JobLog latest =
                    jobLogRepository.findTopByJobNameOrderByStartTimeDesc(jobName);
            LocalDateTime nextRun = getNextFireTime(jobName);

            JobStatusDTO dto = new JobStatusDTO();
            dto.setJobName(jobName);
            dto.setLastStatus(latest != null ? latest.getStatus() : "UNKNOWN");
            dto.setLastRunTime(latest != null ? latest.getStartTime() : null);
            dto.setNextRunTime(nextRun);

            summaries.add(dto);
        }

        model.addAttribute("jobs", summaries);
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

}

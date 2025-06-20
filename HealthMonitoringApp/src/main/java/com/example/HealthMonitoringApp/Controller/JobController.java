package com.example.HealthMonitoringApp.Controller;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.Service.ApiHealthService;
import com.example.HealthMonitoringApp.Service.JobMonitorService;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.Wiremock.properties.MonitorProperties;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class JobController {

    @Autowired
    private JobMonitorService jobMonitorService;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Autowired
    private Scheduler scheduler;

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
}

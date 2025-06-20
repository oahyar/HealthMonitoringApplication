package com.example.HealthMonitoringApp.Controller;

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
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ServerDbController {

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
}
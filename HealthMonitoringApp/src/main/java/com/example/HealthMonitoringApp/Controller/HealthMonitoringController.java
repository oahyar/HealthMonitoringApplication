package com.example.HealthMonitoringApp.Controller;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HealthMonitoringController {

    @Autowired
    private ServerMetricService serverMetricService;

    @Autowired
    private TableSpaceService tableSpaceService;

    /**
     * ✅ Load Dashboard Page with Filtered Data
     * - Displays servers with **high disk usage** (>=70%) only.
     * - Displays tablespaces with **high usage partitions** (>=70%) only.
     */
    @GetMapping("/dashboard")
    public String showDashboard(String hostname, String sid, Model model) {
        // ✅ Load all disk space and tablespace usage
        model.addAttribute("diskUsages", serverMetricService.getAggregatedSpaceMetrics());
        model.addAttribute("tableUsages", tableSpaceService.getAggregatedTableSpaceMetrics());

        // ✅ Filter Servers: Only include those with high-usage filesystems
        List<AggregatedSpaceMetrics> allServers = serverMetricService.getAggregatedSpaceMetrics();
        List<AggregatedSpaceMetrics> filteredServers = new ArrayList<>();

        for (AggregatedSpaceMetrics server : allServers) {
            List<ServerDiskPartition> highUsageFiles = serverMetricService.getHighUsageFilesystems(server.getHostname());
            if (!highUsageFiles.isEmpty()) {
                filteredServers.add(server); // ✅ Only add if server has high-usage filesystems
            }
        }
        model.addAttribute("highUsageServers", filteredServers);
        model.addAttribute("highUsageFile", serverMetricService.getHighUsageFilesystems(hostname));

        // ✅ Filter Tablespaces: Only include those with high-usage partitions
        List<AggregatedTableSpaceMetrics> allTableSpaces = tableSpaceService.getAggregatedTableSpaceMetrics();
        List<AggregatedTableSpaceMetrics> filteredTableSpaces = new ArrayList<>();

        for (AggregatedTableSpaceMetrics tablespace : allTableSpaces) {
            List<TableSpace> highUsageTables = tableSpaceService.getHighUsageTablespaces(tablespace.getSid());
            if (!highUsageTables.isEmpty()) {
                filteredTableSpaces.add(tablespace); // ✅ Only add if at least one tablespace exceeds 70%
            }
        }
        model.addAttribute("highUsageDb", filteredTableSpaces);

        return "dashboard"; // ✅ Render Thymeleaf "dashboard.html" view
    }

    /**
     * ✅ Retrieve **Disk Space Details** for a Specific Hostname (AJAX Call)
     * - Used in frontend to fetch details dynamically.
     */
    @GetMapping("/dashboard/getDetailsByHostname")
    @ResponseBody
    public List<ServerDiskPartition> getDetailsByHostname(@RequestParam String hostname) {
        System.out.println("Fetching details for Hostname: " + hostname);
        return serverMetricService.getDiskDetailByHostname(hostname);
    }

    /**
     * ✅ Retrieve **Aggregated Space Metrics** (All Servers)
     */
    @GetMapping("/aggregated-space")
    @ResponseBody
    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        return serverMetricService.getAggregatedSpaceMetrics();
    }

    /**
     * ✅ Retrieve **Latest Filesystem Data** for a Given Hostname
     */
    @GetMapping("/latest-filesystem/{hostname}")
    @ResponseBody
    public List<ServerDiskPartition> getLatestFilesystemByHostname(@PathVariable String hostname) {
        return serverMetricService.getDiskDetailByHostname(hostname);
    }

    /**
     * ✅ Retrieve **Aggregated Tablespace Metrics** (All Tablespaces)
     */
    @GetMapping("/aggregated-tablespace")
    @ResponseBody
    public List<AggregatedTableSpaceMetrics> getAggregatedTableSpaceMetrics() {
        return tableSpaceService.getAggregatedTableSpaceMetrics();
    }

    /**
     * ✅ Retrieve **Latest Tablespace Data** for a Given Hostname and SID
     */
    @GetMapping("/latest-tablespace/{hostname}/{sid}")
    @ResponseBody
    public List<TableSpace> getLatestTableSpaceByHostname(@PathVariable String hostname, @PathVariable String sid) {
        return tableSpaceService.getLatestTableSpaceDetails(hostname, sid);
    }

    /**
     * ✅ Retrieve **High-Usage Filesystems** (Above 70%) for a Given Hostname
     */
    @GetMapping("/high-usage-filesystems/{hostname}")
    @ResponseBody
    public List<ServerDiskPartition> getHighUsageFilesystems(@PathVariable String hostname) {
        return serverMetricService.getHighUsageFilesystems(hostname);
    }

    /**
     * ✅ Retrieve **High-Usage Tablespaces** (Above 70%) for a Given SID
     */
    @GetMapping("/high-usage-db/{sid}")
    @ResponseBody
    public List<TableSpace> getHighUsageTablespaces(@PathVariable String sid) {
        return tableSpaceService.getHighUsageTablespaces(sid);
    }

}

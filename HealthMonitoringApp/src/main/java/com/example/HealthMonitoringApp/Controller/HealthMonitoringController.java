package com.example.HealthMonitoringApp.Controller;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HealthMonitoringController {

    @Autowired
    private ServerMetricService serverMetricService;

    @Autowired
    private TableSpaceService tableSpaceService;

    // Load Dashboard Page with Existing Data
    @GetMapping("/dashboard")
    public String showDashboard(String hostname,Model model) {
        model.addAttribute("diskUsages", serverMetricService.getAggregatedSpaceMetrics());
        model.addAttribute("tableUsages", tableSpaceService.getAggregatedTableSpaceMetrics());
        return "dashboard";
    }

     //Retrieve Data for Specific SID (AJAX Call)
    @GetMapping("/dashboard/getDetailsByHostname")
    @ResponseBody
    public List<ServerDiskPartition> getDetailsByHostname(@RequestParam String hostname) {
        System.out.println("Fetching details:" + hostname);
        return serverMetricService.getDiskDetailByHostname(hostname);
    }

    // Retrieve Server JSON Response
    @GetMapping("/aggregated-space")
    @ResponseBody
    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        return serverMetricService.getAggregatedSpaceMetrics();
    }

    // Retrieve Server Diskspace
    @GetMapping("/latest-filesystem/{hostname}")
    @ResponseBody
    public List<ServerDiskPartition> getLatestFilesystemByHostname(@PathVariable String hostname) {
        return serverMetricService.getDiskDetailByHostname(hostname);
    }

    // Retrieve aggregated Tablespace
    @GetMapping("/aggregated-tablespace")
    @ResponseBody
    public List<AggregatedTableSpaceMetrics> getAggregatedTableSpaceMetrics() {
        return tableSpaceService.getAggregatedTableSpaceMetrics();
    }

    // Retrieve tablespace details
    @GetMapping("/latest-tablespace/{hostname}/{sid}")
    @ResponseBody
    public List<TableSpace> getLatestTableSpaceByHostname(@PathVariable String hostname, @PathVariable String sid) {
        return tableSpaceService.getLatestTableSpaceDetails(hostname, sid);
    }


}

package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServerMetricService {

    @Autowired
    private ServerMetricRepository serverMetricRepository;

    /**
     * Retrieves all server metrics from the database.
     * @return List of ServerMetric objects.
     */
    public List<ServerMetric> getAllMetrics() {
        return serverMetricRepository.findAll();
    }

    /**
     * Retrieves aggregated disk space metrics per server, including total space,
     * available space, used space, and usage percentage.
     * @return List of AggregatedSpaceMetrics containing summarized disk usage data.
     */
    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        List<Object[]> results = serverMetricRepository.findAggregatedSpaceMetrics();
        List<AggregatedSpaceMetrics> servers = new ArrayList<>();

        // Convert raw query results into AggregatedSpaceMetrics objects
        for (Object[] row : results) {
            AggregatedSpaceMetrics server = new AggregatedSpaceMetrics(
                    row[0].toString(),  // Hostname
                    ((Number) row[1]).longValue(),  // Total Space (MB)
                    ((Number) row[2]).longValue(),  // Available Space (MB)
                    ((Number) row[3]).longValue(),  // Used Space (MB)
                    ((Number) row[4]).longValue()   // Usage Percentage
            );
            servers.add(server);
        }
        return servers;
    }

    /**
     * Retrieves the latest disk partition details for a given hostname.
     * @param hostname The hostname of the server.
     * @return List of ServerDiskPartition objects containing partition details.
     */
    public List<ServerDiskPartition> getDiskDetailByHostname(String hostname) {
        return serverMetricRepository.findLatestFilesystemByHostname(hostname);
    }

    /**
     * Retrieves disk partitions for a given hostname where the disk usage exceeds 70%.
     * This helps in identifying servers with high disk space usage.
     * @param hostname The hostname of the server.
     * @return List of ServerDiskPartition objects where usage is above 70%.
     */
    public List<ServerDiskPartition> getHighUsageFilesystems(String hostname) {
        List<ServerDiskPartition> result = serverMetricRepository.findHighUsageFilesystems(hostname);
        System.out.println("Fetched " + result.size() + " records for hostname: " + hostname);
        return result;
    }

    /**
     * Retrieves a list of servers that have at least one filesystem with disk usage ≥ 70%.
     * This is useful for alerting or monitoring high disk usage across multiple servers.
     * @return List of ServerMetric objects that meet the high-usage threshold.
     */
    public List<ServerMetric> getServersWithHighUsageThreshold() {
        List<ServerMetric> allServers = serverMetricRepository.findAll();
        List<ServerMetric> filteredServers = new ArrayList<>();

        for (ServerMetric server : allServers) {
            // Fetch partitions where usage is above 70% for the current server
            List<ServerDiskPartition> partitions = serverMetricRepository.findByHostnameAndUsageAboveThreshold(server.getHostname(), 70);
            if (!partitions.isEmpty()) {
                filteredServers.add(server); // Add servers with at least one high-usage filesystem
            }
        }
        return filteredServers;
    }
}

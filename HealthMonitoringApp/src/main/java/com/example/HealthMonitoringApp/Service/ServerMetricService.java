package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import org.apache.catalina.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServerMetricService {

    @Autowired
    private ServerMetricRepository serverMetricRepository;

    public List<ServerMetric> getAllMetrics() {
        return serverMetricRepository.findAll();
    }

    // ✅ Get latest disk summary per server
    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        List<Object[]> results = serverMetricRepository.findAggregatedSpaceMetrics();
        List<AggregatedSpaceMetrics> servers = new ArrayList<>();

        for (Object[] row : results) {
            AggregatedSpaceMetrics server = new AggregatedSpaceMetrics(
                    row[0].toString(),  // Hostname
                    ((Number) row[1]).longValue(),  // Total Space
                    ((Number) row[2]).longValue(),  // Available Space
                    ((Number) row[3]).longValue(),  // Used Space
                    ((Number) row[4]).longValue()   // Usage (%)
            );
            servers.add(server);
        }
        return servers;
    }

    public List<ServerDiskPartition> getDiskDetailByHostname(String hostname) {
        List<ServerDiskPartition> result = serverMetricRepository.findLatestFilesystemByHostname(hostname);
        return result;
    }
}

package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import org.apache.catalina.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServerMetricService {

    @Autowired
    private ServerMetricRepository serverMetricRepository;

    public List<ServerMetric> getAllMetrics() {
        return serverMetricRepository.findAll();
    }

    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        List<Object[]> results = serverMetricRepository.findAggregatedSpaceMetrics();

        return results.stream().map(row -> new AggregatedSpaceMetrics(
                (String) row[0], // hostname
                ((Number) row[1]).longValue(), // totalDiskspace
                ((Number) row[2]).longValue(), // totalAvailableDisk
                ((Number) row[3]).longValue(), // totalUsedDisk
                ((Number) row[4]).longValue()  // usagePct
        )).collect(Collectors.toList());
    }

    public List<ServerDiskPartition> getDiskDetailByHostname(String hostname) {
        List<ServerDiskPartition> result = serverMetricRepository.findLatestFilesystemByHostname(hostname);
        return result;
    }

}

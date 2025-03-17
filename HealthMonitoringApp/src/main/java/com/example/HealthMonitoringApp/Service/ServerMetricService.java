package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServerMetricService {

    private static final Logger logger = LoggerFactory.getLogger(ServerMetricService.class);

    @Autowired
    private ServerMetricRepository serverMetricRepository;

    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        logger.info("Fetching aggregated disk space metrics...");
        List<Object[]> results = serverMetricRepository.findAggregatedSpaceMetrics();
        List<AggregatedSpaceMetrics> servers = new ArrayList<>();

        for (Object[] row : results) {
            try {
                AggregatedSpaceMetrics server = new AggregatedSpaceMetrics(
                        row[0].toString(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                );
                servers.add(server);
            } catch (Exception e) {
                logger.error("Error processing row: {}", row, e);
            }
        }

        logger.info("Successfully retrieved {} aggregated server metrics.", servers.size());
        return servers;
    }

    public List<ServerDiskPartition> getDiskDetailByHostname(String hostname) {
        logger.info("Fetching disk partition details for hostname: {}", hostname);
        List<ServerDiskPartition> partitions = serverMetricRepository.findLatestFilesystemByHostname(hostname);

        if (partitions.isEmpty()) {
            logger.warn("No disk partition details found for hostname: {}", hostname);
        } else {
            logger.info("Retrieved {} partitions for hostname: {}", partitions.size(), hostname);
        }

        return partitions;
    }

    public List<ServerDiskPartition> getHighUsageFilesystems(String hostname) {
        logger.info("Fetching high usage filesystems for hostname: {}", hostname);
        List<ServerDiskPartition> result = serverMetricRepository.findHighUsageFilesystems(hostname);

        if (result.isEmpty()) {
            logger.warn("No high usage filesystems found for hostname: {}", hostname);
        } else {
            logger.info("Found {} high usage filesystems for hostname: {}", result.size(), hostname);
        }

        return result;
    }

    public List<ServerDiskPartition> getServersWithHighUsageThreshold() {
        logger.info("Fetching servers with high disk usage (≥70%)...");
        List<ServerDiskPartition> allServers = serverMetricRepository.findAll();
        List<ServerDiskPartition> filteredServers = new ArrayList<>();

        for (ServerDiskPartition server : allServers) {
            List<ServerDiskPartition> partitions = serverMetricRepository.findByHostnameAndUsageAboveThreshold(server.getHostname(), 70);
            if (!partitions.isEmpty()) {
                filteredServers.add(server);
                logger.info("Server {} has {} high usage partitions.", server.getHostname(), partitions.size());
            }
        }

        logger.info("Total servers with high usage: {}", filteredServers.size());
        return filteredServers;
    }
}

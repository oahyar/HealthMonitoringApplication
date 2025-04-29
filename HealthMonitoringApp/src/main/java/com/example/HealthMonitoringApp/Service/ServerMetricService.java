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

    // Logger for recording service operations and errors
    private static final Logger logger = LoggerFactory.getLogger(ServerMetricService.class);

    // Repository for fetching raw server metric data from the database
    @Autowired
    private ServerMetricRepository serverMetricRepository;

    /**
     * Fetches aggregated disk space metrics for all hosts.
     * Converts each raw Object[] row into an AggregatedSpaceMetrics DTO.
     */
    public List<AggregatedSpaceMetrics> getAggregatedSpaceMetrics() {
        logger.info("Fetching aggregated disk space metrics...");
        // Execute the repository query that returns raw metric rows
        List<Object[]> results = serverMetricRepository.findAggregatedSpaceMetrics();
        // Prepare a list to hold the converted DTOs
        List<AggregatedSpaceMetrics> servers = new ArrayList<>();

        // Iterate over each row and convert to DTO
        for (Object[] row : results) {
            try {
                AggregatedSpaceMetrics server = new AggregatedSpaceMetrics(
                        row[0].toString(),                       // hostname
                        ((Number) row[1]).longValue(),           // total disk space
                        ((Number) row[2]).longValue(),           // total available disk
                        ((Number) row[3]).longValue(),           // total used disk
                        ((Number) row[4]).longValue()            // usage percentage
                );
                servers.add(server);
            } catch (Exception e) {
                // Log any conversion errors but continue processing other rows
                logger.error("Error processing row: {}", row, e);
            }
        }

        logger.info("Successfully retrieved {} aggregated server metrics.", servers.size());
        return servers;
    }

    /**
     * Retrieves the latest disk partition details for a given hostname.
     * Returns an empty list if no data is found.
     */
    public List<ServerDiskPartition> getDiskDetailByHostname(String hostname) {
        logger.info("Fetching disk partition details for hostname: {}", hostname);
        List<ServerDiskPartition> partitions =
                serverMetricRepository.findLatestFilesystemByHostname(hostname);

        if (partitions.isEmpty()) {
            logger.warn("No disk partition details found for hostname: {}", hostname);
        } else {
            logger.info("Retrieved {} partitions for hostname: {}", partitions.size(), hostname);
        }

        return partitions;
    }

    /**
     * Finds filesystems on the given host that exceed the configured usage threshold.
     * Returns an empty list if none are above the threshold.
     */
    public List<ServerDiskPartition> getHighUsageFilesystems(String hostname) {
        logger.info("Fetching high usage filesystems for hostname: {}", hostname);
        List<ServerDiskPartition> result =
                serverMetricRepository.findHighUsageFilesystems(hostname);

        if (result.isEmpty()) {
            logger.warn("No high usage filesystems found for hostname: {}", hostname);
        } else {
            logger.info("Found {} high usage filesystems for hostname: {}", result.size(), hostname);
        }

        return result;
    }

}

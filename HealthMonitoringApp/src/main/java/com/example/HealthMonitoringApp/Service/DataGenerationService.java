package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class DataGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(DataGenerationService.class);
    private final ServerMetricRepository serverMetricRepository;
    private final TableSpaceRepository tableSpaceRepository;
    private final Random random = new Random();

    public DataGenerationService(ServerMetricRepository serverMetricRepository, TableSpaceRepository tableSpaceRepository) {
        this.serverMetricRepository = serverMetricRepository;
        this.tableSpaceRepository = tableSpaceRepository;
    }

    @Scheduled(fixedRate = 900000) // Every 15 seconds
    public void updateMockDiskUsage() {
        logger.info("Updating mock disk space data...");

        List<ServerDiskPartition> existingDisks = serverMetricRepository.findAll();
        if (existingDisks.isEmpty()) {
            logger.warn("No disk partitions found in the database.");
            return;
        }

        for (ServerDiskPartition disk : existingDisks) {
            try {
                long totalSpace = disk.getSizeMb();
                long currentUsed = disk.getUsedMb();

                // Apply a small variation (±5% of current usage)
                long variation = (long) (currentUsed * (0.05 * (random.nextDouble() * 2 - 1))); // ±5%
                long newUsedSpace = Math.max(0, Math.min(totalSpace, currentUsed + variation));
                long newAvailableSpace = totalSpace - newUsedSpace;

                disk.setUsedMb(newUsedSpace);
                disk.setAvailableMb(newAvailableSpace);
                disk.setUsagePct((int) ((newUsedSpace * 100) / totalSpace));
                disk.setTimestamp(LocalDateTime.now());

                serverMetricRepository.save(disk);
            } catch (Exception e) {
                logger.error("Error updating disk partition for {}: {}", disk.getHostname(), e.getMessage(), e);
            }
        }

        logger.info("Mock disk space data updated successfully!");
    }

    @Scheduled(fixedRate = 900000) // Every 15 seconds
    public void updateMockTableSpaceUsage() {
        logger.info("Updating mock tablespace data...");

        List<TableSpace> existingTablespaces = tableSpaceRepository.findAll();
        if (existingTablespaces.isEmpty()) {
            logger.warn("No tablespaces found in the database.");
            return;
        }

        for (TableSpace tablespace : existingTablespaces) {
            try {
                long currentUsed = tablespace.getUsedSpaceMb();
                long currentFree = tablespace.getFreeSpaceMb();
                long totalSpace = currentUsed + currentFree;

                // Apply a small variation (±5% of current usage)
                long variation = (long) (currentUsed * (0.05 * (random.nextDouble() * 2 - 1))); // ±5%
                long newUsedSpace = Math.max(0, Math.min(totalSpace, currentUsed + variation));
                long newFreeSpace = totalSpace - newUsedSpace;

                tablespace.setUsedSpaceMb(newUsedSpace);
                tablespace.setFreeSpaceMb(newFreeSpace);
                tablespace.setTotalSpaceMb(totalSpace);
                tablespace.setUsagePct( ((newUsedSpace * 100) / totalSpace));

                tableSpaceRepository.save(tablespace);
            } catch (Exception e) {
                logger.error("Error updating tablespace {} - {}: {}", tablespace.getHostname(), tablespace.getSid(), e.getMessage(), e);
            }
        }

        logger.info("Mock tablespace data updated successfully!");
    }
}

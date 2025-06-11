package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@EnableScheduling
public class DbCleanupService {
    private final ApiStatusLogRepository apiRepo;
    private final JobLogRepository jobRepo;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DbCleanupService(ApiStatusLogRepository apiRepo,
                             JobLogRepository jobRepo) {
        this.apiRepo = apiRepo;
        this.jobRepo = jobRepo;
    }

    /**
     * Purge old logs immediately on startup, and then once every 24h.
     */
    @Scheduled(
            initialDelay = 0,            // run once immediately
            fixedRate    = 24 * 60 * 60 * 1000  // then every 24h in milliseconds
    )
    @Transactional
    public void purgeOldLogs() {
        Instant apiCutoff      = Instant.now().minus(14, ChronoUnit.DAYS);
        LocalDateTime jobCutoff = LocalDateTime.now().minusDays(14);

        int removedApis = apiRepo.deleteByTimestampBefore(apiCutoff);
        int removedJobs = jobRepo.deleteByStartTimeBefore(jobCutoff);

        log.info("Purged {} API logs and {} job logs older than 14 days",
                removedApis, removedJobs);
    }
}

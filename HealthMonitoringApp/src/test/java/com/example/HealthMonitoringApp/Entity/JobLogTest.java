package com.example.HealthMonitoringApp.Entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class JobLogTest {

    @Test
    public void testGettersAndSetters() {
        JobLog log = new JobLog();
        Long id = 1L;
        String jobName = "DiskCheckJob";
        String status = "SUCCESS";
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now();
        String message = "Job executed successfully.";

        log.setId(id);
        log.setJobName(jobName);
        log.setStatus(status);
        log.setStartTime(start);
        log.setEndTime(end);
        log.setMessage(message);

        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getJobName()).isEqualTo(jobName);
        assertThat(log.getStatus()).isEqualTo(status);
        assertThat(log.getStartTime()).isEqualTo(start);
        assertThat(log.getEndTime()).isEqualTo(end);
        assertThat(log.getMessage()).isEqualTo(message);
    }
}
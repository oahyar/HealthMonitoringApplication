package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobMonitorService {

    @Autowired
    private JobLogRepository jobLogRepository;

    private final ObjectProvider<Scheduler> schedulerProvider;

    public JobMonitorService(ObjectProvider<Scheduler> schedulerProvider) {
        this.schedulerProvider = schedulerProvider;
    }

    public String getJobStatus(String jobName, String group) throws SchedulerException {
        Scheduler scheduler = schedulerProvider.getIfAvailable();

        if (scheduler == null) {
            return "SCHEDULER_UNAVAILABLE";
        }

        JobKey jobKey = new JobKey(jobName, group);

        if (!scheduler.checkExists(jobKey)) {
            return "NOT_FOUND";
        }

        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.isEmpty()) {
            return "NO_TRIGGER";
        }

        Trigger.TriggerState triggerState = scheduler.getTriggerState(triggers.get(0).getKey());
        return triggerState.name();
    }

    // Clean up job db every 14 days
    public int cleanOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(14);
        return jobLogRepository.deleteByStartTimeBefore(cutoff);
    }
}


package com.example.HealthMonitoringApp.Wiremock.scheduler;

import com.example.HealthMonitoringApp.Service.ApiHealthService;
import com.example.HealthMonitoringApp.Wiremock.properties.MonitorProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class ScheduledApiChecks {

    private final ApiHealthService health;
    private final MonitorProperties props;

    public ScheduledApiChecks(ApiHealthService health,
                              MonitorProperties props) {
        this.health = health;
        this.props  = props;
    }

    /**
     * Runs every X milliseconds (from monitor.rateMillis)
     * and invokes health.check(name, url) for each entry.
     */
    @Scheduled(fixedRateString = "#{@monitorProperties.rateMillis}")
    public void runAllChecks() {
        props.getEndpoints().forEach(health::check);
    }
}

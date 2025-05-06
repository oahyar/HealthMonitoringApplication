package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ApiStatusLog;
import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

@Service
public class ApiHealthService {

    private static final Logger log = LoggerFactory.getLogger(ApiHealthService.class);
    private final RestTemplate rest;
    private final ApiStatusLogRepository apiStatusLogRepository;

    public ApiHealthService(RestTemplateBuilder builder,
                            ApiStatusLogRepository apiStatusLogRepository) {
        // configure timeouts so your “timeout” stub actually fails
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.apiStatusLogRepository = apiStatusLogRepository;
    }

    /**
     * Pings the given URL, measures up/down and latency,
     * then persists an ApiStatusLog.
     */
    public void check(String apiName, String url) {
        Instant start = Instant.now();
        boolean up;
        String msg = null;

        try {
            ResponseEntity<String> resp = rest.getForEntity(url, String.class);
            up = resp.getStatusCode().is2xxSuccessful();
            if (!up) {
                msg = "HTTP " + resp.getStatusCodeValue();
            }
        } catch (RestClientException e) {
            up = false;
            msg = e.getMessage();   // record the exception message
        }
        long latency = Duration.between(start, Instant.now()).toMillis();

        ApiStatusLog statusLog = new ApiStatusLog();
        statusLog.setApiName(apiName);
        statusLog.setTimestamp(Instant.now());
        statusLog.setUp(up);
        statusLog.setLatencyMillis(latency);
        statusLog.setMessage(msg);   // save the error/HTTP code
        apiStatusLogRepository.save(statusLog);

        log.info("Saved {} → up={} ({}ms) msg={}", apiName, up, latency, msg);
    }
}
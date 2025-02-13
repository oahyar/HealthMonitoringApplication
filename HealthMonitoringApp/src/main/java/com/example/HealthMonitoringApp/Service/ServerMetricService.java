package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerMetricService {

    @Autowired
    private ServerMetricRepository serverMetricRepository;

    public List<ServerMetric> getAllMetrics() {
        return serverMetricRepository.findAll();
    }

}

package com.example.HealthMonitoringApp;

import com.example.HealthMonitoringApp.Service.ServerMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class HealthMonitoringAppApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(HealthMonitoringAppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
    }


}

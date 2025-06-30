package com.example.HealthMonitoringApp;

import com.example.HealthMonitoringApp.Service.ServerMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

import static com.example.HealthMonitoringApp.CpuDuringLoadTest.startCpuLogging;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class HealthMonitoringAppApplication implements CommandLineRunner {

    public static void main(String[] args) {

        SpringApplication.run(HealthMonitoringAppApplication.class, args);

//        new Thread(() -> CpuDuringLoadTest.startCpuLogging(180, "cpu_log.csv")).start();
//
//        try {
//            Process process = new ProcessBuilder("k6", "run", "C:\\Users\\oahya\\Documents\\Y3T2\\CAPSTONE\\HealthMonitoringApplication\\HealthMonitoringApplication\\HealthMonitoringApp\\src\\test\\loadtest\\load-test-api.js")
//                    .inheritIO().start();
//            process.waitFor();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void run(String... args) throws Exception {
    }


}

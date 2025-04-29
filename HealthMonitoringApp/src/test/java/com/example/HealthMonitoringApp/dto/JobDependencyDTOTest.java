package com.example.HealthMonitoringApp.dto;

import com.example.HealthMonitoringApp.dto.JobDependencyDTO.Data;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JobDependencyDTOTest {

    @Test
    public void testGettersAndSetters() {
        JobDependencyDTO dto = new JobDependencyDTO();
        Data data = new Data();

        data.setId("job1");
        data.setLabel("Job 1");
        data.setStatus("SUCCESS");
        data.setSource("job0");
        data.setTarget("job2");
        data.setMessage("Job completed successfully.");

        dto.setData(data);

        assertThat(dto.getData()).isNotNull();
        assertThat(dto.getData().getId()).isEqualTo("job1");
        assertThat(dto.getData().getLabel()).isEqualTo("Job 1");
        assertThat(dto.getData().getStatus()).isEqualTo("SUCCESS");
        assertThat(dto.getData().getSource()).isEqualTo("job0");
        assertThat(dto.getData().getTarget()).isEqualTo("job2");
        assertThat(dto.getData().getMessage()).isEqualTo("Job completed successfully.");
    }
}

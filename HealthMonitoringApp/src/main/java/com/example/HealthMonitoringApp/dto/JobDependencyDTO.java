package com.example.HealthMonitoringApp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public class JobDependencyDTO {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        private String id;      // for nodes
        private String label;   // optional: for node label display
        private String status;  // for coloring nodes

        private String source;  // for edges
        private String target;  // for edges

        private String message;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

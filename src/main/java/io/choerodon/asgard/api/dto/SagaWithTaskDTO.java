package io.choerodon.asgard.api.dto;

import java.util.List;

public class SagaWithTaskDTO {

    private Long id;

    private String code;

    private String description;

    private String inputSchema;

    private String service;

    private  List<List<SagaTaskDTO>> tasks;

    public SagaWithTaskDTO(Long id, String code, String description, String inputSchema, String service) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.inputSchema = inputSchema;
        this.service = service;
    }

    public SagaWithTaskDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(String inputSchema) {
        this.inputSchema = inputSchema;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<List<SagaTaskDTO>> getTasks() {
        return tasks;
    }

    public void setTasks(List<List<SagaTaskDTO>> tasks) {
        this.tasks = tasks;
    }

    public static class TaskNode {

        private String type = "singleton";

        private List<SagaTaskDTO> tasks;

        private SagaTaskDTO task;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<SagaTaskDTO> getTasks() {
            return tasks;
        }

        public void setTasks(List<SagaTaskDTO> tasks) {
            this.tasks = tasks;
        }

        public SagaTaskDTO getTask() {
            return task;
        }

        public void setTask(SagaTaskDTO task) {
            this.task = task;
        }
    }
}

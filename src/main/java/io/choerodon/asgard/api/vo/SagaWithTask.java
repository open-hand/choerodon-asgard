package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.List;

public class SagaWithTask {
    @Encrypt
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "编码")
    private String code;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "输入")
    private String input;

    @ApiModelProperty(value = "服务名")
    private String service;

    @ApiModelProperty(value = "SagaTask 列表")
    private List<List<SagaTask>> tasks;

    public SagaWithTask(Long id, String code, String description, String input, String service) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.input = input;
        this.service = service;
    }

    public SagaWithTask() {
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

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<List<SagaTask>> getTasks() {
        return tasks;
    }

    public void setTasks(List<List<SagaTask>> tasks) {
        this.tasks = tasks;
    }

    public static class TaskNode {

        private String type = "singleton";

        private List<SagaTask> tasks;

        private SagaTask task;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<SagaTask> getTasks() {
            return tasks;
        }

        public void setTasks(List<SagaTask> tasks) {
            this.tasks = tasks;
        }

        public SagaTask getTask() {
            return task;
        }

        public void setTask(SagaTask task) {
            this.task = task;
        }
    }
}

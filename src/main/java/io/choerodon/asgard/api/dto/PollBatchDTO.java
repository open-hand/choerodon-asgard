package io.choerodon.asgard.api.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class PollBatchDTO {

    @NotEmpty(message = "error.pollBatch.instanceEmpty")
    private String instance;

    /**
     * key: sagaCode
     * value: 该saga下的taskCode
     */
    @NotNull(message = "error.pollBatch.codesNull")
    private List<PollCodeDTO> codes;

    private Integer maxPollSize;

    public Integer getMaxPollSize() {
        return maxPollSize;
    }

    public void setMaxPollSize(Integer maxPollSize) {
        this.maxPollSize = maxPollSize;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<PollCodeDTO> getCodes() {
        return codes;
    }

    public void setCodes(List<PollCodeDTO> codes) {
        this.codes = codes;
    }

    @Override
    public String toString() {
        return "PollBatchDTO{" +
                "instance='" + instance + '\'' +
                ", codes=" + codes +
                ", maxPollSize=" + maxPollSize +
                '}';
    }
}

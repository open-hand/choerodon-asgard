package io.choerodon.asgard.infra.enums;

public enum TriggerType {
    SIMPLE("simple-trigger"),
    CRON("cron-trigger");

    private String value;

    TriggerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

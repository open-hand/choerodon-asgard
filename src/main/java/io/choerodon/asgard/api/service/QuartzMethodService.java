package io.choerodon.asgard.api.service;

import io.choerodon.asgard.domain.QuartzMethod;

import java.util.List;

public interface QuartzMethodService {

    void createMethodList(final String service, final List<QuartzMethod> scanMethods);

}

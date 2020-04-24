package io.choerodon.asgard.app.service;

import io.choerodon.asgard.infra.dto.QuartzMethodDTO;

import java.util.List;

public interface QuartzMethodService {

    void createMethodList(final String service, final List<QuartzMethodDTO> scanMethods);

}

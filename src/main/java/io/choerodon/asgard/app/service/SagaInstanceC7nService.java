package io.choerodon.asgard.app.service;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/4/22
 */
public interface SagaInstanceC7nService {

    List<SagaInstanceFailureVO> statisticsFailure(String level, Long sourceId, Integer date);

    Page<SagaInstanceDTO> statisticsFailureList(String level, Long sourceId, Integer date, PageRequest pageRequest);
}

package io.choerodon.asgard.app.service;

import io.choerodon.asgard.api.vo.ScheduleMethod;
import io.choerodon.asgard.api.vo.ScheduleMethodInfo;
import io.choerodon.asgard.api.vo.ScheduleMethodParams;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ScheduleMethodService {

    ResponseEntity<Page<ScheduleMethodInfo>> pageQuery(PageRequest pageRequest, String code, String service, String method, String description, String params, String level);

    List<ScheduleMethod> getMethodByService(String serviceName, String level);


    ScheduleMethodParams getParams(Long id, String level);

    Long getMethodIdByCode(String code);

    /**
     * 查询各层级拥有可执行程序的服务
     *
     * @param level 层级
     * @return 服务列表
     */
    List<String> getServices(String level);

    /**
     * 删除指定Id的可执行程序
     *
     * @param id 可执行程序Id
     */
    void delete(Long id);
}

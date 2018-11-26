package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.SystemNotificationCreateDTO;
import io.choerodon.asgard.api.dto.SystemNotificationDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author Eugen
 */
public interface SystemNocificationService {
    /**
     * 创建系统/组织公告
     *
     * @param dto    组织/系统公告创建DTO：包括公告内容、发送时间
     * @param userId 公告创建人Id
     */
    SystemNotificationDTO create(ResourceLevel level, SystemNotificationCreateDTO dto, Long userId, Long sourceId);

    /**
     * 根据taskId获得公告详情
     *
     * @param taskId 任务Id
     * @return 返回公告详情
     */
    SystemNotificationDTO getDetailById(ResourceLevel level, Long taskId, Long sourceId);

    /**
     * 分页查询公告
     *
     * @param pageRequest 分页信息
     * @param status      状态
     * @param content     内容
     * @param params      过滤params
     * @param level       层级
     * @param sourceId    对应组织Id  , site层为0
     * @return 分页结果
     */
    Page<SystemNotificationDTO> pagingAll(PageRequest pageRequest, String status, String content, String params, ResourceLevel level, long sourceId);

}

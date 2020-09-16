package io.choerodon.asgard.infra.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Param;

import io.choerodon.asgard.infra.dto.JsonDataDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface JsonDataMapper extends BaseMapper<JsonDataDTO> {
    int deleteByDate(@Param("time") Date time);
}

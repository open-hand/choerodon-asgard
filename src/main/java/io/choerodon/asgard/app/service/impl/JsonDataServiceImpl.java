package io.choerodon.asgard.app.service.impl;

import io.choerodon.asgard.app.service.JsonDataService;
import io.choerodon.asgard.infra.dto.JsonDataDTO;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.infra.utils.CommonUtils;
import io.choerodon.core.exception.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JsonDataServiceImpl implements JsonDataService {

    private JsonDataMapper jsonDataMapper;

    public JsonDataServiceImpl(JsonDataMapper jsonDataMapper) {
        this.jsonDataMapper = jsonDataMapper;
    }

    @Override
    public Long insertAndGetId(String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        String sha256 = CommonUtils.sha256(json);
        if (StringUtils.isEmpty(sha256)) {
            return null;
        }
        JsonDataDTO query = new JsonDataDTO();
        query.setSha2(sha256);
        JsonDataDTO data = jsonDataMapper.selectOne(query);
        if (data != null) {
            return data.getId();
        }
        query.setData(json);
        if (jsonDataMapper.insert(query) != 1) {
            throw new FeignException("error.sagaTaskInstanceService.insertJsonData");
        }
        return query.getId();
    }
}

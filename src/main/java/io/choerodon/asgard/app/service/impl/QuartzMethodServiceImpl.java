package io.choerodon.asgard.app.service.impl;

import io.choerodon.asgard.app.service.QuartzMethodService;
import io.choerodon.asgard.infra.dto.QuartzMethodDTO;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuartzMethodServiceImpl implements QuartzMethodService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzMethodService.class);

    private QuartzMethodMapper methodMapper;

    public QuartzMethodServiceImpl(QuartzMethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    @Override
    public void createMethodList(final String service, final List<QuartzMethodDTO> scanMethods) {
        QuartzMethodDTO query = new QuartzMethodDTO();
        query.setService(service);
        List<QuartzMethodDTO> dbMethods = methodMapper.select(query);
        scanMethods.forEach(i -> {
            QuartzMethodDTO dbSameMethod = findByMethodName(dbMethods, i.getMethod());
            if (dbSameMethod == null) {
                if (methodMapper.insertSelective(i) != 1) {
                    LOGGER.warn("error.createMethodList.insert, quartzMethod: {}", i);
                }
            } else {
                i.setId(dbSameMethod.getId());
                i.setObjectVersionNumber(dbSameMethod.getObjectVersionNumber());
                methodMapper.updateByPrimaryKeySelective(i);
            }
        });
        dbMethods.stream().filter(i -> findByMethodName(scanMethods, i.getMethod()) == null)
                .forEach(t -> {
                    if (methodMapper.deleteByPrimaryKey(t.getId()) != 1) {
                        LOGGER.warn("error.createMethodList.delete, quartzMethod: {}", t);
                    }
                });
    }

    private QuartzMethodDTO findByMethodName(final List<QuartzMethodDTO> methods, final String methodName) {
        for (QuartzMethodDTO method : methods) {
            if (method.getMethod().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}

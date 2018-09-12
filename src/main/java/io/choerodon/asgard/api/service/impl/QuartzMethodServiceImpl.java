package io.choerodon.asgard.api.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.api.service.QuartzMethodService;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;

@Service
public class QuartzMethodServiceImpl implements QuartzMethodService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzMethodService.class);

    private QuartzMethodMapper methodMapper;

    public QuartzMethodServiceImpl(QuartzMethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    @Override
    public void createMethodList(final String service, final List<QuartzMethod> scanMethods) {
        QuartzMethod query = new QuartzMethod();
        query.setService(service);
        List<QuartzMethod> dbMethods = methodMapper.select(query);
        scanMethods.forEach(i -> {
            QuartzMethod dbSameMethod = findByMethodName(dbMethods, i.getMethod());
            if (dbSameMethod == null) {
                if (methodMapper.insertSelective(i) != 1) {
                    LOGGER.warn("error.createMethodList.insert, quartzMethod: {}", i);
                }
            } else {
                i.setId(dbSameMethod.getId());
                i.setCode(dbSameMethod.getCode());
                i.setDescription(dbSameMethod.getDescription());
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

    private QuartzMethod findByMethodName(final List<QuartzMethod> methods, final String methodName) {
        for (QuartzMethod method : methods) {
            if (method.getMethod().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}

package io.choerodon.asgard.infra.config;

import io.choerodon.core.swagger.ChoerodonRouteData;
import io.choerodon.swagger.annotation.ChoerodonExtraData;
import io.choerodon.swagger.swagger.extra.ExtraData;
import io.choerodon.swagger.swagger.extra.ExtraDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@ChoerodonExtraData
public class HagdExtraDataManager implements ExtraDataManager {
    @Autowired
    private Environment environment;

    public HagdExtraDataManager() {
    }

    public ExtraData getData() {
        ChoerodonRouteData choerodonRouteData = new ChoerodonRouteData();
        choerodonRouteData.setName(this.environment.getProperty("hzero.service.current.name", "hzero-asgard"));
        choerodonRouteData.setPath(this.environment.getProperty("hzero.service.current.path", "/hagd/**"));
        choerodonRouteData.setServiceId(this.environment.getProperty("hzero.service.current.service-name", "${hzero.service.iam.name:hzero-asgard}"));
        choerodonRouteData.setPackages("org.hzero.asgard");
        extraData.put("choerodon_route", choerodonRouteData);
        return extraData;
    }
}

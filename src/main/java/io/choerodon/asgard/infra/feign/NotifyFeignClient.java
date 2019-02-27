package io.choerodon.asgard.infra.feign;

import io.choerodon.asgard.infra.feign.fallback.NotifyFeignClientFallback;
import io.choerodon.core.notify.NoticeSendDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "notify-service", path = "/v1/notices", fallback = NotifyFeignClientFallback.class)
public interface NotifyFeignClient {

    @PostMapping
    void postNotice(@RequestBody @Valid NoticeSendDTO dto);

}
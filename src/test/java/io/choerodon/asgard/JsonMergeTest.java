package io.choerodon.asgard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.JsonMergeDTO;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class JsonMergeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void merge() throws Exception {
        List<String> list = Arrays.asList("hhshhd", "sdkkskksd", "sdkskdkskd", "wjjsjd");
        String data = objectMapper.writeValueAsString(list);
        System.out.println(data);
        JsonMergeDTO iam = new JsonMergeDTO("iam", data);
        JsonMergeDTO dev = new JsonMergeDTO("dev", data);
        String result = ConvertUtils.jsonMerge(Arrays.asList(iam, dev), objectMapper);
        System.out.println(result);
        System.out.println(ConvertUtils.jsonMerge(Arrays.asList(iam), objectMapper));

    }

}

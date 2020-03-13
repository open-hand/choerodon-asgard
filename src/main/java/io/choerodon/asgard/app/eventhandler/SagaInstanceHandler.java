package io.choerodon.asgard.app.eventhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by hailuo.liu@choerodon.io on 2019/5/15.
 */
public class SagaInstanceHandler{
    private static final Logger LOGGER = LoggerFactory.getLogger(SagaInstanceHandler.class);

    private ConcurrentMap<String , ArrayList<DeferredResult>> requestMap= new ConcurrentHashMap<>();

    public void addDeferredResult(String type,String service,DeferredResult deferredResult){
        String key = SagaInstanceEventPublisher.getMessageKey(type,service);
        if(requestMap.containsKey(key)){
            ArrayList<DeferredResult> deferredResults = requestMap.get(key);
            deferredResults.add(deferredResult);
        }else{
            ArrayList<DeferredResult> deferredResults = new ArrayList();
            deferredResults.add(deferredResult);
            requestMap.put(key,deferredResults);
        }
    }

    public void removeDeferredResult(String type,String service,DeferredResult deferredResult){
        String key = SagaInstanceEventPublisher.getMessageKey(type,service);
        if(requestMap.containsKey(key)){
            ArrayList<DeferredResult> deferredResults = requestMap.get(key);
            deferredResults.remove(deferredResult);
            if(deferredResults.isEmpty()){
                requestMap.remove(key);
            }
        }
    }

    public void onMessage(String typeAndService) {
        LOGGER.info(typeAndService);
        if(requestMap.containsKey(typeAndService)){
            ArrayList<DeferredResult> deferredResults = requestMap.get(typeAndService);
            for(DeferredResult deferredResult:deferredResults){
                deferredResult.setResult(new ResponseEntity<>(ConcurrentHashMap.newKeySet(), HttpStatus.OK));
            }
            requestMap.remove(typeAndService);
        }
    }
}

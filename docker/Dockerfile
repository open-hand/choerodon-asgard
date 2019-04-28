FROM registry.cn-hangzhou.aliyuncs.com/choerodon-tools/javabase:0.8.0
COPY app.jar /asgard-service.jar
CMD java $JAVA_OPTS $SKYWALKING_OPTS -jar /asgard-service.jar

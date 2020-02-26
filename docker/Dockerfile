FROM registry.cn-shanghai.aliyuncs.com/c7n/javabase:0.9.0
COPY app.jar /asgard-service.jar
RUN chown -R www-data:www-data /asgard-service.jar
USER www-data
CMD java $JAVA_OPTS $SKYWALKING_OPTS -jar /asgard-service.jar
- @Saga和@SagaTask
  1. 服务启动时扫描`@Saga`和`@SagaTask`注解
  2. @Saga的code唯一，@SagaTask和SagaCode和code共同唯一
  3. @Saga不存在则插入，插入时保存所在服务名。若已经存在若是同一个服务则更新否则不更新
  4. @SagaTask不存在则插入，插入时保存所在服务名。若已经存在若是同一个服务则更新否则不更新。
     若服务取消了@SagaTask注解，则将该SagaTask的`is_enabled`设置为`false`。
     
- SagaInstance
  1. 每次开启一个saga工作流时，创建`SagaInstance`并查询订阅该Saga的task并排序，创建多个`SagaTaskInstance`，
     将第一个`SagaTaskInstance`设置为`RUNNING`状态，其余设置为`QUEUE`状态。
  2. 每当一个`SagaTaskInstance`消费完成状态更新为`COMPLETED`, 下一个`SagaTaskInstance`状态更新为`RUNNING`, 
     只有为`RUNNING`状态时@SagaTask才可以拉取到信息去消费。当一个`SagaTaskInstance`消费失败，状态更新为`FAILED`,
     该`SagaInstance`状态更新为`FAILED`。
  3. 当`SagaInstance`的`SagaTaskInstance`全部完成，该`SagaInstance`状态更新为`COMPLETED`，并更新输出结果。

- 拉取消息
  1. 服务根据code和instance拉取，如果`SagaTaskInstance`里的`instanceLock`为null或者为`instance`则拉取，拉取之后将
     表里的`instanceLock`更新为`instance`。此段程序根据`taskCode`进行加锁，防止被重复拉取，但没有解决asgard服务多实例下拉取问题。
     
  
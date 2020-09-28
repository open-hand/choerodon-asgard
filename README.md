# choerodon-asgard
分布式事务服务

## Introduction
Choerodon Asgard Service 是一个任务调度服务，通过`saga` 实现微服务之间的数据一致性。

## Documentation
- [saga使用文档](https://choerodon.io/zh/docs/development-guide/backend/framework/saga/)
- [任务使用文档](https://choerodon.io/zh/docs/development-guide/backend/framework/job/)

## Features

- 事务定义：统一管理系统所有定义的事务
- 事务实例：统一管理事务实例执行情况
- 任务管理：统一管理任务定义与执行


## Data initialization

- 创建数据库，本地创建 `asgard_service` 数据库和默认用户，示例如下：

  ```sql
  CREATE USER 'choerodon'@'%' IDENTIFIED BY "123456";
  CREATE DATABASE asgard_service DEFAULT CHARACTER SET utf8;
  GRANT ALL PRIVILEGES ON asgard_service.* TO choerodon@'%';
  FLUSH PRIVILEGES;
  ```

- 初始化 `asgard_service` 数据库，运行项目根目录下的 `init-database.sh`，该脚本默认初始化数据库的地址为 `localhost`，若有变更需要修改脚本文件

  ```sh
  sh init-database.sh
  ```
  

## Changelog

- [更新日志](./CHANGELOG.zh-CN.md)


## Contributing

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建Issue讨论新特性或者变更。

Copyright (c) 2020-present, CHOERODON


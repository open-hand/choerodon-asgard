# @choerodon/asgard

Asgard 用于提供 Choerodon 的分布式定时任务及事务管理的前端交互支持。

基础模块包含3个层级，具体如下：

## 项目层：

* 项目设置
    * 项目事务实例

## 组织层：

* 组织设置
    * 组织事务实例

## 全局层：

* 平台统计
    * 事务定义
    * 事务实例

    
   
## 目录结构

`assets` 存放`css` 文件和`images`
`common` 存放通用配置
`components` 存放公共组件
`containers` 存放前端页面
`dashboard` 存放仪表盘
`guide` 存放新手指引
`locale` 存放多语言文件
`stores` 存放前端页面需要的store

## 依赖

* Node environment (6.9.0+)
* Git environment
* [@choerodon/boot](https://github.com/choerodon/choerodon-front-boot)
* [@choerodon/master](https://github.com/choerodon/choerodon-front-master)

## 运行

``` bash
npm install
npm start
```

启动后，打开 http://localhost:9090

## 相关技术文档

* [React](https://reactjs.org)
* [Mobx](https://github.com/mobxjs/mobx)
* [webpack](https://webpack.docschina.org)
* [gulp](https://gulpjs.com)

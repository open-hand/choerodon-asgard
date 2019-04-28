const config = {
  // use for c7n start
  server: 'http://api.staging.saas.hand-china.com',
  master: 'choerodon-front-master',
  projectType: 'choerodon',
  buildType: 'single',
  dashboard: {},
  resourcesLevel: ['site', 'origanization', 'project', 'user'],
};

module.exports = config;

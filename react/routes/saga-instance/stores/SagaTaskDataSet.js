import { DataSet } from 'choerodon-ui/pro';

export default ({ id = 0, apiGetway, intl, intlPrefix }) => {
  const taskInstanceCode = intl.formatMessage({ id: 'global.saga.task.code' });
  const status = intl.formatMessage({ id: `${intlPrefix}.status` });
  const sagaInstanceCode = intl.formatMessage({ id: 'global.saga-instance.saga' });
  const description = intl.formatMessage({ id: 'description' });
  const plannedStartTime = intl.formatMessage({ id: 'global.saga.task.actualstarttime' });
  const actualEndTime = intl.formatMessage({ id: 'global.saga.task.actualendtime' });
  const retryCount = intl.formatMessage({ id: 'saga-instance.task.retry-count' });
  const statusDataSet = new DataSet({
    data: [{
      value: 'RUNNING',
      meaning: '运行中',
    }, {
      value: 'FAILED',
      meaning: '失败',
    }, {
      value: 'COMPLETED' || 'NON_CONSUMER',
      meaning: '完成',
    }, {
      value: 'WAIT_TO_BE_PULLED',
      meaning: '等待被拉取',
    }],
  });
  
  return {
    autoQuery: true,
    selection: false,
    transport: {
      read: {
        url: `${apiGetway}tasks/instances/list`,
        method: 'post',
      },
    },
    fields: [
      { name: 'taskInstanceCode', type: 'string', label: taskInstanceCode },
      { name: 'status', type: 'string', label: status },
      { name: 'sagaInstanceCode', type: 'string', label: sagaInstanceCode },      
      { name: 'description', type: 'string', label: description },      
      { name: 'plannedStartTime', type: 'string', label: plannedStartTime },      
      { name: 'actualEndTime', type: 'string', label: actualEndTime },      
      { name: 'retryCount', type: 'string', label: retryCount },      
    ],    
    queryFields: [
      { name: 'taskInstanceCode', type: 'string', label: taskInstanceCode },
      { name: 'status', type: 'string', label: status, options: statusDataSet },
      { name: 'sagaInstanceCode', type: 'string', label: sagaInstanceCode },  
    ],
  };
};

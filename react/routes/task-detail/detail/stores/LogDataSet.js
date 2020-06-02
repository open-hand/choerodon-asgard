import { DataSet } from 'choerodon-ui/pro';

export default ({ id = 0, taskId, levelType, intl, intlPrefix }) => {
  const status = intl.formatMessage({ id: 'status' });
  const serviceInstanceId = intl.formatMessage({ id: `${intlPrefix}.instance.id` });
  const plannedStartTime = intl.formatMessage({ id: `${intlPrefix}.plan.execution.time` });
  const actualStartTime = intl.formatMessage({ id: `${intlPrefix}.actual.execution.time` });


  const statusDataSet = new DataSet({
    data: [{
      value: 'RUNNING',
      meaning: '进行中',
    }, {
      value: 'FAILED',
      meaning: '失败',
    }, {
      value: 'COMPLETED',
      meaning: '完成',
    }],
  });

  return {
    autoQuery: true,
    selection: false,
    transport: {
      read: {
        url: `/hagd/v1/schedules${levelType}/tasks/instances/${taskId}`,
        method: 'get',
      },
    },
    fields: [
      { name: 'status', type: 'string', label: status },
      { name: 'serviceInstanceId', type: 'string', label: serviceInstanceId },
      { name: 'plannedStartTime', type: 'string', label: plannedStartTime },
      { name: 'actualStartTime', type: 'string', label: actualStartTime },
    ],
    queryFields: [
      { name: 'status', type: 'string', label: status, options: statusDataSet },
      { name: 'serviceInstanceId', type: 'string', label: serviceInstanceId },
    ],
  };
};

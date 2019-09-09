import { DataSet } from 'choerodon-ui/pro';

export default ({ id = 0, levelType, intl, intlPrefix }) => {
  const name = intl.formatMessage({ id: 'name' });
  const description = intl.formatMessage({ id: 'description' });
  const lastExecTime = intl.formatMessage({ id: `${intlPrefix}.last.execution.time` });
  const nextExecTime = intl.formatMessage({ id: `${intlPrefix}.next.execution.time` });
  const status = intl.formatMessage({ id: 'status' });

  const statusDataSet = new DataSet({
    data: [{
      value: 'ENABLE',
      meaning: intl.formatMessage({ id: 'enable' }),
    }, {
      value: 'DISABLE',
      meaning: intl.formatMessage({ id: 'disable' }),
    }, {
      value: 'FINISHED',
      meaning: intl.formatMessage({ id: 'finished' }),
    }],
  });
  
  return {
    autoQuery: true,
    selection: false,
    transport: {
      read: {
        url: `asgard/v1/schedules${levelType}/tasks/list`,
        method: 'post',
      },
    },
    fields: [
      { name: 'name', type: 'string', label: name },
      { name: 'description', type: 'string', label: description },
      { name: 'lastExecTime', type: 'string', label: lastExecTime },      
      { name: 'nextExecTime', type: 'string', label: nextExecTime },      
      { name: 'status', type: 'string', label: status },  
    ],    
    queryFields: [
      { name: 'name', type: 'string', label: name },
      { name: 'description', type: 'string', label: description },
      { name: 'status', type: 'string', label: status, options: statusDataSet },     
    ],
  };
};

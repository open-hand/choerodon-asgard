import { DataSet } from 'choerodon-ui/pro';

export default ({ id = 0, apiGetway, intl, intlPrefix }) => {
  const sagaCode = intl.formatMessage({ id: 'saga-instance.saga.instance' });
  const status = intl.formatMessage({ id: `${intlPrefix}.status` });
  const startTime = intl.formatMessage({ id: `${intlPrefix}.start.time` });
  const refType = intl.formatMessage({ id: `${intlPrefix}.reftype` });
  const refId = intl.formatMessage({ id: `${intlPrefix}.refid` });
  const progress = intl.formatMessage({ id: `${intlPrefix}.progress` });
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
    }],
  });
  
  return {
    autoQuery: true,
    selection: false,
    transport: {
      read: {
        url: `${apiGetway}instances`,
        method: 'get',
      },
    },
    fields: [
      { name: 'sagaCode', type: 'string', label: sagaCode },
      { name: 'status', type: 'string', label: status },
      { name: 'startTime', type: 'string', label: startTime },      
      { name: 'refType', type: 'string', label: refType },      
      { name: 'refId', type: 'string', label: refId },      
      { name: 'progress', type: 'string', label: progress },      
    ],    
    queryFields: [
      { name: 'sagaCode', type: 'string', label: sagaCode },
      { name: 'status', type: 'string', label: status, options: statusDataSet },
      { name: 'refType', type: 'string', label: refType },      
      { name: 'refId', type: 'string', label: refId },      
    ],
  };
};

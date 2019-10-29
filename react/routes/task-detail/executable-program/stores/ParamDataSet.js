export default function ({ methodDataSet, levelType }) {
  return {
    selection: false,
    autoQuery: false,
    paging: false,
    dataKey: 'paramsList',
    transport: {
      read: () => ({
        url: `/asgard/v1/schedules${levelType}/methods/${methodDataSet.current && methodDataSet.current.get('id')}`,
        method: 'get',
      }),
    },
    fields: [
      { name: 'name', type: 'string', label: '参数名称' },
      { name: 'description', type: 'string', label: '参数描述' },
      { name: 'type', type: 'string', label: '参数类型' },
      { name: 'defaultValue', type: 'string', label: '默认值' },
    ],
  };
}

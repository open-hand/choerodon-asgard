export default function ({ taskCreateDataSet, levelType }) {
  return {
    selection: false,
    autoQuery: false,
    paging: false,
    dataKey: 'paramsList',
    transport: {
      read: () => ({
        url: `/asgard/v1/schedules${levelType}/methods/${taskCreateDataSet.current.get('methodId')}`,
        method: 'get',
      }),
    },
    fields: [
      { name: 'name', type: 'string', label: '参数名称' },
      { name: 'defaultValue', type: 'string', label: '参数值' },
      { name: 'type', type: 'string', label: '参数类型' },
      { name: 'description', type: 'string', label: '参数描述' },
    ],
  };
}

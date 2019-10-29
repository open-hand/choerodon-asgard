export default function ({ type, id }) {
  function getQueryUrl() {
    switch (type) {
      case 'site':
        return `/asgard/v1/schedules/methods?level=${type}&size=999`;
      case 'organization':
        return `/asgard/v1/schedules/organizations/${id}/methods?size=999`;
      default:
        return `/asgard/v1/schedules/methods?level=${type}&size=999`;
    }
  }
  return {
    selection: false,
    autoQuery: true,
    paging: false,
    transport: {
      read: {
        url: getQueryUrl(),
        method: 'get',
      },
    },
    fields: [
      { name: 'code', type: 'string', label: '类编码' },
      { name: 'service', type: 'string', label: '所属微服务' },
      { name: 'method', type: 'string', label: '类名' },
      { name: 'description', type: 'string', label: '描述' },
      { name: 'onlineInstanceNum', type: 'string', label: '在线实例数' },
    ],
  };
}


export default ({ intl, intlPrefix }) => {
  const code = intl.formatMessage({ id: `${intlPrefix}.code` });
  const service = intl.formatMessage({ id: `${intlPrefix}.service` });
  const description = intl.formatMessage({ id: `${intlPrefix}.desc` });
  return {
    autoQuery: true,
    selection: false,
    transport: {
      read: {
        url: '/asgard/v1/sagas/list',
        method: 'post',
      },
    },
    fields: [
      { name: 'code', type: 'string', label: code },
      { name: 'service', type: 'string', label: service },
      { name: 'description', type: 'string', label: description },      
    ],
    queryFields: [
      { name: 'code', type: 'string', label: code },
      { name: 'service', type: 'string', label: service },
      { name: 'description', type: 'string', label: description },      
    ],
  };
};

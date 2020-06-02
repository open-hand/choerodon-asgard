export default function ({ type, id }) {
  function getQueryUrl() {
    switch (type) {
      case 'site':
        return `/hagd/v1/schedules/methods?level=${type}&size=999`;
      case 'organization':
        return `/hagd/v1/schedules/organizations/${id}/methods?size=999`;
      default:
        return `/hagd/v1/schedules/methods?level=${type}&size=999`;
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
  };
}

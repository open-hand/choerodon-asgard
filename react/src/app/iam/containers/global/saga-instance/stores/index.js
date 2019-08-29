import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import SagaInstanceDataSet from './SagaInstanceDataSet';
import SagaTaskDataSet from './SagaTaskDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId } }, intl, children } = props;
    let apiGetway = `/asgard/v1/sagas/${type}s/${id}/`;
    let codePrefix;
    switch (type) {
      case 'organization':
        codePrefix = 'organization';
        break;
      case 'project':
        codePrefix = 'project';
        break;
      case 'site':
        codePrefix = 'global';
        apiGetway = '/asgard/v1/sagas/';
        break;
      default:
        break;
    }
    const code = `${codePrefix}.saga-instance`;
    const intlPrefix = 'global.saga-instance';
    const instanceDataSet = useMemo(() => new DataSet(SagaInstanceDataSet({ id, apiGetway, intl, intlPrefix })), [id]);
    const taskDataSet = useMemo(() => new DataSet(SagaTaskDataSet({ id, apiGetway, intl, intlPrefix })), [id]);
    const value = {
      ...props,
      instanceDataSet,
      taskDataSet,
      apiGetway,
      code,
      prefixCls: 'c7n-saga',
      intlPrefix,
      organizationId,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));

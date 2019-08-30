import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import SagaDataSet from './SagaDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId } }, intl, children } = props;
    const intlPrefix = 'global.saga';
    const dataSet = useMemo(() => new DataSet(SagaDataSet({ id, intl, intlPrefix })), [id]);
    const value = {
      ...props,
      dataSet,
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

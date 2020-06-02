import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { axios } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import SagaDataSet from './SagaDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(
  (props) => {
    const { intl, children } = props;
    const intlPrefix = 'global.saga';
    const dataSet = useMemo(() => new DataSet(SagaDataSet({ intl, intlPrefix })), []);
    /**
    * detail data
    * 详情页数据
    * @param id
    */
    const loadDetailData = (id) => axios.get(`/hagd/v1/sagas/${id}`);
    const value = {
      ...props,
      dataSet,
      loadDetailData,
      prefixCls: 'c7n-saga',
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
);

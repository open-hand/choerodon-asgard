/* eslint-disable no-shadow */
import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { axios } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import SagaTaskDataSet from './TaskDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId } }, intl, children } = props;
    
    const taskdetail = {
      type, id,
    };
    const intlPrefix = 'taskdetail';  
    const levelType = type === 'site' ? '' : `/${type}s/${id}`;  
    const taskDataSet = useMemo(() => new DataSet(SagaTaskDataSet({ id, intl, levelType, intlPrefix })), [id]);

    const value = {
      ...props,
      taskDataSet,
      taskdetail,   
      prefixCls: 'c7n-saga',
      levelType,
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

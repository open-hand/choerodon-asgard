/* eslint-disable no-shadow */
import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { axios } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import LogDataSet from './LogDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId } }, intl, children, taskId } = props;
    
    const taskdetail = {
      type, id,
    };
    const intlPrefix = 'taskdetail';  
    const levelType = type === 'site' ? '' : `/${type}s/${id}`;  
    const logDataSet = useMemo(() => new DataSet(LogDataSet({ id, intl, levelType, taskId, intlPrefix })), [id]);

    const value = {
      ...props,
      logDataSet,
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

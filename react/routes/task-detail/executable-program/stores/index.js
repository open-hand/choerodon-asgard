import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import MethodDataSet from './MethodDataSet';
import ParamDataSet from './ParamDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId } }, children } = props;
    const levelType = type === 'site' ? '' : `/${type}s/${id}`;
    const methodDataSet = useMemo(() => new DataSet(MethodDataSet({ type, id, levelType })), [type, id]);
    const paramDataSet = useMemo(() => new DataSet(ParamDataSet({ methodDataSet, levelType })), [type, id]);

    const value = {
      ...props,
      id,
      type,
      methodDataSet,
      paramDataSet,
      levelType,
      organizationId,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));

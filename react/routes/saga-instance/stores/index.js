/* eslint-disable no-shadow */
import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { axios } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import SagaInstanceDataSet from './SagaInstanceDataSet';
import SagaTaskDataSet from './SagaTaskDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId } }, intl, children } = props;
    let apiGetway = `/hagd/v1/sagas/${type}s/${id}/`;
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
        apiGetway = '/hagd/v1/sagas/';
        break;
      default:
        break;
    }
    const code = `${codePrefix}.saga-instance`;
    const intlPrefix = 'global.saga-instance';
    const instanceDataSet = useMemo(() => new DataSet(SagaInstanceDataSet({ id, apiGetway, intl, intlPrefix })), [id]);
    const taskDataSet = useMemo(() => new DataSet(SagaTaskDataSet({ id, apiGetway, intl, intlPrefix })), [id]);
    /**
    * 重试
    * @param id
    * @returns {IDBRequest | Promise<void>}
    */
    function retry(id) {
      switch (type) {
        case 'organization':
          return axios.put(`/iam/choerodon/v1/organization/${organizationId}/${id}/org/retry`);
        case 'project':
          return axios.put(`${apiGetway}tasks/instances/${id}/retry`);
        case 'site':
          return axios.put(`/iam/choerodon/v1/site/0/${id}/site/retry`);
        default:
          break;
      }
    }

    /**
     * 解锁
     */
    function unLock(id) {
      return axios.put(`${apiGetway}tasks/instances/${id}/unlock`);
    }

    /**
     * 强制失败
     * @param id
     */
    function abort(id) {
      return axios.put(`${apiGetway}tasks/instances/${id}/failed`);
    }
    /**
    * 详情
    * @param id
    */
    function loadDetailData(id) {
      return axios.get(`${apiGetway}instances/${id}`);
    }
    const value = {
      ...props,
      instanceDataSet,
      taskDataSet,
      apiGetway,
      code,
      abort,
      unLock,
      retry,
      loadDetailData,
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

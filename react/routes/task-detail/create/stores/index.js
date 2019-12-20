/* eslint-disable no-shadow */
import React, { createContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import MethodDataSet from './MethodDataSet';
import TaskCreateDataSet from './TaskCreateDataSet';
import ParamDataSet from './ParamDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id, organizationId } }, intl, children } = props;
    const dsStore = useMemo(() => [], []);
    const levelType = type === 'site' ? '' : `/${type}s/${id}`;
    const triggerType = [
      { meaning: '简单任务', value: 'simple-trigger' },
      { meaning: 'Cron任务', value: 'cron-trigger' },
    ];
    const simpleRepeatIntervalUnit = [
      { meaning: '秒', value: 'SECONDS' },
      { meaning: '分', value: 'MINUTES' },
      { meaning: '时', value: 'HOURS' },
      { meaning: '天', value: 'DAYS' },
    ];
    const executeStrategy = [
      { meaning: '阻塞', value: 'STOP' },
      { meaning: '串行', value: 'SERIAL' },
      { meaning: '并行', value: 'PARALLEL' },
    ];
    const notifyUser = [
      { meaning: `${type === 'site' ? '平台管理员' : '组织管理员'}`, value: 'administrator' },
      { meaning: '创建者', value: 'creator' },
      { meaning: '指定用户', value: 'assigner' },
    ];
    const methodDataSet = useMemo(() => new DataSet(MethodDataSet({ type, id, levelType })), [type, id]);
    const taskCreateDataSet = useMemo(() => new DataSet(TaskCreateDataSet({ intl, methodDataSet, triggerType, simpleRepeatIntervalUnit, executeStrategy, notifyUser, id, type, levelType })), [type, id]);
    const paramDataSet = useMemo(() => new DataSet(ParamDataSet({ taskCreateDataSet, type, id, levelType })), [type, id]);
    const value = {
      ...props,
      id,
      type,
      intl,
      intlPrefix: 'taskdetail',
      methodDataSet,
      taskCreateDataSet,
      paramDataSet,
      prefixCls: 'c7n-task-create',
      levelType,
      dsStore,
      organizationId,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));

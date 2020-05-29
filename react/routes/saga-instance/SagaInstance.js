/* eslint-disable max-classes-per-file */
import React, { useState, useContext, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { axios, Breadcrumb, StatusTag, Choerodon } from '@choerodon/boot';
import { Button, Tooltip } from 'choerodon-ui';
import { withRouter } from 'react-router-dom';
import { Table, Modal } from 'choerodon-ui/pro';
import { Content, Page } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';
import MouseOverWrapper from '../../components/mouseOverWrapper';
import './style/saga-instance.scss';
import Store, { StoreProvider } from './stores';
import SagaImg from '../saga/SagaImg';

const { Column } = Table;
const modalKey = Modal.key();
const SagaInstance = withRouter(observer((props) => {
  const { instanceDataSet, intl, taskDataSet, intlPrefix, apiGetway, abort, unLock, retry, loadDetailData, AppState } = useContext(Store);
  const [activeTab, setActiveTab] = useState('instance');
  const [statistics, setStatistics] = useState({
    COMPLETED_COUNT: 0,
    FAILED_COUNT: 0,
    RUNNING_COUNT: 0,
    ROLLBACK_COUNT: 0,
  });

  const init = async () => {
    const { search } = props.location;
    if (search.split('?')[1].split('&').find((i) => i.includes('sagaCode'))) {
      const sagaId = search.split('?')[1].split('&').find((i) => i.includes('sagaCode')).split('=')[1];
      await instanceDataSet.query();
      if (instanceDataSet.length) {
        instanceDataSet.queryDataSet.current.set('params', sagaId);
        instanceDataSet.query();
      }
    } else {
      instanceDataSet.query();
    }
  };

  useEffect(() => {
    init();
  }, []);

  useEffect(() => {
    const loadStatistics = async () => {
      const data = await axios.get(`${apiGetway}instances/statistics`);
      setStatistics(data);
    };
    loadStatistics();
  }, [activeTab]);

  const loadTaskData = () => {
    if (activeTab === 'task') {
      return;
    }
    setActiveTab('task');
    taskDataSet.query();
  };

  const loadAllData = () => {
    if (activeTab === 'instance') {
      return;
    }
    setActiveTab('instance');
    instanceDataSet.query();
  };

  const renderTooltipTitle = (record) => {
    const id = record.get('id');
    const sagaCode = record.get('sagaCode');
    const level = record.get('level');
    const description = record.get('description');
    const service = record.get('service');
    const startTime = record.get('startTime');
    const endTime = record.get('endTime');
    return (
      <div>
        <div className="c7n-saga-instance-table-tooltip-item">
          <div className="c7n-saga-instance-table-tooltip-item-title">
            <FormattedMessage id="saga-instance.saga.instance.id" />
          </div>
          <div className="c7n-saga-instance-table-tooltip-item-value">{id}</div>
        </div>
        <div className="c7n-saga-instance-table-tooltip-item">
          <div className="c7n-saga-instance-table-tooltip-item-title">
            <FormattedMessage id="saga-instance.saga.instance.sagaCode" />

          </div>
          <div className="c7n-saga-instance-table-tooltip-item-value">{sagaCode}</div>
        </div>
        <div className="c7n-saga-instance-table-tooltip-item">
          <div className="c7n-saga-instance-table-tooltip-item-title">
            <FormattedMessage id="saga-instance.saga.instance.description" />
          </div>
          <div className="c7n-saga-instance-table-tooltip-item-value">{description}</div>
        </div>
        <div className="c7n-saga-instance-table-tooltip-item">
          <div className="c7n-saga-instance-table-tooltip-item-title">
            <FormattedMessage id="saga-instance.saga.instance.service" />
          </div>
          <div className="c7n-saga-instance-table-tooltip-item-value">{service}</div>
        </div>
        <div className="c7n-saga-instance-table-tooltip-item">
          <div className="c7n-saga-instance-table-tooltip-item-title">
            <FormattedMessage id="saga-instance.saga.instance.level" />
          </div>
          <div className="c7n-saga-instance-table-tooltip-item-value">
            <FormattedMessage id={`saga-instance.saga.instance.level.${level}`} />
          </div>
        </div>
        <div className="c7n-saga-instance-table-tooltip-item">
          <div className="c7n-saga-instance-table-tooltip-item-title">
            <FormattedMessage id="global.saga-instance.start.time" />
          </div>
          <div className="c7n-saga-instance-table-tooltip-item-value">
            {startTime}
          </div>
        </div>
        <div className="c7n-saga-instance-table-tooltip-item">
          <div className="c7n-saga-instance-table-tooltip-item-title">
            <FormattedMessage id="global.saga-instance.end.time" />
          </div>
          <div className="c7n-saga-instance-table-tooltip-item-value">
            {endTime}
          </div>
        </div>
      </div>
    );
  };

  const renderProgress = ({ record }) => {
    const title = ['completedCount', 'failedCount', 'runningCount', 'waitToBePulledCount'].map((key) => (
      <div style={{ display: 'flex' }}>
        <div style={{ width: 80 }}>
          {intl.formatMessage({ id: `${intlPrefix}.${key}` })}：
        </div>
        <div>
          {record.get(key)}
        </div>
      </div>
    ));
    const progress = (
      <Tooltip title={title}>
        <div className="c7n-saga-instance-table-progress">
          {['completedCount', 'failedCount', 'runningCount', 'waitToBePulledCount'].map((key) => (
            <div
              className={`c7n-saga-instance-table-progress-${key}`}
              style={{ flex: record.get(key) }}
            />
          ))}
        </div>
      </Tooltip>
    );
    return progress;
  };
  const openDetail = async (id) => {
    try {
      const data = await axios.get(`${apiGetway}instances/${id}`);
      Modal.open({
        key: modalKey,
        drawer: true,
        title: <FormattedMessage id={`${intlPrefix}.detail`} />,
        style: {
          width: 'calc(100% - 3.52rem)',
        },
        children: <SagaImg data={data} instance abort={abort} unLock={unLock} retry={retry} loadDetailData={loadDetailData} />,
        footer: (okBtn) => okBtn,
        okText: <FormattedMessage id="close" />,
      });
    } catch (err) {
      Choerodon.prompt(err);
    }
  };
  const renderTable = () => (
    <Table dataSet={instanceDataSet} key="instance">
      <Column
        name="sagaCode"
        className="c7n-asgard-table-cell-click"
        renderer={({ text, record }) => (
          <Tooltip title={renderTooltipTitle(record)}>
            {`${text}-${record.get('id')}`}
          </Tooltip>
        )}
        onCell={({ record }) => ({
          onClick: () => { openDetail(record.get('id')); },
        })}
      />
      <Column
        width={130}
        name="status"
        renderer={({ text: status }) => (
          <StatusTag
            name={intl.formatMessage({ id: status.toLowerCase() })}
            colorCode={status === 'WAIT_TO_BE_PULLED' ? 'QUEUE' : status}
          />
        )}
      />
      <Column name="startTime" className="c7n-asgard-table-cell" />
      <Column name="refType" className="c7n-asgard-table-cell" />
      <Column name="refId" className="c7n-asgard-table-cell" />
      <Column name="progress" renderer={renderProgress} />
    </Table>
  );
  const renderTaskTable = () => (
    <Table dataSet={taskDataSet} key="task">
      <Column
        name="taskInstanceCode"
        className="c7n-asgard-table-cell-click"
        onCell={({ record }) => ({
          onClick: () => { openDetail(record.get('sagaInstanceId')); },
        })}
        renderer={({ text }) => (
          <MouseOverWrapper text={text} width={0.1}>
            {text}
          </MouseOverWrapper>
        )}
      />
      <Column
        width={130}
        name="status"
        renderer={({ text: status }) => (
          <StatusTag
            name={intl.formatMessage({ id: status.toLowerCase() })}
            colorCode={status === 'WAIT_TO_BE_PULLED' ? 'QUEUE' : status}
          />
        )}
      />
      <Column
        name="sagaInstanceCode"
        renderer={({ text }) => (
          <MouseOverWrapper text={text} width={0.1} className="c7n-asgard-table-cell">
            {text}
          </MouseOverWrapper>
        )}
      />
      <Column
        name="description"
        renderer={({ text }) => (
          <MouseOverWrapper text={text} width={0.1} className="c7n-asgard-table-cell">
            {text}
          </MouseOverWrapper>
        )}
      />
      <Column name="plannedStartTime" className="c7n-asgard-table-cell" />
      <Column name="actualEndTime" className="c7n-asgard-table-cell" />
      <Column name="retryCount" className="c7n-asgard-table-cell" renderer={({ record }) => `${record.get('retriedCount')}/${record.get('maxRetryCount')}`} />
    </Table>
  );
  const handleStatusClick = (status) => {
    const dataSet = activeTab === 'instance' ? instanceDataSet : taskDataSet;
    dataSet.queryDataSet.current.set('status', status);
    dataSet.query();
  };

  const istStatusType = ['COMPLETED', 'RUNNING', 'FAILED'];
  const { type } = AppState.currentMenuType;
  let services = ['choerodon.code.site.manager.saga-manager.saga-instance.ps.default'];
  if (type === 'organization') {
    services = ['choerodon.code.organization.manager.saga-instance.ps.default'];
  } else {
    services = ['choerodon.code.project.manager.saga-instance.ps.default'];
  }
  return (
    <Page
      className="c7n-saga-instance"
      service={services}
    >
      <div className="c7n-saga-instance-title">
        <Breadcrumb />
        <div className="c7n-saga-status-content">
          <div className="c7n-saga-status-text"><FormattedMessage id="saga-instance.overview" /></div>
          <div className="c7n-saga-status-wrap">
            {istStatusType.map((item) => (
              <div onClick={() => handleStatusClick(item)} key={item.toLowerCase()} className={`c7n-saga-status-num c7n-saga-status-${item.toLowerCase()}`}>
                <div>{statistics[`${item}_COUNT`] || 0}</div>
                <div><FormattedMessage id={item.toLowerCase()} /></div>
              </div>
            ))}
          </div>
        </div>
      </div>
      <Content style={{ padding: 0 }}>
        <div className="c7n-saga-instance-btns">
          <span className="text">
            <FormattedMessage id={`${intlPrefix}.view`} />：
          </span>
          <Button
            onClick={loadAllData}
            className={activeTab === 'instance' && 'active'}
            type="primary"
          >
            <FormattedMessage id={`${intlPrefix}.instance`} />
          </Button>
          <Button
            className={activeTab === 'task' && 'active'}
            onClick={loadTaskData}
            type="primary"
          >
            <FormattedMessage id={`${intlPrefix}.task`} />
          </Button>
        </div>
        <div className="c7n-saga-instance-table">
          {activeTab === 'instance' ? renderTable() : renderTaskTable()}
        </div>
      </Content>
    </Page>
  );
}));
export default (props) => (
  <StoreProvider {...props}>
    <SagaInstance />
  </StoreProvider>
);

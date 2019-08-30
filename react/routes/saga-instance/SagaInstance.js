/* eslint-disable max-classes-per-file */
import React, { useState, useContext, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { axios, Breadcrumb } from '@choerodon/master';
import { Button, Tooltip } from 'choerodon-ui';
import { Table, Modal } from 'choerodon-ui/pro';
import { Content, Page } from '@choerodon/master';
import { FormattedMessage } from 'react-intl';
import MouseOverWrapper from '../../components/mouseOverWrapper';
import './style/saga-instance.scss';
import StatusTag from '../../components/statusTag';
import Store, { StoreProvider } from './stores';
import SagaImg from '../saga/SagaImg';

const { Column } = Table;

const SagaInstance = observer(() => {
  const { instanceDataSet, taskDataSet, intlPrefix, apiGetway, abort, unLock, retry, loadDetailData } = useContext(Store);
  const [activeTab, setActiveTab] = useState('instance');
  const [statistics, setStatistics] = useState({
    COMPLETED_COUNT: 0,
    FAILED_COUNT: 0,
    RUNNING_COUNT: 0,
    ROLLBACK_COUNT: 0,
  });
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
  function renderTag(status) {
    let color = '';
    let text = '';
    if (status === 'FAILED') {
      color = '#F44336';
      text = '失败';
    } else if (status === 'COMPLETED') {
      text = '完成';
      color = '#00BFA5';
    } else {
      text = '运行中';
      color = '#4D90FE';
    }
    return (
      <StatusTag
        color={color}
        name={text}
        style={{
          lineHeight: '20px',
          padding: '0 7px',
        }}
      />
    );
  }


  const renderTooltipTitle = (record) => {
    const id = record.get('id');
    const sagaCode = record.get('sagaCode');
    const level = record.get('level');
    const description = record.get('description');
    const service = record.get('service');
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
      </div>
    );
  };

  const renderProgress = ({ record }) => (
    <div className="c7n-saga-instance-table-progress">
      {['completedCount', 'failedCount', 'runningCount', 'waitToBePulledCount'].map((key) => (
        <div
          className={`c7n-saga-instance-table-progress-${key}`}
          style={{ flex: record.get(key) }}
        />
      ))}
    </div>
  );
  const openDetail = async (id) => {
    try {
      const data = await axios.get(`${apiGetway}instances/${id}`);
      Modal.open({
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
    <Table dataSet={instanceDataSet}>
      <Column
        name="sagaCode"
        style={{ cursor: 'pointer' }}
        renderer={({ text, record }) => (
          <Tooltip title={renderTooltipTitle(record)}>
            {text}
          </Tooltip>
        )}
        onCell={({ record }) => ({
          onClick: () => { openDetail(record.get('id')); },
        })}
      />
      <Column
        width={130}
        name="status"
        renderer={({ text: status }) => renderTag(status)}
      />
      <Column name="startTime" />
      <Column name="refType" />
      <Column name="refId" />
      <Column name="progress" renderer={renderProgress} />
    </Table>
  );
  const renderTaskTable = () => (
    <Table dataSet={taskDataSet}>
      <Column
        name="taskInstanceCode"
        style={{ cursor: 'pointer' }}
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
        renderer={({ text: status }) => renderTag(status)}
      />
      <Column
        name="sagaInstanceCode"
        renderer={({ text }) => (
          <MouseOverWrapper text={text} width={0.1}>
            {text}
          </MouseOverWrapper>
        )}
      />
      <Column
        name="description"
        renderer={({ text }) => (
          <MouseOverWrapper text={text} width={0.1}>
            {text}
          </MouseOverWrapper>
        )}
      />
      <Column name="plannedStartTime" />
      <Column name="actualEndTime" />
      <Column name="retryCount" renderer={({ record }) => `${record.get('retriedCount')}/${record.get('maxRetryCount')}`} />
    </Table>
  );
  const handleStatusClick = (status) => {
    const dataSet = activeTab === 'instance' ? instanceDataSet : taskDataSet;
    dataSet.queryDataSet.current.set('status', status);
    dataSet.query();
  };

  const istStatusType = ['COMPLETED', 'RUNNING', 'FAILED'];
  return (
    <Page
      className="c7n-saga-instance"
      service={[
        'asgard-service.saga-instance.pagingQuery',
        'asgard-service.saga-instance.query',
        'asgard-service.saga-instance.statistics',
        'asgard-service.saga-instance.queryDetails',
        'asgard-service.saga-instance-org.pagingQuery',
        'asgard-service.saga-instance-org.statistics',
        'asgard-service.saga-instance-org.query',
        'asgard-service.saga-instance-org.queryDetails',
        'asgard-service.saga-instance-project.pagingQuery',
        'asgard-service.saga-instance-project.statistics',
        'asgard-service.saga-instance-project.query',
        'asgard-service.saga-instance-project.queryDetails',
      ]}
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
});
export default (props) => (
  <StoreProvider {...props}>
    <SagaInstance />
  </StoreProvider>
);

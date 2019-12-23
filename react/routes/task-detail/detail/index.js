import React, { useState, useContext } from 'react';
import { Tabs, Row, Col } from 'choerodon-ui';
import { Table, DataSet } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import { Content, StatusTag } from '@choerodon/boot';
import classnames from 'classnames';
import Store, { StoreProvider } from './stores';
import MouseOverWrapper from '../../../components/mouseOverWrapper';
import './index.less';

const { TabPane } = Tabs;
const { Column } = Table;
const Detail = () => {
  const { intl: { formatMessage }, intlPrefix, logDataSet, info, AppState } = useContext(Store);
  const { currentMenuType: { type } } = AppState;
  const level = `${type === 'site' ? '平台' : '组织'}`;
  const [showLog, setShowLog] = useState(false);
  let unit;
  switch (info.simpleRepeatIntervalUnit) {
    case 'SECONDS':
      unit = '秒';
      break;
    case 'MINUTES':
      unit = '分钟';
      break;
    case 'HOURS':
      unit = '小时';
      break;
    case 'DAYS':
      unit = '天';
      break;
    default:
      break;
  }
  /**
  * 侧边栏tab切换
  * @param showLog
  */
  const handleTabChange = (key) => {
    setShowLog(key === 'log');
  };

  const infoDataSet = new DataSet({
    selection: false,
    data: info.params,
  });
  const infoList = [{
    key: formatMessage({ id: `${intlPrefix}.task.name` }),
    value: info.name,
  }, {
    key: formatMessage({ id: `${intlPrefix}.task.description` }),
    value: info.description,
  }, {
    key: formatMessage({ id: `${intlPrefix}.task.start.time` }),
    value: info.startTime,
  }, {
    key: formatMessage({ id: `${intlPrefix}.task.end.time` }),
    value: info.endTime,
  }, {
    key: formatMessage({ id: `${intlPrefix}.trigger.type` }),
    value: info.triggerType === 'simple-trigger' ? formatMessage({ id: `${intlPrefix}.easy.task` }) : formatMessage({ id: `${intlPrefix}.cron.task` }),
  }, {
    key: formatMessage({ id: `${intlPrefix}.cron.expression` }),
    value: info.cronExpression,
  }, {
    key: formatMessage({ id: `${intlPrefix}.repeat.interval` }),
    value: info.triggerType === 'simple-trigger' ? `${info.simpleRepeatInterval}${unit}` : null,
  }, {
    key: formatMessage({ id: `${intlPrefix}.repeat.time` }),
    value: info.simpleRepeatCount,
  }, {
    key: formatMessage({ id: `${intlPrefix}.last.execution.time` }),
    value: info.lastExecTime,
  }, {
    key: formatMessage({ id: `${intlPrefix}.next.execution.time` }),
    value: info.nextExecTime,
  }, {
    key: formatMessage({ id: `${intlPrefix}.service.name` }),
    value: info.serviceName,
  }, {
    key: formatMessage({ id: `${intlPrefix}.execute-strategy` }),
    value: info.executeStrategy && formatMessage({ id: `${intlPrefix}.${info.executeStrategy.toLowerCase()}` }),
  }, {
    key: formatMessage({ id: `${intlPrefix}.task.class.name` }),
    value: info.methodDescription,
  }, {
    key: formatMessage({ id: `${intlPrefix}.params.data` }),
    value:
  <Table
    dataSet={infoDataSet}
    style={{ width: '512px' }}
    pagination={false}
    queryBar="none"
  >
    <Column
      name="name"
      header={<FormattedMessage id={`${intlPrefix}.params.name`} />}
      renderer={({ text }) => (
        <MouseOverWrapper text={text} width={0.4}>
          {text}
        </MouseOverWrapper>
      )}
    />
    <Column
      name="value"
      header={<FormattedMessage id={`${intlPrefix}.params.value`} />}
      renderer={({ text }) => (
        <MouseOverWrapper text={text} width={0.4}>
          {text}
        </MouseOverWrapper>
      )}
    />
  </Table>,
  }];
  const renderStatus = ({ text: status }) => (
    <StatusTag
      name={formatMessage({ id: status.toLowerCase() })}
      colorCode={status}
    />
  );
  return (
    <Content
      className="sidebar-content"
    >
      <Tabs activeKey={showLog ? 'log' : 'info'} onChange={handleTabChange}>
        <TabPane tab={<FormattedMessage id={`${intlPrefix}.task.info`} />} key="info" />
        <TabPane tab={<FormattedMessage id={`${intlPrefix}.task.log`} />} key="log" />
      </Tabs>
      <div className="c7n-task-detail-wrapper">
        {!showLog
          ? (
            <div>
              {
                infoList.map(({ key, value }) => (
                  <Row key={key} className={classnames('c7n-task-detail-row', { 'c7n-task-detail-row-hide': value === null })}>
                    <Col span={3}>{key}</Col>
                    <Col span={21}>{value}</Col>
                  </Row>
                ))
              }

              <Row className={classnames({ 'c7n-task-detail-row': !info.notifyUser })}>
                <Col span={3}>{formatMessage({ id: `${intlPrefix}.inform.person` })}</Col>
                <Col span={21}>
                  {
                    info.notifyUser ? (
                      <ul style={{ paddingLeft: '0' }}>
                        <li className={classnames('c7n-task-detail-row-inform-person', { 'c7n-task-detail-row-hide': !info.notifyUser.creator })}>
                          {formatMessage({ id: `${intlPrefix}.creator` })}
                          <span style={{ marginLeft: '10px' }}>{info.notifyUser.creator ? info.notifyUser.creator.loginName : null}{info.notifyUser.creator ? info.notifyUser.creator.realName : null}</span>
                        </li>
                        <li className={classnames('c7n-task-detail-row-inform-person', { 'c7n-task-detail-row-hide': !info.notifyUser.administrator })}>
                          {level}{formatMessage({ id: `${intlPrefix}.manager` })}
                        </li>
                        <li className={classnames('c7n-task-detail-row-inform-person', { 'c7n-task-detail-row-hide': !info.notifyUser.assigner.length })}>
                          {formatMessage({ id: `${intlPrefix}.user` })}
                          {info.notifyUser.assigner.length ? (
                            <div className="c7n-task-detail-row-inform-person-informlist-name-container">
                              {
                                info.notifyUser.assigner.map((item) => (
                                  <div key={item.loginName}>
                                    <span>{item.loginName}{item.realName}</span>
                                    <span>、</span>
                                  </div>
                                ))
                              }
                            </div>
                          ) : <div>{formatMessage({ id: `${intlPrefix}.empty` })}</div>}
                        </li>
                      </ul>
                    ) : (
                      <Col span={21} className="c7n-task-detail-row-inform-person-empty">{formatMessage({ id: `${intlPrefix}.empty` })}</Col>
                    )
                  }
                </Col>
              </Row>
            </div>
          )
          : (
            <Table dataSet={logDataSet}>
              <Column name="status" width={100} renderer={renderStatus} />

              <Column name="serviceInstanceId" className="c7n-asgard-table-cell" />
              <Column name="plannedStartTime" className="c7n-asgard-table-cell" />
              <Column name="actualStartTime" className="c7n-asgard-table-cell" />

            </Table>
          )}
      </div>
    </Content>
  );
};
export default (props) => (
  <StoreProvider {...props}>
    <Detail />
  </StoreProvider>
);

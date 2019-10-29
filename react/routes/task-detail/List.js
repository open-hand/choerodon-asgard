import React, { useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui';
import { Modal, Table } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import { Content, Header, Page, Breadcrumb, Permission, Action, axios, StatusTag, Choerodon } from '@choerodon/boot';
import './List.less';
import '../../common/ConfirmModal.scss';
import MouseOverWrapper from '../../components/mouseOverWrapper';

import Store, { StoreProvider } from './stores';
import Create from './create';
import Detail from './detail';
import Executable from './executable-program';

// 页面权限
function getPermission(AppState) {
  const { type } = AppState.currentMenuType;
  let createService = ['asgard-service.schedule-task-site.create'];
  let enableService = ['asgard-service.schedule-task-site.enable'];
  let disableService = ['asgard-service.schedule-task-site.disable'];
  let deleteService = ['asgard-service.schedule-task-site.delete'];
  let detailService = ['asgard-service.schedule-task-site.getTaskDetail'];
  if (type === 'organization') {
    createService = ['asgard-service.schedule-task-org.create'];
    enableService = ['asgard-service.schedule-task-org.enable'];
    disableService = ['asgard-service.schedule-task-org.disable'];
    deleteService = ['asgard-service.schedule-task-org.delete'];
    detailService = ['asgard-service.schedule-task-org.getTaskDetail'];
  } else if (type === 'project') {
    createService = ['asgard-service.schedule-task-project.create'];
    enableService = ['asgard-service.schedule-task-project.enable'];
    disableService = ['asgard-service.schedule-task-project.disable'];
    deleteService = ['asgard-service.schedule-task-project.delete'];
    detailService = ['asgard-service.schedule-task-project.getTaskDetail'];
  }
  return {
    createService,
    enableService,
    disableService,
    deleteService,
    detailService,
  };
}
const { Column } = Table;
const List = observer(() => {
  const { AppState, intl, intlPrefix, taskDataSet, taskdetail, levelType } = useContext(Store);
  const { deleteService, detailService, createService, disableService, enableService } = getPermission(AppState);
  function getLevelType(type, id) {
    return (type === 'site' ? '' : `/${type}s/${id}`);
  }
  /**
   * 启停用任务
   * @param record 表格行数据
   */
  const handleAble = (record) => {
    const id = record.get('id');
    const objectVersionNumber = record.get('objectVersionNumber');
    const status = record.get('status') === 'ENABLE' ? 'disable' : 'enable';
    
    axios.put(`/asgard/v1/schedules${getLevelType(taskdetail.type, taskdetail.id)}/tasks/${id}/${status}?objectVersionNumber=${objectVersionNumber}`).then((data) => {
      if (data.failed) {
        Choerodon.prompt(data.message);
      } else {
        Choerodon.prompt(intl.formatMessage({ id: `${status}.success` }));
        taskDataSet.query();
      }
    }).catch(() => {
      Choerodon.prompt(intl.formatMessage({ id: `${status}.error` }));
    });
  };
  /**
   * 渲染任务明细列表启停用按钮
   * @param record 表格行数据
   * @returns {*}
   */
  function showActionButton(record) {
    const status = record.get('status');
    if (status === 'ENABLE') {
      return [{
        service: disableService,
        action: handleAble.bind(this, record),
        text: <FormattedMessage id="disable" />,
      }];
    } else if (status === 'DISABLE') {
      return [{
        service: enableService,
        action: handleAble.bind(this, record),
        text: <FormattedMessage id="enable" />,
      }];
    }
    return [];
  }


  const handleCreateOk = () => {
    taskDataSet.query();
  };
  /**
   * 删除任务
   * @param record 表格行数据
   */
  const handleDelete = (record) => { 
    const { type, id } = taskdetail;
    Modal.confirm({
      className: 'c7n-iam-confirm-modal',
      title: intl.formatMessage({ id: `${intlPrefix}.delete.title` }),
      children: intl.formatMessage({ id: `${intlPrefix}.delete.content` }, { name: record.get('name') }),
      
      onOk: () => axios.delete(`/asgard/v1/schedules${getLevelType(type, id)}/tasks/${record.get('id')}`).then(({ failed, message }) => {
        if (failed) {
          Choerodon.prompt(message);
        } else {
          Choerodon.prompt(intl.formatMessage({ id: 'delete.success' }));
          taskDataSet.query();
        }
      }).catch(() => {
        Choerodon.prompt(intl.formatMessage({ id: 'delete.error' }));
      }),
    });
  };


  function createTask() {
    Modal.open({
      title: <FormattedMessage id={`${intlPrefix}.create`} />,
      drawer: true,
      style: {
        width: 'calc(100% - 3.52rem)',
      },
      className: 'c7n-task-create',
      okText: '保存',
      children: <Create onOk={handleCreateOk} />,
    });
  }

  function openExecutableProgram() {
    Modal.open({
      title: '可执行程序',
      drawer: true,
      style: {
        width: 'calc(100% - 3.52rem)',
      },
      okText: '关闭',
      okCancel: false,
      children: <Executable />,
    });
  }


  /**
   * 开启侧边栏
   * @param selectType create/detail
   * @param record 列表行数据
   */
  const openDetail = async (record) => {
    const id = record.get('id');
    const info = await axios.get(`/asgard/v1/schedules${levelType}/tasks/${id}`);
    Modal.open({
      drawer: true,
      style: {
        width: 'calc(100% - 3.52rem)',
      },
      className: 'c7n-task-detail-sidebar',
      title: <FormattedMessage id={`${intlPrefix}.detail.header.title`} />,
      children: <Detail info={info} taskId={id} />,
      footer: (okBtn) => okBtn,
      okText: <FormattedMessage id="close" />,
    });
  };


  const renderName = ({ text, record }) => (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <MouseOverWrapper text={text} width={0.2}>
        <Permission service={detailService} noAccessChildren={text}>
          <span className="c7n-asgard-table-cell-click" onClick={openDetail.bind(this, record)}>
            {text}
          </span>
        </Permission>
      </MouseOverWrapper>
      <Action
        style={{ marginLeft: 'auto', flexShrink: 0 }}
        data={[
          ...showActionButton(record),
          {
            service: deleteService,
            action: handleDelete.bind(this, record),
            text: <FormattedMessage id="delete" />,
          }]}
      />
    </div>
  );
  const renderStatus = ({ text: status }) => (
    <StatusTag
      name={intl.formatMessage({ id: status.toLowerCase() })}
      colorCode={status}
    />
  );
  return (
    <Page
      service={[
        'asgard-service.schedule-task-site.pagingQuery',
        'asgard-service.schedule-task-org.pagingQuery',
        'asgard-service.schedule-task-project.pagingQuery',
        'asgard-service.schedule-task-site.create',
        'asgard-service.schedule-task-org.create',
        'asgard-service.schedule-task-project.create',
        'asgard-service.schedule-task-site.enable',
        'asgard-service.schedule-task-org.enable',
        'asgard-service.schedule-task-project.enable',
        'asgard-service.schedule-task-site.disable',
        'asgard-service.schedule-task-org.disable',
        'asgard-service.schedule-task-project.disable',
        'asgard-service.schedule-task-site.delete',
        'asgard-service.schedule-task-org.delete',
        'asgard-service.schedule-task-project.delete',
        'asgard-service.schedule-task-site.getTaskDetail',
        'asgard-service.schedule-task-org.getTaskDetail',
        'asgard-service.schedule-task-project.getTaskDetail',
        'asgard-service.schedule-task-instance-site.pagingQueryByTaskId',
        'asgard-service.schedule-task-instance-org.pagingQueryByTaskId',
        'asgard-service.schedule-task-instance-project.pagingQueryByTaskId',
        'asgard-service.schedule-method-site.pagingQuery',
        'asgard-service.schedule-method-site.getParams',
      ]}
    >
      <Header
        title={<FormattedMessage id={`${intlPrefix}.header.title`} />}
      >
        <Permission service={createService}>
          <Button
            icon="playlist_add"
            onClick={createTask}
          >
            <FormattedMessage id={`${intlPrefix}.create`} />
          </Button>
        </Permission>
        <Permission service={[
          'asgard-service.schedule-method-site.pagingQuery',
          'asgard-service.schedule-method-site.getParams',
        ]}
        >
          <Button
            icon="classname"
            onClick={openExecutableProgram}
          >
            可执行程序
          </Button>
        </Permission>
      </Header>
      <Breadcrumb />
      <Content style={{ paddingTop: 0 }}>
        <Table
          dataSet={taskDataSet}
        >
          <Column name="name" renderer={renderName} />
          <Column
            name="description"
            renderer={({ text }) => (
              <MouseOverWrapper text={text} width={0.2} className="c7n-asgard-table-cell">
                {text}
              </MouseOverWrapper>
            )} 
          />
          <Column name="lastExecTime" className="c7n-asgard-table-cell" />
          <Column name="nextExecTime" className="c7n-asgard-table-cell" />
          <Column name="status" renderer={renderStatus} />
        </Table>
      </Content>
    </Page>
  );
});
export default (props) => (
  <StoreProvider {...props}>
    <List />
  </StoreProvider>
);

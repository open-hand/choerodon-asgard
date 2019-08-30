/* eslint-disable max-classes-per-file */
import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { Select, Table, Form, Input, InputNumber } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import TaskDetailStore from '../../../stores/global/task-detail';
import '../TaskDetail.scss';

const FormItem = Form.Item;

const { Option } = Select;

const intlPrefix = 'taskdetail';

const stepPrefix = 'c7n-iam-create-task-content-step2-container';
@observer
class SelectMethod extends Component {
  selectRow = (record) => {
    this.loadParamsTable(record.id);
    TaskDetailStore.setSelectedRowKeys([record.id]);
  }

  onSelectedRowKeysChange = (selectedRowKeys) => {
    const id = selectedRowKeys.slice(-1)[0];
    this.loadParamsTable(id);
    TaskDetailStore.setSelectedRowKeys([id]);
  }

  getParamsInitvalue = (text, record) => {
    const { params } = TaskDetailStore;
    const { name } = record;
    let value;
    if (`${name}` in params) {
      value = params[name];
    } else {
      value = text === null ? undefined : text;
    }
    return value;
  };

  /**
   * 获取参数列表
   */
  loadParamsTable = (methodId) => {
    TaskDetailStore.setParamsLoading(true);
    const { taskdetail } = this.props;
    const { type, id } = taskdetail;
    if (methodId) {
      TaskDetailStore.loadParams(methodId, type, id).then((data) => {
        if (data.failed) {
          Choerodon.prompt(data.message);
        } else {
          const filteredParamsData = data.paramsList.length ? data.paramsList.filter((item) => item.default === false) : [];
          TaskDetailStore.setParamsLoading(false);
          TaskDetailStore.setParams(filteredParamsData);  
        }
        TaskDetailStore.setParamsLoading(false);
      });
    } else {
      TaskDetailStore.setParamsLoading(false);
      TaskDetailStore.setParams([]);  
    }
  };

  renderExpand = (record) => {
    const { selectedRowKeys, paramsLoading, params } = TaskDetailStore;
    if (!selectedRowKeys.includes(record.id)) {
      return null;
    }
    const { intl, form: { getFieldDecorator } } = this.props;
    const innerColumns = [{
      title: <FormattedMessage id={`${intlPrefix}.params.name`} />,
      dataIndex: 'name',
      key: 'name',
    }, {
      title: <FormattedMessage id={`${intlPrefix}.params.value`} />,
      dataIndex: 'defaultValue',
      key: 'defaultValue',
      width: 258,
      render: (text, record) => {
        let editableNode;
        if (record.type === 'Boolean') {
          editableNode = (
            <FormItem style={{ marginBottom: 0, width: '55px' }}>
              {
                getFieldDecorator(`params.${record.name}`, {
                  rules: [{
                    required: text === null,
                    message: intl.formatMessage({ id: `${intlPrefix}.default.required` }),
                  }],
                  initialValue: this.getParamsInitvalue(text, record),
                })(
                  <Select
                    getPopupContainer={() => document.getElementsByClassName('page-content')[0].parentNode}
                    style={{ width: '55px' }}
                  >
                    <Option value={null} style={{ height: '22px' }} key="null" />
                    <Option value key="true">true</Option>
                    <Option value={false} key="false">false</Option>
                  </Select>,
                )
              }
            </FormItem>
          );
        } else if (record.type === 'Integer' || record.type === 'Long' || record.type === 'Double' || record.type === 'Float') {
          editableNode = (
            <FormItem style={{ marginBottom: 0 }}>
              {
                getFieldDecorator(`params.${record.name}`, {
                  rules: [{
                    required: text === null,
                    message: intl.formatMessage({ id: `${intlPrefix}.num.required` }),
                  }, {
                    transform: (value) => Number(value),
                    type: 'number',
                    message: intl.formatMessage({ id: `${intlPrefix}.number.pattern` }),
                  }],
                  validateFirst: true,
                  initialValue: this.getParamsInitvalue(text, record),
                })(
                  <InputNumber
                    style={{ width: '200px' }}
                    onFocus={this.inputOnFocus}
                    autoComplete="off"
                  />,
                )
              }
            </FormItem>
          );
        } else {
          editableNode = (
            <FormItem style={{ marginBottom: 0 }}>
              {
                getFieldDecorator(`params.${record.name}`, {
                  rules: [{
                    required: text === null,
                    whitespace: true,
                    message: intl.formatMessage({ id: `${intlPrefix}.default.required` }),
                  }],
                  initialValue: this.getParamsInitvalue(text, record),
                })(
                  <Input
                    style={{ width: '200px', height: 32 }}
                    onFocus={this.inputOnFocus}
                    autoComplete="off"
                  />,
                )
              }
            </FormItem>
          );
        }

        if (record.type !== 'Boolean') {
          editableNode = (
            <div className="c7n-taskdetail-text">
              {editableNode}              
            </div>
          );
        }
        return editableNode;
      },
    }, {
      title: <FormattedMessage id={`${intlPrefix}.params.type`} />,
      dataIndex: 'type',
      key: 'type',
    }];

    return (
      <div className={`${stepPrefix}`}>
        <Form>
          <div className="c7n-task-deatil-params-container">
            <Table
              loading={paramsLoading}
              pagination={false}
              filterBar={false}
              columns={innerColumns}
              rowKey="name"
              dataSource={params}
              // style={{ width: '100%', marginRight: '0' }}
            />
          </div>
        </Form>
      </div>
    );
  };

  render() {
    const { methods } = TaskDetailStore;
    const columns = [{
      title: <FormattedMessage id={`${intlPrefix}.service.name`} />,
      dataIndex: 'service',
      key: 'service',
    }, {
      title: <FormattedMessage id={`${intlPrefix}.task.class.name`} />,
      dataIndex: 'description',
      key: 'description',
    }];
    const rowSelection = {
      selectedRowKeys: TaskDetailStore.getSelectedRowKeys,
      onChange: this.onSelectedRowKeysChange,
    };
    return (
      <div className="c7n-task-deatil-methods-container">     
        <Table        
          style={{ width: 1006 }}
          columns={columns}
          rowSelection={rowSelection}
          dataSource={methods}
          expandedRowKeys={TaskDetailStore.getSelectedRowKeys}
          expandedRowRender={this.renderExpand}
          rowKey="id"
          onRow={(record) => ({
            onClick: () => {
              this.selectRow(record);
            },
          })}
        />
      </div>
    );
  }
}

SelectMethod.propTypes = {

};

export default SelectMethod;

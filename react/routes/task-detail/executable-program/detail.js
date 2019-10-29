/* eslint-disable max-classes-per-file */
import React from 'react';
import { observer } from 'mobx-react-lite';
import { Table } from 'choerodon-ui/pro';
import { Tabs } from 'choerodon-ui';
import './index.less';


const { TabPane } = Tabs;
const { Column } = Table;

const ExecutableDetail = observer(({ context }) => {
  const { paramDataSet } = context;

  return (
    <div className="c7n-executable-program-detail">
      <Tabs defaultActiveKey="1">
        <TabPane tab="参数列表" key="1">
          <Table dataSet={paramDataSet}>
            <Column className="text-gray" name="name" />
            <Column className="text-gray" name="description" />
            <Column className="text-gray" name="type" />
            <Column className="text-gray" name="defaultValue" />
          </Table>
        </TabPane>
        <TabPane tab="JSON" key="2">
          <div className="json-content">
            <div>{'{'}</div>
            {paramDataSet.map((r) => (<div className="json-text">{`"${r.get('name')}": "${r.get('type')}", //${r.get('description')}`}</div>))}
            <div>{'}'}</div>
          </div>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default ExecutableDetail;

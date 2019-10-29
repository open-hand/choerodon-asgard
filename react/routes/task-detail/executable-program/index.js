/* eslint-disable max-classes-per-file */
import React, { useContext, useRef } from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Table, Modal } from 'choerodon-ui/pro';
import Store, { StoreProvider } from './stores';
import ExecutableDetail from './detail.js';
import './index.less';


const { Column } = Table;

const Executable = observer(() => {
  const context = useContext(Store);
  const { methodDataSet, paramDataSet } = context;
  const detailModal = useRef();
  function showDetail(record) {
    methodDataSet.current = record;
    paramDataSet.query();
    detailModal.current = Modal.open({
      drawer: true,
      title: <div className="c7n-executable-program-detail"><Button icon="arrow_back" onClick={() => detailModal.current.close(true)} />可执行程序详情</div>,
      style: {
        width: 'calc(100% - 3.52rem)',
      },
      okCancel: false,
      okText: '关闭',
      className: 'c7n-executable-program',
      children: <ExecutableDetail context={context} />,
    });
  }
  function renderCode({ text, record }) {
    return (
      <div className="link" onClick={() => showDetail(record)}>
        {text}
      </div>
    );
  }
  return (
    <div className="c7n-executable-program">
      <Table dataSet={methodDataSet}>
        <Column renderer={renderCode} name="code" />
        <Column className="text-gray" name="service" />
        <Column className="text-gray" name="method" tooltip="overflow" />
        <Column className="text-gray" name="description" tooltip="overflow" />
        <Column className="text-gray" name="onlineInstanceNum" />
      </Table>
    </div>
  );
});

export default (props) => (
  <StoreProvider {...props}>
    <Executable />
  </StoreProvider>
);

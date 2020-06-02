import React, { useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { axios, Breadcrumb, Choerodon } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import { Content, Page } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';
import Detail from './detail';
import Store, { StoreProvider } from './stores';
import './style/saga.scss';
import './style/json.scss';

const key = Modal.key();
const { Column } = Table;
const Saga = observer(() => {
  const { dataSet, intlPrefix, loadDetailData } = useContext(Store);
  const openDetail = async (id) => {
    try {
      const data = await axios.get(`/hagd/v1/sagas/${id}`);
      Modal.open({
        key,
        drawer: true,
        title: <FormattedMessage id={`${intlPrefix}.detail`} />,
        style: {
          width: 'calc(100% - 3.52rem)',
        },
        children: <Detail data={data} intlPrefix={intlPrefix} loadDetailData={loadDetailData} />,
        footer: (okBtn) => okBtn,
        okText: <FormattedMessage id="close" />,
      });
    } catch (err) {
      Choerodon.prompt(err);
    }
  };
  // const renderCode = ({ record, code }) => <span onClick={() => { openDetail(record.get('id')); }}>{code}</span>;
  const renderTable = () => (
    <Table dataSet={dataSet}>
      <Column
        name="code"
        className="c7n-asgard-table-cell-click"
        onCell={({ record }) => ({
          onClick: () => { openDetail(record.get('id')); },
        })}
      />
      <Column name="service" className="c7n-asgard-table-cell" />
      <Column name="description" className="c7n-asgard-table-cell" />
    </Table>
  );

  return (
    <Page
      className="c7n-saga"
      service={[
        'choerodon.code.site.manager.saga-manager.saga.ps.default',
      ]}
    >
      <Breadcrumb />
      <Content style={{ paddingTop: 0 }}>
        {renderTable()}
      </Content>
    </Page>
  );
});

export default (props) => (
  <StoreProvider {...props}>
    <Saga />
  </StoreProvider>
);

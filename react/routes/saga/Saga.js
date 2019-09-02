import React, { useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { axios, Breadcrumb } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Content, Page } from '@choerodon/master';
import { FormattedMessage } from 'react-intl';
import Detail from './detail';
import Store, { StoreProvider } from './stores';
import './style/saga.scss';
import './style/json.scss';


const { Column } = Table;
const Saga = observer(() => {
  const { dataSet, intlPrefix, loadDetailData } = useContext(Store);
  const openDetail = async (id) => {
    try {
      const data = await axios.get(`/asgard/v1/sagas/${id}`);
      Modal.open({
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
        style={{ cursor: 'pointer' }}
        onCell={({ record }) => ({
          onClick: () => { openDetail(record.get('id')); },
        })}
      />
      <Column name="service" />
      <Column name="description" />
    </Table>
  );

  return (
    <Page
      className="c7n-saga"
      service={[
        'asgard-service.saga.pagingQuery',
        'asgard-service.saga.query',
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

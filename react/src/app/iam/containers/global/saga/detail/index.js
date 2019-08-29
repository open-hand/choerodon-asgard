import React, { useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { Tabs } from 'choerodon-ui';
import jsonFormat from '../../../../common/json-format';
import SagaImg from '../SagaImg';
 
const { TabPane } = Tabs;
const Detail = ({
  data,
  intlPrefix,
}) => {
  const [showJson, setShowJson] = useState(false);

  return (
    <div>
      <Tabs activeKey={showJson ? 'json' : 'img'} onChange={(activeKey) => { setShowJson(activeKey === 'json'); }}>
        <TabPane tab={<FormattedMessage id={`${intlPrefix}.img`} />} key="img" />
        <TabPane tab={<FormattedMessage id={`${intlPrefix}.json`} />} key="json" />
      </Tabs>
      {showJson
        ? (
          <div className="c7n-saga-detail-json" style={{ margin: 0 }}>
            <pre>
              <code id="json">{jsonFormat(data)}</code>
            </pre>
          </div>
        )
        : (<SagaImg data={data} />)}
    </div>
  );
};
export default Detail;

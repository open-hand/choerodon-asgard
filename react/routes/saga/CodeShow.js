import React from 'react';
import { CodeArea } from 'choerodon-ui/pro';
import JSONFormatter from 'choerodon-ui/pro/lib/code-area/formatters/JSONFormatter';
import './CodeShow.less';

require('codemirror/mode/javascript/javascript');

const CodeShow = ({ value = '' }) => {
  let isJSON = false;
  try {
    JSON.parse(value);
    isJSON = true;
  } catch (error) {
    isJSON = false;
  }
  return (
    <CodeArea
      className="c7n-saga-CodeShow"
      style={{
        height: 350,
      }}
      value={value}
      name="content"
      options={{
        readOnly: true,
        lineNumbers: false,
        theme: 'code-show',
      }}
      {...isJSON ? { formatter: JSONFormatter } : {}}
    />
  );
};
export default CodeShow;

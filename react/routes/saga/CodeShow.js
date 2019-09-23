import React, { useEffect } from 'react';
import { CodeArea } from 'choerodon-ui/pro';
import JSONFormatter from 'choerodon-ui/pro/lib/code-area/formatters/JSONFormatter';
import './CodeShow.less';

require('codemirror/mode/javascript/javascript');

function isJSON(str) {
  if (typeof str === 'string') {
    try {
      const obj = JSON.parse(str);
      if (typeof obj === 'object' && obj) {
        // console.log('是JSON');
        return true;
      } else {
        return false;
      }
    } catch (e) {
      // console.log('不是JSON');
      return false;
    }
  }
}
export default function CodeShow({ value = '' }) {
  // console.log('value: ', value);
  return (
    <CodeArea
      className="c7n-saga-CodeShow"
      style={{
        height: 350,
      }}
      value={value}
      name="content"
      options={{
        // readOnly: true,
        lineNumbers: false,
        theme: 'code-show',
      }}
      // formatter={JSONFormatter}
      {...(isJSON(value) ? { formatter: JSONFormatter } : {})}
    />
  );
}

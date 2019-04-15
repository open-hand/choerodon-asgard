import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import { asyncLocaleProvider, asyncRouter, nomatch } from 'choerodon-boot-combine';

const taskDetail = asyncRouter(() => import('./global/task-detail'));
const executionRecord = asyncRouter(() => import('./global/execution-record'));
const executableProgram = asyncRouter(() => import('./global/executable-program'));
const saga = asyncRouter(() => import('./global/saga'));
const sagaInstance = asyncRouter(() => import('./global/saga-instance'));

@inject('AppState')
class IAMIndex extends React.Component {
  render() {
    const { match, AppState } = this.props;
    const langauge = AppState.currentLanguage || 'zh_CN';
    const IntlProviderAsync = asyncLocaleProvider(langauge, () => import(`../locale/${langauge}`));
    return (
      <IntlProviderAsync>
        <Switch>
          <Route path={`${match.url}/saga`} component={saga} />
          <Route path={`${match.url}/saga-instance`} component={sagaInstance} />
          <Route path={`${match.url}/task-detail`} component={taskDetail} />
          <Route path={`${match.url}/execution-record`} component={executionRecord} />
          <Route path={`${match.url}/executable-program`} component={executableProgram} />
          <Route path="*" component={nomatch} />
        </Switch>
      </IntlProviderAsync>
    );
  }
}

export default IAMIndex;

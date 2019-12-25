import React, { Fragment } from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import { ModalContainer } from 'choerodon-ui/pro';
import { asyncLocaleProvider, asyncRouter, nomatch } from '@choerodon/boot';
import './index.less';

const taskDetail = asyncRouter(() => import('./routes/task-detail'));
const saga = asyncRouter(() => import('./routes/saga'));
const sagaInstance = asyncRouter(() => import('./routes/saga-instance'));
// saga 事务管理
@inject('AppState')
class Index extends React.Component {
  render() {
    const { match, AppState } = this.props;
    const langauge = AppState.currentLanguage || 'zh_CN';
    const IntlProviderAsync = asyncLocaleProvider(langauge, () => import(`./locale/${langauge}`));
    return (
      <IntlProviderAsync>
        <Fragment>
          <Switch>
            <Route path={`${match.url}/saga`} component={saga} />
            <Route path={`${match.url}/saga-instance`} component={sagaInstance} />
            <Route path={`${match.url}/task-detail`} component={taskDetail} />
            <Route path={`${match.url}/org-saga-instance`} component={sagaInstance} />
            <Route path={`${match.url}/project-saga-instance`} component={sagaInstance} />
            <Route path="*" component={nomatch} />
          </Switch>
          <ModalContainer />
        </Fragment>
      </IntlProviderAsync>
    );
  }
}

export default Index;

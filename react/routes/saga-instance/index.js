import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const SagaInstance = asyncRouter(() => import('./SagaInstance'));

const Index = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={SagaInstance} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default Index;

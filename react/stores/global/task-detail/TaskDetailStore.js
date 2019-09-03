import { action, computed, observable, toJS } from 'mobx';
import { find } from 'lodash';
import { axios } from '@choerodon/master';
import querystring from 'query-string';

// @store('TaskDetailStore')
class TaskDetailStore {
  @observable data = [];

  @observable service = [];

  @observable info = {}; // 任务信息

  @observable log = []; // 任务日志

  @observable currentService = [];

  @observable classNames = []; // 任务类名下拉框数据

  @observable currentClassNames = {}; // 当前任务程序

  @observable currentTask = {};

  @observable userdata = [];

  @observable methods = [];

  @observable methodPagination = {
    current: 1,
    total: 0,
    pageSize: 10,
  };

  @observable methodFilters = {};

  @observable methodSort= {
    columnKey: 'id',
    order: 'descend',
  }
  
  @observable methodParams= []

  @observable selectedRowKeys = [];

  @observable params = [];

  @observable paramsLoading = false;

  @action setData(data) {
    this.data = data;
  }

  @computed get getData() {
    return this.data;
  }

  @action setLog(data) {
    this.log = data;
  }

  @computed get getLog() {
    return this.log;
  }

  @action setService(data) {
    this.service = data;
  }

  @action setCurrentService(data) {
    this.currentService = data;
  }

  @computed get getCurrentService() {
    return this.currentService;
  }

  @action setCurrentClassNames(data) {
    this.currentClassNames = data;
  }

  @computed get getCurrentClassNames() {
    return this.currentClassNames;
  }

  @action setClassNames(data) {
    this.classNames = data;
  }

  @computed get getClassNames() {
    return this.classNames;
  }

  @action setInfo(data) {
    this.info = data;
    if (this.info.simpleRepeatCount != null) this.info.simpleRepeatCount += 1;
  }

  @action setCurrentTask(data) {
    this.currentTask = data;
  }

  @action setUserData(data) {
    this.userdata = data;
  }

  @computed get getUserData() {
    return this.userdata;
  }

  @action setMethods(methods) {
    this.methods = methods;
  }

  @action setSelectedRowKeys(selectedRowKeys) {
    this.selectedRowKeys = selectedRowKeys;
  }

  @computed get getSelectedRowKeys() {
    return toJS(this.selectedRowKeys);
  }

  @computed get getSelectedMethod() {
    return find(this.methods, { id: this.selectedRowKeys[0] });
  }

  @action setParams(params) {
    this.params = params;
  }

  @action setParamsLoading(paramsLoading) {
    this.paramsLoading = paramsLoading;
  }

  @action setMethodPagination(methodPagination) {
    this.methodPagination = methodPagination;
  }

  @action setMethodFilters(methodFilters) {
    this.methodFilters = methodFilters;
  }

  @action setMethodSort(sort) {
    this.methodSort = sort;
  }

  @action setMethodParams(methodParams) {
    this.methodParams = methodParams;
  }

  getLevelType = (type, id) => (type === 'site' ? '' : `/${type}s/${id}`);

  getRoleLevelType = (type, id) => (type === 'site' ? `/base/v1/${type}` : `/base/v1/${type}s/${id}`);

  loadData(
    { current, pageSize },
    { status, name, description },
    { columnKey = 'id', order = 'descend' },
    params, type, id,
  ) {
    const queryObj = {
      page: current,
      size: pageSize,
      status,
      name,
      description,
      params,
    };
    if (columnKey) {
      const sorter = [];
      sorter.push(columnKey);
      if (order === 'descend') {
        sorter.push('desc');
      }
      queryObj.sort = sorter.join(',');
    }
    return axios.get(`asgard/v1/schedules${this.getLevelType(type, id)}/tasks?${querystring.stringify(queryObj)}`);
  }

  loadLogData(
    { current, pageSize },
    { status, serviceInstanceId },
    { columnKey = 'id', order = 'descend' },
    params, taskId, type, id,
  ) {
    const queryObj = {
      page: current,
      size: pageSize,
      status,
      serviceInstanceId,
      params,
    };
    if (columnKey) {
      const sorter = [];
      sorter.push(columnKey);
      if (order === 'descend') {
        sorter.push('desc');
      }
      queryObj.sort = sorter.join(',');
    }
    return axios.get(`/asgard/v1/schedules${this.getLevelType(type, id)}/tasks/instances/${taskId}?${querystring.stringify(queryObj)}`);
  }


  loadUserDatas(
    { current, pageSize },
    { columnKey = 'id', order = 'descend' },
    params, type, id,
  ) {
    const body = {
      param: params,
    };
    const queryObj = {
      size: pageSize,
      page: current,
    };
    if (columnKey) {
      const sorter = [];
      sorter.push(columnKey);
      if (order === 'descend') {
        sorter.push('desc');
      }
      queryObj.sort = sorter.join(',');
    }
    if (type === 'site') {
      return axios.post(`${this.getRoleLevelType(type, id)}/role_members/users/roles/for_all?${querystring.stringify(queryObj)}`, JSON.stringify(body));
    } else {
      return axios.post(`${this.getRoleLevelType(type, id)}/role_members/users/roles?${querystring.stringify(queryObj)}`, JSON.stringify(body));
    }
  }

  loadMethods(
    { current, pageSize },
    { service, description },
    { columnKey = 'id', order = 'descend' },
    params, type, id,
  ) {
    const queryObj = {
      size: pageSize,
      page: current,
      service,
      description,
    };
    if (columnKey) {
      const sorter = [];
      sorter.push(columnKey);
      if (order === 'descend') {
        sorter.push('desc');
      }
      queryObj.sort = sorter.join(',');
    }
    return axios.get(`/asgard/v1/schedules${this.getLevelType(type, id)}/methods?${querystring.stringify(queryObj)}`);
  }

  loadService = (type, id) => axios.get(`/asgard/v1/schedules${this.getLevelType(type, id)}/methods/services`);

  loadClass = (service, type, id) => axios.get(`/asgard/v1/schedules${this.getLevelType(type, id)}/methods/service?service=${service}`);

  loadParams = (classId, type, id) => axios.get(`/asgard/v1/schedules${this.getLevelType(type, id)}/methods/${classId}`);

  ableTask = (taskId, objectVersionNumber, status, type, id) => axios.put(`/asgard/v1/schedules${this.getLevelType(type, id)}/tasks/${taskId}/${status}?objectVersionNumber=${objectVersionNumber}`);

  deleteTask = (taskId, type, id) => axios.delete(`/asgard/v1/schedules${this.getLevelType(type, id)}/tasks/${taskId}`);

  checkName = (name, type, id) => axios.post(`/asgard/v1/schedules${this.getLevelType(type, id)}/tasks/check`, name);

  createTask = (body, type, id) => axios.post(`/asgard/v1/schedules${this.getLevelType(type, id)}/tasks`, JSON.stringify(body));

  loadInfo = (currentId, type, id) => axios.get(`/asgard/v1/schedules${this.getLevelType(type, id)}/tasks/${currentId}`);

  checkCron = (body, type, id) => axios.post(`/asgard/v1/schedules${this.getLevelType(type, id)}/tasks/cron`, body);
}


const taskDetailStore = new TaskDetailStore();
export default taskDetailStore;

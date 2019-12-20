import { DataSet } from 'choerodon-ui/pro';
import { axios } from '@choerodon/boot';
import { observable } from 'mobx';

function noop() { }
export default function ({ levelType, triggerType, simpleRepeatIntervalUnit, executeStrategy, notifyUser, intl }) {
  async function checkTaskName(value, name, record) {
    try {
      const res = await axios.post(`/asgard/v1/schedules${levelType}/tasks/check`, value);
      if (res.failed) {
        return res.message;
      }
    } catch (err) {
      return err;
    }
  }
  async function checkCronExpression(value, name, record) {
    try {
      const res = await axios.post(`/asgard/v1/schedules${levelType}/tasks/cron`, value);
      if (res.failed) {
        record.set('cronTime', observable(['请输入正确的cron表达式']));
        return intl.formatMessage({ id: res.message });
      } else {
        debugger;
        record.set('cronTime', observable(['最近3次运行时间：'].concat(res)));
      }
    } catch (err) {
      return err;
    }
  }
  const triggerTypeOptionDs = new DataSet({
    data: triggerType,
    selection: 'single',
  });
  const simpleRepeatIntervalUnitOptionDs = new DataSet({
    data: simpleRepeatIntervalUnit,
    selection: 'single',
  });
  const executeStrategyOptionDs = new DataSet({
    data: executeStrategy,
    selection: 'single',
  });
  const notifyUserOptionDs = new DataSet({
    data: notifyUser,
  });
  const defaultValidationMessagesSelect = {
    valueMissing: '请选择{label}',
  };
  return {
    selection: false,
    autoCreate: true,
    transport: {
      create: ({ data: [data] }) => {
        const tmpObj = {};
        data.notifyUser.forEach((v) => { tmpObj[v] = true; });
        data.notifyUser = tmpObj;
        // [data.startTime, data.endTime] = data.time.map((moment) => moment.format('YYYY-MM-DD HH:mm:ss'));
        return {
          url: `/asgard/v1/schedules${levelType}/tasks`,
          method: 'post',
          data,
        };
      },
    },
    fields: [
      { name: 'id', type: 'number' },
      { name: 'cronTime', defaultValue: ['请输入cron表达式'] },
      { name: 'service', type: 'string', label: '所属微服务', required: true, defaultValidationMessages: defaultValidationMessagesSelect },
      { name: 'methodId', type: 'number', label: '可执行程序', required: true, defaultValidationMessages: defaultValidationMessagesSelect },
      { name: 'name', type: 'string', label: '任务名称', validator: checkTaskName, required: true },
      { name: 'description', type: 'string', label: '任务描述', required: true },
      { name: 'startTime', type: 'dateTime', label: '起始日期', required: true },
      { name: 'endTime', type: 'dateTime', label: '失效日期' },
      { name: 'triggerType', type: 'string', required: true, defaultValue: 'simple-trigger', options: triggerTypeOptionDs },
      { name: 'simpleRepeatInterval', type: 'number', label: '重复间隔', min: 1, dynamicProps: ({ record }) => ({ required: record.get('triggerType') === 'simple-trigger' }) },
      { name: 'simpleRepeatIntervalUnit', type: 'string', label: '间隔单位', defaultValue: 'SECONDS', options: simpleRepeatIntervalUnitOptionDs },
      { name: 'simpleRepeatCount', type: 'number', label: '执行次数', min: 1, dynamicProps: ({ record }) => ({ required: record.get('triggerType') === 'simple-trigger' }) },
      { name: 'cronExpression', type: 'string', label: 'cron表达式', dynamicProps: ({ record }) => ({ required: record.get('triggerType') !== 'simple-trigger', validator: record.get('triggerType') !== 'simple-trigger' ? checkCronExpression : noop }) },
      {
        name: 'executeStrategy',
        type: 'string',
        label: '超时策略',
        required: true,
        options: executeStrategyOptionDs,
        defaultValidationMessages: defaultValidationMessagesSelect,
      },
      { name: 'assignUserIds', label: '指定用户', textField: 'realName', valueField: 'id' },
      { name: 'notifyUser', type: 'string', options: notifyUserOptionDs, multiple: true },
    ],
  };
}

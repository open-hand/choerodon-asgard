package script.db

databaseChangeLog(logicalFilePath: 'asgard_quartz_task.groovy') {
    if(helper.dbType().isSupportSequence()){
        createSequence(sequenceName: 'ASGARD_QUARTZ_TASK_S', startValue:"1")
    }
    changeSet(id: '2018-09-05-create-table-asgard_quartz_task', author: 'flyleft') {
        createTable(tableName: "ASGARD_QUARTZ_TASK") {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: 'ID', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_ASGARD_ORCH_SAGA_TASK')
            }
            column(name: 'NAME', type: 'VARCHAR(64)', remarks: '任务名'){
                constraints(nullable: false)
            }
            column(name: 'DESCRIPTION', type: 'VARCHAR(255)', remarks: '描述')
            column(name: "START_TIME", type: "DATETIME", remarks: '开始时间')
            column(name: "END_TIME", type: "DATETIME", remarks: '结束时间')
            column(name: 'TRIGGER_TYPE', type: 'VARCHAR(16)', remarks: '触发器类型。simple_trigger和cron_trigger') {
                constraints(nullable: false)
            }
            column(name: "SIMPLE_REPEAT_COUNT", type: "BIGINT(7) UNSIGNED", remarks: 'simple-trigger重复次数')
            column(name: "SIMPLE_REPEAT_INTERVAL", type: "BIGINT UNSIGNED", remarks: 'simple-trigger执行间隔')
            column(name: "CRON_EXPRESSION", type: "VARCHAR(120)", remarks: 'cron-trigger表达式')

            column(name: 'EXECUTE_PARAMS', type: 'TEXT', remarks: '任务执行参数') {
                constraints(nullable: false)
            }
            column(name: 'EXECUTE_METHOD', type: 'VARCHAR(128)', remarks: '任务执行方法') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(16)', defaultValue: "ENABLE", remarks: '任务状态。ENABLE,DISABLE,FINISHED') {
                constraints(nullable: false)
            }

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}

package script.db

databaseChangeLog(logicalFilePath: 'asgard_quartz_trigger.groovy') {
    if(helper.dbType().isSupportSequence()){
        createSequence(sequenceName: 'ASGARD_QUARTZ_TRIGGER_S', startValue:"1")
    }
    changeSet(id: '2018-09-05-create-table-asgard_quartz_trigger', author: 'flyleft') {
        createTable(tableName: "ASGARD_QUARTZ_TRIGGER") {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: 'ID', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_ASGARD_QUARTZ_TRIGGER')
            }

            column(name: "SIMPLE_REPEAT_COUNT", type: "BIGINT(7) UNSIGNED", remarks: 'simple-trigger重复次数')
            column(name: "SIMPLE_REPEAT_INTERVAL", type: "BIGINT UNSIGNED", remarks: 'simple-trigger执行间隔')
            column(name: "CRON_EXPRESSION", type: "VARCHAR(120)", remarks: 'cron-trigger表达式')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

}

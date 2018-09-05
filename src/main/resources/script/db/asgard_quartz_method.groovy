package script.db

databaseChangeLog(logicalFilePath: 'asgard_quartz_method.groovy') {
    if(helper.dbType().isSupportSequence()){
        createSequence(sequenceName: 'ASGARD_QUARTZ_METHOD_S', startValue:"1")
    }
    changeSet(id: '2018-09-05-create-table-asgard_quartz_method', author: 'flyleft') {
        createTable(tableName: "ASGARD_QUARTZ_METHOD") {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: 'ID', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_ASGARD_ORCH_SAGA_TASK')
            }

            column(name: 'METHOD', type: 'VARCHAR(128)', remarks: '方法名') {
                constraints(nullable: false)
            }

            column(name: 'MAX_RETRY_COUNT', type: 'INT(7) UNSIGNED', defaultValue: "0", remarks: '最大重试次数') {
                constraints(nullable: false)
            }

            column(name: 'PARAMS', type: 'TEXT', remarks: '方法参数') {
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
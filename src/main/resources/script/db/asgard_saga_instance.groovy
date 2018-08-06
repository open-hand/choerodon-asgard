package script.db

databaseChangeLog(logicalFilePath: 'asgard_saga_instance.groovy') {
    changeSet(id: '2018-07-04-create-table-asgard_saga_instance', author: 'jcalaz@163.com') {
        validCheckSum '7:d4acc43d89c6f99bca2a1b4de5b8e003'
        createTable(tableName: "asgard_saga_instance") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'saga_code', type: 'VARCHAR(64)', remarks: 'saga标识') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(32)', defaultValue: "RUNNING", remarks: 'saga执行状态。RUNNING,ROLLBACK,FAILED,NON_CONSUMER,COMPLETED') {
                constraints(nullable: false)
            }
            column(name: 'ref_type', type: 'VARCHAR(128)', remarks: '关联类型')
            column(name: 'ref_id', type: 'VARCHAR(128)', remarks: '关联id')
            column(name: 'input_data_id', type: 'BIGINT UNSIGNED', remarks: '输入参数的json data id')
            column(name: 'output_data_id', type: 'BIGINT UNSIGNED', remarks: '输出参数的json data id')
            column(name: "start_time", type: "DATETIME(3)", remarks: 'saga开始执行的时间')
            column(name: "end_time", type: "DATETIME(3)", remarks: 'saga执行结束的时间')


            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}

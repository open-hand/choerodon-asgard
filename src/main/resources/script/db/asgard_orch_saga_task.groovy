package script.db

databaseChangeLog(logicalFilePath: 'asgard_orch_saga_task.groovy') {
    changeSet(id: '2018-07-04-create-table-asgard_orch_saga_task', author: 'jcalaz@163.com') {
        createTable(tableName: "asgard_orch_saga_task") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'code', type: 'VARCHAR(64)', remarks: '任务标识')
            column(name: 'timeout_policy', type: 'VARCHAR(64)', remarks: '超时策略')
            column(name: 'timeout_seconds', type: 'INT UNSIGNED', remarks: '超时时间(s)')
            column(name: 'concurrent_limit_num', type: 'INT UNSIGNED', remarks: '最大并发数', defaultValue: "1") {
                constraints(nullable: false)
            }
            column(name: 'concurrent_limit_policy', type: 'VARCHAR(32)', remarks: '并发策略。NONE,TYPE,TYPE_AND_ID', defaultValue: 'NONE'){
                constraints(nullable: false)
            }
            column(name: 'saga_code', type: 'VARCHAR(64)', remarks: 'saga标识') {
                constraints(nullable: false)
            }
            column(name: 'output_schema', type: 'text', remarks: 'task输出的json schema')
            column(name: 'is_enabled', type: 'TINYINT(1)', defaultValue: "1", remarks: '是否启用') {
                constraints(nullable: false)
            }
            column(name: 'service', type: 'VARCHAR(64)', remarks: '创建该task的服务') {
                constraints(nullable: false)
            }
            column(name: 'seq', type: 'INT', remarks: 'saga中任务次序') {
                constraints(nullable: false)
            }
            column(name: 'description', type: 'VARCHAR(255)', remarks: '描述')

            column(name: 'max_retry_count', type: 'INT UNSIGNED', defaultValue: "0", remarks: '最大重试次数') {
                constraints(nullable: false)
            }

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'asgard_orch_saga_task', columnNames: 'saga_code,code', constraintName: "saga_task_code_unique")
    }

    changeSet(id: '2018-08-03-add-outputSchemaSource', author: 'jcalaz@163.com') {
        addColumn(tableName: 'asgard_orch_saga_task') {
            column(name: "output_schema_source", type: "VARCHAR(32)", defaultValue: "NONE", remarks: 'outputSchema定义来源，取值: OUTPUT_SCHEMA,OUTPUT_SCHEMA_CLASS,METHOD_RETURN_TYPE,NONE') {
                constraints(nullable: false)
            }
        }
    }

}

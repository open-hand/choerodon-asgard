package script.db

databaseChangeLog(logicalFilePath: 'asgard_saga_task_instance.groovy') {
    changeSet(id: '2018-07-04-create-table-asgard_saga_task_instance', author: 'jcalaz@163.com') {
        createTable(tableName: "asgard_saga_task_instance") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'saga_instance_id', type: 'BIGINT UNSIGNED', remarks: '关联的instance id') {
                constraints(nullable: false)
            }
            column(name: 'saga_code', type: 'VARCHAR(128)', remarks: 'saga标识') {
                constraints(nullable: false)
            }
            column(name: 'task_code', type: 'VARCHAR(64)', remarks: '任务标识') {
                constraints(nullable: false)
            }
            column(name: 'timeout_policy', type: 'VARCHAR(64)', remarks: '超时策略') {
                constraints(nullable: false)
            }
            column(name: 'timeout_seconds', type: 'INT UNSIGNED', defaultValue: "300", remarks: '超时时间(s)') {
                constraints(nullable: false)
            }
            column(name: 'ref_type', type: 'VARCHAR(128)', remarks: '关联类型')
            column(name: 'ref_id', type: 'VARCHAR(128)', remarks: '关联id')
            column(name: 'instance_lock', type: 'VARCHAR(64)', remarks: '消费该消息的实例锁')

            column(name: 'retried_count', type: 'INT UNSIGNED', defaultValue: "0", remarks: '重试次数') {
                constraints(nullable: false)
            }

            column(name: 'concurrent_limit_num', type: 'INT UNSIGNED', remarks: '最大并发数，当并发策略不为NONE时生效', defaultValue: "1") {
                constraints(nullable: false)
            }

            column(name: 'concurrent_limit_policy', type: 'VARCHAR(32)', remarks: '并发策略。NONE,TYPE,TYPE_AND_ID', defaultValue: 'NONE'){
                constraints(nullable: false)
            }

            column(name: 'exception_message', type: 'TEXT', remarks: '消息消费失败的异常信息')

            column(name: 'status', type: 'VARCHAR(32)', defaultValue: "RUNNING", remarks: 'saga执行状态。QUEUE,RUNNING,ROLLBACK,FAILED,COMPLETED') {
                constraints(nullable: false)
            }
            column(name: 'input_data_id', type: 'BIGINT UNSIGNED', remarks: '输入参数的json data id')
            column(name: 'output_data_id', type: 'BIGINT UNSIGNED', remarks: '输出参数的json data id')
            column(name: 'seq', type: 'INT', remarks: 'saga中任务次序') {
                constraints(nullable: false)
            }
            column(name: 'max_retry_count', type: 'INT UNSIGNED', defaultValue: "0", remarks: '最大重试次数') {
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

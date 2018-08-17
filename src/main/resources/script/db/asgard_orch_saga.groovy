package script.db

databaseChangeLog(logicalFilePath: 'asgard_orch_saga.groovy') {
    changeSet(id: '2018-07-04-create-table-asgard_orch_saga', author: 'jcalaz@163.com') {
        createTable(tableName: "asgard_orch_saga") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'code', type: 'VARCHAR(64)', remarks: 'saga标识') {
                constraints(unique: true)
            }
            column(name: 'service', type: 'VARCHAR(64)', remarks: '创建该saga的服务') {
                constraints(nullable: false)
            }
            column(name: 'description', type: 'VARCHAR(255)', remarks: '描述')
            column(name: 'input_schema', type: 'text', remarks: '输入的json schema')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "-1")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2018-08-03-add-inputSchemaSource', author: 'jcalaz@163.com') {
        addColumn(tableName: 'asgard_orch_saga') {
            column(name: "input_schema_source", type: "VARCHAR(32)", defaultValue: "NONE", remarks: 'inputSchema定义来源，取值: INPUT_SCHEMA,INPUT_SCHEMA_CLASS,NONE') {
                constraints(nullable: false)
            }
        }
    }
}

package script.db

databaseChangeLog(logicalFilePath: 'asgard_fix_data_task.groovy') {
    changeSet(id: '2020-10-19-asgard_fix_data_task', author: 'scp') {
        sql("""
            DELETE
            FROM
            asgard_orch_saga_task
            WHERE
            CODE = 'devopsCreateHarbor'
            AND saga_code = 'iam-create-project'
            AND service = 'devops_service'
            """)
    }
}




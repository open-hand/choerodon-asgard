package io.choerodon.asgard.infra.utils

import io.choerodon.asgard.IntegrationTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class StringLockProviderSpec extends Specification {

    @Autowired
    StringLockProvider stringLockProvider

    def 'mutex'() {
        given: '创建一个Mutex'
        StringLockProvider.Mutex one1 = stringLockProvider.getMutex('one')

        when: '创建code相同的Mutex'
        StringLockProvider.Mutex one2 = stringLockProvider.getMutex('one')
        then: 'code相同则Mutex为同一个对象'
        one1 == one2

        when: '创建多个code不同的Mutex'
        StringLockProvider.Mutex two = stringLockProvider.getMutex('two')
        StringLockProvider.Mutex three = stringLockProvider.getMutex('three')
        StringLockProvider.Mutex four = stringLockProvider.getMutex('four')
        StringLockProvider.Mutex five = stringLockProvider.getMutex('five')
        then: 'code不同则Mutex不同'
        one1 != null
        one1 != two
        one1 != three
        one1 != four
        one1 != five
        !one1.equals(null)
        !one1.equals(new String("aa"))

        when: 'getMutex传入参数为null'
        stringLockProvider.getMutex(null)
        then: '抛出空指针异常'
        thrown NullPointerException
    }
}

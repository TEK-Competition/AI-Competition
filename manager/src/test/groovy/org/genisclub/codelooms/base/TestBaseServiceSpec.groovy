import jakarta.persistence.criteria.Predicate
import org.geniusSociety.codelooms.common.exception.AssertException
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import spock.lang.Specification

import javax.persistence.criteria.*

class TestBaseServiceSpec extends Specification {
    @Mock
    JpaSpecificationExecutor<TestDO> repository

    @Mock
    BaseMapper<TestVO, TestDO> mapper

    @Mock
    Root<TestDO> root

    @Mock
    CriteriaQuery<?> query

    @Mock
    CriteriaBuilder cb

    @Mock
    Predicate predicate

    TestService testService

    def setup() {
        MockitoAnnotations.openMocks(this)
        testService = new TestService()
    }

    def "test detail with id and userId - found"() {
        given:
        def id = 1L
        def userId = 123
        def testDO = new TestDO(id: id, userId: userId)
        def testVO = new TestVO(id: id, userId: userId)

        when:
        def result = testService.detail(id, userId)

        then:
        1 * repository.findOne(_ as Specification) >> Optional.of(testDO)
        1 * mapper.domainToVo(testDO) >> testVO
        result == testVO
    }

    def "test detail with id and userId - not found"() {
        given:
        def id = 1L
        def userId = 123

        when:
        def result = testService.detail(id, userId)

        then:
        1 * repository.findOne(_ as Specification) >> Optional.empty()
        1 * mapper.domainToVo(null) >> null
        result == null
    }

    def "test detail with DO object"() {
        given:
        def testDO = new TestDO(id: 1L, userId: 123)
        def testVO = new TestVO(id: 1L, userId: 123)

        when:
        def result = testService.detail(testDO)

        then:
        1 * mapper.domainToVo(testDO) >> testVO
        result == testVO
    }

    def "test list with query and userId"() {
        given:
        def userId = 123
        def query = new BaseQuery(name: "test")
        def testDOList = [new TestDO(id: 1L, userId: userId), new TestDO(id: 2L, userId: userId)]
        def testVOList = [new TestVO(id: 1L, userId: userId), new TestVO(id: 2L, userId: userId)]

        when:
        def result = testService.list(query, userId)

        then:
        1 * repository.findAll(_ as Specification) >> testDOList
        2 * mapper.domainToVo(_) >> { args ->
            def doObj = args[0] as TestDO
            new TestVO(id: doObj.id, userId: doObj.userId)
        }
        result.size() == 2
        result*.id == [1L, 2L]
    }

    def "test list with empty name query"() {
        given:
        def userId = 123
        def query = new BaseQuery(name: "")
        def testDOList = [new TestDO(id: 1L, userId: userId)]
        def testVOList = [new TestVO(id: 1L, userId: userId)]

        when:
        def result = testService.list(query, userId)

        then:
        1 * repository.findAll(_ as Specification) >> testDOList
        1 * mapper.domainToVo(_) >> testVOList[0]
        result.size() == 1
    }

    def "test query with page"() {
        given:
        def userId = 123
        def pageQuery = new PageQuery(name: "test", page: 0, pageSize: 10)
        def testDOList = [new TestDO(id: 1L, userId: userId), new TestDO(id: 2L, userId: userId)]
        def page = new PageImpl<>(testDOList, PageRequest.of(0, 10), 2)
        def testVOList = [new TestVO(id: 1L, userId: userId), new TestVO(id: 2L, userId: userId)]

        when:
        def result = testService.query(pageQuery, userId)

        then:
        1 * repository.findAll(_ as Specification, _ as PageRequest) >> page
        2 * mapper.domainToVo(_) >> { args ->
            def doObj = args[0] as TestDO
            new TestVO(id: doObj.id, userId: doObj.userId)
        }
        result.list.size() == 2
        result.total == 2
        result.pageSize == 10
    }

    def "test findAll with list"() {
        given:
        def testDOList = [new TestDO(id: 1L), new TestDO(id: 2L)]
        def testVOList = [new TestVO(id: 1L), new TestVO(id: 2L)]

        when:
        def result = testService.findAll(testDOList)

        then:
        2 * mapper.domainToVo(_) >> { args ->
            def doObj = args[0] as TestDO
            new TestVO(id: doObj.id)
        }
        result.size() == 2
        result*.id == [1L, 2L]
    }

    def "test findAll with page"() {
        given:
        def testDOList = [new TestDO(id: 1L), new TestDO(id: 2L)]
        def page = new PageImpl<>(testDOList, PageRequest.of(0, 10), 2)
        def testVOList = [new TestVO(id: 1L), new TestVO(id: 2L)]

        when:
        def result = testService.findAll(page)

        then:
        2 * mapper.domainToVo(_) >> { args ->
            def doObj = args[0] as TestDO
            new TestVO(id: doObj.id)
        }
        result.list.size() == 2
        result.total == 2
        result.pageSize == 10
    }

    def "test spec with name query"() {
        given:
        def query = new BaseQuery(name: "test")
        def userId = 123

        when:
        def spec = testService.spec(query, userId)
        def predicate = spec.toPredicate(root, query, cb)

        then:
        1 * cb.equal(root.get("userId"), userId) >> predicate
        1 * cb.like(root.get("name"), "%test%") >> predicate
        1 * cb.and(predicate, predicate) >> predicate
        predicate != null
    }

    def "test spec without name query"() {
        given:
        def query = new BaseQuery(name: "")
        def userId = 123

        when:
        def spec = testService.spec(query, userId)
        def predicate = spec.toPredicate(root, query, cb)

        then:
        1 * cb.equal(root.get("userId"), userId) >> predicate
        0 * cb.like(_, _)
        predicate != null
    }

    def "test assertUser - success"() {
        when:
        testService.assertUser(123, 123)

        then:
        noExceptionThrown()
    }

    def "test assertUser - failure"() {
        when:
        testService.assertUser(123, 456)

        then:
        thrown(AssertException)
    }

    def "test delete method"() {
        when:
        testService.delete(1L, 123)

        then:
        // delete 方法为空实现，没有交互
        0 * repository._
    }
}
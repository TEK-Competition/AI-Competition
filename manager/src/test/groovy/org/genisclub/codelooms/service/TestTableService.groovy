import jakarta.persistence.criteria.Predicate
import org.geniusSociety.codelooms.common.bo.BaseQuery
import org.geniusSociety.codelooms.dao.MateTableRepository
import org.geniusSociety.codelooms.domain.entity.MateTable
import org.geniusSociety.codelooms.mapper.TableMapper
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

import javax.persistence.criteria.*

class TestTableService extends Specification {

    @Mock
    MateTableRepository tableRepository

    @Mock
    TableMapper tableMapper

    @InjectMocks
    TableService tableService

    @Mock
    Root<MateTable> root

    @Mock
    CriteriaQuery<?> criteriaQuery

    @Mock
    CriteriaBuilder criteriaBuilder

    @Mock
    Predicate predicate

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test getRepository returns tableRepository"() {
        when:
        def result = tableService.getRepository()

        then:
        result == tableRepository
    }

    def "test getMapper returns tableMapper"() {
        when:
        def result = tableService.getMapper()

        then:
        result == tableMapper
    }

    def "test spec with name query"() {
        given:
        def query = new BaseQuery(id: 123L, name: "testTable")
        def userId = 456

        when:
        def spec = tableService.spec(query, userId)
        def resultPredicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder)

        then:
        1 * criteriaBuilder.equal(root.get("itemId"), 123L) >> predicate
        1 * criteriaBuilder.equal(root.get("userId"), 456) >> predicate
        1 * criteriaBuilder.and(predicate, predicate) >> predicate // and predicate for itemId and userId
        1 * criteriaBuilder.like(root.get("name"), "%testTable%") >> predicate
        1 * criteriaBuilder.and(predicate, predicate) >> predicate // and predicate with name
        resultPredicate != null
    }

    def "test spec without name query"() {
        given:
        def query = new BaseQuery(id: 123L, name: "")
        def userId = 456

        when:
        def spec = tableService.spec(query, userId)
        def resultPredicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder)

        then:
        1 * criteriaBuilder.equal(root.get("itemId"), 123L) >> predicate
        1 * criteriaBuilder.equal(root.get("userId"), 456) >> predicate
        1 * criteriaBuilder.and(predicate, predicate) >> predicate
        0 * criteriaBuilder.like(_, _)
        resultPredicate == predicate
    }

    def "test spec with null name query"() {
        given:
        def query = new BaseQuery(id: 123L, name: null)
        def userId = 456

        when:
        def spec = tableService.spec(query, userId)
        def resultPredicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder)

        then:
        1 * criteriaBuilder.equal(root.get("itemId"), 123L) >> predicate
        1 * criteriaBuilder.equal(root.get("userId"), 456) >> predicate
        1 * criteriaBuilder.and(predicate, predicate) >> predicate
        0 * criteriaBuilder.like(_, _)
        resultPredicate == predicate
    }
}
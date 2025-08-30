import jakarta.persistence.criteria.Predicate
import org.geniusSociety.codelooms.common.bo.BaseQuery
import org.geniusSociety.codelooms.dao.MateTableFieldRepository
import org.geniusSociety.codelooms.domain.entity.MateTableField
import org.geniusSociety.codelooms.mapper.TableFieldMapper
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

import javax.persistence.criteria.*

class TestTableFieldService extends Specification {

    @Mock
    MateTableFieldRepository tableFieldRepository

    @Mock
    TableFieldMapper tableFieldMapper

    @InjectMocks
    TableFieldService tableFieldService

    @Mock
    Root<MateTableField> root

    @Mock
    CriteriaQuery<?> criteriaQuery

    @Mock
    CriteriaBuilder criteriaBuilder

    @Mock
    Predicate predicate

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test getRepository returns tableFieldRepository"() {
        when:
        def result = tableFieldService.getRepository()

        then:
        result == tableFieldRepository
    }

    def "test getMapper returns tableFieldMapper"() {
        when:
        def result = tableFieldService.getMapper()

        then:
        result == tableFieldMapper
    }

    def "test spec with name query"() {
        given:
        def query = new BaseQuery(id: 123L, name: "testField")
        def userId = 456

        when:
        def spec = tableFieldService.spec(query, userId)
        def resultPredicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder)

        then:
        1 * criteriaBuilder.equal(root.get("tableId"), 123L) >> predicate
        1 * criteriaBuilder.like(root.get("name"), "%testField%") >> predicate
        1 * criteriaBuilder.and(predicate, predicate) >> predicate
        resultPredicate != null
    }

    def "test spec without name query"() {
        given:
        def query = new BaseQuery(id: 123L, name: "")
        def userId = 456

        when:
        def spec = tableFieldService.spec(query, userId)
        def resultPredicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder)

        then:
        1 * criteriaBuilder.equal(root.get("tableId"), 123L) >> predicate
        0 * criteriaBuilder.like(_, _)
        resultPredicate == predicate
    }

    def "test spec with null name query"() {
        given:
        def query = new BaseQuery(id: 123L, name: null)
        def userId = 456

        when:
        def spec = tableFieldService.spec(query, userId)
        def resultPredicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder)

        then:
        1 * criteriaBuilder.equal(root.get("tableId"), 123L) >> predicate
        0 * criteriaBuilder.like(_, _)
        resultPredicate == predicate
    }
}

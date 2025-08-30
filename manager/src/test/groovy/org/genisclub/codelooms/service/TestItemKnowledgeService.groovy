import org.geniusSociety.codelooms.dao.CvItemKnowledgeRepository
import org.geniusSociety.codelooms.mapper.ItemKnowledgeMapper
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

class TestItemKnowledgeService extends Specification {

    @Mock
    CvItemKnowledgeRepository itemKnowledgeRepository

    @Mock
    ItemKnowledgeMapper itemKnowledgeMapper

    @InjectMocks
    ItemKnowledgeService itemKnowledgeService

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test getRepository returns itemKnowledgeRepository"() {
        when:
        def result = itemKnowledgeService.getRepository()

        then:
        result == itemKnowledgeRepository
    }

    def "test getMapper returns itemKnowledgeMapper"() {
        when:
        def result = itemKnowledgeService.getMapper()

        then:
        result == itemKnowledgeMapper
    }
}
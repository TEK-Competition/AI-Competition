import org.geniusSociety.codelooms.domain.dto.AnswerDTO
import org.geniusSociety.codelooms.domain.dto.QuestionDTO
import spock.lang.Specification

import javax.persistence.criteria.*

@ActiveProfiles("test")
class TestCodeloomsFeignClient extends Specification {

    @MockBean
    CodeloomsFeignClient codeloomsFeignClient

    @Autowired
    SomeService someService

    def "test someBusinessMethod calls feign client correctly"() {
        given:
        def questionDTO = new QuestionDTO(/* 参数 */)
        def expectedAnswerDTO = new AnswerDTO(/* 期望返回值 */)

        Mockito.when(codeloomsFeignClient.conversion(questionDTO))
                .thenReturn(expectedAnswerDTO)

        when:
        def result = someService.someBusinessMethod(questionDTO)

        then:
        result == expectedAnswerDTO
        Mockito.verify(codeloomsFeignClient, Mockito.times(1)).conversion(questionDTO)
    }
}
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.geniusSociety.codelooms.common.bo.BaseQuery
import org.geniusSociety.codelooms.common.bo.PageQuery
import org.geniusSociety.codelooms.common.constant.BaseConstant
import org.geniusSociety.codelooms.common.dto.PageDTO
import org.geniusSociety.codelooms.common.dto.UserDTO
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification
import spock.lang.Unroll

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when


class TestBaseActionSpec extends Specification {

    // 模拟依赖
    @Mock
    IService<TestVO> mockService

    // 使用具体实现类进行测试
    @InjectMocks
    TestAction testAction

    def setup() {
        MockitoAnnotations.openMocks(this)
        // 设置BaseController的模拟request和response
        testAction.request = mock(HttpServletRequest)
        testAction.response = mock(HttpServletResponse)
        def mockSession = mock(HttpSession)
        when(testAction.request.getSession()).thenReturn(mockSession)
    }

    def "test query should call service with correct parameters"() {
        given: "准备测试数据"
        def query = new PageQuery()
        def userId = 123
        def expectedResult = new PageDTO<TestVO>(data: [new TestVO(id: 1)], total: 1)

        and: "设置用户ID"
        def mockUser = new UserDTO(id: userId)
        when(testAction.request.getSession().getAttribute(BaseConstant.SESSION_USER_KEY)).thenReturn(mockUser)

        and: "设置service返回值"
        when(mockService.query(query, userId)).thenReturn(expectedResult)

        when: "调用query方法"
        def result = testAction.query(query)

        then: "验证结果"
        result.success
        result.data == expectedResult
        1 * mockService.query(query, userId)
    }

    def "test list should call service with correct parameters"() {
        given: "准备测试数据"
        def query = new BaseQuery()
        def userId = 123
        def expectedList = [new TestVO(id: 1), new TestVO(id: 2)]

        and: "设置用户ID"
        def mockUser = new UserDTO(id: userId)
        when(testAction.request.getSession().getAttribute(BaseConstant.SESSION_USER_KEY)).thenReturn(mockUser)

        and: "设置service返回值"
        when(mockService.list(query, userId)).thenReturn(expectedList)

        when: "调用list方法"
        def result = testAction.list(query)

        then: "验证结果"
        result.success
        result.data == expectedList
        1 * mockService.list(query, userId)
    }

    def "test detail should call service with correct parameters"() {
        given: "准备测试数据"
        def id = 1L
        def userId = 123
        def expectedVO = new TestVO(id: id, name: "Test")

        and: "设置用户ID"
        def mockUser = new UserDTO(id: userId)
        when(testAction.request.getSession().getAttribute(BaseConstant.SESSION_USER_KEY)).thenReturn(mockUser)

        and: "设置service返回值"
        when(mockService.detail(id, userId)).thenReturn(expectedVO)

        when: "调用detail方法"
        def result = testAction.detail(id)

        then: "验证结果"
        result.success
        result.data == expectedVO
        1 * mockService.detail(id, userId)
    }

    def "test delete should call service with correct parameters"() {
        given: "准备测试数据"
        def id = 1L
        def userId = 123

        and: "设置用户ID"
        def mockUser = new UserDTO(id: userId)
        when(testAction.request.getSession().getAttribute(BaseConstant.SESSION_USER_KEY)).thenReturn(mockUser)

        when: "调用delete方法"
        def result = testAction.delete(id)

        then: "验证结果"
        result.success
        1 * mockService.delete(id, userId)
    }

    @Unroll
    def "test methods should use base id when no user in session - #method"() {
        given: "session中没有用户"
        when(testAction.request.getSession().getAttribute(BaseConstant.SESSION_USER_KEY)).thenReturn(null)

        and: "设置service返回值"
        if (method == "query") {
            when(mockService.query(any(), BaseConstant.BASE_ID)).thenReturn(new PageDTO<>())
        } else if (method == "list") {
            when(mockService.list(any(), BaseConstant.BASE_ID)).thenReturn([])
        } else if (method == "detail") {
            when(mockService.detail(any(), BaseConstant.BASE_ID)).thenReturn(new TestVO())
        } else if (method == "delete") {
            // delete方法没有返回值
        }

        when: "调用方法"
        def result
        switch (method) {
            case "query":
                result = testAction.query(new PageQuery())
                break
            case "list":
                result = testAction.list(new BaseQuery())
                break
            case "detail":
                result = testAction.detail(1L)
                break
            case "delete":
                result = testAction.delete(1L)
                break
        }

        then: "验证使用了基础ID"
        if (method != "delete") {
            assert result.success
        }

        where:
        method << ["query", "list", "detail", "delete"]
    }
}
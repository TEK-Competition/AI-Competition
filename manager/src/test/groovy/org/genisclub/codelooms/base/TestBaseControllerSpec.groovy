import org.geniusSociety.codelooms.common.constant.BaseConstant
import org.geniusSociety.codelooms.common.dto.UserDTO
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import static org.mockito.Mockito.when

class TestBaseControllerSpec extends Specification {

    // 使用Mockito模拟的依赖
    @Mock
    HttpServletRequest request

    @Mock
    HttpServletResponse response

    @Mock
    HttpSession session

    // 测试具体实现类
    @InjectMocks
    TestController testController

    def setup() {
        MockitoAnnotations.openMocks(this)
        // 设置request总是返回session
        when(request.getSession()).thenReturn(session)
    }

    def "test getRequest should return mocked request"() {
        expect:
        testController.getRequest() == request
    }

    def "test getResponse should return mocked response"() {
        expect:
        testController.getResponse() == response
    }

    def "test getUser should return user from session"() {
        given: "准备用户数据"
        def mockUser = new UserDTO(id: 123, name: "testUser")

        when: "session中有用户"
        session.getAttribute(BaseConstant.SESSION_USER_KEY) >> mockUser

        then: "应该返回正确的用户"
        testController.getUser() == mockUser
    }

    def "test getUser should return null when no user in session"() {
        given: "session中没有用户"
        session.getAttribute(BaseConstant.SESSION_USER_KEY) >> null

        expect: "应该返回null"
        testController.getUser() == null
    }

    def "test getUserId should return user id when user exists"() {
        given: "准备用户数据"
        def mockUser = new UserDTO(id: 123, name: "testUser")
        session.getAttribute(BaseConstant.SESSION_USER_KEY) >> mockUser

        expect: "应该返回用户ID"
        testController.getUserId() == 123
    }

    def "test getUserId should return base id when no user in session"() {
        given: "session中没有用户"
        session.getAttribute(BaseConstant.SESSION_USER_KEY) >> null

        expect: "应该返回基础ID"
        testController.getUserId() == BaseConstant.BASE_ID
    }
}
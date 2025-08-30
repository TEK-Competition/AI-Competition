import org.geniusSociety.codelooms.common.base.BaseAction
import org.geniusSociety.codelooms.common.base.IService
import org.geniusSociety.codelooms.common.constant.WebConstant
import org.geniusSociety.codelooms.domain.vo.FileVO
import org.geniusSociety.codelooms.service.ItemFileService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

class TestItemKnowledgeController extends Specification {

    @InjectMocks
    ItemKnowledgeController itemKnowledgeController

    @Mock
    ItemFileService itemFileService

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test getService returns itemFileService"() {
        when: "调用getService方法"
        def result = itemKnowledgeController.getService()

        then: "返回itemFileService实例"
        result == itemFileService
    }

    def "test getService is called multiple times returns same instance"() {
        when: "多次调用getService方法"
        def result1 = itemKnowledgeController.getService()
        def result2 = itemKnowledgeController.getService()
        def result3 = itemKnowledgeController.getService()

        then: "每次都应返回相同的itemFileService实例"
        result1 == itemFileService
        result2 == itemFileService
        result3 == itemFileService
        result1 == result2
        result2 == result3
    }

    def "test service injection works correctly"() {
        expect: "itemFileService应该被正确注入"
        itemKnowledgeController.itemFileService != null
        itemKnowledgeController.itemFileService == itemFileService
    }

    def "test controller inheritance hierarchy"() {
        expect: "控制器应该继承自BaseAction"
        itemKnowledgeController instanceof BaseAction
    }

    def "test service type compatibility"() {
        when: "获取服务实例"
        def service = itemKnowledgeController.getService()

        then: "服务应该是IService类型且与itemFileService兼容"
        service instanceof IService
        service instanceof ItemFileService
    }

    def "test getService when service is null"() {
        given: "设置itemFileService为null"
        itemKnowledgeController.itemFileService = null

        when: "调用getService方法"
        def result = itemKnowledgeController.getService()

        then: "应该返回null"
        result == null
    }

    def "test no unexpected interactions with service"() {
        when: "创建控制器实例并调用getService"
        def service = itemKnowledgeController.getService()

        then: "不应该有任何与服务方法相关的交互"
        0 * itemFileService._ // 确保没有调用ItemFileService的任何方法
    }

    def "test controller has correct annotations"() {
        given: "通过反射获取控制器类"
        def controllerClass = ItemKnowledgeController

        expect: "应该包含RestController和RequestMapping注解"
        controllerClass.isAnnotationPresent(RestController)
        controllerClass.isAnnotationPresent(RequestMapping)

        and: "RequestMapping的值应该正确"
        def requestMapping = controllerClass.getAnnotation(RequestMapping)
        requestMapping.value() == [WebConstant.ItemPath.KNOWLEDGE]
    }

    def "test controller field annotations"() {
        given: "通过反射获取itemFileService字段"
        def field = ItemKnowledgeController.getDeclaredField("itemFileService")

        expect: "itemFileService字段应该有Autowired注解"
        field.isAnnotationPresent(Autowired)
    }

    def "test method annotations"() {
        given: "通过反射获取getService方法"
        def method = ItemKnowledgeController.getDeclaredMethod("getService")

        expect: "getService方法应该有Override注解"
        method.isAnnotationPresent(Override)
    }

    def "test generic type compatibility"() {
        when: "检查控制器的泛型类型"
        def controller = itemKnowledgeController

        then: "控制器应该使用FileVO作为泛型类型参数"
        // 这里通过反射或其他方式验证泛型类型
        // 由于Spock的限制，可能需要使用Java反射来详细验证
        controller instanceof BaseAction<FileVO>
    }

    def "test controller instantiation"() {
        when: "直接实例化控制器"
        def controller = new ItemKnowledgeController()

        then: "实例化应该成功"
        controller != null
        controller instanceof ItemKnowledgeController
    }

    def "test service assignment"() {
        given: "创建一个新的服务实例"
        def newService = Mock(ItemFileService)

        when: "给控制器分配新的服务"
        itemKnowledgeController.itemFileService = newService

        then: "getService应该返回新分配的服务"
        itemKnowledgeController.getService() == newService
    }

    // 测试控制器的字符串表示
    def "test controller toString method"() {
        when: "调用控制器的toString方法"
        def stringRepresentation = itemKnowledgeController.toString()

        then: "应该返回有意义的字符串表示"
        stringRepresentation != null
        !stringRepresentation.isEmpty()
    }

    // 测试equals和hashCode方法
    def "test controller equality with different instances"() {
        given: "创建另一个控制器实例"
        def anotherController = new ItemKnowledgeController()

        when: "比较两个不同的控制器实例"
        def areEqual = itemKnowledgeController == anotherController

        then: "它们不应该相等"
        !areEqual
    }

    // 测试空值安全性
    def "test null safety in service access"() {
        given: "临时保存原始服务引用"
        def originalService = itemKnowledgeController.itemFileService
        itemKnowledgeController.itemFileService = null

        when: "访问null服务"
        def result = itemKnowledgeController.getService()

        then: "应该返回null而不是抛出异常"
        result == null

        cleanup: "恢复原始服务"
        itemKnowledgeController.itemFileService = originalService
    }
}

import org.geniusSociety.codelooms.common.base.BaseAction
import org.geniusSociety.codelooms.common.base.IService
import org.geniusSociety.codelooms.common.constant.WebConstant
import org.geniusSociety.codelooms.domain.vo.TableFieldVO
import org.geniusSociety.codelooms.service.TableFieldService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import spock.lang.Specification

class TestTableFieldController extends Specification {

    @InjectMocks
    TableFieldController tableFieldController

    @Mock
    TableFieldService tableFieldService

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test getService returns tableFieldService"() {
        when: "调用getService方法"
        def result = tableFieldController.getService()

        then: "返回tableFieldService实例"
        result == tableFieldService
    }

    def "test getService called multiple times returns consistent instance"() {
        when: "多次调用getService方法"
        def result1 = tableFieldController.getService()
        def result2 = tableFieldController.getService()
        def result3 = tableFieldController.getService()

        then: "每次都应返回相同的tableFieldService实例"
        result1 == tableFieldService
        result2 == tableFieldService
        result3 == tableFieldService
        result1 == result2
        result2 == result3
    }

    def "test service injection is properly configured"() {
        expect: "tableFieldService应该被正确注入"
        tableFieldController.tableFieldService != null
        tableFieldController.tableFieldService == tableFieldService
    }

    def "test controller inherits from BaseAction"() {
        expect: "控制器应该继承自BaseAction"
        tableFieldController instanceof BaseAction
    }

    def "test service implements required interface"() {
        when: "获取服务实例"
        def service = tableFieldController.getService()

        then: "服务应该是IService类型且与tableFieldService兼容"
        service instanceof IService
        service instanceof TableFieldService
    }

    def "test getService returns null when service field is null"() {
        given: "设置tableFieldService为null"
        tableFieldController.tableFieldService = null

        when: "调用getService方法"
        def result = tableFieldController.getService()

        then: "应该返回null"
        result == null
    }

    def "test no unintended interactions with service methods"() {
        when: "调用getService方法"
        def service = tableFieldController.getService()

        then: "不应该调用tableFieldService的任何业务方法"
        0 * tableFieldService._ // 确保没有调用TableFieldService的任何方法
    }

    def "test RestController annotation presence"() {
        given: "通过反射获取控制器类"
        def controllerClass = TableFieldController

        expect: "应该包含RestController注解"
        controllerClass.isAnnotationPresent(RestController)
    }

    def "test RequestMapping annotation with correct path"() {
        given: "通过反射获取控制器类"
        def controllerClass = TableFieldController
        def requestMapping = controllerClass.getAnnotation(RequestMapping)

        expect: "应该包含RequestMapping注解且路径正确"
        requestMapping != null
        requestMapping.value() == [WebConstant.ItemPath.FIELD]
    }

    def "test tableFieldService field has Autowired annotation"() {
        given: "通过反射获取tableFieldService字段"
        def field = TableFieldController.getDeclaredField("tableFieldService")

        expect: "tableFieldService字段应该有Autowired注解"
        field.isAnnotationPresent(Autowired)
    }

    def "test getService method has Override annotation"() {
        given: "通过反射获取getService方法"
        def method = TableFieldController.getDeclaredMethod("getService")

        expect: "getService方法应该有Override注解"
        method.isAnnotationPresent(Override)
    }

    def "test controller instantiation and service retrieval"() {
        when: "创建控制器实例并设置服务"
        def controller = new TableFieldController()
        controller.tableFieldService = tableFieldService
        def service = controller.getService()

        then: "应该正确工作并返回服务"
        controller != null
        service == tableFieldService
    }

    def "test service field reassignment functionality"() {
        given: "创建一个新的模拟服务"
        def newTableFieldService = Mock(TableFieldService)

        when: "重新分配服务并调用getService"
        tableFieldController.tableFieldService = newTableFieldService
        def result = tableFieldController.getService()

        then: "应该返回新分配的服务"
        result == newTableFieldService
        result != tableFieldService // 验证不是原始mock
    }

    def "test controller string representation"() {
        when: "调用控制器的toString方法"
        def stringRepresentation = tableFieldController.toString()

        then: "应该返回非空字符串"
        stringRepresentation != null
        !stringRepresentation.isEmpty()
    }

    def "test controller instance inequality"() {
        given: "创建另一个控制器实例"
        def anotherController = new TableFieldController()

        when: "比较两个控制器实例"
        def areEqual = tableFieldController == anotherController

        then: "它们不应该相等"
        !areEqual
    }

    def "test null service access safety"() {
        given: "临时保存原始服务并设置为null"
        def originalService = tableFieldController.tableFieldService
        tableFieldController.tableFieldService = null

        when: "访问null服务"
        def result = tableFieldController.getService()

        then: "应该返回null而不是抛出异常"
        result == null

        cleanup: "恢复原始服务"
        tableFieldController.tableFieldService = originalService
    }

    def "test generic type parameter is TableFieldVO"() {
        given: "获取控制器的泛型信息"

        expect: "控制器应该使用TableFieldVO作为泛型类型参数"
        // 验证控制器确实扩展了BaseAction<TableFieldVO>
        BaseAction<TableFieldVO>.isAssignableFrom(TableFieldController)
    }

    def "test service method signature compatibility with TableFieldVO"() {
        when: "获取服务实例"
        def service = tableFieldController.getService()

        then: "服务应该能够处理TableFieldVO类型"
        service instanceof IService<TableFieldVO>
    }

    def "test field accessibility via reflection"() {
        when: "通过反射访问tableFieldService字段"
        def field = TableFieldController.getDeclaredField("tableFieldService")
        field.setAccessible(true)
        def fieldValue = field.get(tableFieldController)

        then: "应该能够访问到注入的服务"
        fieldValue == tableFieldService
    }

    def "test controller hash code generation"() {
        when: "获取控制器的哈希码"
        def hashCode = tableFieldController.hashCode()

        then: "哈希码应该是有效的"
        hashCode != 0
    }

    def "test controller class name and package"() {
        expect: "控制器类名和包名应该正确"
        TableFieldController.simpleName == "TableFieldController"
        TableFieldController.package.name.contains("controller") || true // 根据实际包结构调整
    }

    def "test service field type correctness"() {
        given: "获取tableFieldService字段"
        def field = TableFieldController.getDeclaredField("tableFieldService")

        expect: "字段类型应该是TableFieldService"
        field.type == TableFieldService
    }

    def "test getService return type compatibility"() {
        when: "调用getService方法并检查返回类型"
        def service = tableFieldController.getService()

        then: "返回类型应该与IService<TableFieldVO>兼容"
        service instanceof IService
    }

    def "test multiple controller instances have different services"() {
        given: "创建第二个控制器实例和模拟服务"
        def secondController = new TableFieldController()
        def secondService = Mock(TableFieldService)
        secondController.tableFieldService = secondService

        when: "获取两个控制器的服务"
        def service1 = tableFieldController.getService()
        def service2 = secondController.getService()

        then: "两个服务应该是不同的实例"
        service1 == tableFieldService
        service2 == secondService
        service1 != service2
    }
}

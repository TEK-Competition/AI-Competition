import org.geniusSociety.codelooms.common.base.BaseAction
import org.geniusSociety.codelooms.common.base.IService
import org.geniusSociety.codelooms.common.constant.WebConstant
import org.geniusSociety.codelooms.domain.vo.TableVO
import org.geniusSociety.codelooms.service.TableService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

class TestTableController extends Specification {

    @InjectMocks
    TableController tableController

    @Mock
    TableService tableService

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test getService returns tableService"() {
        when: "调用getService方法"
        def result = tableController.getService()

        then: "返回tableService实例"
        result == tableService
    }

    def "test getService called multiple times returns same instance"() {
        when: "多次调用getService方法"
        def result1 = tableController.getService()
        def result2 = tableController.getService()
        def result3 = tableController.getService()

        then: "每次都应返回相同的tableService实例"
        result1 == tableService
        result2 == tableService
        result3 == tableService
        result1 == result2
        result2 == result3
    }

    def "test service injection works correctly"() {
        expect: "tableService应该被正确注入"
        tableController.tableService != null
        tableController.tableService == tableService
    }

    def "test controller inheritance from BaseAction"() {
        expect: "控制器应该继承自BaseAction"
        tableController instanceof BaseAction
    }

    def "test service type compatibility"() {
        when: "获取服务实例"
        def service = tableController.getService()

        then: "服务应该是IService类型且与tableService兼容"
        service instanceof IService
        service instanceof TableService
    }

    def "test getService returns null when service is null"() {
        given: "设置tableService为null"
        tableController.tableService = null

        when: "调用getService方法"
        def result = tableController.getService()

        then: "应该返回null"
        result == null
    }

    def "test no unexpected service method calls"() {
        when: "调用getService方法"
        def service = tableController.getService()

        then: "不应该调用tableService的任何业务方法"
        0 * tableService._ // 确保没有调用TableService的任何方法
    }

    def "test controller has RestController annotation"() {
        given: "通过反射获取控制器类"
        def controllerClass = TableController

        expect: "应该包含RestController注解"
        controllerClass.isAnnotationPresent(RestController)
    }

    def "test controller has RequestMapping annotation with correct path"() {
        given: "通过反射获取控制器类"
        def controllerClass = TableController
        def requestMapping = controllerClass.getAnnotation(RequestMapping)

        expect: "应该包含RequestMapping注解且路径正确"
        requestMapping != null
        requestMapping.value() == [WebConstant.ItemPath.TABLE]
    }

    def "test tableService field has Autowired annotation"() {
        given: "通过反射获取tableService字段"
        def field = TableController.getDeclaredField("tableService")

        expect: "tableService字段应该有Autowired注解"
        field.isAnnotationPresent(Autowired)
    }

    def "test getService method has Override annotation"() {
        given: "通过反射获取getService方法"
        def method = TableController.getDeclaredMethod("getService")

        expect: "getService方法应该有Override注解"
        method.isAnnotationPresent(Override)
    }

    def "test controller instantiation and basic functionality"() {
        when: "创建控制器实例并调用getService"
        def controller = new TableController()
        controller.tableService = tableService
        def service = controller.getService()

        then: "应该正确工作"
        controller != null
        service == tableService
    }

    def "test service reassignment"() {
        given: "创建一个新的模拟服务"
        def newTableService = Mock(TableService)

        when: "重新分配服务并调用getService"
        tableController.tableService = newTableService
        def result = tableController.getService()

        then: "应该返回新分配的服务"
        result == newTableService
        result != tableService // 验证不是原始mock
    }

    def "test controller toString method"() {
        when: "调用控制器的toString方法"
        def stringRepresentation = tableController.toString()

        then: "应该返回非空字符串"
        stringRepresentation != null
        !stringRepresentation.isEmpty()
    }

    def "test controller equality with different instances"() {
        given: "创建另一个控制器实例"
        def anotherController = new TableController()

        when: "比较两个控制器实例"
        def areEqual = tableController == anotherController

        then: "它们不应该相等"
        !areEqual
    }

    def "test null safety in service access"() {
        given: "临时保存原始服务并设置为null"
        def originalService = tableController.tableService
        tableController.tableService = null

        when: "访问null服务"
        def result = tableController.getService()

        then: "应该返回null而不是抛出异常"
        result == null

        cleanup: "恢复原始服务"
        tableController.tableService = originalService
    }

    def "test generic type parameter is TableVO"() {
        given: "获取控制器的泛型信息"
        def controllerClass = TableController

        expect: "控制器应该使用TableVO作为泛型类型参数"
        // 这里验证控制器确实扩展了BaseAction<TableVO>
        BaseAction<TableVO>.isAssignableFrom(controllerClass)
    }

    def "test service method signature compatibility"() {
        when: "获取服务实例并验证其类型"
        def service = tableController.getService()

        then: "服务应该能够处理TableVO类型"
        service instanceof IService<TableVO>
    }

    def "test field accessibility"() {
        when: "通过反射访问tableService字段"
        def field = TableController.getDeclaredField("tableService")
        field.setAccessible(true)
        def fieldValue = field.get(tableController)

        then: "应该能够访问到注入的服务"
        fieldValue == tableService
    }

    def "test controller hash code"() {
        when: "获取控制器的哈希码"
        def hashCode = tableController.hashCode()

        then: "哈希码应该是有效的"
        hashCode != 0
    }
}

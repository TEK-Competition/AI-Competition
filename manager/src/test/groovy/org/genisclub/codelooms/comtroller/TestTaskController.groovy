import org.geniusSociety.codelooms.common.constant.WebConstant
import org.geniusSociety.codelooms.common.vo.CommonResultVO
import org.geniusSociety.codelooms.service.TaskService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

class TestTaskController extends Specification {

    @InjectMocks
    TaskController taskController

    @Mock
    TaskService taskService

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    // 测试 getService 方法
    def "test getService returns taskService"() {
        when: "调用getService方法"
        def result = taskController.getService()

        then: "返回taskService实例"
        result == taskService
    }

    // 测试 renewTask 方法
    def "test renewTask successfully"() {
        given: "准备任务ID"
        def taskId = 123L

        when: "调用renewTask方法"
        def result = taskController.renewTask(taskId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewTask(taskId)
        result.isSuccess()
        result.getData() == taskId
    }

    def "test renewTask with different IDs"() {
        given: "准备不同的任务ID"
        def taskId = 456L

        when: "调用renewTask方法"
        def result = taskController.renewTask(taskId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewTask(taskId)
        result.isSuccess()
        result.getData() == taskId
    }

    def "test renewTask with null ID"() {
        given: "准备null任务ID"
        def taskId = null

        when: "调用renewTask方法"
        def result = taskController.renewTask(taskId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewTask(taskId)
        result.isSuccess()
        result.getData() == null
    }

    // 测试 renewStage 方法
    def "test renewStage successfully"() {
        given: "准备阶段ID"
        def stageId = 123L

        when: "调用renewStage方法"
        def result = taskController.renewStage(stageId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewStage(stageId)
        result.isSuccess()
        result.getData() == stageId
    }

    def "test renewStage with different IDs"() {
        given: "准备不同的阶段ID"
        def stageId = 789L

        when: "调用renewStage方法"
        def result = taskController.renewStage(stageId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewStage(stageId)
        result.isSuccess()
        result.getData() == stageId
    }

    // 测试 renewFile 方法
    def "test renewFile successfully"() {
        given: "准备任务ID和文件ID"
        def taskId = 123L
        def fileId = 456L

        when: "调用renewFile方法"
        def result = taskController.renewFile(taskId, fileId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewFile(taskId, fileId)
        result.isSuccess()
        result.getData() == fileId
    }

    def "test renewFile with different IDs"() {
        given: "准备不同的任务ID和文件ID"
        def taskId = 999L
        def fileId = 888L

        when: "调用renewFile方法"
        def result = taskController.renewFile(taskId, fileId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewFile(taskId, fileId)
        result.isSuccess()
        result.getData() == fileId
    }

    def "test renewFile with null IDs"() {
        given: "准备null的ID"
        def taskId = null
        def fileId = null

        when: "调用renewFile方法"
        def result = taskController.renewFile(taskId, fileId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewFile(taskId, fileId)
        result.isSuccess()
        result.getData() == null
    }

    // 测试注解
    def "test controller has RestController annotation"() {
        given: "通过反射获取控制器类"
        def controllerClass = TaskController

        expect: "应该包含RestController注解"
        controllerClass.isAnnotationPresent(RestController)
    }

    def "test controller has RequestMapping annotation with correct path"() {
        given: "通过反射获取控制器类"
        def controllerClass = TaskController
        def requestMapping = controllerClass.getAnnotation(RequestMapping)

        expect: "应该包含RequestMapping注解且路径正确"
        requestMapping != null
        requestMapping.value() == [WebConstant.TASK]
    }

    def "test renewTask method has PostMapping and Operation annotations"() {
        given: "通过反射获取renewTask方法"
        def method = TaskController.getDeclaredMethod("renewTask", Long.class)

        expect: "应该包含PostMapping和Operation注解"
        method.isAnnotationPresent(PostMapping)
        method.isAnnotationPresent(Operation)

        and: "PostMapping值正确"
        def postMapping = method.getAnnotation(PostMapping)
        postMapping.value() == ["renew"]
    }

    def "test renewStage method has PostMapping and Operation annotations"() {
        given: "通过反射获取renewStage方法"
        def method = TaskController.getDeclaredMethod("renewStage", Long.class)

        expect: "应该包含PostMapping和Operation注解"
        method.isAnnotationPresent(PostMapping)
        method.isAnnotationPresent(Operation)

        and: "PostMapping值正确"
        def postMapping = method.getAnnotation(PostMapping)
        postMapping.value() == ["renew/stage"]
    }

    def "test renewFile method has PostMapping and Operation annotations"() {
        given: "通过反射获取renewFile方法"
        def method = TaskController.getDeclaredMethod("renewFile", Long.class, Long.class)

        expect: "应该包含PostMapping和Operation注解"
        method.isAnnotationPresent(PostMapping)
        method.isAnnotationPresent(Operation)

        and: "PostMapping值正确"
        def postMapping = method.getAnnotation(PostMapping)
        postMapping.value() == ["renew/file"]
    }

    // 测试服务注入
    def "test taskService field has Autowired annotation"() {
        given: "通过反射获取taskService字段"
        def field = TaskController.getDeclaredField("taskService")

        expect: "taskService字段应该有Autowired注解"
        field.isAnnotationPresent(Autowired)
    }

    // 测试继承方法
    def "test getService method has Override annotation"() {
        given: "通过反射获取getService方法"
        def method = TaskController.getDeclaredMethod("getService")

        expect: "getService方法应该有Override注解"
        method.isAnnotationPresent(Override)
    }

    // 测试异常情况
    def "test renewTask when service throws exception"() {
        given: "准备任务ID"
        def taskId = 123L

        when: "调用renewTask方法，但服务抛出异常"
        taskService.renewTask(taskId) >> { throw new RuntimeException("Service error") }
        taskController.renewTask(taskId)

        then: "应该抛出RuntimeException"
        thrown(RuntimeException)
    }

    def "test renewStage when service throws exception"() {
        given: "准备阶段ID"
        def stageId = 123L

        when: "调用renewStage方法，但服务抛出异常"
        taskService.renewStage(stageId) >> { throw new RuntimeException("Service error") }
        taskController.renewStage(stageId)

        then: "应该抛出RuntimeException"
        thrown(RuntimeException)
    }

    def "test renewFile when service throws exception"() {
        given: "准备任务ID和文件ID"
        def taskId = 123L
        def fileId = 456L

        when: "调用renewFile方法，但服务抛出异常"
        taskService.renewFile(taskId, fileId) >> { throw new RuntimeException("Service error") }
        taskController.renewFile(taskId, fileId)

        then: "应该抛出RuntimeException"
        thrown(RuntimeException)
    }

    // 测试边界值
    def "test renewTask with zero ID"() {
        given: "准备零值ID"
        def taskId = 0L

        when: "调用renewTask方法"
        def result = taskController.renewTask(taskId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewTask(taskId)
        result.isSuccess()
        result.getData() == 0L
    }

    def "test renewTask with negative ID"() {
        given: "准备负值ID"
        def taskId = -1L

        when: "调用renewTask方法"
        def result = taskController.renewTask(taskId)

        then: "验证服务调用并返回成功结果"
        1 * taskService.renewTask(taskId)
        result.isSuccess()
        result.getData() == -1L
    }

    // 测试方法调用顺序和次数
    def "test multiple method calls with correct invocation counts"() {
        when: "依次调用所有方法"
        def result1 = taskController.renewTask(1L)
        def result2 = taskController.renewStage(2L)
        def result3 = taskController.renewFile(3L, 4L)

        then: "验证每个服务方法被正确调用一次"
        1 * taskService.renewTask(1L)
        1 * taskService.renewStage(2L)
        1 * taskService.renewFile(3L, 4L)

        and: "验证所有结果都是成功的"
        result1.isSuccess()
        result2.isSuccess()
        result3.isSuccess()
    }

    // 测试返回值类型
    def "test all methods return CommonResultVO with Long data"() {
        when: "调用各个方法"
        def result1 = taskController.renewTask(1L)
        def result2 = taskController.renewStage(2L)
        def result3 = taskController.renewFile(3L, 4L)

        then: "所有结果都应该是CommonResultVO<Long>类型"
        result1 instanceof CommonResultVO
        result2 instanceof CommonResultVO
        result3 instanceof CommonResultVO
        result1.getData() instanceof Long
        result2.getData() instanceof Long
        result3.getData() instanceof Long
    }
}

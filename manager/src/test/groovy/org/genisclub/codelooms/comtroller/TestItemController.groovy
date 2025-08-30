import org.geniusSociety.codelooms.domain.bo.ItemCreateBO
import org.geniusSociety.codelooms.domain.bo.ItemUpdateBO
import org.geniusSociety.codelooms.service.ItemService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

class TestItemController extends Specification {

    @InjectMocks
    ItemController itemController

    @Mock
    ItemService itemService

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test getService returns itemService"() {
        when: "调用getService方法"
        def result = itemController.getService()

        then: "返回itemService实例"
        result == itemService
    }

    def "test create item successfully"() {
        given: "准备创建参数和预期结果"
        def createBO = new ItemCreateBO(
                // 根据实际ItemCreateBO结构设置属性
                name: "Test Item",
                description: "Test Description"
        )
        def userId = 123L
        def expectedId = 456L

        when: "调用创建方法"
        def result = itemController.create(createBO)

        then: "验证服务调用并返回成功结果"
        1 * itemController.getUserId() >> userId
        1 * itemService.create(createBO, userId) >> expectedId
        result.isSuccess()
        result.getData() == expectedId
    }

    def "test create item with different parameters"() {
        given: "准备不同的创建参数"
        def createBO = new ItemCreateBO(
                name: "Another Item",
                description: "Another Description",
                price: 99.99
        )
        def userId = 789L
        def expectedId = 999L

        when: "调用创建方法"
        def result = itemController.create(createBO)

        then: "验证服务调用并返回成功结果"
        1 * itemController.getUserId() >> userId
        1 * itemService.create(createBO, userId) >> expectedId
        result.isSuccess()
        result.getData() == expectedId
    }

    def "test update item successfully"() {
        given: "准备更新参数"
        def updateBO = new ItemUpdateBO(
                id: 456L,
                name: "Updated Item",
                description: "Updated Description"
        )
        def userId = 123L

        when: "调用更新方法"
        def result = itemController.update(updateBO)

        then: "验证服务调用并返回成功结果"
        1 * itemController.getUserId() >> userId
        1 * itemService.update(updateBO, userId)
        result.isSuccess()
        result.getData() == updateBO.id
    }

    def "test update item with different parameters"() {
        given: "准备不同的更新参数"
        def updateBO = new ItemUpdateBO(
                id: 789L,
                name: "Another Updated Item",
                description: "Another Updated Description",
                price: 199.99
        )
        def userId = 456L

        when: "调用更新方法"
        def result = itemController.update(updateBO)

        then: "验证服务调用并返回成功结果"
        1 * itemController.getUserId() >> userId
        1 * itemService.update(updateBO, userId)
        result.isSuccess()
        result.getData() == updateBO.id
    }

    def "test update item with null ID should still work"() {
        given: "准备没有ID的更新参数"
        def updateBO = new ItemUpdateBO(
                name: "Item without ID",
                description: "This should not happen but testing anyway"
        )
        def userId = 123L

        when: "调用更新方法"
        def result = itemController.update(updateBO)

        then: "验证服务调用并返回结果"
        1 * itemController.getUserId() >> userId
        1 * itemService.update(updateBO, userId)
        result.isSuccess()
        result.getData() == null // 或者根据实际情况可能是0或其他值
    }

    // 验证方法调用次数测试
    def "test service methods are called exactly once"() {
        given: "准备创建和更新参数"
        def createBO = new ItemCreateBO(name: "Test")
        def updateBO = new ItemUpdateBO(id: 1L, name: "Updated")
        def userId = 123L
        def createdId = 1L

        when: "分别调用创建和更新方法"
        def createResult = itemController.create(createBO)
        def updateResult = itemController.update(updateBO)

        then: "验证每个服务方法只被调用一次"
        2 * itemController.getUserId() >> userId
        1 * itemService.create(createBO, userId) >> createdId
        1 * itemService.update(updateBO, userId)

        and: "验证返回结果正确"
        createResult.isSuccess()
        createResult.getData() == createdId
        updateResult.isSuccess()
        updateResult.getData() == updateBO.id
    }

    // 边界情况测试 - 空对象
    def "test create with minimal data"() {
        given: "准备最小数据量的创建参数"
        def createBO = new ItemCreateBO(name: "Minimal")
        def userId = 123L
        def expectedId = 1L

        when: "调用创建方法"
        def result = itemController.create(createBO)

        then: "验证服务调用并返回成功结果"
        1 * itemController.getUserId() >> userId
        1 * itemService.create(createBO, userId) >> expectedId
        result.isSuccess()
    }
}

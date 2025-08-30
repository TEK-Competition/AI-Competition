import cn.hutool.core.io.FileUtil
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.CharsetUtil
import org.geniusSociety.codelooms.common.constant.EntityType
import org.geniusSociety.codelooms.dao.*
import org.geniusSociety.codelooms.domain.dto.AnswerDTO
import org.geniusSociety.codelooms.domain.entity.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.context.ApplicationContext
import spock.lang.Specification

import java.util.function.Consumer

class TaskWorkerTest extends Specification {

    @Mock
    ApplicationContext context

    @Mock
    CvTaskStageRepository taskStageRepository
    @Mock
    CvItemFileRepository itemFileRepository
    @Mock
    CodeloomsFeignClient feignClient
    @Mock
    CvTaskRepository taskRepository
    @Mock
    CvItemRepository itemRepository
    @Mock
    CvTableRepository tableRepository
    @Mock
    CvTableFieldRepository tableFieldRepository
    @Mock
    MateTableRepository mateTableRepository
    @Mock
    MateTableFieldRepository mateTableFieldRepository

    def taskWorker
    def taskId = 1L
    def itemId = 2L
    def userId = 3

    def setup() {
        MockitoAnnotations.openMocks(this)

        // 设置ApplicationContext返回mock beans
        when(context.getBean(CvTaskStageRepository.class)).thenReturn(taskStageRepository)
        when(context.getBean(CvItemFileRepository.class)).thenReturn(itemFileRepository)
        when(context.getBean(CodeloomsFeignClient.class)).thenReturn(feignClient)
        when(context.getBean(CvTaskRepository.class)).thenReturn(taskRepository)
        when(context.getBean(CvItemRepository.class)).thenReturn(itemRepository)
        when(context.getBean(CvTableRepository.class)).thenReturn(tableRepository)
        when(context.getBean(CvTableFieldRepository.class)).thenReturn(tableFieldRepository)
        when(context.getBean(MateTableRepository.class)).thenReturn(mateTableRepository)
        when(context.getBean(MateTableFieldRepository.class)).thenReturn(mateTableFieldRepository)

        def task = new CvTask(id: taskId, itemId: itemId, userId: userId)
        taskWorker = new TaskWorker(task, context)
    }

    def "test constructor initialization"() {
        given:
        def task = new CvTask(id: taskId, itemId: itemId, userId: userId)
        def files = [new CvItemFile(id: 1L), new CvItemFile(id: 2L)]

        when(itemFileRepository.findAll(any())).thenReturn(files)

        when:
        def worker = new TaskWorker(task, context)

        then:
        worker.taskId == taskId
        worker.itemId == itemId
        worker.userId == userId
        worker.files.size() == 2
    }

    def "test run method success flow"() {
        given:
        def task = new CvTask(id: taskId, status: EntityType.TaskStaus.NEW)
        def stages = [
                new CvTaskStage(stage: EntityType.TaskStage.TABLE_RELATION, status: EntityType.TaskStaus.NEW),
                new CvTaskStage(stage: EntityType.TaskStage.TABLE_FIELD, status: EntityType.TaskStaus.NEW),
                new CvTaskStage(stage: EntityType.TaskStage.CONVERSION, status: EntityType.TaskStaus.NEW),
                new CvTaskStage(stage: EntityType.TaskStage.EXEGESIS, status: EntityType.TaskStaus.NEW),
                new CvTaskStage(stage: EntityType.TaskStage.FINISH, status: EntityType.TaskStaus.NEW)
        ]
        def item = new CvItem(id: itemId)

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task))
        when(taskStageRepository.findAll(any())).thenReturn(stages)
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item))

        when:
        taskWorker.run()

        then:
        // 验证任务状态更新
        1 * taskRepository.save(_) >> { args ->
            def savedTask = args[0]
            assert savedTask.status == EntityType.TaskStaus.RUNNING
            assert savedTask.startTime != null
            savedTask
        }

        // 验证最终任务状态
        1 * taskRepository.save(_) >> { args ->
            def savedTask = args[0]
            assert savedTask.status == EntityType.TaskStaus.FINISH
            savedTask
        }

        // 验证项目状态更新
        1 * itemRepository.save(_) >> { args ->
            def savedItem = args[0]
            assert savedItem.status == EntityType.TaskStaus.FINISH
            savedItem
        }
    }

    def "test run method with exception"() {
        given:
        def task = new CvTask(id: taskId, status: EntityType.TaskStaus.NEW)
        def stages = [new CvTaskStage(stage: EntityType.TaskStage.TABLE_RELATION, status: EntityType.TaskStaus.NEW)]

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task))
        when(taskStageRepository.findAll(any())).thenReturn(stages)
        // 模拟relation方法抛出异常
        when(feignClient.relation(any())).thenThrow(new RuntimeException("Test exception"))

        when:
        taskWorker.run()

        then:
        // 验证任务状态变为FAIL
        1 * taskRepository.save(_) >> { args ->
            def savedTask = args[0]
            assert savedTask.status == EntityType.TaskStaus.FAIL
            savedTask
        }
    }

    def "test execute method success"() {
        given:
        def step = new CvTaskStage(stage: 1, status: EntityType.TaskStaus.NEW)
        def files = [new CvItemFile(id: 1L), new CvItemFile(id: 2L)]
        def consumer = Mock(Consumer)

        when:
        taskWorker.execute(step, consumer)

        then:
        // 验证步骤状态更新
        2 * taskStageRepository.save(_) >> { args ->
            def savedStep = args[0]
            if (savedStep.startTime != null) {
                assert savedStep.status == EntityType.TaskStaus.RUNNING
            } else {
                assert savedStep.status == EntityType.TaskStaus.FINISH
                assert savedStep.finishTime != null
            }
            savedStep
        }

        // 验证文件处理
        2 * itemFileRepository.save(_)
        files.each { 1 * consumer.accept(it) }
    }

    def "test execute method with exception"() {
        given:
        def step = new CvTaskStage(stage: 1, status: EntityType.TaskStaus.NEW)
        def consumer = { throw new RuntimeException("Test exception") } as Consumer

        when:
        taskWorker.execute(step, consumer)

        then:
        // 验证步骤状态变为FAIL
        2 * taskStageRepository.save(_) >> { args ->
            def savedStep = args[0]
            if (savedStep.startTime != null) {
                assert savedStep.status == EntityType.TaskStaus.RUNNING
            } else {
                assert savedStep.status == EntityType.TaskStaus.FAIL
            }
            savedStep
        }
    }

    def "test relation method"() {
        given:
        def file = new CvItemFile(spPath: "/path/to/sp.sql")
        def spContent = "CREATE PROCEDURE test() BEGIN END"
        def answer = new AnswerDTO(data: '[{"from":"table1","to":"table2"}]')

        def mockFile = Mock(File) {
            exists() >> true
        }

        when(FileUtil.file("/path/to/sp.sql")).thenReturn(mockFile)
        when(FileUtil.readString(mockFile, CharsetUtil.CHARSET_UTF_8)).thenReturn(spContent)
        when(feignClient.relation(any())).thenReturn(answer)

        when:
        def consumer = taskWorker.relation()
        consumer.accept(file)

        then:
        file.relation == '[{"from":"table1","to":"table2"}]'
        1 * itemFileRepository.save(file)
        1 * ThreadUtil.safeSleep(1000)
    }

    def "test generateRelation method"() {
        given:
        def files = [
                new CvItemFile(name: "file1.sql", relation: '[{"from":"table1","to":"table2"}]'),
                new CvItemFile(name: "file2.sql", relation: '[{"from":"table2","to":"table3"}]')
        ]
        def item = new CvItem(id: itemId)
        def tableAnswer = new AnswerDTO(data: '[{"from":"table1","to":"table2"},{"from":"table2","to":"table3"}]')
        def processAnswer = new AnswerDTO(data: '[{"from":"file1","to":"file2"}]')

        taskWorker.files.addAll(files)

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item))
        when(feignClient.graph(any())).thenReturn(tableAnswer)
        when(feignClient.process(any())).thenReturn(processAnswer)

        when:
        taskWorker.generateRelation()

        then:
        1 * itemRepository.save(_) >> { args ->
            def savedItem = args[0]
            assert savedItem.tableRelation.contains('"from":"table1"')
            assert savedItem.spRelation.contains('"from":"file1"')
            savedItem
        }
    }

    def "test pretreatmentRelation method"() {
        given:
        def input = '[{"from":"NONE","to":"table1"},{"from":"table2","to":"NONE"}]'
        def expected = '[{"from":"START","to":"table1"},{"from":"table2","to":"END"}]'

        when:
        def result = taskWorker.pretreatmentRelation(input)

        then:
        result == expected
    }

    def "test field method"() {
        given:
        def file = new CvItemFile(spPath: "/path/to/sp.sql")
        def spContent = "CREATE PROCEDURE test() BEGIN END"
        def answer = new AnswerDTO(data: '[{"table":"users","fields":[{"name":"id","dataType":"int"},{"name":"name","dataType":"varchar"}]}]')
        def table = new CvTable(id: 10L, name: "users")

        def mockFile = Mock(File) {
            exists() >> true
        }

        when(FileUtil.file("/path/to/sp.sql")).thenReturn(mockFile)
        when(FileUtil.readString(mockFile, CharsetUtil.CHARSET_UTF_8)).thenReturn(spContent)
        when(feignClient.field(any())).thenReturn(answer)
        when(tableRepository.save(any())).thenReturn(table)

        when:
        def consumer = taskWorker.field()
        consumer.accept(file)

        then:
        1 * tableRepository.save(_)
        2 * tableFieldRepository.save(_)
        1 * ThreadUtil.safeSleep(1000)
    }

    def "test generateMeta method"() {
        given:
        def tables = [
                new CvTable(id: 1L, name: "users"),
                new CvTable(id: 2L, name: "orders")
        ]
        def fields = [
                new CvTableField(id: 1L, tableId: 1L, name: "id", dataType: "int"),
                new CvTableField(id: 2L, tableId: 1L, name: "name", dataType: "varchar"),
                new CvTableField(id: 3L, tableId: 2L, name: "order_id", dataType: "int")
        ]
        def mateTable = new MateTable(id: 100L, name: "users")

        when(tableRepository.findAll(any())).thenReturn(tables)
        when(tableFieldRepository.findAll(any())).thenReturn(fields)
        when(mateTableRepository.saveAndFlush(any())).thenReturn(mateTable)

        when:
        taskWorker.generateMeta()

        then:
        2 * mateTableRepository.saveAndFlush(_)
        3 * mateTableFieldRepository.saveAndFlush(_)
    }

    def "test conversion method"() {
        given:
        def file = new CvItemFile(spPath: "/path/to/sp.sql", sqlPath: null)
        def spContent = "CREATE PROCEDURE test() BEGIN END"
        def convertedContent = "CREATE OR REPLACE PROCEDURE test() AS BEGIN END"
        def answer = new AnswerDTO(data: convertedContent)

        def mockFile = Mock(File) {
            getParentFile() >> Mock(File) {
                getParentFile() >> Mock(File) {
                    getAbsolutePath() >> "/parent/path"
                }
            }
            getName() >> "test.sql"
        }

        when(FileUtil.file("/path/to/sp.sql")).thenReturn(mockFile)
        when(FileUtil.readString(mockFile, CharsetUtil.CHARSET_UTF_8)).thenReturn(spContent)
        when(feignClient.conversion(any())).thenReturn(answer)

        when:
        def consumer = taskWorker.conversion()
        consumer.accept(file)

        then:
        file.sqlPath == "/parent/path/test.sql"
        1 * FileUtil.writeString(convertedContent, "/parent/path/test.sql", CharsetUtil.CHARSET_UTF_8)
        1 * itemFileRepository.save(file)
        1 * ThreadUtil.safeSleep(1000)
    }

    def "test exegesis method"() {
        given:
        def file = new CvItemFile()
        def tableId = 100L
        def mateTable = new MateTable(id: tableId, name: "users")
        def mateFields = [
                new MateTableField(name: "id", dataType: "int"),
                new MateTableField(name: "name", dataType: "varchar")
        ]
        def answer = new AnswerDTO(data: '[{"name":"id","comment":"主键ID"},{"name":"name","comment":"用户姓名"}]')

        // 设置tableFieldMap
        taskWorker.tableFieldMap.put(tableId, mateFields)

        when(mateTableRepository.findById(tableId)).thenReturn(Optional.of(mateTable))
        when(feignClient.exegesis(any())).thenReturn(answer)

        when:
        def consumer = taskWorker.exegesis()
        consumer.accept(file)

        then:
        mateFields.find { it.name == "id" }.description == "主键ID"
        mateFields.find { it.name == "name" }.description == "用户姓名"
        1 * mateTableFieldRepository.saveAll(mateFields)
        1 * ThreadUtil.safeSleep(1000)
    }

    def "test exegesis method with empty tableFieldMap"() {
        given:
        def file = new CvItemFile()

        // tableFieldMap为空
        taskWorker.tableFieldMap.clear()

        when:
        def consumer = taskWorker.exegesis()
        consumer.accept(file)

        then:
        // 应该记录错误但不会抛出异常
        0 * mateTableFieldRepository.saveAll(_)
        1 * ThreadUtil.safeSleep(1000)
    }
}
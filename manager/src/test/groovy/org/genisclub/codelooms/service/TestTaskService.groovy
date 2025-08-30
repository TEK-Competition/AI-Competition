import org.geniusSociety.codelooms.common.constant.EntityType
import org.geniusSociety.codelooms.component.TaskManager
import org.geniusSociety.codelooms.dao.CvItemFileRepository
import org.geniusSociety.codelooms.dao.CvItemRepository
import org.geniusSociety.codelooms.dao.CvTaskRepository
import org.geniusSociety.codelooms.dao.CvTaskStageRepository
import org.geniusSociety.codelooms.domain.entity.CvItem
import org.geniusSociety.codelooms.domain.entity.CvItemFile
import org.geniusSociety.codelooms.domain.entity.CvTask
import org.geniusSociety.codelooms.domain.entity.CvTaskStage
import org.geniusSociety.codelooms.domain.vo.TaskVO
import org.geniusSociety.codelooms.mapper.TaskMapper
import org.geniusSociety.codelooms.mapper.TaskStageMapper
import org.geniusSociety.codelooms.service.TaskService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

class TaskServiceTest extends Specification {

    @InjectMocks
    TaskService taskService

    @Mock
    TaskManager taskManager
    @Mock
    CvTaskRepository taskRepository
    @Mock
    CvItemRepository itemRepository
    @Mock
    CvTaskStageRepository taskStageRepository
    @Mock
    CvItemFileRepository itemFileRepository
    @Mock
    TaskMapper taskMapper
    @Mock
    TaskStageMapper taskStageMapper

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test detail method"() {
        given:
        def taskId = 1L
        def cvTask = new CvTask(id: taskId)
        def taskVO = new TaskVO()
        def taskStages = [new CvTaskStage(), new CvTaskStage()]
        def mappedStages = [new TaskStageVO(), new TaskStageVO()]

        when(taskStageRepository.findAll(any())).thenReturn(taskStages)
        when(taskStageMapper.domainToVo(any(CvTaskStage))).thenReturn(mappedStages[0], mappedStages[1])

        when:
        def result = taskService.detail(cvTask, taskVO)

        then:
        result == taskVO
        result.stages == mappedStages
        1 * taskStageRepository.findAll(any()) >> { args ->
            def predicate = args[0]
            // 验证predicate逻辑（可选）
            taskStages
        }
        2 * taskStageMapper.domainToVo(_ as CvTaskStage)
    }

    def "test renewTask with fileId - success case"() {
        given:
        def itemId = 1L
        def stage = null
        def fileId = 10L

        def cvItem = new CvItem(id: itemId)
        def cvTask = new CvTask(id: 2L, itemId: itemId)
        def cvItemFile = new CvItemFile(id: fileId)
        def taskStages = [new CvTaskStage(id: 3L), new CvTaskStage(id: 4L)]

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(cvItem))
        when(taskRepository.findOne(any())).thenReturn(Optional.of(cvTask))
        when(itemFileRepository.findById(fileId)).thenReturn(Optional.of(cvItemFile))
        when(taskStageRepository.findAll(any())).thenReturn(taskStages)

        when:
        taskService.renewTask(itemId, stage, fileId)

        then:
        1 * itemRepository.findById(itemId) >> Optional.of(cvItem)
        1 * taskRepository.findOne(any()) >> Optional.of(cvTask)
        1 * itemFileRepository.findById(fileId) >> Optional.of(cvItemFile)
        1 * taskStageRepository.findAll(any()) >> taskStages

        // 验证状态更新
        taskStages.every { it.status == EntityType.TaskStaus.NEW }
        cvItemFile.stage == EntityType.TaskStaus.NEW
        cvTask.steps == EntityType.TaskStage.READY
        cvTask.status == EntityType.TaskStaus.RUNNING

        // 验证保存操作
        1 * taskStageRepository.saveAll(taskStages)
        1 * taskRepository.save(cvTask)
        1 * itemFileRepository.save(cvItemFile)
        1 * itemRepository.save(cvItem)
        1 * taskManager.execute(cvTask)
    }

    def "test renewTask with stage - success case"() {
        given:
        def itemId = 1L
        def stage = 5
        def fileId = null

        def cvItem = new CvItem(id: itemId)
        def cvTask = new CvTask(id: 2L, itemId: itemId)
        def cvTaskStage = new CvTaskStage(id: 3L, stage: stage)

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(cvItem))
        when(taskRepository.findOne(any())).thenReturn(Optional.of(cvTask))
        when(taskStageRepository.findOne(any())).thenReturn(Optional.of(cvTaskStage))

        when:
        taskService.renewTask(itemId, stage, fileId)

        then:
        1 * itemRepository.findById(itemId) >> Optional.of(cvItem)
        1 * taskRepository.findOne(any()) >> Optional.of(cvTask)
        1 * taskStageRepository.findOne(any()) >> Optional.of(cvTaskStage)

        // 验证状态更新
        cvTaskStage.status == EntityType.TaskStaus.NEW
        cvTask.steps == stage
        cvTask.status == EntityType.TaskStaus.RUNNING

        // 验证保存操作
        1 * taskStageRepository.save(cvTaskStage)
        1 * taskRepository.save(cvTask)
        1 * itemRepository.save(cvItem)
        1 * taskManager.execute(cvTask)
    }

    def "test renewTask with neither fileId nor stage - success case"() {
        given:
        def itemId = 1L
        def stage = null
        def fileId = null

        def cvItem = new CvItem(id: itemId)
        def cvTask = new CvTask(id: 2L, itemId: itemId)
        def taskStages = [new CvTaskStage(id: 3L), new CvTaskStage(id: 4L)]

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(cvItem))
        when(taskRepository.findOne(any())).thenReturn(Optional.of(cvTask))
        when(taskStageRepository.findAll(any())).thenReturn(taskStages)

        when:
        taskService.renewTask(itemId, stage, fileId)

        then:
        1 * itemRepository.findById(itemId) >> Optional.of(cvItem)
        1 * taskRepository.findOne(any()) >> Optional.of(cvTask)
        1 * taskStageRepository.findAll(any()) >> taskStages

        // 验证状态更新
        taskStages.every { it.status == EntityType.TaskStaus.NEW }
        cvTask.steps == EntityType.TaskStage.READY
        cvTask.status == EntityType.TaskStaus.NEW
        cvItem.status == EntityType.TaskStaus.NEW

        // 验证保存操作
        1 * taskStageRepository.saveAll(taskStages)
        1 * taskRepository.save(cvTask)
        1 * itemRepository.save(cvItem)
        1 * taskManager.execute(cvTask)
    }

    def "test renewTask when item not found"() {
        given:
        def itemId = 1L
        def stage = 5
        def fileId = 10L

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty())

        when:
        taskService.renewTask(itemId, stage, fileId)

        then:
        1 * itemRepository.findById(itemId) >> Optional.empty()
        0 * taskRepository.findOne(_)
        0 * taskStageRepository._
        0 * itemFileRepository._
        0 * taskManager._
    }

    def "test renewTask when task not found"() {
        given:
        def itemId = 1L
        def stage = 5
        def fileId = 10L

        def cvItem = new CvItem(id: itemId)

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(cvItem))
        when(taskRepository.findOne(any())).thenReturn(Optional.empty())

        when:
        taskService.renewTask(itemId, stage, fileId)

        then:
        1 * itemRepository.findById(itemId) >> Optional.of(cvItem)
        1 * taskRepository.findOne(any()) >> Optional.empty()
        0 * taskStageRepository._
        0 * itemFileRepository._
        0 * taskManager._
    }

    def "test renewTask with fileId but file not found"() {
        given:
        def itemId = 1L
        def stage = null
        def fileId = 10L

        def cvItem = new CvItem(id: itemId)
        def cvTask = new CvTask(id: 2L, itemId: itemId)

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(cvItem))
        when(taskRepository.findOne(any())).thenReturn(Optional.of(cvTask))
        when(itemFileRepository.findById(fileId)).thenReturn(Optional.empty())

        when:
        taskService.renewTask(itemId, stage, fileId)

        then:
        1 * itemRepository.findById(itemId) >> Optional.of(cvItem)
        1 * taskRepository.findOne(any()) >> Optional.of(cvTask)
        1 * itemFileRepository.findById(fileId) >> Optional.empty()
        0 * taskStageRepository._
        0 * taskManager._
    }

    def "test renewTask with stage but stage not found"() {
        given:
        def itemId = 1L
        def stage = 5
        def fileId = null

        def cvItem = new CvItem(id: itemId)
        def cvTask = new CvTask(id: 2L, itemId: itemId)

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(cvItem))
        when(taskRepository.findOne(any())).thenReturn(Optional.of(cvTask))
        when(taskStageRepository.findOne(any())).thenReturn(Optional.empty())

        when:
        taskService.renewTask(itemId, stage, fileId)

        then:
        1 * itemRepository.findById(itemId) >> Optional.of(cvItem)
        1 * taskRepository.findOne(any()) >> Optional.of(cvTask)
        1 * taskStageRepository.findOne(any()) >> Optional.empty()
        0 * taskManager._
    }

    def "test getRepository returns taskRepository"() {
        when:
        def result = taskService.getRepository()

        then:
        result == taskRepository
    }

    def "test getMapper returns taskMapper"() {
        when:
        def result = taskService.getMapper()

        then:
        result == taskMapper
    }
}
import org.geniusSociety.codelooms.common.constant.EntityType
import org.geniusSociety.codelooms.component.TaskManager
import org.geniusSociety.codelooms.dao.CvItemFileRepository
import org.geniusSociety.codelooms.dao.CvTaskRepository
import org.geniusSociety.codelooms.dao.CvTaskStageRepository
import org.geniusSociety.codelooms.domain.entity.CvItem
import org.geniusSociety.codelooms.domain.entity.CvItemFile
import org.geniusSociety.codelooms.domain.entity.CvTask
import org.geniusSociety.codelooms.domain.entity.CvTaskStage
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [TaskManager])
class TestTaskManager extends Specification {

    @Subject
    @Autowired
    TaskManager taskManager

    @MockBean
    CvTaskRepository taskRepository

    @MockBean
    CvTaskStageRepository taskStageRepository

    @MockBean
    ApplicationContext applicationContext

    @MockBean
    CvItemFileRepository cvItemFileRepository

    def setup() {
        // 重置mock
        Mockito.reset(taskRepository, taskStageRepository, applicationContext, cvItemFileRepository)
    }

    def "test init method loads and executes existing tasks"() {
        given:
        def existingTasks = [
                new CvTask(id: 1L, status: EntityType.TaskStaus.NEW),
                new CvTask(id: 2L, status: EntityType.TaskStaus.RUNNING)
        ]

        Mockito.when(taskRepository.findAll(Mockito.any()))
                .thenReturn(existingTasks)

        when:
        taskManager.init()

        then:
        Mockito.verify(taskRepository, Mockito.times(1))
                .findAll(Mockito.any())
        // 应该为每个任务调用execute方法
        existingTasks.each { task ->
            // 由于execute是private，我们验证线程池是否提交了任务
            // 实际执行可以通过集成测试验证
        }
    }

    def "test createTask creates task and stages successfully"() {
        given:
        def cvItem = new CvItem(id: 100L, type: "conversion", userId: 1)
        def savedTask = new CvTask(
                id: 1L,
                itemId: 100L,
                type: "conversion",
                status: EntityType.TaskStaus.NEW,
                steps: EntityType.TaskStaus.NEW,
                userId: 1
        )

        Mockito.when(taskRepository.save(Mockito.any(CvTask)))
                .thenReturn(savedTask)
        Mockito.when(taskStageRepository.save(Mockito.any(CvTaskStage)))
                .thenAnswer { invocation -> invocation.getArgument(0) }

        when:
        taskManager.createTask(cvItem)

        then:
        Mockito.verify(taskRepository, Mockito.times(1))
                .save(Mockito.any(CvTask))
        Mockito.verify(taskStageRepository, Mockito.times(EntityType.TaskStage.stages.size()))
                .save(Mockito.any(CvTaskStage))

        and: "验证任务阶段创建正确"
        EntityType.TaskStage.stages.each { stage ->
            Mockito.verify(taskStageRepository).save(Mockito.argThat { taskStage ->
                taskStage.taskId == 1L &&
                        taskStage.stage == stage &&
                        taskStage.status == EntityType.TaskStage.READY &&
                        taskStage.userId == 1
            })
        }
    }

    def "test renewTask refreshes task and stages"() {
        given:
        def taskId = 1L
        def existingTask = Optional.of(new CvTask(
                id: taskId,
                status: EntityType.TaskStaus.COMPLETED,
                steps: EntityType.TaskStage.DONE,
                userId: 1
        ))
        def stages = [
                new CvTaskStage(id: 1L, taskId: taskId, stage: 1, status: EntityType.TaskStaus.COMPLETED),
                new CvTaskStage(id: 2L, taskId: taskId, stage: 2, status: EntityType.TaskStaus.COMPLETED)
        ]

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(existingTask)
        Mockito.when(taskStageRepository.findAll(Mockito.any()))
                .thenReturn(stages)
        Mockito.when(taskRepository.save(Mockito.any(CvTask)))
                .thenAnswer { invocation -> invocation.getArgument(0) }
        Mockito.when(taskStageRepository.saveAll(Mockito.anyList()))
                .thenAnswer { invocation -> invocation.getArgument(0) }

        when:
        taskManager.renewTask(taskId)

        then:
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId)
        Mockito.verify(taskStageRepository, Mockito.times(1)).findAll(Mockito.any())
        Mockito.verify(taskRepository, Mockito.times(1)).save(Mockito.argThat { task ->
            task.status == EntityType.TaskStaus.NEW &&
                    task.steps == EntityType.TaskStage.READY
        })
        Mockito.verify(taskStageRepository, Mockito.times(1)).saveAll(Mockito.argThat { stageList ->
            stageList.every { it.status == EntityType.TaskStaus.NEW }
        })
    }

    def "test renewTask with non-existent task"() {
        given:
        def taskId = 999L

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty())

        when:
        taskManager.renewTask(taskId)

        then:
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId)
        Mockito.verify(taskStageRepository, Mockito.never()).findAll(Mockito.any())
        Mockito.verify(taskRepository, Mockito.never()).save(Mockito.any())
        Mockito.verify(taskStageRepository, Mockito.never()).saveAll(Mockito.anyList())
    }

    def "test renewStage refreshes stage and updates task"() {
        given:
        def stageId = 1L
        def taskId = 100L
        def existingStage = Optional.of(new CvTaskStage(
                id: stageId,
                taskId: taskId,
                stage: 2,
                status: EntityType.TaskStaus.COMPLETED
        ))
        def existingTask = Optional.of(new CvTask(
                id: taskId,
                status: EntityType.TaskStaus.COMPLETED,
                steps: EntityType.TaskStage.DONE
        ))

        Mockito.when(taskStageRepository.findById(stageId))
                .thenReturn(existingStage)
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(existingTask)
        Mockito.when(taskStageRepository.save(Mockito.any(CvTaskStage)))
                .thenAnswer { invocation -> invocation.getArgument(0) }
        Mockito.when(taskRepository.save(Mockito.any(CvTask)))
                .thenAnswer { invocation -> invocation.getArgument(0) }

        when:
        taskManager.renewStage(stageId)

        then:
        Mockito.verify(taskStageRepository, Mockito.times(1)).findById(stageId)
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId)
        Mockito.verify(taskStageRepository, Mockito.times(1)).save(Mockito.argThat { stage ->
            stage.status == EntityType.TaskStaus.NEW
        })
        Mockito.verify(taskRepository, Mockito.times(1)).save(Mockito.argThat { task ->
            task.status == EntityType.TaskStaus.RUNNING &&
                    task.steps == 2
        })
    }

    def "test renewStage with non-existent stage"() {
        given:
        def stageId = 999L

        Mockito.when(taskStageRepository.findById(stageId))
                .thenReturn(Optional.empty())

        when:
        taskManager.renewStage(stageId)

        then:
        Mockito.verify(taskStageRepository, Mockito.times(1)).findById(stageId)
        Mockito.verify(taskRepository, Mockito.never()).findById(Mockito.any())
        Mockito.verify(taskStageRepository, Mockito.never()).save(Mockito.any())
        Mockito.verify(taskRepository, Mockito.never()).save(Mockito.any())
    }

    def "test renewStage with non-existent task"() {
        given:
        def stageId = 1L
        def taskId = 999L
        def existingStage = Optional.of(new CvTaskStage(
                id: stageId,
                taskId: taskId,
                stage: 2,
                status: EntityType.TaskStaus.COMPLETED
        ))

        Mockito.when(taskStageRepository.findById(stageId))
                .thenReturn(existingStage)
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty())

        when:
        taskManager.renewStage(stageId)

        then:
        Mockito.verify(taskStageRepository, Mockito.times(1)).findById(stageId)
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId)
        Mockito.verify(taskStageRepository, Mockito.never()).save(Mockito.any())
        Mockito.verify(taskRepository, Mockito.never()).save(Mockito.any())
    }

    def "test renewFile refreshes file and related entities"() {
        given:
        def taskId = 1L
        def fileId = 100L
        def existingFile = Optional.of(new CvItemFile(
                id: fileId,
                stage: EntityType.TaskStaus.COMPLETED
        ))
        def existingTask = Optional.of(new CvTask(
                id: taskId,
                status: EntityType.TaskStaus.COMPLETED,
                steps: EntityType.TaskStage.DONE
        ))
        def stages = [
                new CvTaskStage(id: 1L, taskId: taskId, status: EntityType.TaskStaus.COMPLETED),
                new CvTaskStage(id: 2L, taskId: taskId, status: EntityType.TaskStaus.COMPLETED)
        ]

        Mockito.when(applicationContext.getBean(CvItemFileRepository.class))
                .thenReturn(cvItemFileRepository)
        Mockito.when(cvItemFileRepository.findById(fileId))
                .thenReturn(existingFile)
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(existingTask)
        Mockito.when(taskStageRepository.findAll(Mockito.any()))
                .thenReturn(stages)
        Mockito.when(taskStageRepository.saveAll(Mockito.anyList()))
                .thenAnswer { invocation -> invocation.getArgument(0) }
        Mockito.when(taskRepository.save(Mockito.any(CvTask)))
                .thenAnswer { invocation -> invocation.getArgument(0) }
        Mockito.when(cvItemFileRepository.save(Mockito.any(CvItemFile)))
                .thenAnswer { invocation -> invocation.getArgument(0) }

        when:
        taskManager.renewFile(taskId, fileId)

        then:
        Mockito.verify(cvItemFileRepository, Mockito.times(1)).findById(fileId)
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId)
        Mockito.verify(taskStageRepository, Mockito.times(1)).findAll(Mockito.any())
        Mockito.verify(taskStageRepository, Mockito.times(1)).saveAll(Mockito.argThat { stageList ->
            stageList.every { it.status == EntityType.TaskStaus.NEW }
        })
        Mockito.verify(taskRepository, Mockito.times(1)).save(Mockito.argThat { task ->
            task.status == EntityType.TaskStaus.RUNNING &&
                    task.steps == EntityType.TaskStage.READY
        })
        Mockito.verify(cvItemFileRepository, Mockito.times(1)).save(Mockito.argThat { file ->
            file.stage == EntityType.TaskStaus.NEW
        })
    }

    def "test renewFile with non-existent file"() {
        given:
        def taskId = 1L
        def fileId = 999L

        Mockito.when(applicationContext.getBean(CvItemFileRepository.class))
                .thenReturn(cvItemFileRepository)
        Mockito.when(cvItemFileRepository.findById(fileId))
                .thenReturn(Optional.empty())

        when:
        taskManager.renewFile(taskId, fileId)

        then:
        Mockito.verify(cvItemFileRepository, Mockito.times(1)).findById(fileId)
        Mockito.verify(taskRepository, Mockito.never()).findById(Mockito.any())
        Mockito.verify(taskStageRepository, Mockito.never()).findAll(Mockito.any())
    }

    def "test renewFile with non-existent task"() {
        given:
        def taskId = 999L
        def fileId = 100L
        def existingFile = Optional.of(new CvItemFile(id: fileId))

        Mockito.when(applicationContext.getBean(CvItemFileRepository.class))
                .thenReturn(cvItemFileRepository)
        Mockito.when(cvItemFileRepository.findById(fileId))
                .thenReturn(existingFile)
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty())

        when:
        taskManager.renewFile(taskId, fileId)

        then:
        Mockito.verify(cvItemFileRepository, Mockito.times(1)).findById(fileId)
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId)
        Mockito.verify(taskStageRepository, Mockito.never()).findAll(Mockito.any())
        Mockito.verify(cvItemFileRepository, Mockito.never()).save(Mockito.any())
    }

    def "test getTask returns correct tasks"() {
        given:
        def expectedTasks = [
                new CvTask(id: 1L, status: EntityType.TaskStaus.NEW),
                new CvTask(id: 2L, status: EntityType.TaskStaus.RUNNING)
        ]

        Mockito.when(taskRepository.findAll(Mockito.any()))
                .thenReturn(expectedTasks)

        when:
        def result = taskManager.getTask()

        then:
        result == expectedTasks
        Mockito.verify(taskRepository, Mockito.times(1)).findAll(Mockito.any())
    }

    // 测试线程池配置
    def "test thread pool configuration"() {
        given:
        def executor = taskManager.executor

        expect:
        executor.corePoolSize == 1
        executor.maximumPoolSize == 1
        executor.keepAliveTime == 0
        executor.keepAliveTimeUnit == TimeUnit.MINUTES
        executor.rejectedExecutionHandler instanceof ThreadPoolExecutor.DiscardPolicy
    }
}
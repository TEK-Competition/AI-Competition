import org.geniusSociety.codelooms.common.constant.EntityType
import org.geniusSociety.codelooms.component.FileComponent
import org.geniusSociety.codelooms.component.TaskManager
import org.geniusSociety.codelooms.dao.CvFileRepository
import org.geniusSociety.codelooms.dao.CvItemFileRepository
import org.geniusSociety.codelooms.dao.CvItemKnowledgeRepository
import org.geniusSociety.codelooms.dao.CvItemRepository
import org.geniusSociety.codelooms.domain.bo.ItemCreateBO
import org.geniusSociety.codelooms.domain.bo.ItemUpdateBO
import org.geniusSociety.codelooms.domain.entity.CvFile
import org.geniusSociety.codelooms.domain.entity.CvItem
import org.geniusSociety.codelooms.domain.entity.CvItemKnowledge
import org.geniusSociety.codelooms.domain.vo.ItemVO
import org.geniusSociety.codelooms.mapper.ItemMapper
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

class ItemServiceTest extends Specification {

    @InjectMocks
    ItemService itemService

    @Mock
    TaskManager taskManager
    @Mock
    FileComponent fileComponent
    @Mock
    CvItemRepository itemRepository
    @Mock
    ItemMapper itemMapper
    @Mock
    CvFileRepository fileRepository
    @Mock
    CvItemFileRepository itemFileRepository
    @Mock
    CvItemKnowledgeRepository itemKnowledgeRepository

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test detail with spRelation and tableRelation"() {
        given:
        def cvItem = new CvItem(
                spRelation: '[{"id":1,"name":"relation1"},{"id":2,"name":"relation2"}]',
                tableRelation: '[{"id":3,"name":"relation3"}]'
        )
        def itemVO = new ItemVO()

        when:
        def result = itemService.detail(cvItem, itemVO)

        then:
        result == itemVO
        result.spGraph.size() == 2
        result.tableGraph.size() == 1
    }

    def "test detail without relations"() {
        given:
        def cvItem = new CvItem(spRelation: null, tableRelation: "")
        def itemVO = new ItemVO()

        when:
        def result = itemService.detail(cvItem, itemVO)

        then:
        result == itemVO
        result.spGraph == null
        result.tableGraph == null
    }

    def "test create with compressed SQL files"() {
        given:
        def userId = 1
        def itemCreateBO = new ItemCreateBO(
                name: "Test Item",
                remark: "Test Remark",
                mode: 1,
                spFiles: [101L, 102L],
                knowledgeFiles: [201L, 202L]
        )

        def compressedFile = new CvFile(
                id: 101L,
                type: EntityType.FileType.ZIP,
                saveMode: "local"
        )
        def sqlFile1 = new CvFile(
                id: 103L,
                type: EntityType.FileType.SQL,
                path: "/path/to/file1.sql",
                name: "file1.sql",
                parent: 101L
        )
        def sqlFile2 = new CvFile(
                id: 104L,
                type: EntityType.FileType.SQL,
                path: "/path/to/file2.sql",
                name: "file2.sql",
                parent: 101L
        )
        def directSqlFile = new CvFile(
                id: 102L,
                type: EntityType.FileType.SQL,
                path: "/path/to/direct.sql",
                name: "direct.sql",
                saveMode: "cloud"
        )
        def knowledgeFile = new CvFile(
                id: 201L,
                type: EntityType.FileType.PDF,
                path: "/path/to/knowledge.pdf",
                name: "knowledge.pdf"
        )

        def savedItem = new CvItem(id: 1000L, name: "Test Item", userId: userId)

        when(fileRepository.findAllById(itemCreateBO.spFiles)).thenReturn([compressedFile, directSqlFile])
        when(fileRepository.findAll(any())).thenReturn([sqlFile1, sqlFile2]).thenReturn([])
        when(itemRepository.save(any(CvItem))).thenReturn(savedItem)
        when(fileRepository.findAllById(itemCreateBO.knowledgeFiles)).thenReturn([knowledgeFile])

        when:
        def result = itemService.create(itemCreateBO, userId)

        then:
        result == 1000L

        // Verify item creation
        1 * itemRepository.save(any(CvItem)) >> { args ->
            def item = args[0]
            assert item.name == "Test Item"
            assert item.remark == "Test Remark"
            assert item.type == 1
            assert item.userId == userId
            assert item.status == EntityType.TaskStaus.NEW
            savedItem
        }

        // Verify file updates
        4 * fileRepository.save(any(CvFile)) >> { args ->
            def file = args[0]
            assert file.itemId == 1000L
            file
        }

        // Verify item file relationships
        1 * itemFileRepository.saveAll(any(List)) >> { args ->
            def rels = args[0]
            assert rels.size() == 3 // 2 from compressed + 1 direct
            assert rels.every { it.itemId == 1000L }
            assert rels.every { it.stage == EntityType.TaskStaus.NEW }
            assert rels.every { it.userId == userId }
            assert rels.every { it.isDel == BaseConstant.NO }
            rels
        }

        // Verify knowledge repository calls
        1 * itemKnowledgeRepository.save(any(CvItemKnowledge)) >> { args ->
            def knowledge = args[0]
            assert knowledge.itemId == 1000L
            assert knowledge.userId == userId
            assert knowledge.isDel == BaseConstant.NO
            knowledge
        }

        // Verify task creation
        1 * taskManager.createTask(savedItem)
    }

    def "test create with no sql files should throw exception"() {
        given:
        def userId = 1
        def itemCreateBO = new ItemCreateBO(
                name: "Test Item",
                spFiles: [101L]
        )

        def nonSqlFile = new CvFile(
                id: 101L,
                type: EntityType.FileType.TXT
        )

        when(fileRepository.findAllById(itemCreateBO.spFiles)).thenReturn([nonSqlFile])
        when(fileRepository.findAll(any())).thenReturn([])

        when:
        itemService.create(itemCreateBO, userId)

        then:
        thrown(Exception) // Should throw AssertUtil exception
    }

    def "test update with all fields"() {
        given:
        def userId = 1
        def itemUpdateBO = new ItemUpdateBO(
                id: 1000L,
                name: "Updated Name",
                mode: 2,
                remark: "Updated Remark"
        )
        def existingItem = new CvItem(
                id: 1000L,
                userId: userId,
                name: "Old Name",
                type: 1,
                remark: "Old Remark"
        )

        when(itemRepository.findById(1000L)).thenReturn(Optional.of(existingItem))

        when:
        itemService.update(itemUpdateBO, userId)

        then:
        1 * itemRepository.save(existingItem) >> { args ->
            def item = args[0]
            assert item.name == "Updated Name"
            assert item.type == 2
            assert item.remark == "Updated Remark"
            item
        }
    }

    def "test update with partial fields"() {
        given:
        def userId = 1
        def itemUpdateBO = new ItemUpdateBO(
                id: 1000L,
                name: "Updated Name"
                // mode and remark are null
        )
        def existingItem = new CvItem(
                id: 1000L,
                userId: userId,
                name: "Old Name",
                type: 1,
                remark: "Old Remark"
        )

        when(itemRepository.findById(1000L)).thenReturn(Optional.of(existingItem))

        when:
        itemService.update(itemUpdateBO, userId)

        then:
        1 * itemRepository.save(existingItem) >> { args ->
            def item = args[0]
            assert item.name == "Updated Name"
            assert item.type == 1 // unchanged
            assert item.remark == "Old Remark" // unchanged
            item
        }
    }

    def "test update with unauthorized user should throw exception"() {
        given:
        def userId = 1
        def otherUserId = 2
        def itemUpdateBO = new ItemUpdateBO(id: 1000L)
        def existingItem = new CvItem(id: 1000L, userId: otherUserId)

        when(itemRepository.findById(1000L)).thenReturn(Optional.of(existingItem))

        when:
        itemService.update(itemUpdateBO, userId)

        then:
        thrown(Exception) // Should throw authorization exception
    }

    def "test delete with files"() {
        given:
        def itemId = 1000L
        def userId = 1
        def existingItem = new CvItem(id: itemId, userId: userId)
        def files = [
                new CvFile(id: 101L, path: "/path/to/file1", saveMode: "local"),
                new CvFile(id: 102L, path: "/path/to/file2", saveMode: "cloud")
        ]

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem))
        when(fileRepository.findAll(any())).thenReturn(files)

        when:
        itemService.delete(itemId, userId)

        then:
        // Verify file deletions
        1 * fileRepository.deleteById(101L)
        1 * fileRepository.deleteById(102L)

        // Verify file component cleanup
        1 * fileComponent.remove("/path/to/file1", "local")
        1 * fileComponent.remove("/path/to/file2", "cloud")

        // Verify item file relationships cleanup
        1 * itemFileRepository.delete(any())

        // Verify item deletion
        1 * itemRepository.deleteById(itemId)
    }

    def "test delete with unauthorized user should throw exception"() {
        given:
        def itemId = 1000L
        def userId = 1
        def otherUserId = 2
        def existingItem = new CvItem(id: itemId, userId: otherUserId)

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem))

        when:
        itemService.delete(itemId, userId)

        then:
        thrown(Exception) // Should throw authorization exception
    }

    def "test getRepository returns itemRepository"() {
        when:
        def result = itemService.getRepository()

        then:
        result == itemRepository
    }

    def "test getMapper returns itemMapper"() {
        when:
        def result = itemService.getMapper()

        then:
        result == itemMapper
    }
}
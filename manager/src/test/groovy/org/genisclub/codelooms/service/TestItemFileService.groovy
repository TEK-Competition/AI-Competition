import cn.hutool.core.io.FileUtil
import org.geniusSociety.codelooms.common.bo.BaseQuery
import org.geniusSociety.codelooms.common.constant.BaseConstant
import org.geniusSociety.codelooms.dao.CvItemFileRepository
import org.geniusSociety.codelooms.domain.entity.CvItemFile
import org.geniusSociety.codelooms.domain.vo.FileVO
import org.geniusSociety.codelooms.mapper.ItemFileMapper
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

import javax.persistence.criteria.*

class ItemFileServiceTest extends Specification {

    @InjectMocks
    ItemFileService itemFileService

    @Mock
    ItemFileMapper itemFileMapper
    @Mock
    CvItemFileRepository itemFileRepository

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test detail with both spPath and sqlPath files existing"() {
        given:
        def spContent = "存储过程内容"
        def sqlContent = "SQL语句内容"
        def spFile = Mock(File) {
            exists() >> true
        }
        def sqlFile = Mock(File) {
            exists() >> true
        }

        def record = new CvItemFile(
                spPath: "/path/to/sp.sql",
                sqlPath: "/path/to/query.sql"
        )
        def vo = new FileVO()

        when(FileUtil.file("/path/to/sp.sql")).thenReturn(spFile)
        when(FileUtil.file("/path/to/query.sql")).thenReturn(sqlFile)
        when(FileUtil.readString(spFile, CharsetUtil.CHARSET_UTF_8)).thenReturn(spContent)
        when(FileUtil.readString(sqlFile, CharsetUtil.CHARSET_UTF_8)).thenReturn(sqlContent)

        when:
        def result = itemFileService.detail(record, vo)

        then:
        result == vo
        result.sp == spContent
        result.sql == sqlContent
    }

    def "test detail with only spPath file existing"() {
        given:
        def spContent = "存储过程内容"
        def spFile = Mock(File) {
            exists() >> true
        }

        def record = new CvItemFile(
                spPath: "/path/to/sp.sql",
                sqlPath: null
        )
        def vo = new FileVO()

        when(FileUtil.file("/path/to/sp.sql")).thenReturn(spFile)
        when(FileUtil.readString(spFile, CharsetUtil.CHARSET_UTF_8)).thenReturn(spContent)

        when:
        def result = itemFileService.detail(record, vo)

        then:
        result == vo
        result.sp == spContent
        result.sql == null
    }

    def "test detail with only sqlPath file existing"() {
        given:
        def sqlContent = "SQL语句内容"
        def sqlFile = Mock(File) {
            exists() >> true
        }

        def record = new CvItemFile(
                spPath: null,
                sqlPath: "/path/to/query.sql"
        )
        def vo = new FileVO()

        when(FileUtil.file("/path/to/query.sql")).thenReturn(sqlFile)
        when(FileUtil.readString(sqlFile, CharsetUtil.CHARSET_UTF_8)).thenReturn(sqlContent)

        when:
        def result = itemFileService.detail(record, vo)

        then:
        result == vo
        result.sp == null
        result.sql == sqlContent
    }

    def "test detail with non-existing files"() {
        given:
        def spFile = Mock(File) {
            exists() >> false
        }
        def sqlFile = Mock(File) {
            exists() >> false
        }

        def record = new CvItemFile(
                spPath: "/path/to/sp.sql",
                sqlPath: "/path/to/query.sql"
        )
        def vo = new FileVO()

        when(FileUtil.file("/path/to/sp.sql")).thenReturn(spFile)
        when(FileUtil.file("/path/to/query.sql")).thenReturn(sqlFile)

        when:
        def result = itemFileService.detail(record, vo)

        then:
        result == vo
        result.sp == null
        result.sql == null

        0 * FileUtil.readString(_, _)
    }

    def "test detail with empty file paths"() {
        given:
        def record = new CvItemFile(
                spPath: "",
                sqlPath: ""
        )
        def vo = new FileVO()

        when:
        def result = itemFileService.detail(record, vo)

        then:
        result == vo
        result.sp == null
        result.sql == null

        0 * FileUtil.file(_)
        0 * FileUtil.readString(_, _)
    }

    def "test spec with name query"() {
        given:
        def query = new BaseQuery(id: 1000L, name: "test")
        def userId = 1

        when:
        def spec = itemFileService.spec(query, userId)
        def predicate = spec.toPredicate(null, null, null)

        then:
        // 这里主要验证spec方法不会抛出异常
        // 实际的Predicate逻辑测试可能需要更复杂的集成测试
        noExceptionThrown()
    }

    def "test spec without name query"() {
        given:
        def query = new BaseQuery(id: 1000L, name: null)
        def userId = 1

        when:
        def spec = itemFileService.spec(query, userId)
        def predicate = spec.toPredicate(null, null, null)

        then:
        noExceptionThrown()
    }

    def "test delete success"() {
        given:
        def fileId = 1000L
        def userId = 1
        def existingFile = new CvItemFile(
                id: fileId,
                userId: userId,
                isDel: BaseConstant.NO
        )

        when(itemFileRepository.findById(fileId)).thenReturn(Optional.of(existingFile))

        when:
        itemFileService.delete(fileId, userId)

        then:
        1 * itemFileRepository.save(_) >> { args ->
            def file = args[0]
            assert file.isDel == BaseConstant.YES
            file
        }
    }

    def "test delete with file not found"() {
        given:
        def fileId = 1000L
        def userId = 1

        when(itemFileRepository.findById(fileId)).thenReturn(Optional.empty())

        when:
        itemFileService.delete(fileId, userId)

        then:
        0 * itemFileRepository.save(_)
    }

    def "test delete with unauthorized user should throw exception"() {
        given:
        def fileId = 1000L
        def userId = 1
        def otherUserId = 2
        def existingFile = new CvItemFile(
                id: fileId,
                userId: otherUserId
        )

        when(itemFileRepository.findById(fileId)).thenReturn(Optional.of(existingFile))

        when:
        itemFileService.delete(fileId, userId)

        then:
        thrown(Exception) // Should throw authorization exception
    }

    def "test getRepository returns itemFileRepository"() {
        when:
        def result = itemFileService.getRepository()

        then:
        result == itemFileRepository
    }

    def "test getMapper returns itemFileMapper"() {
        when:
        def result = itemFileService.getMapper()

        then:
        result == itemFileMapper
    }

    // 辅助方法用于测试spec方法的Predicate逻辑（可选）
    def "test spec predicate logic - integration style"() {
        given:
        def query = new BaseQuery(id: 1000L, name: "test")
        def userId = 1

        // 创建mock的CriteriaBuilder和Root
        def cb = Mock(javax.persistence.criteria.CriteriaBuilder)
        def root = Mock(javax.persistence.criteria.Root)
        def queryMock = Mock(javax.persistence.criteria.CriteriaQuery)
        def path = Mock(javax.persistence.criteria.Path)

        when(root.get("itemId")).thenReturn(path)
        when(root.get("isDel")).thenReturn(path)
        when(root.get("userId")).thenReturn(path)
        when(root.get("name")).thenReturn(path)

        when(cb.equal(any(), any())).thenReturn(Mock(javax.persistence.criteria.Predicate))
        when(cb.like(any(), any())).thenReturn(Mock(javax.persistence.criteria.Predicate))
        when(cb.and(any())).thenReturn(Mock(javax.persistence.criteria.Predicate))

        when:
        def spec = itemFileService.spec(query, userId)
        def predicate = spec.toPredicate(root, queryMock, cb)

        then:
        noExceptionThrown()

        // 验证必要的交互
        1 * root.get("itemId")
        1 * root.get("isDel")
        1 * root.get("userId")
        1 * root.get("name")
        3 * cb.equal(_, _)
        1 * cb.like(_, _)
        2 * cb.and(_)
    }
}
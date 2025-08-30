import org.geniusSociety.codelooms.common.constant.ErrorCode
import org.geniusSociety.codelooms.common.exception.AssertException
import org.geniusSociety.codelooms.component.FileComponent
import org.geniusSociety.codelooms.dao.CvFileRepository
import org.geniusSociety.codelooms.domain.dto.FileDTO
import org.geniusSociety.codelooms.domain.entity.CvFile
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
class TestFileManagerServiceSpec extends Specification {

    @InjectMocks
    FileManagerService fileManagerService

    @Mock
    CvFileRepository fileRepository

    @Mock
    FileComponent fileComponent

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test create file successfully"() {
        given: "准备测试数据"
        def multipartFile = Mock(MultipartFile)
        def userId = 1
        def fileDTO = new FileDTO(
                fileName: "test.txt",
                filePath: "/path/to/file",
                fileType: "txt",
                saveMode: 1,
                files: []
        )

        def savedFile = CvFile.builder()
                .id(1L)
                .name("test.txt")
                .path("/path/to/file")
                .type("txt")
                .saveMode(1)
                .parent(0L)
                .userId(userId)
                .build()

        when: "调用创建文件方法"
        def result = fileManagerService.create(multipartFile, userId)

        then: "验证结果"
        1 * fileComponent.save(multipartFile, userId) >> fileDTO
        1 * fileRepository.save(_ as CvFile) >> savedFile
        0 * fileRepository.saveAll(_)

        result != null
        result.id == 1L
        result.name == "test.txt"
    }

    def "test create compressed file with valid content"() {
        given: "准备压缩文件测试数据"
        def multipartFile = Mock(MultipartFile)
        def userId = 1
        def innerFile1 = new FileDTO.FileInfo(fileName: "inner1.jpg", filePath: "/path/to/inner1", fileType: "jpg", saveMode: 1)
        def innerFile2 = new FileDTO.FileInfo(fileName: "inner2.png", filePath: "/path/to/inner2", fileType: "png", saveMode: 1)

        def fileDTO = new FileDTO(
                fileName: "test.zip",
                filePath: "/path/to/file",
                fileType: "zip",
                saveMode: 1,
                files: [innerFile1, innerFile2]
        )

        def savedFile = CvFile.builder()
                .id(1L)
                .name("test.zip")
                .path("/path/to/file")
                .type("zip")
                .saveMode(1)
                .parent(0L)
                .userId(userId)
                .build()

        when: "调用创建压缩文件方法"
        def result = fileManagerService.create(multipartFile, userId)

        then: "验证结果"
        1 * fileComponent.save(multipartFile, userId) >> fileDTO
        1 * fileRepository.save(_ as CvFile) >> savedFile
        1 * fileRepository.saveAll(_) >> [CvFile.builder().build(), CvFile.builder().build()]

        result != null
        result.id == 1L
    }

    @Unroll
    def "test create compressed file with invalid content should throw exception - #scenario"() {
        given: "准备无效的压缩文件测试数据"
        def multipartFile = Mock(MultipartFile)
        def userId = 1
        def fileDTO = new FileDTO(
                fileName: "test.zip",
                filePath: "/path/to/file",
                fileType: "zip",
                saveMode: 1,
                files: innerFiles
        )

        when: "调用创建压缩文件方法"
        fileManagerService.create(multipartFile, userId)

        then: "应该抛出异常"
        1 * fileComponent.save(multipartFile, userId) >> fileDTO
        def e = thrown(expectedException)
        e.code == expectedCode
        e.desc == expectedDesc

        where:
        scenario             | innerFiles                                                            | expectedException | expectedCode                          | expectedDesc
        "empty content"      | []                                                                    | AssertException   | ErrorCode.FILE_CONTENT_FORBIDDEN.code | ErrorCode.FILE_CONTENT_FORBIDDEN.desc
        "invalid file types" | [new FileDTO.FileInfo(fileName: "test.invalid", fileType: "invalid")] | AssertException   | ErrorCode.FILE_CONTENT_FORBIDDEN.code | ErrorCode.FILE_CONTENT_FORBIDDEN.desc
    }

    def "test remove file successfully"() {
        given: "准备删除文件测试数据"
        def fileId = 1L
        def userId = 1
        def existingFile = Optional.of(CvFile.builder()
                .id(fileId)
                .name("test.txt")
                .path("/path/to/file")
                .type("txt")
                .saveMode(1)
                .userId(userId)
                .build())

        when: "调用删除文件方法"
        fileManagerService.remove(fileId, userId)

        then: "验证结果"
        1 * fileRepository.findById(fileId) >> existingFile
        1 * fileRepository.deleteById(fileId)
        1 * fileComponent.remove("/path/to/file", 1)
        0 * fileRepository.findAll(_)
    }

    def "test remove file with children"() {
        given: "准备删除有子文件的测试数据"
        def fileId = 1L
        def userId = 1
        def existingFile = Optional.of(CvFile.builder()
                .id(fileId)
                .name("test.zip")
                .path("/path/to/file")
                .type("zip")
                .saveMode(1)
                .parent(0L)
                .userId(userId)
                .build())

        def childFile1 = CvFile.builder().id(2L).path("/path/to/child1").saveMode(1).build()
        def childFile2 = CvFile.builder().id(3L).path("/path/to/child2").saveMode(1).build()

        when: "调用删除文件方法"
        fileManagerService.remove(fileId, userId)

        then: "验证结果"
        1 * fileRepository.findById(fileId) >> existingFile
        1 * fileRepository.deleteById(fileId)
        1 * fileComponent.remove("/path/to/file", 1)
        1 * fileRepository.findAll(_) >> [childFile1, childFile2]
        1 * fileRepository.deleteById(2L)
        1 * fileComponent.remove("/path/to/child1", 1)
        1 * fileRepository.deleteById(3L)
        1 * fileComponent.remove("/path/to/child2", 1)
    }

    def "test remove non-existent file should throw exception"() {
        given: "准备不存在的文件测试数据"
        def fileId = 1L
        def userId = 1

        when: "调用删除文件方法"
        fileManagerService.remove(fileId, userId)

        then: "应该抛出异常"
        1 * fileRepository.findById(fileId) >> Optional.empty()
        def e = thrown(AssertException)
        e.code == ErrorCode.REQUEST_NOT_FOUND.code
        e.desc == ErrorCode.REQUEST_NOT_FOUND.desc
    }

    def "test remove file with wrong user should throw exception"() {
        given: "准备错误的用户测试数据"
        def fileId = 1L
        def userId = 1
        def wrongUserId = 2
        def existingFile = Optional.of(CvFile.builder()
                .id(fileId)
                .userId(wrongUserId)
                .build())

        when: "调用删除文件方法"
        fileManagerService.remove(fileId, userId)

        then: "应该抛出异常"
        1 * fileRepository.findById(fileId) >> existingFile
        def e = thrown(AssertException)
        e.code == ErrorCode.REQUEST_NOT_FOUND.code
        e.desc == ErrorCode.REQUEST_NOT_FOUND.desc
    }
}

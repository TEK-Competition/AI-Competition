import org.geniusSociety.codelooms.common.constant.ErrorCode
import org.geniusSociety.codelooms.domain.vo.FileVO
import org.geniusSociety.codelooms.service.FileManagerService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class TestFileManagerController extends Specification {

    @InjectMocks
    FileManagerController fileManagerController

    @Mock
    FileManagerService fileService

    def setup() {
        MockitoAnnotations.openMocks(this)
    }

    def "test upload with valid file type"() {
        given: "准备有效的文件"
        def fileName = "test.jpg"
        def fileContent = "test file content".bytes
        MultipartFile file = new MockMultipartFile("file", fileName, "image/jpeg", fileContent)

        def userId = 123L
        def expectedFileVO = new FileVO() // 根据实际情况初始化FileVO对象

        when: "调用上传方法"
        def result = fileManagerController.upload(file)

        then: "验证服务调用并返回成功结果"
        1 * fileManagerController.getUserId() >> userId
        1 * fileService.create(file, userId) >> expectedFileVO
        result.isSuccess()
        result.getData() == expectedFileVO
    }

    def "test upload with invalid file type"() {
        given: "准备无效文件类型的文件"
        def fileName = "test.exe"
        def fileContent = "test file content".bytes
        MultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", fileContent)

        when: "调用上传方法"
        def result = fileManagerController.upload(file)

        then: "验证返回文件类型禁止的错误"
        result.isFail()
        result.getCode() == ErrorCode.FILE_TYPE_FORBIDDEN.getCode()

        and: "确保文件服务没有被调用"
        0 * fileService.create(_, _)
    }

    def "test upload with null file type"() {
        given: "准备没有文件扩展名的文件"
        def fileName = "test"
        def fileContent = "test file content".bytes
        MultipartFile file = new MockMultipartFile("file", fileName, "text/plain", fileContent)

        when: "调用上传方法"
        def result = fileManagerController.upload(file)

        then: "验证返回文件类型禁止的错误"
        result.isFail()
        result.getCode() == ErrorCode.FILE_TYPE_FORBIDDEN.getCode()

        and: "确保文件服务没有被调用"
        0 * fileService.create(_, _)
    }

    def "test delete file successfully"() {
        given: "准备文件ID"
        def fileId = 1L
        def userId = 123L

        when: "调用删除方法"
        def result = fileManagerController.delete(fileId)

        then: "验证服务调用并返回成功结果"
        1 * fileManagerController.getUserId() >> userId
        1 * fileService.remove(fileId, userId)
        result.isSuccess()
    }

    def "test delete with different file ids"() {
        given: "准备不同的文件ID"
        def fileId = 456L
        def userId = 789L

        when: "调用删除方法"
        def result = fileManagerController.delete(fileId)

        then: "验证服务调用并返回成功结果"
        1 * fileManagerController.getUserId() >> userId
        1 * fileService.remove(fileId, userId)
        result.isSuccess()
    }

    // 异常情况测试
    def "test upload with IOException"() {
        given: "准备有效的文件"
        def fileName = "test.jpg"
        def fileContent = "test file content".bytes
        MultipartFile file = new MockMultipartFile("file", fileName, "image/jpeg", fileContent)

        def userId = 123L

        when: "调用上传方法，但服务抛出IOException"
        fileManagerController.getUserId() >> userId
        fileService.create(file, userId) >> { throw new IOException("File storage error") }
        fileManagerController.upload(file)

        then: "验证IOException被抛出"
        thrown(IOException)
    }

    // 边界情况测试
    def "test upload with empty file"() {
        given: "准备空文件"
        def fileName = "empty.jpg"
        def fileContent = new byte[0]
        MultipartFile file = new MockMultipartFile("file", fileName, "image/jpeg", fileContent)

        def userId = 123L
        def expectedFileVO = new FileVO()

        when: "调用上传方法"
        def result = fileManagerController.upload(file)

        then: "验证服务调用并返回成功结果"
        1 * fileManagerController.getUserId() >> userId
        1 * fileService.create(file, userId) >> expectedFileVO
        result.isSuccess()
    }
}

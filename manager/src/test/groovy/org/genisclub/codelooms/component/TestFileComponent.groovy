import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.IdUtil
import org.geniusSociety.codelooms.component.FileComponent
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestPropertySource
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Files
import java.nio.file.Paths

@SpringBootTest(classes = [FileComponent.class])
@TestPropertySource(properties = [
        "config.file.save-mode=local",
        "config.file.base-path=/tmp/test-files"
])
class TestFileComponent extends Specification {

    @Subject
    @Autowired
    FileComponent fileComponent

    @MockBean
    IdUtil idUtil // Mock Snowflake 生成

    def setup() {
        // 清理测试目录
        FileUtil.del("/tmp/test-files")
    }

    def cleanup() {
        // 测试完成后清理目录
        FileUtil.del("/tmp/test-files")
    }

    def "test save file successfully"() {
        given:
        def mockFile = new MockMultipartFile(
                "test.txt",
                "test.txt",
                "text/plain",
                "Hello World".bytes
        )
        def userId = 123
        def expectedFileId = "1234567890"

        Mockito.when(idUtil.getSnowflake(1, 1).nextIdStr())
                .thenReturn(expectedFileId)

        when:
        def result = fileComponent.save(mockFile, userId)

        then:
        result != null
        result.fileName == "test.txt"
        result.fileType == "txt"
        result.saveMode == "local"
        result.filePath.contains("/tmp/test-files/123/1234567890.txt")
        result.files == null // 非压缩文件，files应为null

        and: "文件实际被创建"
        new File(result.filePath).exists()
    }

    def "test save zip file and decompress"() {
        given:
        // 创建测试zip文件
        def zipContent = Files.readAllBytes(Paths.get("src/test/resources/test.zip"))
        def mockFile = new MockMultipartFile(
                "test.zip",
                "test.zip",
                "application/zip",
                zipContent
        )
        def userId = 123
        def expectedFileId = "zip123456"

        Mockito.when(idUtil.getSnowflake(1, 1).nextIdStr())
                .thenReturn(expectedFileId)

        when:
        def result = fileComponent.save(mockFile, userId)

        then:
        result != null
        result.fileName == "test.zip"
        result.fileType == "zip"
        result.saveMode == "local"
        result.filePath.contains("/tmp/test-files/123/zip123456.zip")
        result.files != null
        result.files.size() > 0

        and: "解压后的文件存在"
        result.files.every { new File(it.filePath).exists() }
    }

    def "test save tar.gz file and decompress"() {
        given:
        // 创建测试tar.gz文件
        def tarGzContent = Files.readAllBytes(Paths.get("src/test/resources/test.tar.gz"))
        def mockFile = new MockMultipartFile(
                "test.tar.gz",
                "test.tar.gz",
                "application/gzip",
                tarGzContent
        )
        def userId = 123
        def expectedFileId = "targz123"

        Mockito.when(idUtil.getSnowflake(1, 1).nextIdStr())
                .thenReturn(expectedFileId)

        when:
        def result = fileComponent.save(mockFile, userId)

        then:
        result != null
        result.fileName == "test.tar.gz"
        result.fileType == "tar.gz"
        result.saveMode == "local"
        result.filePath.contains("/tmp/test-files/123/targz123.tar.gz")
        result.files != null
        result.files.size() > 0
    }

    def "test save with empty file"() {
        given:
        def mockFile = new MockMultipartFile(
                "empty.txt",
                "empty.txt",
                "text/plain",
                new byte[0]
        )
        def userId = 123
        def expectedFileId = "empty123"

        Mockito.when(idUtil.getSnowflake(1, 1).nextIdStr())
                .thenReturn(expectedFileId)

        when:
        def result = fileComponent.save(mockFile, userId)

        then:
        result != null
        result.fileName == "empty.txt"
        result.fileType == "txt"
        result.saveMode == "local"
        new File(result.filePath).exists()
    }

    def "test save with special characters in filename"() {
        given:
        def mockFile = new MockMultipartFile(
                "test file@123.txt",
                "test file@123.txt",
                "text/plain",
                "special chars".bytes
        )
        def userId = 123
        def expectedFileId = "special123"

        Mockito.when(idUtil.getSnowflake(1, 1).nextIdStr())
                .thenReturn(expectedFileId)

        when:
        def result = fileComponent.save(mockFile, userId)

        then:
        result != null
        result.fileName == "test file@123.txt"
        result.fileType == "txt"
        result.saveMode == "local"
        new File(result.filePath).exists()
    }

    def "test remove file successfully"() {
        given:
        def testFilePath = "/tmp/test-files/test-remove.txt"
        FileUtil.writeBytes("test content".bytes, testFilePath)

        expect: "文件存在"
        new File(testFilePath).exists()

        when:
        fileComponent.remove(testFilePath, "local")

        then:
        !new File(testFilePath).exists()
    }

    def "test remove non-existent file"() {
        given:
        def nonExistentPath = "/tmp/test-files/non-existent.txt"

        expect: "文件不存在"
        !new File(nonExistentPath).exists()

        when: "删除不存在的文件"
        fileComponent.remove(nonExistentPath, "local")

        then: "不应抛出异常"
        noExceptionThrown()
        !new File(nonExistentPath).exists()
    }

    def "test save with IOException"() {
        given:
        def mockFile = Mock(MultipartFile)
        def userId = 123

        Mockito.when(mockFile.getInputStream())
                .thenThrow(new IOException("File read error"))
        Mockito.when(mockFile.getOriginalFilename())
                .thenReturn("test.txt")

        when:
        fileComponent.save(mockFile, userId)

        then:
        thrown(IOException)
    }

    def "test save with different user ids"() {
        given:
        def mockFile = new MockMultipartFile(
                "test.txt",
                "test.txt",
                "text/plain",
                "user test".bytes
        )
        def userId = 999
        def expectedFileId = "user999"

        Mockito.when(idUtil.getSnowflake(1, 1).nextIdStr())
                .thenReturn(expectedFileId)

        when:
        def result = fileComponent.save(mockFile, userId)

        then:
        result != null
        result.filePath.contains("/tmp/test-files/999/")
        new File(result.filePath).exists()
    }

    // 测试不同的保存模式
    @SpringBootTest(classes = [FileComponent])
    @TestPropertySource(properties = [
            "config.file.save-mode=cloud",
            "config.file.base-path=/tmp/cloud-files"
    ])
    static class CloudModeSpec extends Specification {

        @Autowired
        FileComponent fileComponent

        @MockBean
        IdUtil idUtil

        def "test save with cloud mode"() {
            given:
            def mockFile = new MockMultipartFile(
                    "cloud.txt",
                    "cloud.txt",
                    "text/plain",
                    "cloud content".bytes
            )
            def userId = 123
            def expectedFileId = "cloud123"

            Mockito.when(idUtil.getSnowflake(1, 1).nextIdStr())
                    .thenReturn(expectedFileId)

            when:
            def result = fileComponent.save(mockFile, userId)

            then:
            result != null
            result.saveMode == "cloud"
            result.filePath.contains("/tmp/cloud-files/")
        }
    }
}
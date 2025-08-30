import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.geniusSociety.codelooms.common.util.DecompressUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

class TestDecompressUtil {

    @TempDir
    Path tempDir

    @Test
    void "test decompressTarGz with directory and file entries"() {
        // Create a mock TarArchiveInputStream
        TarArchiveInputStream mockTarStream = mock(TarArchiveInputStream)

        // Mock entries sequence
        TarArchiveEntry dirEntry = new TarArchiveEntry("testdir/")
        dirEntry.setDirectory(true)

        TarArchiveEntry fileEntry = new TarArchiveEntry("testdir/testfile.txt")
        fileEntry.setSize(10)

        when(mockTarStream.getNextEntry()).thenReturn(dirEntry, fileEntry, null)

        // Mock the stream chain
        GzipCompressorInputStream mockGzipStream = mock(GzipCompressorInputStream)
        when(mockGzipStream.read(any())).thenAnswer(new Answer<Integer>() {
            private int count = 0

            @Override
            Integer answer(InvocationOnMock invocation) throws Throwable {
                if (count++ < 10) {
                    return 'a'.bytes[0] // return 'a' 10 times
                }
                return -1
            }
        })

        when(mockTarStream.read(any())).thenAnswer(new Answer<Integer>() {
            private int count = 0

            @Override
            Integer answer(InvocationOnMock invocation) throws Throwable {
                if (count++ < 10) {
                    return 'a'.bytes[0] // return 'a' 10 times
                }
                return -1
            }
        })

        // Create a test tar.gz file (empty, since we're mocking the streams)
        File inputFile = tempDir.resolve("test.tar.gz").toFile()
        inputFile.createNewFile()

        // Test the method
        String outputDir = tempDir.resolve("output").toString()
        List<File> result = DecompressUtil.decompressTarGz(inputFile, outputDir)

        // Verify results
        assert result.size() == 1
        assert result[0].name == "testfile.txt"
        assert new File(outputDir, "testdir").exists()
        assert new File(outputDir, "testdir/testfile.txt").exists()
        assert new File(outputDir, "testdir/testfile.txt").text == "aaaaaaaaaa"
    }

    @Test
    void "test decompressTarGz with empty archive"() {
        // Create a test tar.gz file
        File inputFile = tempDir.resolve("empty.tar.gz").toFile()
        inputFile.createNewFile()

        // Test the method
        String outputDir = tempDir.resolve("output").toString()
        List<File> result = DecompressUtil.decompressTarGz(inputFile, outputDir)

        // Verify results
        assert result.isEmpty()
    }

    @Test
    void "test decompressTgz delegates to decompressTarGz"() {
        // Create a test tgz file
        File inputFile = tempDir.resolve("test.tgz").toFile()
        inputFile.createNewFile()

        // Mock the decompressTarGz method
        DecompressUtil util = spy(DecompressUtil)
        when(util.decompressTarGz(any(File), anyString())).thenReturn([new File("test.txt")])

        // Test the method
        String outputDir = tempDir.resolve("output").toString()
        List<File> result = util.decompressTgz(inputFile, outputDir)

        // Verify results
        assert result.size() == 1
        assert result[0].name == "test.txt"
        verify(util).decompressTarGz(inputFile, outputDir)
    }

    @Test
    void "test decompressZip with single file"() {
        // Create a test zip file
        File zipFile = tempDir.resolve("test.zip").toFile()
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))
        ZipEntry entry = new ZipEntry("testfile.txt")
        zos.putNextEntry(entry)
        zos.write("test content".bytes)
        zos.closeEntry()
        zos.close()

        // Test the method
        String outputDir = tempDir.resolve("output").toString()
        List<File> result = DecompressUtil.decompressZip(zipFile, outputDir)

        // Verify results
        assert result.size() == 1
        assert result[0].name == "testfile.txt"
        assert result[0].text == "test content"
    }

    @Test
    void "test decompressZip with nested directory structure"() {
        // Create a test zip file with nested structure
        File zipFile = tempDir.resolve("nested.zip").toFile()
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))

        // Add directory entry
        zos.putNextEntry(new ZipEntry("parent/"))
        zos.closeEntry()

        // Add file entry
        ZipEntry fileEntry = new ZipEntry("parent/child/file.txt")
        zos.putNextEntry(fileEntry)
        zos.write("nested content".bytes)
        zos.closeEntry()
        zos.close()

        // Test the method
        String outputDir = tempDir.resolve("output").toString()
        List<File> result = DecompressUtil.decompressZip(zipFile, outputDir)

        // Verify results
        assert result.size() == 1
        assert result[0].name == "file.txt"
        assert result[0].parentFile.name == "child"
        assert result[0].parentFile.parentFile.name == "parent"
        assert result[0].text == "nested content"
    }

    @Test
    void "test decompressTarGz handles IOException"() {
        // Create a test tar.gz file
        File inputFile = tempDir.resolve("corrupted.tar.gz").toFile()
        inputFile.createNewFile()

        // Mock the streams to throw IOException
        FileInputStream mockFileInputStream = mock(FileInputStream)
        whenNew(FileInputStream).withAnyArguments().thenReturn(mockFileInputStream)

        BufferedInputStream mockBufferedInputStream = mock(BufferedInputStream)
        whenNew(BufferedInputStream).withAnyArguments().thenReturn(mockBufferedInputStream)

        GzipCompressorInputStream mockGzipStream = mock(GzipCompressorInputStream)
        whenNew(GzipCompressorInputStream).withAnyArguments().thenReturn(mockGzipStream)

        when(mockGzipStream.read(any())).thenThrow(new IOException("Corrupted gzip stream"))

        // Test the method and expect exception
        String outputDir = tempDir.resolve("output").toString()
        def exception = shouldFail(IOException) {
            DecompressUtil.decompressTarGz(inputFile, outputDir)
        }

        assert exception.message == "Corrupted gzip stream"
    }
}
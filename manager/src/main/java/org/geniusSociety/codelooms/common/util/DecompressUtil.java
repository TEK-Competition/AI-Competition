package org.geniusSociety.codelooms.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 解压工具
 *
 * @author Cealus
 */
public class DecompressUtil {

    /**
     * 解压 tar.gz
     *
     * @param inputFile
     * @param outputDir
     * @return
     * @throws IOException
     */
    public static List<File> decompressTarGz(File inputFile, String outputDir) throws IOException {
        final List<File> files = new ArrayList<>();
        try (InputStream fi = new FileInputStream(inputFile);
             InputStream bi = new BufferedInputStream(fi);
             InputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {
            TarArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (OutputStream outputFileStream = new FileOutputStream(outputFile)) {
                        IOUtils.copy(ti, outputFileStream);
                    }
                    files.add(outputFile);
                }
            }
        }
        return files;
    }

    /**
     * 解压TGZ
     *
     * @param inputFile
     * @param outputDir
     * @return
     * @throws IOException
     */
    public static List<File> decompressTgz(File inputFile, String outputDir) throws IOException {
        return decompressTarGz(inputFile, outputDir);
    }

    /**
     * 解压zip
     *
     * @param inputFile
     * @param outputDir
     * @return
     */
    public static List<File> decompressZip(File inputFile, String outputDir) {
        ZipUtil.unzip(inputFile, FileUtil.file(outputDir));
        return FileUtil.loopFiles(outputDir);
    }
}

package com.ai.resume.airesume.utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AudioRecorder {
    // 默认音频格式参数，会在初始化时尝试获取系统支持的格式
    private static final AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    private static final float RATE = 16000.0f;
    private static final int CHANNELS = 1; // 单声道
    private static final int SAMPLE_SIZE = 16;
    private static final boolean BIG_ENDIAN = false;
    
    private final String outputFolder;
    private String outputFileName;
    private TargetDataLine line;
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private File outputFile;
    private Thread recordingThread;
    private boolean isRecording = false;
    
    public AudioRecorder(String outputFolder) {
        this.outputFolder = outputFolder;
        this.outputFileName = generateDefaultFileName();
        
        // 确保输出目录存在
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
    
    /**
     * 设置输出文件名（不包含扩展名）
     * @param fileName 文件名
     */
    public void setOutputFileName(String fileName) {
        this.outputFileName = fileName;
    }
    
    /**
     * 生成默认文件名
     * @return 默认文件名
     */
    private String generateDefaultFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "recording_" + dateFormat.format(new Date());
    }
    
    /**
     * 获取可用的音频设备信息
     * @return 音频设备信息列表
     */
    public List<String> getAudioDeviceInfo() {
        return AudioDeviceDiagnostic.listAudioMixers();
    }
    
    /**
     * 开始录音
     * @return 是否成功开始录音
     */
    public boolean startRecording() {
        try {
            // 检查是否有可用的麦克风设备
            if (!AudioDeviceDiagnostic.hasMicrophone()) {
                System.err.println("未检测到可用的麦克风设备");
                return false;
            }
            
            // 创建音频格式 - 使用单声道、较低采样率以提高兼容性
            AudioFormat format = new AudioFormat(
                    RATE,  // 采样率 (Hz)
                    SAMPLE_SIZE,     // 采样位数
                    CHANNELS,      // 声道数 (单声道)
                    true,   // 有符号
                    BIG_ENDIAN   // 大端字节序
            );
            
            // 创建数据线信息对象
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            System.out.println("请求的音频格式: " + format);
            System.out.println("系统支持该格式: " + AudioSystem.isLineSupported(info));
            
            // 检查系统是否支持该格式
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("不支持的音频格式");
                
                // 尝试使用更低的采样率和位数
                format = new AudioFormat(
                        8000,  // 采样率 (Hz)
                        8,     // 采样位数
                        1,     // 声道数 (单声道)
                        true,  // 有符号
                        false  // 大端字节序
                );
                
                info = new DataLine.Info(TargetDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("仍然不支持的音频格式");
                    return false;
                }
            }
            
            // 获取并打开数据线
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            
            // 创建输出文件
            outputFile = new File(outputFolder, outputFileName + ".wav");
            
            // 创建录音线程
            isRecording = true;
            recordingThread = new Thread(() -> {
                try {
                    AudioSystem.write(
                            new AudioInputStream(line),
                            fileType,
                            outputFile
                    );
                } catch (IOException e) {
                    System.err.println("录音失败: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            recordingThread.start();
            return true;
        } catch (LineUnavailableException e) {
            System.err.println("无法获取音频线: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 停止录音
     * @return 录音文件路径，如果录音失败则返回null
     */
    public String stopRecording() {
        if (line != null && isRecording) {
            isRecording = false;
            line.stop();
            line.close();
            
            try {
                // 等待录音线程结束
                recordingThread.join();
                return outputFile.getAbsolutePath();
            } catch (InterruptedException e) {
                System.err.println("录音线程中断: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * 检查是否正在录音
     * @return 是否正在录音
     */
    public boolean isRecording() {
        return isRecording;
    }
} 
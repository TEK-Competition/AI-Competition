package com.ai.resume.airesume.utils;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频设备诊断工具类
 * 用于检测和列出系统可用的音频设备和支持的格式
 */
public class AudioDeviceDiagnostic {
    
    /**
     * 列出所有可用的音频混音器
     * @return 混音器信息列表
     */
    public static List<String> listAudioMixers() {
        List<String> result = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        
        result.add("===== 可用音频混音器 =====");
        for (int i = 0; i < mixerInfos.length; i++) {
            Mixer.Info info = mixerInfos[i];
            result.add(String.format("[%d] 名称: %s", i, info.getName()));
            result.add(String.format("    描述: %s", info.getDescription()));
            result.add(String.format("    供应商: %s", info.getVendor()));
            result.add(String.format("    版本: %s", info.getVersion()));
            
            try {
                Mixer mixer = AudioSystem.getMixer(info);
                Line.Info[] sourceLines = mixer.getSourceLineInfo();
                Line.Info[] targetLines = mixer.getTargetLineInfo();
                
                result.add(String.format("    源线路数量: %d", sourceLines.length));
                result.add(String.format("    目标线路数量: %d", targetLines.length));
                
                // 检查是否支持TargetDataLine
                for (Line.Info lineInfo : targetLines) {
                    if (lineInfo instanceof DataLine.Info) {
                        DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                        if (TargetDataLine.class.isAssignableFrom(dataLineInfo.getLineClass())) {
                            result.add("    支持TargetDataLine (录音)");
                            
                            // 获取支持的格式
                            AudioFormat[] formats = dataLineInfo.getFormats();
                            if (formats.length > 0) {
                                result.add("    支持的音频格式:");
                                for (int j = 0; j < Math.min(3, formats.length); j++) {
                                    result.add("      " + formats[j]);
                                }
                                if (formats.length > 3) {
                                    result.add("      ... 等" + (formats.length - 3) + "种格式");
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                result.add("    获取详细信息时出错: " + e.getMessage());
            }
            
            result.add("");
        }
        
        return result;
    }
    
    /**
     * 检查是否有可用的麦克风设备
     * @return 是否有可用麦克风
     */
    public static boolean hasMicrophone() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        
        for (Mixer.Info info : mixerInfos) {
            try {
                Mixer mixer = AudioSystem.getMixer(info);
                Line.Info[] targetLines = mixer.getTargetLineInfo();
                
                for (Line.Info lineInfo : targetLines) {
                    if (lineInfo instanceof DataLine.Info) {
                        DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                        if (TargetDataLine.class.isAssignableFrom(dataLineInfo.getLineClass())) {
                            return true;
                        }
                    }
                }
            } catch (Exception ignored) {
                // 忽略异常
            }
        }
        
        return false;
    }
    
    /**
     * 获取一个系统支持的音频格式
     * @return 系统支持的音频格式，如果没有则返回null
     */
    public static AudioFormat getSupportedFormat() {
        // 尝试不同的音频格式参数组合
        AudioFormat[] formatsToTry = {
            // 16kHz 单声道 16位
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0f, 16, 1, 2, 16000.0f, false),
            // 8kHz 单声道 16位
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0f, 16, 1, 2, 8000.0f, false),
            // 16kHz 单声道 8位
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0f, 8, 1, 1, 16000.0f, false),
            // 44.1kHz 单声道 16位
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2, 44100.0f, false),
            // 44.1kHz 双声道 16位
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 2, 4, 44100.0f, false)
        };
        
        for (AudioFormat format : formatsToTry) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(info)) {
                return format;
            }
        }
        
        return null;
    }
} 
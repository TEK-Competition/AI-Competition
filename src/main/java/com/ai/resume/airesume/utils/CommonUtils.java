package com.ai.resume.airesume.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通用工具类，提供系统中常用的工具方法
 */
public class CommonUtils {
    
    /**
     * 显示错误提示对话框
     * @param title 标题
     * @param message 消息
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示信息提示对话框
     * @param title 标题
     * @param message 消息
     */
    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示异常详情对话框
     * @param title 标题
     * @param message 消息
     * @param exception 异常
     */
    public static void showExceptionDialog(String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        
        // 创建可展开的异常详情区域
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }
    
    /**
     * 检查字符串是否为空
     * @param str 要检查的字符串
     * @return 如果字符串为null或者空白则返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 检查字符串是否非空
     * @param str 要检查的字符串
     * @return 如果字符串不为null且不为空白则返回true，否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 格式化日期为字符串
     * @param date 日期
     * @param format 格式，例如：yyyy-MM-dd HH:mm:ss
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    /**
     * 将字符串解析为日期
     * @param dateStr 日期字符串
     * @param format 格式，例如：yyyy-MM-dd HH:mm:ss
     * @return 解析后的日期对象，解析失败返回null
     */
    public static Date parseDate(String dateStr, String format) {
        if (isEmpty(dateStr)) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 读取文件内容为字符串
     * @param file 文件对象
     * @return 文件内容，如果读取失败返回null
     */
    public static String readFileAsString(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 将字符串写入文件
     * @param file 文件对象
     * @param content 要写入的内容
     * @return 是否写入成功
     */
    public static boolean writeStringToFile(File file, String content) {
        if (file == null) {
            return false;
        }
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取项目根目录
     * @return 项目根目录的绝对路径
     */
    public static String getProjectRoot() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            return System.getProperty("user.dir");
        }
    }
    
    /**
     * 创建目录（如果不存在）
     * @param dirPath 目录路径
     * @return 是否成功创建目录
     */
    public static boolean createDirectory(String dirPath) {
        if (isEmpty(dirPath)) {
            return false;
        }
        
        File dir = new File(dirPath);
        if (dir.exists()) {
            return dir.isDirectory();
        }
        
        return dir.mkdirs();
    }
} 
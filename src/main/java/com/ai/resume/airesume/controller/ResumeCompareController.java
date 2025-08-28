package com.ai.resume.airesume.controller;

import com.ai.resume.airesume.dto.Education;
import com.ai.resume.airesume.dto.Language;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.dto.Work;
import com.ai.resume.airesume.utils.DatabaseManager;
import com.ai.resume.airesume.utils.PythonResumeParser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ResumeCompareController implements Initializable {
    
    @FXML
    private GridPane resumeGrid;
    
    @FXML
    private TextArea aiAnalysisArea;
    
    @FXML
    private Button analyzeButton;
    
    @FXML
    private StackPane rootPane;
    
    private StackPane loadingPane;
    private ProgressIndicator progressIndicator;
    
    private List<Resume> resumes;
    private PythonResumeParser resumeParser;
    private DatabaseManager databaseManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化解析器
        resumeParser = new PythonResumeParser();
        
        // 初始化数据库管理器
        databaseManager = DatabaseManager.getInstance();
        
        // 初始化Loading面板
        initLoadingPane();
        
        // 配置TextArea自适应高度
        configureTextAreaAutoHeight();
    }
    
    /**
     * 配置TextArea自适应高度
     */
    private void configureTextAreaAutoHeight() {
        // 设置TextArea自动换行
        aiAnalysisArea.setWrapText(true);
        
        // 监听文本变化，调整高度
        aiAnalysisArea.textProperty().addListener((observable, oldValue, newValue) -> {
            adjustTextAreaHeight(newValue);
        });
        
        // 监听窗口大小变化，重新调整高度
        aiAnalysisArea.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (aiAnalysisArea.getText() != null && !aiAnalysisArea.getText().isEmpty()) {
                adjustTextAreaHeight(aiAnalysisArea.getText());
            }
        });
    }
    
    /**
     * 初始化Loading面板
     */
    private void initLoadingPane() {
        loadingPane = new StackPane();
        loadingPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        loadingPane.setVisible(false);
        
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(100, 100);
        
        Label loadingLabel = new Label("正在分析，请稍候...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        loadingPane.getChildren().add(loadingBox);
        
        rootPane.getChildren().add(loadingPane);
    }
    
    /**
     * 显示Loading面板
     * @param message 显示消息
     */
    private void showLoading(String message) {
        Platform.runLater(() -> {
            ((Label) ((VBox) loadingPane.getChildren().get(0)).getChildren().get(1)).setText(message);
            loadingPane.setVisible(true);
        });
    }
    
    /**
     * 隐藏Loading面板
     */
    private void hideLoading() {
        Platform.runLater(() -> {
            loadingPane.setVisible(false);
        });
    }
    
    /**
     * 设置要比较的简历列表
     * @param resumes 简历列表
     */
    public void setResumes(List<Resume> resumes) {
        this.resumes = resumes;
        
        // 创建简历详情视图
        createResumeDetailsView();
        
        // 自动开始分析
        handleAnalyze();
    }
    
    /**
     * 创建简历详情视图
     */
    private void createResumeDetailsView() {
        resumeGrid.getChildren().clear();
        
        for (int i = 0; i < resumes.size(); i++) {
            Resume resume = resumes.get(i);
            
            VBox resumeBox = new VBox(10);
            resumeBox.getStyleClass().add("resume-box");
            
            // 添加简历详情
            Label nameLabel = new Label("姓名: " + resume.getName());
            nameLabel.getStyleClass().add("resume-name");
            
            Label infoLabel = new Label(
                    "性别: " + resume.getGender() + "\n" +
                    "年龄: " + resume.getAge() + "\n" +
                    "最高学历: " + resume.getHighestEducation()
            );
            infoLabel.setWrapText(true);
            
            // 教育经历
            TitledPane educationPane = createListPane("教育经历", resume.getEducationExperience());
            
            // 工作经历
            TitledPane workPane = createListPane("工作经历", resume.getWorkExperience());
            
            // 语言能力
            TitledPane languagePane = createListPane("语言能力", resume.getLanguageSkills());
            
            // 自我评价
            TitledPane selfEvalPane = createTextPane("自我评价", resume.getSelfEvaluation());
            
            // 面试记录按钮
            Button recordsButton = new Button("面试记录");
            recordsButton.setOnAction(event -> {
                // 打开面试记录界面
                openInterviewRecords(resume);
            });
            
            resumeBox.getChildren().addAll(
                    nameLabel, infoLabel, 
                    educationPane, workPane, languagePane, 
                    selfEvalPane, recordsButton
            );
            
            resumeGrid.add(resumeBox, i, 0);
        }
    }
    
    /**
     * 创建列表面板
     * @param title 标题
     * @param items 列表项
     * @return 面板
     */
    private <T> TitledPane createListPane(String title, List<T> items) {
        ListView<String> listView = new ListView<>();
        if (items != null) {
            for (T item : items) {
                listView.getItems().add(item.toString());
            }
        }
        
        TitledPane pane = new TitledPane(title, listView);
        pane.setExpanded(false);
        return pane;
    }
    
    /**
     * 创建文本面板
     * @param title 标题
     * @param text 文本内容
     * @return 面板
     */
    private TitledPane createTextPane(String title, String text) {
        TextArea textArea = new TextArea(text);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        
        TitledPane pane = new TitledPane(title, textArea);
        pane.setExpanded(false);
        return pane;
    }
    
    /**
     * 打开面试记录界面
     * @param resume 简历对象
     */
    private void openInterviewRecords(Resume resume) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ai/resume/airesume/interview-records-view.fxml"));
            Parent root = loader.load();
            
            InterviewRecordsController controller = loader.getController();
            controller.setResume(resume);
            
            Stage stage = new Stage();
            stage.setTitle("面试记录 - " + resume.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("错误", "无法打开面试记录窗口: " + e.getMessage());
        }
    }
    
    /**
     * 处理分析按钮点击
     */
    @FXML
    private void handleAnalyze() {
        if (resumes == null || resumes.isEmpty() || resumes.size() < 2) {
            aiAnalysisArea.setText("需要至少两份简历才能进行对比分析。");
            return;
        }
        
        // 显示Loading
        showLoading("正在分析简历，请稍候...");
        analyzeButton.setDisable(true);
        
        // 创建后台任务
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                try {
                    // 调用Python脚本进行简历对比分析
                    return resumeParser.compareResumes(resumes);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "分析失败: " + e.getMessage();
                }
            }
        };
        
        // 设置任务完成后的处理
        task.setOnSucceeded(event -> {
            hideLoading();
            analyzeButton.setDisable(false);
            String result = task.getValue();
            aiAnalysisArea.setText(result);
            // 调整TextArea高度
            adjustTextAreaHeight(result);
        });
        
        task.setOnFailed(event -> {
            hideLoading();
            analyzeButton.setDisable(false);
            showAlert("分析失败", "无法分析简历: " + task.getException().getMessage());
            aiAnalysisArea.setText("分析失败，请稍后再试。");
        });
        
        // 启动任务
        new Thread(task).start();
    }
    
    /**
     * 调整TextArea高度
     * @param text 文本内容
     */
    private void adjustTextAreaHeight(String text) {
        Platform.runLater(() -> {
            // 计算行数
            int lineCount = 1;
            if (text != null) {
                // 计算换行符数量
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '\n') {
                        lineCount++;
                    }
                }
                
                // 考虑自动换行的情况
                // 假设每行平均字符数为50（根据实际情况调整）
                int charsPerLine = 50;
                int textWidth = (int) aiAnalysisArea.getWidth();
                if (textWidth > 0) {
                    // 根据TextArea宽度估算每行可容纳的字符数
                    charsPerLine = textWidth / 8; // 假设每个字符平均宽度为8像素
                }
                
                // 估算因自动换行导致的额外行数
                String[] lines = text.split("\n");
                for (String line : lines) {
                    if (charsPerLine > 0 && line.length() > charsPerLine) {
                        // 添加因自动换行产生的额外行数
                        lineCount += Math.ceil((double) line.length() / charsPerLine) - 1;
                    }
                }
            }
            
            // 设置合适的高度，每行约20像素，最小高度100，最大高度根据内容自适应
            double lineHeight = 20; // 每行高度
            double newHeight = Math.max(100, Math.min(lineCount * lineHeight, 500));
            
            // 如果内容较少，使用较小高度；如果内容较多，适当增加高度但不超过最大值
            aiAnalysisArea.setPrefHeight(newHeight);
        });
    }
    
    @FXML
    private void handleClose() {
        ((Stage) resumeGrid.getScene().getWindow()).close();
    }
    
    /**
     * 显示警告对话框
     * @param title 标题
     * @param content 内容
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
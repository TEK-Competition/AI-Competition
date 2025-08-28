package com.ai.resume.airesume.controller;

import com.ai.resume.airesume.dto.InterviewRecord;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.utils.AudioRecorder;
import com.ai.resume.airesume.utils.DatabaseManager;
import com.ai.resume.airesume.utils.PythonResumeParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterviewRecordsController implements Initializable {

    @FXML
    private TableView<InterviewRecord> recordsTableView;
    
    @FXML
    private TableColumn<InterviewRecord, String> displayNameColumn;
    
    @FXML
    private TableColumn<InterviewRecord, String> filePathColumn;
    
    @FXML
    private TableColumn<InterviewRecord, Void> operationColumn;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Button recordButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private StackPane rootPane;
    
    private StackPane loadingPane;
    private ProgressIndicator progressIndicator;
    
    private Resume resume;
    private DatabaseManager databaseManager;
    private PythonResumeParser resumeParser;
    private AudioRecorder audioRecorder;
    private final String RECORDING_FOLDER = System.getProperty("user.home") + "/AiResume/recordings";
    private boolean isRecording = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格列
        displayNameColumn.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        filePathColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFilePath()));
        
        // 设置列宽比例
        displayNameColumn.prefWidthProperty().bind(recordsTableView.widthProperty().multiply(0.4));
        filePathColumn.prefWidthProperty().bind(recordsTableView.widthProperty().multiply(0.3));
        operationColumn.prefWidthProperty().bind(recordsTableView.widthProperty().multiply(0.3));
        
        // 设置操作列
        setupOperationColumn();
        
        // 初始化数据库管理器
        databaseManager = DatabaseManager.getInstance();
        
        // 初始化简历解析器
        resumeParser = new PythonResumeParser();
        
        // 初始化录音器
        audioRecorder = new AudioRecorder(RECORDING_FOLDER);
        
        // 初始化Loading面板
        initLoadingPane();
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
        
        Label loadingLabel = new Label("正在处理，请稍候...");
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
     * 设置操作列
     */
    private void setupOperationColumn() {
        operationColumn.setCellFactory(new Callback<TableColumn<InterviewRecord, Void>, TableCell<InterviewRecord, Void>>() {
            @Override
            public TableCell<InterviewRecord, Void> call(TableColumn<InterviewRecord, Void> param) {
                return new TableCell<InterviewRecord, Void>() {
                    private final Button deleteButton = new Button("删除录音");
                    private final Button analyzeButton = new Button("录音识别");
                    private final Button viewAnalysisButton = new Button("AI面试分析");
                    private final HBox pane = new HBox(5);
                    
                    {
                        // 设置删除按钮样式和事件
                        deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-xs");
                        deleteButton.setOnAction(event -> {
                            InterviewRecord record = getTableView().getItems().get(getIndex());
                            handleDeleteRecord(record);
                        });
                        
                        // 设置面试分析按钮样式和事件
                        analyzeButton.getStyleClass().addAll("btn", "btn-info", "btn-xs");
                        analyzeButton.setText("录音识别");
                        analyzeButton.setOnAction(event -> {
                            InterviewRecord record = getTableView().getItems().get(getIndex());
                            handleAnalyzeInterview(record);
                        });
                        
                        // 设置查看AI分析按钮样式和事件
                        viewAnalysisButton.getStyleClass().addAll("btn", "btn-success", "btn-xs");
                        viewAnalysisButton.setOnAction(event -> {
                            InterviewRecord record = getTableView().getItems().get(getIndex());
                            handleViewAnalysis(record);
                        });
                        viewAnalysisButton.setVisible(false); // 默认不可见，只有分析完成后才显示
                        
                        pane.setAlignment(Pos.CENTER);
                        pane.getChildren().addAll(deleteButton, analyzeButton);
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            InterviewRecord record = getTableView().getItems().get(getIndex());
                            
                            // 如果已经有分析结果，显示查看分析按钮
                            if (record.getInterviewContent() != null && !record.getInterviewContent().isEmpty()) {
                                if (!pane.getChildren().contains(viewAnalysisButton)) {
                                    pane.getChildren().add(viewAnalysisButton);
                                }
                                viewAnalysisButton.setVisible(true);
                            } else {
                                pane.getChildren().remove(viewAnalysisButton);
                            }
                            
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
    }
    
    /**
     * 设置简历
     * @param resume 简历对象
     */
    public void setResume(Resume resume) {
        this.resume = resume;
        titleLabel.setText(resume.getName() + " 的面试记录");
        loadRecordsFromDatabase();
    }
    
    /**
     * 从数据库加载面试记录
     */
    private void loadRecordsFromDatabase() {
        if (resume != null) {
            List<InterviewRecord> records = databaseManager.getInterviewRecordsByResumeId(resume.getId());
            recordsTableView.setItems(FXCollections.observableArrayList(records));
        }
    }
    
    /**
     * 处理返回按钮点击
     */
    @FXML
    private void handleBack() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }
    
    /**
     * 处理录音按钮点击
     */
    @FXML
    private void handleRecord() {
        if (!isRecording) {
            this.startRecording();
            return;
        }
        this.stopRecording();
    }
    
    /**
     * 开始录音
     */
    private void startRecording() {
        // 创建麦克风权限提示
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("麦克风权限");
            alert.setHeaderText("需要麦克风权限");
            alert.setContentText("录音功能需要访问麦克风。如果录音失败，请确保已在系统偏好设置中允许此应用访问麦克风。");
            alert.showAndWait();
        }
        
        // 确保录音目录存在
        File recordingDir = new File(RECORDING_FOLDER);
        if (!recordingDir.exists()) {
            recordingDir.mkdirs();
        }
        
        // 构建录音文件名（简历ID_时间戳）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String fileName = resume.getId() + "_" + timestamp;
        audioRecorder.setOutputFileName(fileName);
        
        if (audioRecorder.startRecording()) {
            isRecording = true;
            recordButton.setText("结束录音");
            recordButton.getStyleClass().removeAll("btn-primary");
            recordButton.getStyleClass().addAll("btn-success");
            statusLabel.setText("正在录音...");
        } else {
            showAlert("录音失败", "无法启动录音功能，请检查麦克风设置和权限");
        }
    }
    
    /**
     * 停止录音
     */
    private void stopRecording() {
        String recordingPath = audioRecorder.stopRecording();
        isRecording = false;
        recordButton.setText("开始录音");
        recordButton.getStyleClass().removeAll("btn-success");
        recordButton.getStyleClass().addAll("btn-primary");
        
        if (recordingPath != null) {
            // 创建面试记录
            File recordFile = new File(recordingPath);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String displayName = "面试录音 " + sdf.format(new Date());
            
            InterviewRecord record = new InterviewRecord(
                    resume.getId(),
                    recordFile.getName(),
                    displayName,
                    recordingPath
            );
            
            // 保存到数据库
            long id = databaseManager.saveInterviewRecord(record);
            if (id > 0) {
                statusLabel.setText("录音已保存: " + recordingPath);
                loadRecordsFromDatabase();
            } else {
                showAlert("保存失败", "无法保存面试记录到数据库");
            }
        }
    }
    
    /**
     * 处理删除录音
     * @param record 面试记录
     */
    private void handleDeleteRecord(InterviewRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除这条面试记录吗？");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 删除文件
                File file = new File(record.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
                
                // 从数据库中删除
                if (databaseManager.deleteInterviewRecord(record.getId())) {
                    // 刷新列表
                    loadRecordsFromDatabase();
                    statusLabel.setText("已删除面试记录");
                } else {
                    showAlert("删除失败", "无法从数据库中删除面试记录");
                }
            }
        });
    }
    
    /**
     * 处理面试分析
     * @param record 面试记录
     */
    private void handleAnalyzeInterview(InterviewRecord record) {
        // 检查是否已经识别过
        if (record.getInterviewContent() != null && !record.getInterviewContent().isEmpty()) {
            // 已识别过，直接显示结果并提供重新生成选项
            showRecognitionResult(resume.getName(), record.getInterviewContent(), record);
        } else {
            // 显示Loading
            showLoading("正在提交面试录音进行识别，请稍候...");
            
            // 异步处理语音识别和面试分析
            CompletableFuture<String> future = resumeParser.processInterviewRecordingAsync(record, resume, databaseManager);
            
            future.thenAccept(result -> {
                Platform.runLater(() -> {
                    hideLoading();
                    if (result != null && !result.startsWith("处理失败")) {
                        // 更新面试记录的分析结果
                        record.setInterviewContent(result);
                        databaseManager.updateInterviewRecord(record);
                        
                        // 刷新表格，显示AI分析按钮
                        loadRecordsFromDatabase();
                        
                        // 显示成功消息
                        statusLabel.setText("录音识别完成");
                        
                        // 显示识别结果
                        showRecognitionResult(resume.getName(), result, record);
                    } else {
                        showAlert("识别失败", "无法识别面试录音: " + (result != null ? result : "未知错误"));
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    hideLoading();
                    showAlert("识别失败", "无法识别面试录音: " + ex.getMessage());
                });
                return null;
            });
        }
    }
    
    /**
     * 处理查看AI分析结果
     * @param record 面试记录
     */
    private void handleViewAnalysis(InterviewRecord record) {
        if (record.getInterviewContent() != null && !record.getInterviewContent().isEmpty()) {
            // 显示Loading
            showLoading("正在进行AI面试分析，请稍候...");
            
            // 调用Python脚本进行面试分析
            CompletableFuture.supplyAsync(() -> {
                try {
                    return resumeParser.analyzeInterviewWithText(record.getFilePath(), resume, record.getInterviewContent());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "分析失败: " + e.getMessage();
                }
            }).thenAccept(analysisResult -> {
                Platform.runLater(() -> {
                    hideLoading();
                    if (analysisResult != null && !analysisResult.startsWith("分析失败")) {
                        // 显示AI分析结果
                        showAIAnalysisResult(resume.getName(), analysisResult, record);
                    } else {
                        showAlert("分析失败", "无法分析面试录音: " + (analysisResult != null ? analysisResult : "未知错误"));
                    }
                });
            });
        } else {
            showAlert("无法分析", "请先进行录音识别");
        }
    }
    
    /**
     * 显示识别结果
     * @param name 姓名
     * @param result 识别结果
     * @param record 面试记录对象，用于重新生成
     */
    private void showRecognitionResult(String name, String result, InterviewRecord record) {
        // 创建自定义对话框
        Stage dialogStage = new Stage();
        dialogStage.setTitle("录音识别结果");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        // 创建UI布局
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // 添加标题
        Label titleLabel = new Label(name + " 的录音识别结果");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // 创建表格，分为时间、角色、内容三列
        TableView<InterviewSegment> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add("interview-analysis-table");
        
        // 时间列
        TableColumn<InterviewSegment, String> timeColumn = new TableColumn<>("时间");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setPrefWidth(150);
        timeColumn.getStyleClass().add("time-column");
        
        // 角色列
        TableColumn<InterviewSegment, String> roleColumn = new TableColumn<>("角色");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setPrefWidth(100);
        roleColumn.getStyleClass().add("role-column");
        
        // 内容列
        TableColumn<InterviewSegment, String> contentColumn = new TableColumn<>("内容");
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentColumn.setPrefWidth(350);
        
        // 设置内容列的单元格工厂，支持文本换行
        contentColumn.setCellFactory(tc -> {
            TableCell<InterviewSegment, String> cell = new TableCell<InterviewSegment, String>() {
                private final Text text = new Text();
                
                {
                    setGraphic(text);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                    text.wrappingWidthProperty().bind(contentColumn.widthProperty().subtract(10));
                    text.getStyleClass().add("content-text");
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        text.setText("");
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                        
                        // 调整单元格高度
                        Platform.runLater(() -> {
                            double textHeight = text.getBoundsInLocal().getHeight();
                            double padding = 10; // 上下填充
                            setPrefHeight(textHeight + padding);
                        });
                    }
                }
            };
            
            return cell;
        });
        
        tableView.getColumns().addAll(timeColumn, roleColumn, contentColumn);
        
        // 解析结果文本，填充表格数据
        ObservableList<InterviewSegment> segments = parseInterviewResult(result);
        tableView.setItems(segments);
        
        // 设置表格高度
        tableView.setPrefHeight(400);
        
        // 底部按钮容器
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button regenerateButton = new Button("重新生成");
        regenerateButton.getStyleClass().addAll("btn", "btn-primary");
        regenerateButton.setPrefWidth(100);
        regenerateButton.setOnAction(event -> {
            dialogStage.close();
            // 重新调用识别
            handleAnalyzeInterview(record);
        });
        
        Button closeButton = new Button("关闭");
        closeButton.getStyleClass().addAll("btn", "btn-default");
        closeButton.setPrefWidth(80);
        closeButton.setOnAction(event -> dialogStage.close());
        
        buttonBox.getChildren().addAll(regenerateButton, closeButton);
        
        // 添加组件到布局
        root.getChildren().addAll(titleLabel, tableView, buttonBox);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        
        // 设置场景和显示对话框
        Scene scene = new Scene(root, 600, 500);
        
        // 应用CSS样式
        scene.getStylesheets().add(getClass().getResource("/com/ai/resume/airesume/css/style.css").toExternalForm());
        
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    /**
     * 显示AI分析结果
     * @param name 姓名
     * @param result 分析结果
     * @param record 面试记录对象，用于重新生成
     */
    private void showAIAnalysisResult(String name, String result, InterviewRecord record) {
        // 创建自定义对话框
        Stage dialogStage = new Stage();
        dialogStage.setTitle("AI面试分析结果");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        // 创建UI布局
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // 添加标题
        Label titleLabel = new Label(name + " 的AI面试分析");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // 创建不可编辑的文本区域
        TextArea textArea = new TextArea(result);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(400);
        
        // 底部按钮容器
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button regenerateButton = new Button("重新生成");
        regenerateButton.getStyleClass().addAll("btn", "btn-primary");
        regenerateButton.setPrefWidth(100);
        regenerateButton.setOnAction(event -> {
            dialogStage.close();
            // 重新调用AI分析
            handleViewAnalysis(record);
        });
        
        Button closeButton = new Button("关闭");
        closeButton.getStyleClass().addAll("btn", "btn-default");
        closeButton.setPrefWidth(80);
        closeButton.setOnAction(event -> dialogStage.close());
        
        buttonBox.getChildren().addAll(regenerateButton, closeButton);
        
        // 添加组件到布局
        root.getChildren().addAll(titleLabel, textArea, buttonBox);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        VBox.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        
        // 设置场景和显示对话框
        Scene scene = new Scene(root, 600, 500);
        
        // 应用CSS样式
        scene.getStylesheets().add(getClass().getResource("/com/ai/resume/airesume/css/style.css").toExternalForm());
        
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    /**
     * 解析面试结果文本，提取时间、角色和内容
     * @param result 面试结果文本
     * @return 解析后的面试片段列表
     */
    private ObservableList<InterviewSegment> parseInterviewResult(String result) {
        ObservableList<InterviewSegment> segments = FXCollections.observableArrayList();
        
        // 使用正则表达式匹配 [时间] 说话人X：内容 的格式
        Pattern pattern = Pattern.compile("\\[(.*?)\\]\\s*说话人(\\d+)：(.*)");
        
        // 按行分割文本
        String[] lines = result.split("\\n");
        
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.matches()) {
                String time = matcher.group(1);
                String speaker = "说话人" + matcher.group(2);
                String content = matcher.group(3);
                
                segments.add(new InterviewSegment(time, speaker, content));
            } else if (!line.trim().isEmpty()) {
                // 处理不匹配模式的行
                segments.add(new InterviewSegment("", "", line));
            }
        }
        
        return segments;
    }
    
    /**
     * 面试片段类，用于在表格中显示面试内容
     */
    public static class InterviewSegment {
        private final String time;
        private final String role;
        private final String content;
        
        public InterviewSegment(String time, String role, String content) {
            this.time = time;
            this.role = role;
            this.content = content;
        }
        
        public String getTime() {
            return time;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
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
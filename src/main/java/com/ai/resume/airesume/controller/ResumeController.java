package com.ai.resume.airesume.controller;

import com.ai.resume.airesume.dto.Education;
import com.ai.resume.airesume.dto.InterviewRecord;
import com.ai.resume.airesume.dto.Language;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.dto.Work;
import com.ai.resume.airesume.service.InterviewService;
import com.ai.resume.airesume.service.ResumeService;
import com.ai.resume.airesume.utils.AudioDeviceDiagnostic;
import com.ai.resume.airesume.utils.AudioRecorder;
import com.ai.resume.airesume.utils.PythonResumeParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * 简历列表控制器
 */
public class ResumeController implements Initializable {

    @FXML
    private TableView<ResumeTableItem> resumeTableView;
    
    @FXML
    private TableColumn<ResumeTableItem, Boolean> selectColumn;
    
    @FXML
    private TableColumn<ResumeTableItem, String> nameColumn;
    
    @FXML
    private TableColumn<ResumeTableItem, String> genderColumn;
    
    @FXML
    private TableColumn<ResumeTableItem, Integer> ageColumn;
    
    @FXML
    private TableColumn<ResumeTableItem, String> educationColumn;
    
    @FXML
    private TableColumn<ResumeTableItem, Void> operationColumn;
    
    @FXML
    private CheckBox selectAllCheckBox;
    
    @FXML
    private Button uploadButton;
    
    @FXML
    private Button compareButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private StackPane rootPane;
    
    private StackPane loadingPane;
    private ProgressIndicator progressIndicator;
    
    private ObservableList<ResumeTableItem> resumeItems = FXCollections.observableArrayList();
    
    // 使用Service替代直接引用工具类
    private final ResumeService resumeService;
    private final InterviewService interviewService;
    
    /**
     * 构造函数
     */
    public ResumeController() {
        this.resumeService = new ResumeService();
        this.interviewService = new InterviewService();
    }
    
    // 简历表格项，包装Resume对象，添加选择属性
    public static class ResumeTableItem {
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        private final Resume resume;
        
        public ResumeTableItem(Resume resume) {
            this.resume = resume;
        }
        
        public boolean isSelected() {
            return selected.get();
        }
        
        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }
        
        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }
        
        public Resume getResume() {
            return resume;
        }
        
        public String getName() {
            return resume.getName();
        }
        
        public String getGender() {
            return resume.getGender();
        }
        
        public int getAge() {
            return resume.getAge();
        }
        
        public String getHighestEducation() {
            return resume.getHighestEducation();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格列
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        
        // 使用自定义的单元格工厂，确保勾选功能正常
        selectColumn.setCellFactory(column -> {
            TableCell<ResumeTableItem, Boolean> cell = new TableCell<ResumeTableItem, Boolean>() {
                private final CheckBox checkBox = new CheckBox();
                
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // 重置之前的事件处理器，避免重复添加
                        checkBox.setOnAction(null);
                        
                        // 更新勾选状态
                        checkBox.setSelected(item != null && item);
                        setGraphic(checkBox);
                        
                        // 添加新的事件处理器
                        checkBox.setOnAction(event -> {
                            if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                                ResumeTableItem tableItem = getTableView().getItems().get(getIndex());
                                boolean newValue = checkBox.isSelected();
                                tableItem.setSelected(newValue);
                            }
                        });
                    }
                }
            };
            
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        educationColumn.setCellValueFactory(new PropertyValueFactory<>("highestEducation"));
        
        // 设置列宽比例
        selectColumn.setMaxWidth(50);
        selectColumn.setMinWidth(50);
        nameColumn.prefWidthProperty().bind(resumeTableView.widthProperty().multiply(0.15));
        genderColumn.prefWidthProperty().bind(resumeTableView.widthProperty().multiply(0.10));
        ageColumn.prefWidthProperty().bind(resumeTableView.widthProperty().multiply(0.10));
        educationColumn.prefWidthProperty().bind(resumeTableView.widthProperty().multiply(0.20));
        operationColumn.prefWidthProperty().bind(resumeTableView.widthProperty().multiply(0.45));
        
        // 设置操作列
        setupOperationColumn();
        
        // 设置表格数据
        resumeTableView.setItems(resumeItems);
        
        // 初始化Loading面板
        initLoadingPane();
        
        // 加载数据库中的简历
        loadResumesFromDatabase();
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
        operationColumn.setCellFactory(new Callback<TableColumn<ResumeTableItem, Void>, TableCell<ResumeTableItem, Void>>() {
            @Override
            public TableCell<ResumeTableItem, Void> call(TableColumn<ResumeTableItem, Void> param) {
                return new TableCell<ResumeTableItem, Void>() {
                    private final Button deleteButton = new Button("删除");
                    private final Button recordButton = new Button("开始录音");
                    private final Button recordsButton = new Button("面试记录");
                    private final HBox pane = new HBox(5, deleteButton, recordButton, recordsButton);
                    
                    {
                        // 设置删除按钮样式和事件
                        deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-xs");
                        deleteButton.setOnAction(event -> {
                            ResumeTableItem item = getTableView().getItems().get(getIndex());
                            handleDeleteResume(item);
                        });
                        
                        // 设置录音按钮样式和事件
                        recordButton.getStyleClass().addAll("btn", "btn-primary", "btn-xs");
                        recordButton.setOnAction(event -> {
                            ResumeTableItem item = getTableView().getItems().get(getIndex());
                            handleRecordForResume(item, recordButton);
                        });
                        
                        // 设置面试记录按钮样式和事件
                        recordsButton.getStyleClass().addAll("btn", "btn-info", "btn-xs");
                        recordsButton.setOnAction(event -> {
                            ResumeTableItem item = getTableView().getItems().get(getIndex());
                            handleOpenInterviewRecords(item);
                        });
                        
                        pane.setAlignment(Pos.CENTER);
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        });
    }
    
    /**
     * 处理简历删除
     * @param item 简历表格项
     */
    private void handleDeleteResume(ResumeTableItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除这份简历吗？");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 从数据库中删除
                if (resumeService.deleteResume(item.getResume().getId())) {
                    // 从表格中移除
                    resumeItems.remove(item);
                    statusLabel.setText("已删除简历: " + item.getName());
                } else {
                    showAlert("删除失败", "无法从数据库中删除简历");
                }
            }
        });
    }
    
    /**
     * 处理简历录音
     * @param item 简历表格项
     * @param recordButton 录音按钮
     */
    private void handleRecordForResume(ResumeTableItem item, Button recordButton) {
        if (recordButton.getText().equals("开始录音")) {
            startRecording(item, recordButton);
        } else {
            stopRecording(item, recordButton);
        }
    }
    
    /**
     * 开始录音
     * @param item 简历表格项
     * @param recordButton 录音按钮
     */
    private void startRecording(ResumeTableItem item, Button recordButton) {
        // 创建麦克风权限提示
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("麦克风权限");
            alert.setHeaderText("需要麦克风权限");
            alert.setContentText("录音功能需要访问麦克风。如果录音失败，请确保已在系统偏好设置中允许此应用访问麦克风。");
            alert.showAndWait();
        }
        
        // 确保录音目录存在
        File recordingDir = new File(System.getProperty("user.home") + "/AiResume/recordings");
        if (!recordingDir.exists()) {
            recordingDir.mkdirs();
        }
        
        // 构建录音文件名（简历ID_时间戳）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String fileName = item.getResume().getId() + "_" + timestamp;
        AudioRecorder audioRecorder = new AudioRecorder(System.getProperty("user.home") + "/AiResume/recordings");
        audioRecorder.setOutputFileName(fileName);
        
        if (audioRecorder.startRecording()) {
            recordButton.setText("结束录音");
            recordButton.getStyleClass().removeAll("btn-primary");
            recordButton.getStyleClass().addAll("btn-success");
            statusLabel.setText("正在录音: " + item.getName());
        } else {
            showAlert("录音失败", "无法启动录音功能，请检查麦克风设置和权限");
            
            // 显示音频设备诊断信息
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("录音失败");
            alert.setHeaderText("无法启动录音功能");
            alert.setContentText("您想查看音频设备诊断信息吗？");
            
            ButtonType diagnoseButton = new ButtonType("查看诊断");
            ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(diagnoseButton, cancelButton);
            
            alert.showAndWait().ifPresent(type -> {
                if (type == diagnoseButton) {
                    showAudioDeviceDiagnostic();
                }
            });
        }
    }
    
    /**
     * 停止录音
     * @param item 简历表格项
     * @param recordButton 录音按钮
     */
    private void stopRecording(ResumeTableItem item, Button recordButton) {
        String recordingPath = new AudioRecorder(System.getProperty("user.home") + "/AiResume/recordings").stopRecording();
        recordButton.setText("开始录音");
        recordButton.getStyleClass().removeAll("btn-success");
        recordButton.getStyleClass().addAll("btn-primary");
        
        if (recordingPath != null) {
            // 创建面试记录
            File recordFile = new File(recordingPath);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String displayName = "面试录音 " + sdf.format(new Date());
            
            InterviewRecord record = new InterviewRecord(
                    item.getResume().getId(),
                    recordFile.getName(),
                    displayName,
                    recordingPath
            );
            
            // 保存到数据库
            long id = interviewService.stopRecordingAndSave(item.getResume().getId());
            if (id > 0) {
                statusLabel.setText("录音已保存: " + recordingPath);
            } else {
                showAlert("保存失败", "无法保存面试记录到数据库");
            }
        }
    }
    
    /**
     * 处理打开面试记录列表
     * @param item 简历表格项
     */
    private void handleOpenInterviewRecords(ResumeTableItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ai/resume/airesume/interview-records-view.fxml"));
            Parent root = loader.load();
            
            InterviewRecordsController controller = loader.getController();
            controller.setResume(item.getResume());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(item.getName() + " 的面试记录");
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("错误", "无法打开面试记录窗口: " + e.getMessage());
        }
    }
    
    /**
     * 显示音频设备诊断信息
     */
    private void showAudioDeviceDiagnostic() {
        List<String> deviceInfo = AudioDeviceDiagnostic.listAudioMixers();
        
        // 创建文本区域显示诊断信息
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(600);
        
        // 添加诊断信息
        StringBuilder sb = new StringBuilder();
        sb.append("===== 音频设备诊断信息 =====\n\n");
        sb.append("操作系统: ").append(System.getProperty("os.name"))
          .append(" ").append(System.getProperty("os.version")).append("\n");
        sb.append("Java版本: ").append(System.getProperty("java.version")).append("\n\n");
        
        deviceInfo.forEach(line -> sb.append(line).append("\n"));
        
        textArea.setText(sb.toString());
        
        // 创建并显示对话框
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("音频设备诊断");
        alert.setHeaderText("系统音频设备诊断信息");
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(650, 500);
        alert.showAndWait();
    }
    
    /**
     * 从数据库加载所有简历
     */
    public void loadResumesFromDatabase() {
        List<Resume> resumes = resumeService.getAllResumes();
        resumeItems.clear();
        for (Resume resume : resumes) {
            resumeItems.add(new ResumeTableItem(resume));
        }
    }
    
    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择简历文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("PDF文件", "*.pdf"),
                new FileChooser.ExtensionFilter("Word文件", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("文本文件", "*.txt")
        );
        
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            // 显示Loading
            showLoading("正在解析简历，请稍候...");
            
            // 创建后台任务
            Task<Resume> task = new Task<Resume>() {
                @Override
                protected Resume call() throws Exception {
                    try {
                        // 使用Python脚本解析简历
                        return new PythonResumeParser().parseResume(selectedFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
            
            // 设置任务完成后的处理
            task.setOnSucceeded(event -> {
                hideLoading();
                Resume resume = task.getValue();
                if (resume != null) {
                    // 保存到数据库
                    long id = resumeService.saveResume(resume);
                    if (id > 0) {
                        resumeItems.add(new ResumeTableItem(resume));
                        statusLabel.setText("已上传: " + selectedFile.getName());
                    } else {
                        showAlert("上传失败", "无法保存简历到数据库");
                    }
                } else {
                    showAlert("上传失败", "无法解析简历文件");
                }
            });
            
            task.setOnFailed(event -> {
                hideLoading();
                showAlert("上传失败", "无法解析简历文件: " + task.getException().getMessage());
            });
            
            // 启动任务
            new Thread(task).start();
        }
    }
    
    @FXML
    private void handleCompare() {
        List<Resume> selectedResumes = getSelectedResumes();
        
        if (selectedResumes.size() < 2) {
            showAlert("选择错误", "请选择至少两份简历进行对比");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ai/resume/airesume/resume-compare-view.fxml"));
            Parent root = loader.load();
            
            ResumeCompareController controller = loader.getController();
            controller.setResumes(selectedResumes);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("简历对比");
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("错误", "无法打开简历对比窗口: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ResumeTableItem selectedItem = resumeTableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                openResumeDetail(selectedItem.getResume());
            }
        }
    }
    
    /**
     * 处理全选勾选框状态变化
     */
    @FXML
    private void handleSelectAll() {
        boolean selectAll = selectAllCheckBox.isSelected();
        
        // 设置所有项的选中状态
        for (ResumeTableItem item : resumeItems) {
            item.setSelected(selectAll);
        }
        
        // 刷新表格以显示变更
        resumeTableView.refresh();
    }
    
    /**
     * 打开简历详情窗口
     * @param resume 简历对象
     */
    private void openResumeDetail(Resume resume) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ai/resume/airesume/resume-detail-view.fxml"));
            Parent root = loader.load();
            
            ResumeDetailController controller = loader.getController();
            controller.setResume(resume);
            
            // 设置保存回调
            controller.setOnSaveCallback(success -> {
                if (success) {
                    // 重新加载简历列表
                    loadResumesFromDatabase();
                }
            });
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("简历详情");
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("错误", "无法打开简历详情窗口: " + e.getMessage());
        }
    }
    
    /**
     * 获取选中的简历列表
     * @return 选中的简历列表
     */
    private List<Resume> getSelectedResumes() {
        return resumeItems.stream()
                .filter(ResumeTableItem::isSelected)
                .map(ResumeTableItem::getResume)
                .collect(Collectors.toList());
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
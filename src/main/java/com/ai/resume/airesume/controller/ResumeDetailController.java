package com.ai.resume.airesume.controller;

import com.ai.resume.airesume.dto.Education;
import com.ai.resume.airesume.dto.Language;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.dto.Work;
import com.ai.resume.airesume.service.ResumeDetailService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ResumeDetailController implements Initializable {

    @FXML
    private TextField nameField;
    
    @FXML
    private ComboBox<String> genderComboBox;
    
    @FXML
    private TextField ageField;
    
    @FXML
    private ComboBox<String> educationComboBox;
    
    // 教育经历表格
    @FXML
    private TableView<Education> educationTableView;
    
    @FXML
    private TableColumn<Education, String> eduStartDateColumn;
    
    @FXML
    private TableColumn<Education, String> eduEndDateColumn;
    
    @FXML
    private TableColumn<Education, String> eduSchoolColumn;
    
    @FXML
    private TableColumn<Education, String> eduMajorColumn;
    
    @FXML
    private TableColumn<Education, String> eduDegreeColumn;
    
    // 工作经历表格
    @FXML
    private TableView<Work> workTableView;
    
    @FXML
    private TableColumn<Work, String> workStartDateColumn;
    
    @FXML
    private TableColumn<Work, String> workEndDateColumn;
    
    @FXML
    private TableColumn<Work, String> workCompanyColumn;
    
    @FXML
    private TableColumn<Work, String> workPositionColumn;
    
    @FXML
    private TableColumn<Work, String> workResponsibilityColumn;
    
    // 语言能力表格
    @FXML
    private TableView<Language> languageTableView;
    
    @FXML
    private TableColumn<Language, String> langNameColumn;
    
    @FXML
    private TableColumn<Language, String> langLevelColumn;
    
    // 附件表格
    @FXML
    private TableView<AttachmentItem> attachmentTableView;
    
    @FXML
    private TableColumn<AttachmentItem, String> attachmentPathColumn;
    
    @FXML
    private TextArea selfEvaluationArea;
    
    private Resume resume;
    private final ResumeDetailService resumeDetailService;
    private Consumer<Boolean> onSaveCallback;
    
    // 附件项，用于表格显示
    public static class AttachmentItem {
        private String path;
        
        public AttachmentItem(String path) {
            this.path = path;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        @Override
        public String toString() {
            return path;
        }
    }

    /**
     * 构造函数
     */
    public ResumeDetailController() {
        this.resumeDetailService = new ResumeDetailService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化性别下拉框
        genderComboBox.getItems().addAll("男", "女", "未提供");
        
        // 初始化学历下拉框
        educationComboBox.getItems().addAll(
                "博士", "硕士", "本科", "大专", "高中", "其他"
        );
        
        // 初始化教育经历表格
        initEducationTable();
        
        // 初始化工作经历表格
        initWorkTable();
        
        // 初始化语言能力表格
        initLanguageTable();
        
        // 初始化附件表格
        initAttachmentTable();
    }
    
    /**
     * 初始化教育经历表格
     */
    private void initEducationTable() {
        eduStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        eduEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        eduSchoolColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        eduMajorColumn.setCellValueFactory(new PropertyValueFactory<>("major"));
        eduDegreeColumn.setCellValueFactory(new PropertyValueFactory<>("education"));
        
        // 设置表格自适应高度
        educationTableView.setFixedCellSize(25);
        educationTableView.prefHeightProperty().bind(
                educationTableView.fixedCellSizeProperty()
                        .multiply(Bindings.max(3, Bindings.size(educationTableView.getItems())))
                        .add(30));
        
        // 设置列宽自适应
        educationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    /**
     * 初始化工作经历表格
     */
    private void initWorkTable() {
        workStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        workEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        workCompanyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        workPositionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        workResponsibilityColumn.setCellValueFactory(new PropertyValueFactory<>("responsibility"));
        
        // 设置表格自适应高度
        workTableView.setFixedCellSize(25);
        workTableView.prefHeightProperty().bind(
                workTableView.fixedCellSizeProperty()
                        .multiply(Bindings.max(3, Bindings.size(workTableView.getItems())))
                        .add(30));
        
        // 设置列宽自适应
        workTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    /**
     * 初始化语言能力表格
     */
    private void initLanguageTable() {
        langNameColumn.setCellValueFactory(new PropertyValueFactory<>("languageName"));
        langLevelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        
        // 设置表格自适应高度
        languageTableView.setFixedCellSize(25);
        languageTableView.prefHeightProperty().bind(
                languageTableView.fixedCellSizeProperty()
                        .multiply(Bindings.max(3, Bindings.size(languageTableView.getItems())))
                        .add(30));
        
        // 设置列宽自适应
        languageTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    /**
     * 初始化附件表格
     */
    private void initAttachmentTable() {
        attachmentPathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        
        // 设置表格自适应高度
        attachmentTableView.setFixedCellSize(25);
        attachmentTableView.prefHeightProperty().bind(
                attachmentTableView.fixedCellSizeProperty()
                        .multiply(Bindings.max(3, Bindings.size(attachmentTableView.getItems())))
                        .add(30));
        
        // 设置列宽自适应
        attachmentTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    /**
     * 设置保存回调
     * @param callback 保存回调
     */
    public void setOnSaveCallback(Consumer<Boolean> callback) {
        this.onSaveCallback = callback;
    }
    
    /**
     * 设置简历
     * @param resume 简历对象
     */
    public void setResume(Resume resume) {
        this.resume = resume;
        
        // 加载简历数据到表单
        nameField.setText(resume.getName());
        genderComboBox.setValue(resume.getGender());
        ageField.setText(String.valueOf(resume.getAge()));
        educationComboBox.setValue(resume.getHighestEducation());
        selfEvaluationArea.setText(resume.getSelfEvaluation());
        
        // 加载教育经历
        ObservableList<Education> educationList = FXCollections.observableArrayList(resume.getEducationExperience());
        educationTableView.setItems(educationList);
        
        // 加载工作经历
        ObservableList<Work> workList = FXCollections.observableArrayList(resume.getWorkExperience());
        workTableView.setItems(workList);
        
        // 加载语言能力
        ObservableList<Language> languageList = FXCollections.observableArrayList(resume.getLanguageSkills());
        languageTableView.setItems(languageList);
        
        // 加载附件
        ObservableList<AttachmentItem> attachmentList = FXCollections.observableArrayList();
        for (String path : resume.getInterviewAttachments()) {
            attachmentList.add(new AttachmentItem(path));
        }
        attachmentTableView.setItems(attachmentList);
    }
    
    /**
     * 聚焦到自我评价区域
     */
    public void focusOnSelfEvaluation() {
        selfEvaluationArea.requestFocus();
    }
    
    @FXML
    private void handleAddEducation() {
        Dialog<Education> dialog = new Dialog<>();
        dialog.setTitle("添加教育经历");
        dialog.setHeaderText(null);
        
        // 设置确认和取消按钮
        ButtonType confirmButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField startDateField = new TextField();
        startDateField.setPromptText("开始时间");
        TextField endDateField = new TextField();
        endDateField.setPromptText("结束时间");
        TextField schoolField = new TextField();
        schoolField.setPromptText("学校");
        TextField majorField = new TextField();
        majorField.setPromptText("专业");
        TextField degreeField = new TextField();
        degreeField.setPromptText("学历");
        
        grid.add(new Label("开始时间:"), 0, 0);
        grid.add(startDateField, 1, 0);
        grid.add(new Label("结束时间:"), 0, 1);
        grid.add(endDateField, 1, 1);
        grid.add(new Label("学校:"), 0, 2);
        grid.add(schoolField, 1, 2);
        grid.add(new Label("专业:"), 0, 3);
        grid.add(majorField, 1, 3);
        grid.add(new Label("学历:"), 0, 4);
        grid.add(degreeField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // 请求焦点
        Platform.runLater(() -> startDateField.requestFocus());
        
        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                Education education = new Education();
                education.setStartDate(startDateField.getText());
                education.setEndDate(endDateField.getText());
                education.setName(schoolField.getText());
                education.setMajor(majorField.getText());
                education.setEducation(degreeField.getText());
                return education;
            }
            return null;
        });
        
        Optional<Education> result = dialog.showAndWait();
        
        result.ifPresent(education -> {
            educationTableView.getItems().add(education);
        });
    }
    
    @FXML
    private void handleRemoveEducation() {
        int selectedIndex = educationTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            educationTableView.getItems().remove(selectedIndex);
        } else {
            showAlert("提示", "请先选择要删除的教育经历");
        }
    }
    
    @FXML
    private void handleAddWork() {
        Dialog<Work> dialog = new Dialog<>();
        dialog.setTitle("添加工作经历");
        dialog.setHeaderText(null);
        
        // 设置确认和取消按钮
        ButtonType confirmButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField startDateField = new TextField();
        startDateField.setPromptText("开始时间");
        TextField endDateField = new TextField();
        endDateField.setPromptText("结束时间");
        TextField companyField = new TextField();
        companyField.setPromptText("公司");
        TextField positionField = new TextField();
        positionField.setPromptText("职位");
        TextArea responsibilityArea = new TextArea();
        responsibilityArea.setPromptText("工作职责");
        responsibilityArea.setPrefRowCount(3);
        
        grid.add(new Label("开始时间:"), 0, 0);
        grid.add(startDateField, 1, 0);
        grid.add(new Label("结束时间:"), 0, 1);
        grid.add(endDateField, 1, 1);
        grid.add(new Label("公司:"), 0, 2);
        grid.add(companyField, 1, 2);
        grid.add(new Label("职位:"), 0, 3);
        grid.add(positionField, 1, 3);
        grid.add(new Label("工作职责:"), 0, 4);
        grid.add(responsibilityArea, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // 请求焦点
        Platform.runLater(() -> startDateField.requestFocus());
        
        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                Work work = new Work();
                work.setStartDate(startDateField.getText());
                work.setEndDate(endDateField.getText());
                work.setCompanyName(companyField.getText());
                work.setPosition(positionField.getText());
                work.setResponsibility(responsibilityArea.getText());
                return work;
            }
            return null;
        });
        
        Optional<Work> result = dialog.showAndWait();
        
        result.ifPresent(work -> {
            workTableView.getItems().add(work);
        });
    }
    
    @FXML
    private void handleRemoveWork() {
        int selectedIndex = workTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            workTableView.getItems().remove(selectedIndex);
        } else {
            showAlert("提示", "请先选择要删除的工作经历");
        }
    }
    
    @FXML
    private void handleAddLanguage() {
        Dialog<Language> dialog = new Dialog<>();
        dialog.setTitle("添加语言能力");
        dialog.setHeaderText(null);
        
        // 设置确认和取消按钮
        ButtonType confirmButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("语种");
        TextField levelField = new TextField();
        levelField.setPromptText("等级");
        
        grid.add(new Label("语种:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("等级:"), 0, 1);
        grid.add(levelField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // 请求焦点
        Platform.runLater(() -> nameField.requestFocus());
        
        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                Language language = new Language();
                language.setLanguageName(nameField.getText());
                language.setLevel(levelField.getText());
                return language;
            }
            return null;
        });
        
        Optional<Language> result = dialog.showAndWait();
        
        result.ifPresent(language -> {
            languageTableView.getItems().add(language);
        });
    }
    
    @FXML
    private void handleRemoveLanguage() {
        int selectedIndex = languageTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            languageTableView.getItems().remove(selectedIndex);
        } else {
            showAlert("提示", "请先选择要删除的语言能力");
        }
    }
    
    @FXML
    private void handleAddAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择附件");
        
        File selectedFile = fileChooser.showOpenDialog(attachmentTableView.getScene().getWindow());
        if (selectedFile != null) {
            attachmentTableView.getItems().add(new AttachmentItem(selectedFile.getAbsolutePath()));
        }
    }
    
    @FXML
    private void handleRemoveAttachment() {
        int selectedIndex = attachmentTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            attachmentTableView.getItems().remove(selectedIndex);
        } else {
            showAlert("提示", "请先选择要删除的附件");
        }
    }
    
    /**
     * 处理保存按钮点击
     */
    @FXML
    private void handleSave() {
        // 验证表单
        if (nameField.getText().trim().isEmpty()) {
            showAlert("验证失败", "姓名不能为空");
            return;
        }
        
        if (genderComboBox.getValue() == null) {
            showAlert("验证失败", "请选择性别");
            return;
        }
        
        if (ageField.getText().trim().isEmpty()) {
            showAlert("验证失败", "年龄不能为空");
            return;
        }
        
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age <= 0 || age > 120) {
                showAlert("验证失败", "年龄必须在1-120之间");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("验证失败", "年龄必须是数字");
            return;
        }
        
        if (educationComboBox.getValue() == null) {
            showAlert("验证失败", "请选择最高学历");
            return;
        }
        
        // 更新简历对象
        resume.setName(nameField.getText().trim());
        resume.setGender(genderComboBox.getValue());
        resume.setAge(Integer.parseInt(ageField.getText().trim()));
        resume.setHighestEducation(educationComboBox.getValue());
        resume.setSelfEvaluation(selfEvaluationArea.getText());
        
        // 更新教育经历
        List<Education> educations = new ArrayList<>(educationTableView.getItems());
        resume.setEducationExperience(educations);
        
        // 更新工作经历
        List<Work> works = new ArrayList<>(workTableView.getItems());
        resume.setWorkExperience(works);
        
        // 更新语言能力
        List<Language> languages = new ArrayList<>(languageTableView.getItems());
        resume.setLanguageSkills(languages);
        
        // 更新附件
        List<String> attachments = new ArrayList<>();
        for (AttachmentItem item : attachmentTableView.getItems()) {
            attachments.add(item.getPath());
        }
        resume.setInterviewAttachments(attachments);
        
        // 保存到数据库
        boolean success = resumeDetailService.updateResume(resume);
        
        if (success) {
            showInfo("保存成功", "简历信息已成功保存");
            if (onSaveCallback != null) {
                onSaveCallback.accept(true);
            }
        } else {
            showAlert("保存失败", "无法保存简历信息到数据库");
            if (onSaveCallback != null) {
                onSaveCallback.accept(false);
            }
        }
    }
    
    @FXML
    private void handleClose() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
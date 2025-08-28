package com.ai.resume.airesume;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

/**
 * AI简历分析系统主应用类
 */
public class HelloApplication extends Application {
    
    /**
     * 应用程序启动方法
     * @param stage 主舞台
     * @throws IOException 如果FXML加载失败
     */
    @Override
    public void start(Stage stage) throws IOException {
        // 加载简历列表视图
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("resume-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        
        // 应用Bootstrap样式
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        
        // 加载自定义样式
        scene.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());
        
        // 设置舞台属性
        stage.setTitle("AI简历分析系统");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 应用程序主入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch();
    }
}
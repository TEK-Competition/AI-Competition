module com.ai.resume.airesume {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires java.xml.bind;

    opens com.ai.resume.airesume to javafx.fxml;
    opens com.ai.resume.airesume.controller to javafx.fxml;
    opens com.ai.resume.airesume.dto to javafx.fxml, com.fasterxml.jackson.databind;
    
    exports com.ai.resume.airesume;
    exports com.ai.resume.airesume.controller;
    exports com.ai.resume.airesume.dto;
}
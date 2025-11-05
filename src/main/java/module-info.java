module org.angel.curso.javafx.chatapp.appjavafxchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.net.http;
    requires spring.websocket;
    requires spring.messaging;
    requires com.fasterxml.jackson.databind;
    requires java.logging;
    requires spring.core;


    opens org.angel.curso.javafx.chatapp.appjavafxchat.controllers to javafx.fxml;
    opens org.angel.curso.javafx.chatapp.appjavafxchat.models to javafx.base, com.fasterxml.jackson.databind;
    exports org.angel.curso.javafx.chatapp.appjavafxchat;
}
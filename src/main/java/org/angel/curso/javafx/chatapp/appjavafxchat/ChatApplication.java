package org.angel.curso.javafx.chatapp.appjavafxchat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(ChatApplication.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(loader.load(), 680, 400);
        stage.setTitle("Chat WebSocket con Spring Boot!");
        stage.setScene(scene);
        stage.show();

    }
}

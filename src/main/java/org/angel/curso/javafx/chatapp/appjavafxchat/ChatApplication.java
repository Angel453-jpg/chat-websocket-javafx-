package org.angel.curso.javafx.chatapp.appjavafxchat;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.angel.curso.javafx.chatapp.appjavafxchat.models.Messages;

import java.io.IOException;

public class ChatApplication extends Application {

    private Messages message = new Messages();

    @Override
    public void start(Stage stage) {

        TextField username = new TextField();
        username.setPromptText("Tu Username.....");
        Button conButton = new Button("Conectar");

        HBox header = new HBox(10, username, conButton);

        HBox chat = new HBox(10);
        ScrollPane scroll = new ScrollPane(chat);

        scroll.setPadding(new Insets(10));
        scroll.setFitToWidth(true);
        scroll.setPannable(true);

        TextField messageField = new TextField();
        messageField.setPromptText("Escribe un mensaje...");
        Button sendButton = new Button("Enviar");
        Button disconnectButton = new Button("Desconectar Chat");

        HBox footer = new HBox(10, messageField, sendButton, disconnectButton);
        VBox panel = new VBox(10, header, scroll, footer);
        panel.setPadding(new Insets(10));
        Scene scene = new Scene(panel, 680, 400);
        stage.setTitle("Chat Web Socket con Spring Boot!");
        stage.setScene(scene);
        stage.show();
    }
}

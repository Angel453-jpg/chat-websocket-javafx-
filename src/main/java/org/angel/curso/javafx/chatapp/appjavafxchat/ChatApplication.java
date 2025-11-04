package org.angel.curso.javafx.chatapp.appjavafxchat;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.angel.curso.javafx.chatapp.appjavafxchat.models.Messages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatApplication extends Application {

    private Messages message = new Messages();

    @Override
    public void start(Stage stage) {

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tu Username.....");
        Button conButton = new Button("Conectar");
        HBox header = new HBox(10, usernameField, conButton);

        VBox chat = new VBox(10);

        chat.setVisible(false);

        ScrollPane scroll = new ScrollPane(chat);
        scroll.setPadding(new Insets(10));
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVisible(false);

        TextField messageField = new TextField();
        messageField.setPromptText("Escribe un mensaje...");
        Button sendButton = new Button("Enviar");
        Button disconnectButton = new Button("Desconectar Chat");
        HBox footer = new HBox(10, messageField, sendButton, disconnectButton);
        footer.setVisible(false);

        conButton.setOnAction(e -> {
            if (!usernameField.getText().isBlank()) {

                this.message.setUsername(usernameField.getText());
                System.out.println(usernameField.getText());

                chat.setVisible(true);
                scroll.setVisible(true);
                footer.setVisible(true);

                usernameField.setVisible(false);
                conButton.setVisible(false);

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor ingrese el nombre de usuario!");
                alert.show();
            }
        });

        disconnectButton.setOnAction(e -> {
            chat.setVisible(false);
            chat.getChildren().clear();
            scroll.setVisible(false);
            footer.setVisible(false);

            usernameField.setVisible(true);
            conButton.setVisible(true);

            this.message = new Messages();
            messageField.setText("");

        });

        sendButton.setOnAction(e -> {
            if (!messageField.getText().isBlank()) {

                this.message.setType("MESSAGE");
                this.message.setText(messageField.getText());

                SimpleDateFormat format = new SimpleDateFormat("hh:mm:a");
                String time = format.format(new Date().getTime());
                Text username = new Text(message.getUsername());
                username.setFill(Color.RED);
                username.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                TextFlow textFlow = new TextFlow(new Text(time + " @"));
                textFlow.getChildren().add(username);
                textFlow.getChildren().add(new Text(" dice: \n".concat(message.getText())));

                chat.getChildren().add(textFlow);
                messageField.setText("");

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor ingrese un mensaje...");
                alert.show();
            }
        });

        VBox pane = new VBox(10, header, scroll, footer);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane, 680, 400);
        stage.setTitle("Chat Web Socket con Spring Boot!");
        stage.setScene(scene);
        stage.show();
    }
}

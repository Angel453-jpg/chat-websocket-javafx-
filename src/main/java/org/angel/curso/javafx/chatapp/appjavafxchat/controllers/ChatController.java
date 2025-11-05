package org.angel.curso.javafx.chatapp.appjavafxchat.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.angel.curso.javafx.chatapp.appjavafxchat.models.Messages;
import org.angel.curso.javafx.chatapp.appjavafxchat.network.ChatClient;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class ChatController {

    @FXML
    private TextField usernameField;
    @FXML
    private Button connectButton;
    @FXML
    private VBox chatBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private Label writingLabel;

    private ChatClient chatClient;

    private final Messages message = new Messages();

    private Timer writingTimer;

    @FXML
    private void initialize() {
        chatBox.setVisible(false);
        scrollPane.setVisible(false);
        messageField.setVisible(false);
        sendButton.setVisible(false);
        disconnectButton.setVisible(false);

        connectButton.setOnAction(e -> connect());
        sendButton.setOnAction(e -> sendMessage());
        disconnectButton.setOnAction(e -> disconnect());
        messageField.setOnKeyTyped(e -> chatClient.sendWriting(message.getUsername()));

    }

    private void connect() {
        String username = usernameField.getText();
        if (username.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Por favor ingrese un nombre de usuario!").show();
            return;
        }

        chatClient = new ChatClient(this::onMessageReceived, this::onHistoryReceived, this::onUserWriting);
        chatClient.connect(username);

        usernameField.setVisible(false);
        connectButton.setVisible(false);
        chatBox.setVisible(true);
        scrollPane.setVisible(true);
        messageField.setVisible(true);
        sendButton.setVisible(true);
        disconnectButton.setVisible(true);

        message.setUsername(username);
    }

    private void sendMessage() {
        if (!messageField.getText().isBlank()) {
            message.setType("MESSAGE");
            message.setText(messageField.getText());
            chatClient.sendMessage(message);
            messageField.clear();
        } else {
            new Alert(Alert.AlertType.ERROR, "Por favor ingrese un mensaje!").show();
        }
    }

    private void disconnect() {
        chatClient.disconnect();
        chatBox.getChildren().clear();

        usernameField.setVisible(true);
        connectButton.setVisible(true);
        chatBox.setVisible(false);
        scrollPane.setVisible(false);
        messageField.setVisible(false);
        sendButton.setVisible(false);
        disconnectButton.setVisible(false);
    }

    // --- Callbacks desde chatClient
    private void onMessageReceived(Messages msg) {
        Platform.runLater(() -> {

            //Si el servidor nos asigna un color en el mensaje de tipo NEW_USER
            if ("NEW_USER".equals(msg.getType()) && msg.getUsername().equals(message.getUsername())) {
                message.setColor(msg.getColor());
            }

            SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
            String time = format.format(msg.getDate());

            //Texto del nombre del usuario
            Text username = new Text(msg.getUsername() + ": ");
            username.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            //Aplicar colo si viene en el backend
            if (msg.getColor() != null && !msg.getColor().isBlank()) {
                username.setStyle("-fx-fill: " + msg.getColor() + ";");
            }

            //Texto del mensaje
            Text messageText = new Text(msg.getText());
            TextFlow flow = new TextFlow(new Text(time + " "), username, messageText);
            chatBox.getChildren().add(flow);

            //Baja automáticamente el Scroll
            Platform.runLater(() -> {
                scrollPane.layout();
                scrollPane.setVvalue(scrollPane.getVmax());
            });

        });
    }

    private void onHistoryReceived(Iterable<Messages> messages) {
        Platform.runLater(() -> {
            for (Messages msg : messages) {
                onMessageReceived(msg);
            }
        });
    }

    private void onUserWriting(String username) {
        Platform.runLater(() -> {
            if (username == null || username.isBlank()) {
                writingLabel.setText("");
                return;
            }
            writingLabel.setText(username);

            if (writingTimer != null) {
                writingTimer.cancel();
            }

            writingTimer = new Timer();
            writingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> writingLabel.setText(""));
                }
            }, 3000); //Se oculta después de 3 segundos

        });
    }

}

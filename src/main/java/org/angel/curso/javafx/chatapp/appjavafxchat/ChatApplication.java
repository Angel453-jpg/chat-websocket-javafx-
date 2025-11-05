package org.angel.curso.javafx.chatapp.appjavafxchat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.angel.curso.javafx.chatapp.appjavafxchat.models.Messages;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ChatApplication extends Application {

    private Messages message = new Messages();
    private String clientId;

    private final Label writing = new Label();

    @Override
    public void start(Stage stage) {

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tu Username ...");
        Button connButton = new Button("Conectar");
        HBox header = new HBox(10, usernameField, connButton);

        VBox chat = new VBox(10);
        chat.setVisible(false);
        ScrollPane scrollPane = new ScrollPane(chat);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVisible(false);

        TextField messageField = new TextField();
        messageField.setPromptText("Escribe un mensaje ...");
        Button sendButton = new Button("Enviar");
        Button deConnButton = new Button("Cerrar Chat");
        HBox footer = new HBox(10, messageField, sendButton, deConnButton);
        footer.setVisible(false);

        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(transports));
        List<MessageConverter> converters = List.of(new StringMessageConverter(), new MappingJackson2MessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converters));

        connButton.setOnAction((event) -> {
            if (!usernameField.getText().isBlank()) {
                stompClient.connectAsync("http://localhost:8080/chat-websocket", new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        clientId = session.getSessionId();
                        message.setUsername(usernameField.getText());
                        System.out.println(usernameField.getText());
                        chat.setVisible(true);
                        scrollPane.setVisible(true);
                        footer.setVisible(true);

                        usernameField.setVisible(false);
                        connButton.setVisible(false);
                        System.out.println("Conectado: " + session.isConnected() + " Id: " + session.getSessionId());

                        session.subscribe("/chat/message", new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return Messages.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                Messages messagePayload = (Messages) payload;

                                SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
                                String time = format.format(messagePayload.getDate());

                                if (message.getUsername().equals(messagePayload.getUsername())
                                        && message.getColor() == null
                                        && messagePayload.getType().equals("NEW_USER")) {
                                    message.setColor(messagePayload.getColor());
                                }
                                Platform.runLater(() -> {

                                    // --- Manejo seguro del color ---
                                    String colorCode = messagePayload.getColor();
                                    Color color;
                                    try {
                                        if (colorCode == null || colorCode.isBlank()) {
                                            color = Color.BLACK;
                                        } else {
                                            color = Color.web(colorCode);
                                        }
                                    } catch (IllegalArgumentException ex) {
                                        color = Color.BLACK;
                                    }

                                    Text username = new Text(messagePayload.getUsername());
                                    username.setFill(color);
                                    username.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                                    TextFlow textFlow = null;

                                    if (messagePayload.getType().equals("MESSAGE")) {
                                        textFlow = new TextFlow(new Text(time + " @"));
                                        textFlow.getChildren().add(username);
                                        textFlow.getChildren().add(new Text(" dice: \n".concat(messagePayload.getText())));
                                    } else if (messagePayload.getType().equals("NEW_USER")) {
                                        textFlow = new TextFlow(new Text(time + ": " + messagePayload.getText()));
                                        textFlow.getChildren().add(new Text(" conectado! @"));
                                        textFlow.getChildren().add(username);
                                    }

                                    chat.getChildren().add(textFlow);
                                });
                            }
                        });

                        session.subscribe("/chat/history/".concat(clientId), new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return Messages[].class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                List<Messages> messages = Arrays.asList((Messages[]) payload);
                                Platform.runLater(() -> {

                                    for (Messages messagePayload : messages) {
                                        // --- Manejo seguro del color ---
                                        String colorCode = messagePayload.getColor();
                                        Color color;
                                        try {
                                            if (colorCode == null || colorCode.isBlank()) {
                                                color = Color.BLACK;
                                            } else {
                                                color = Color.web(colorCode);
                                            }
                                        } catch (IllegalArgumentException ex) {
                                            color = Color.BLACK;
                                        }

                                        Text username = new Text(messagePayload.getUsername());
                                        username.setFill(color);
                                        username.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                                        String time = new SimpleDateFormat("hh:mm a").format(messagePayload.getDate());
                                        TextFlow textFlow = new TextFlow(new Text(time + " @"));
                                        textFlow.getChildren().add(username);
                                        textFlow.getChildren().add(new Text(" dice: \n".concat(messagePayload.getText())));

                                        chat.getChildren().add(textFlow);
                                    }

                                });
                            }
                        });

                        session.subscribe("/chat/writing", new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return String.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                Platform.runLater(() -> writing.setText(payload.toString()));
                                CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> Platform.runLater(() -> writing.setText("")));
                            }
                        });
                        session.send("/app/history", clientId);

                        message.setType("NEW_USER");
                        session.send("/app/message", message);

                        sendButton.setOnAction(event -> {
                            if (!messageField.getText().isBlank()) {
                                message.setType("MESSAGE");
                                message.setText(messageField.getText());
                                session.send("/app/message", message);
                                messageField.setText("");
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor ingrese un mensaje!");
                                alert.show();
                            }
                        });

                        deConnButton.setOnAction(event -> {
                            if (session.isConnected()) {
                                session.disconnect();
                            }
                            chat.setVisible(false);
                            chat.getChildren().clear();
                            scrollPane.setVisible(false);
                            footer.setVisible(false);

                            usernameField.setVisible(true);
                            connButton.setVisible(true);

                            message = new Messages();
                            messageField.setText("");
                        });

                        messageField.setOnKeyTyped(event -> {
                            session.send("/app/writing", message.getUsername());
                        });
                    }
                });


            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor ingrese el nombre de usuario!");
                alert.show();
            }
        });

        VBox pane = new VBox(10, header, scrollPane, footer, writing);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane, 680, 400);
        stage.setTitle("Chat Web Socket con Spring Boot!");
        stage.setScene(scene);
        stage.show();
    }
}
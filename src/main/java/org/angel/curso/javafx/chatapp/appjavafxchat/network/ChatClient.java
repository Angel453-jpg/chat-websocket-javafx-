package org.angel.curso.javafx.chatapp.appjavafxchat.network;

import org.angel.curso.javafx.chatapp.appjavafxchat.models.Messages;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
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

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChatClient {

    private StompSession session;
    private String clientId;
    private final Consumer<Messages> onMessageReceived;
    private final Consumer<Iterable<Messages>> onHistoryReceived;
    private final Consumer<String> onUserWriting;

    public ChatClient(Consumer<Messages> onMessageReceived, Consumer<Iterable<Messages>> onHistoryReceived, Consumer<String> onUserWriting) {
        this.onMessageReceived = onMessageReceived;
        this.onHistoryReceived = onHistoryReceived;
        this.onUserWriting = onUserWriting;
    }

    public void connect(String username) {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new CompositeMessageConverter(
                List.of(new StringMessageConverter(), new MappingJackson2MessageConverter())
        ));

        stompClient.connectAsync("http://localhost:8080/chat-websocket", new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(@NonNull StompSession session, @NonNull StompHeaders connectedHeaders) {

                ChatClient.this.session = session;
                clientId = session.getSessionId();

                session.subscribe("/chat/message", new StompFrameHandler() {
                    @Override
                    public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                        return Messages.class;
                    }

                    @Override
                    public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                        onMessageReceived.accept((Messages) payload);
                    }
                });

                session.subscribe("/chat/history/" + clientId, new StompFrameHandler() {

                    @Override
                    public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                        return Messages[].class;
                    }

                    @Override
                    public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                        Messages[] msgs = (Messages[]) payload;
                        onHistoryReceived.accept(List.of(msgs));
                    }
                });

                session.subscribe("/chat/writing", new StompFrameHandler() {

                    @Override
                    public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                        onUserWriting.accept(payload.toString());
                        CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS)
                                .execute(() -> onUserWriting.accept(""));
                    }
                });

                Messages newUser = new Messages();
                newUser.setUsername(username);
                newUser.setType("NEW_USER");
                session.send("/app/history", clientId);
                session.send("/app/message", newUser);

            }
        });

    }

    public void sendMessage(Messages message) {
        if (session != null && session.isConnected()) {
            session.send("/app/message", message);
        }
    }

    public void sendWriting(String username) {
        if (session != null && session.isConnected()) {
            session.send("/app/writing", username);
        }
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

}

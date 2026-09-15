package com.jgcb.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RoomWebSocketConfig implements WebSocketConfigurer {
    private final RoomWebSocketHandler roomWebSocketHandler;
    private final RoomHandshakeInterceptor roomHandshakeInterceptor;

    public RoomWebSocketConfig(RoomWebSocketHandler roomWebSocketHandler,
                               RoomHandshakeInterceptor roomHandshakeInterceptor) {
        this.roomWebSocketHandler = roomWebSocketHandler;
        this.roomHandshakeInterceptor = roomHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomWebSocketHandler, "/ws/room")
                .addInterceptors(roomHandshakeInterceptor)
                .setAllowedOrigins(
                        "http://127.0.0.1", "http://localhost",
                        "http://127.0.0.1:8080", "http://localhost:8080",
                        "https://218.12.120.170:52444"
                );
    }
}
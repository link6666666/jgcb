package com.jgcb.config.websocket;

import com.jgcb.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class RoomHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtUtil jwtUtil;

    public RoomHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        String token = params.getFirst("token");
        String roomValue = params.getFirst("roomId");
        if (!StringUtils.hasText(token) || jwtUtil.isTokenExpired(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            long roomId = Long.parseLong(roomValue == null ? "1" : roomValue);
            if (roomId < 1) throw new IllegalArgumentException("invalid room");
            attributes.put("userId", jwtUtil.getUserId(token));
            attributes.put("roomId", roomId);
            return true;
        } catch (Exception ignored) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // No resources are allocated during the handshake.
    }
}
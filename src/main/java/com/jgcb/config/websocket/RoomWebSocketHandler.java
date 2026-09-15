package com.jgcb.config.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgcb.dto.PlayerStateRequest;
import com.jgcb.dto.RoomPlayerVO;
import com.jgcb.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(RoomWebSocketHandler.class);
    private static final int SEND_TIMEOUT_MS = 5000;
    private static final int BUFFER_SIZE = 64 * 1024;

    private final RoomService roomService;
    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();

    public RoomWebSocketHandler(RoomService roomService, ObjectMapper objectMapper) {
        this.roomService = roomService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = attributeAsLong(session, "roomId");
        Long userId = attributeAsLong(session, "userId");
        RoomPlayerVO player = roomService.enterRoom(roomId, userId);
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(session, SEND_TIMEOUT_MS, BUFFER_SIZE);
        sessionsByRoom.computeIfAbsent(roomId, ignored -> ConcurrentHashMap.newKeySet()).add(safeSession);

        send(safeSession, Map.of("type", "snapshot", "players", roomService.getOnlinePlayers(roomId)));
        broadcast(roomId, Map.of("type", "player_joined", "player", player), session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json;
        try {
            json = objectMapper.readTree(message.getPayload());
        } catch (Exception ignored) {
            return;
        }
        if (!"move".equals(json.path("type").asText())) return;
        int x = json.path("x").asInt(-1);
        int y = json.path("y").asInt(-1);
        if (x < 0 || x > 1200 || y < 0 || y > 800) return;

        PlayerStateRequest request = new PlayerStateRequest();
        request.setX(x);
        request.setY(y);
        request.setDirection(json.path("direction").asText("down"));
        request.setAction(json.path("action").asText("walk"));
        Long roomId = attributeAsLong(session, "roomId");
        Long userId = attributeAsLong(session, "userId");
        RoomPlayerVO player = roomService.updatePlayerState(roomId, userId, request);
        broadcast(roomId, Map.of("type", "player_moved", "player", player), null);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long roomId = nullableAttributeAsLong(session, "roomId");
        Long userId = nullableAttributeAsLong(session, "userId");
        if (roomId == null || userId == null) return;
        Set<WebSocketSession> roomSessions = sessionsByRoom.get(roomId);
        if (roomSessions != null) {
            roomSessions.removeIf(candidate -> candidate.getId().equals(session.getId()) || !candidate.isOpen());
            if (roomSessions.isEmpty()) sessionsByRoom.remove(roomId);
        }
        boolean anotherTabOpen = roomSessions != null && roomSessions.stream()
                .anyMatch(candidate -> candidate.isOpen() && userId.equals(nullableAttributeAsLong(candidate, "userId")));
        if (!anotherTabOpen) {
            roomService.leaveRoom(roomId, userId);
            broadcast(roomId, Map.of("type", "player_left", "userId", userId), null);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.debug("Room WebSocket transport error: {}", exception.getMessage());
        if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
    }

    private void broadcast(Long roomId, Object payload, String excludedSessionId) {
        Set<WebSocketSession> sessions = sessionsByRoom.get(roomId);
        if (sessions == null || sessions.isEmpty()) return;
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            log.warn("Unable to serialize room event", exception);
            return;
        }
        for (WebSocketSession session : sessions) {
            if (!session.isOpen() || (excludedSessionId != null && excludedSessionId.equals(session.getId()))) continue;
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException exception) {
                log.debug("Unable to send room event to {}", session.getId());
            }
        }
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private Long attributeAsLong(WebSocketSession session, String name) {
        Long value = nullableAttributeAsLong(session, name);
        if (value == null) throw new IllegalStateException("Missing WebSocket attribute: " + name);
        return value;
    }

    private Long nullableAttributeAsLong(WebSocketSession session, String name) {
        Object value = session.getAttributes().get(name);
        return value instanceof Number number ? number.longValue() : null;
    }
}
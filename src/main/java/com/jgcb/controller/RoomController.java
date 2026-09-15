package com.jgcb.controller;

import com.jgcb.common.Result;
import com.jgcb.dto.PlayerStateRequest;
import com.jgcb.dto.RoomInfoVO;
import com.jgcb.dto.RoomObjectStateRequest;
import com.jgcb.dto.RoomPlayerVO;
import com.jgcb.entity.RoomObject;
import com.jgcb.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{roomId}")
    public Result<RoomInfoVO> getRoom(@PathVariable Long roomId) {
        return Result.ok(roomService.getRoom(roomId));
    }

    @GetMapping("/{roomId}/players")
    public Result<List<RoomPlayerVO>> getPlayers(@PathVariable Long roomId) {
        return Result.ok(roomService.getOnlinePlayers(roomId));
    }

    @PutMapping("/{roomId}/players/me/state")
    public Result<RoomPlayerVO> savePlayerState(@PathVariable Long roomId,
                                                 @Valid @RequestBody PlayerStateRequest request,
                                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(roomService.updatePlayerState(roomId, userId, request));
    }

    @PutMapping("/{roomId}/objects/{objectId}/state")
    public Result<RoomObject> saveObjectState(@PathVariable Long roomId,
                                               @PathVariable Long objectId,
                                               @Valid @RequestBody RoomObjectStateRequest request) {
        return Result.ok(roomService.updateObjectState(roomId, objectId, request.getState()));
    }
}
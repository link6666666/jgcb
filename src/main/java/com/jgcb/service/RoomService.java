package com.jgcb.service;

import com.jgcb.dto.PlayerStateRequest;
import com.jgcb.dto.RoomInfoVO;
import com.jgcb.dto.RoomPlayerVO;
import com.jgcb.entity.RoomObject;
import java.util.List;

public interface RoomService {
    RoomInfoVO getRoom(Long roomId);
    List<RoomPlayerVO> getOnlinePlayers(Long roomId);
    RoomPlayerVO enterRoom(Long roomId, Long userId);
    RoomPlayerVO updatePlayerState(Long roomId, Long userId, PlayerStateRequest request);
    void leaveRoom(Long roomId, Long userId);
    RoomObject updateObjectState(Long roomId, Long objectId, String state);
}
package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jgcb.dto.PlayerStateRequest;
import com.jgcb.dto.RoomInfoVO;
import com.jgcb.dto.RoomPlayerVO;
import com.jgcb.entity.PetOutfit;
import com.jgcb.entity.PlayerState;
import com.jgcb.entity.Room;
import com.jgcb.entity.RoomObject;
import com.jgcb.entity.SysUser;
import com.jgcb.mapper.PetOutfitMapper;
import com.jgcb.mapper.PlayerStateMapper;
import com.jgcb.mapper.RoomMapper;
import com.jgcb.mapper.RoomObjectMapper;
import com.jgcb.mapper.SysUserMapper;
import com.jgcb.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {
    private static final int SPAWN_X = 240;
    private static final int SPAWN_Y = 650;
    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            "idle", "walk", "interact:computer", "interact:sofa",
            "interact:mahjong_table", "interact:plant"
    );

    private final RoomMapper roomMapper;
    private final RoomObjectMapper roomObjectMapper;
    private final PlayerStateMapper playerStateMapper;
    private final SysUserMapper sysUserMapper;
    private final PetOutfitMapper petOutfitMapper;

    public RoomServiceImpl(RoomMapper roomMapper,
                           RoomObjectMapper roomObjectMapper,
                           PlayerStateMapper playerStateMapper,
                           SysUserMapper sysUserMapper,
                           PetOutfitMapper petOutfitMapper) {
        this.roomMapper = roomMapper;
        this.roomObjectMapper = roomObjectMapper;
        this.playerStateMapper = playerStateMapper;
        this.sysUserMapper = sysUserMapper;
        this.petOutfitMapper = petOutfitMapper;
    }

    @Override
    public RoomInfoVO getRoom(Long roomId) {
        Room room = requireRoom(roomId);
        List<RoomObject> objects = roomObjectMapper.selectList(new LambdaQueryWrapper<RoomObject>()
                .eq(RoomObject::getRoomId, roomId)
                .orderByAsc(RoomObject::getId));
        RoomInfoVO vo = new RoomInfoVO();
        vo.setId(room.getId());
        vo.setName(room.getName());
        vo.setMapImage("/room-assets/room-map.jpg");
        vo.setMapWidth(1200);
        vo.setMapHeight(800);
        vo.setObjects(objects);
        return vo;
    }

    @Override
    public List<RoomPlayerVO> getOnlinePlayers(Long roomId) {
        requireRoom(roomId);
        List<PlayerState> states = playerStateMapper.selectList(new LambdaQueryWrapper<PlayerState>()
                .eq(PlayerState::getRoomId, roomId)
                .eq(PlayerState::getOnline, 1));
        if (states.isEmpty()) return Collections.emptyList();
        List<Long> userIds = states.stream().map(PlayerState::getUserId).toList();
        Map<Long, SysUser> users = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, PetOutfit> outfits = petOutfitMapper.selectList(new LambdaQueryWrapper<PetOutfit>()
                        .in(PetOutfit::getUserId, userIds)).stream()
                .collect(Collectors.toMap(PetOutfit::getUserId, Function.identity(), (first, ignored) -> first));
        return states.stream()
                .map(state -> toPlayer(state, users.get(state.getUserId()), outfits.get(state.getUserId())))
                .filter(player -> player.getNickname() != null)
                .toList();
    }

    @Override
    @Transactional
    public RoomPlayerVO enterRoom(Long roomId, Long userId) {
        requireRoom(roomId);
        SysUser user = requireUser(userId);
        PlayerState state = playerStateMapper.selectById(userId);
        if (state == null) {
            state = new PlayerState();
            state.setUserId(userId);
            state.setRoomId(roomId);
            state.setX(SPAWN_X);
            state.setY(SPAWN_Y);
            state.setAction("idle");
            state.setOnline(1);
            playerStateMapper.insert(state);
        } else {
            state.setRoomId(roomId);
            state.setOnline(1);
            state.setAction("idle");
            if (state.getX() == null) state.setX(SPAWN_X);
            if (state.getY() == null) state.setY(SPAWN_Y);
            playerStateMapper.updateById(state);
        }
        return toPlayer(state, user, findOutfit(userId));
    }

    @Override
    @Transactional
    public RoomPlayerVO updatePlayerState(Long roomId, Long userId, PlayerStateRequest request) {
        requireRoom(roomId);
        SysUser user = requireUser(userId);
        PlayerState state = playerStateMapper.selectById(userId);
        if (state == null || !roomId.equals(state.getRoomId())) {
            enterRoom(roomId, userId);
            state = playerStateMapper.selectById(userId);
        }
        state.setX(clamp(request.getX(), 30, 1170));
        state.setY(clamp(request.getY(), 70, 760));
        state.setAction(sanitizeAction(request.getAction()));
        state.setOnline(1);
        playerStateMapper.updateById(state);
        RoomPlayerVO player = toPlayer(state, user, findOutfit(userId));
        player.setDirection(sanitizeDirection(request.getDirection()));
        return player;
    }

    @Override
    public void leaveRoom(Long roomId, Long userId) {
        playerStateMapper.update(null, new LambdaUpdateWrapper<PlayerState>()
                .eq(PlayerState::getUserId, userId)
                .eq(PlayerState::getRoomId, roomId)
                .set(PlayerState::getOnline, 0)
                .set(PlayerState::getAction, "idle"));
    }

    @Override
    public RoomObject updateObjectState(Long roomId, Long objectId, String state) {
        RoomObject object = roomObjectMapper.selectOne(new LambdaQueryWrapper<RoomObject>()
                .eq(RoomObject::getId, objectId)
                .eq(RoomObject::getRoomId, roomId));
        if (object == null) throw new RuntimeException("房间物品不存在");
        object.setState(state);
        roomObjectMapper.updateById(object);
        return object;
    }

    private Room requireRoom(Long roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) throw new RuntimeException("房间不存在");
        return room;
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new RuntimeException("用户不存在或已禁用");
        }
        return user;
    }

    private PetOutfit findOutfit(Long userId) {
        return petOutfitMapper.selectOne(new LambdaQueryWrapper<PetOutfit>()
                .eq(PetOutfit::getUserId, userId)
                .last("LIMIT 1"));
    }

    private RoomPlayerVO toPlayer(PlayerState state, SysUser user, PetOutfit outfit) {
        RoomPlayerVO vo = new RoomPlayerVO();
        vo.setId(state.getUserId());
        vo.setUserId(state.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        vo.setX(state.getX());
        vo.setY(state.getY());
        vo.setDirection("down");
        vo.setAction(state.getAction() == null ? "idle" : state.getAction());
        vo.setOnline(state.getOnline() != null && state.getOnline() == 1);
        if (outfit != null) {
            vo.setHead(outfit.getHead());
            vo.setNeck(outfit.getNeck());
            vo.setBody(outfit.getBody());
            vo.setAccessory(outfit.getAccessory());
        }
        return vo;
    }

    private int clamp(Integer value, int min, int max) {
        return Math.max(min, Math.min(value == null ? min : value, max));
    }

    private String sanitizeDirection(String direction) {
        return List.of("left", "right", "up", "down").contains(direction) ? direction : "down";
    }

    private String sanitizeAction(String action) {
        return action != null && ALLOWED_ACTIONS.contains(action) ? action : "idle";
    }
}
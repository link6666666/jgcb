-- MVP schema reference. RoomSchemaInitializer applies the same idempotent setup at startup.
CREATE TABLE IF NOT EXISTS room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS room_object (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    state TEXT NULL,
    INDEX idx_room_object_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS player_state (
    user_id BIGINT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    x INT NOT NULL DEFAULT 240,
    y INT NOT NULL DEFAULT 650,
    action VARCHAR(40) NOT NULL DEFAULT 'idle',
    online TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_player_state_room_online (room_id, online)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
package com.jgcb.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class RoomSchemaInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public RoomSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS room (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS room_object (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    room_id BIGINT NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    state TEXT NULL,
                    INDEX idx_room_object_room (room_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS player_state (
                    user_id BIGINT PRIMARY KEY,
                    room_id BIGINT NOT NULL,
                    x INT NOT NULL DEFAULT 240,
                    y INT NOT NULL DEFAULT 650,
                    action VARCHAR(40) NOT NULL DEFAULT 'idle',
                    online TINYINT(1) NOT NULL DEFAULT 0,
                    INDEX idx_player_state_room_online (room_id, online)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.update("INSERT INTO room (id, name) VALUES (1, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)", "糍粑小屋");
        seedObject(1L, "mahjong_table", 620, 450);
        seedObject(2L, "computer", 470, 165);
        seedObject(3L, "sofa", 875, 235);
        seedObject(4L, "plant", 1070, 545);
        jdbcTemplate.update("UPDATE player_state SET online = 0, action = 'idle'");
    }

    private void seedObject(Long id, String type, int x, int y) {
        jdbcTemplate.update("""
                INSERT INTO room_object (id, room_id, type, x, y, state)
                VALUES (?, 1, ?, ?, ?, '{}')
                ON DUPLICATE KEY UPDATE room_id = 1, type = VALUES(type), x = VALUES(x), y = VALUES(y)
                """, id, type, x, y);
    }
}
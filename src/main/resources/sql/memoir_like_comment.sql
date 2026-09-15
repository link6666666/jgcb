CREATE TABLE IF NOT EXISTS `memoir_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `memoir_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_memoir` (`user_id`, `memoir_id`),
    INDEX `idx_memoir_id` (`memoir_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `memoir_comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `memoir_id` BIGINT NOT NULL,
    `content` VARCHAR(500) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_memoir_id` (`memoir_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

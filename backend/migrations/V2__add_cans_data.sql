ALTER TABLE cans ADD deleted_at TIMESTAMP NULL;
ALTER TABLE cans ADD identifier VARCHAR(50) NOT NULL UNIQUE AFTER id;


CREATE TABLE can_data (
    id              INT UNSIGNED NOT NULL  AUTO_INCREMENT,
    can_id          INT UNSIGNED NOT NULL,
    moment   TIMESTAMP NOT NULL DEFAULT NOW(),
    filling_rate    DOUBLE(5,3) NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (can_id) REFERENCES cans (id)
)
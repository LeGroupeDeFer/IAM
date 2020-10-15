CREATE TABLE users (
  id            INT UNSIGNED    NOT NULL AUTO_INCREMENT,
  username      VARCHAR(128)    NOT NULL,
  password      VARCHAR(128)    NOT NULL,

  PRIMARY KEY(id)
);

CREATE TABLE cans (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    latitude    DOUBLE(12, 8)   NOT NULL,
    longitude   DOUBLE(12, 8)   NOT NULL,
    public_key  VARCHAR(2048)   NOT NULL,

    PRIMARY KEY(id)
);
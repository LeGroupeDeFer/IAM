CREATE TABLE tokens (
  id                INT UNSIGNED    NOT NULL AUTO_INCREMENT,
  hash              VARCHAR(32)     NOT NULL,
  creation_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
  expiration_date   TIMESTAMP       NULL,

  PRIMARY KEY (id),

  CONSTRAINT unique_hash            UNIQUE(hash)
);

CREATE TABLE users (
  id                INT UNSIGNED    NOT NULL AUTO_INCREMENT,
  username          VARCHAR(128)    NOT NULL,
  password          VARCHAR(128)    NOT NULL,
  refresh_token_id  INT UNSIGNED    NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (refresh_token_id) REFERENCES tokens (id),

  CONSTRAINT unique_username        UNIQUE(username)
);

CREATE TABLE cans (
  id                INT UNSIGNED    NOT NULL AUTO_INCREMENT,
  latitude          DOUBLE(12, 8)   NOT NULL,
  longitude         DOUBLE(12, 8)   NOT NULL,
  public_key        VARCHAR(2048)   NOT NULL,

  PRIMARY KEY (id)
);
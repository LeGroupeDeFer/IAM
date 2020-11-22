ALTER TABLE cans
    ADD sign_protocol VARCHAR(50) NOT NULL DEFAULT 'rsa' AFTER public_key;
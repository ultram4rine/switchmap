CREATE TABLE IF NOT EXISTS last_sync_time (
    sync_time TIMESTAMP NOT NULL,
    lock VARCHAR(1) NOT NULL DEFAULT 'X',
    PRIMARY KEY (lock),
    CONSTRAINT locked CHECK (lock = 'X')
);
INSERT INTO last_sync_time
VALUES (NOW(), 'X');
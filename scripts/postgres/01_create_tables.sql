-- init.sql para Postgres
CREATE TABLE authentication (
    uuid      VARCHAR(36)  PRIMARY KEY,
    user_id   VARCHAR(100) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_active BOOLEAN      DEFAULT TRUE
);
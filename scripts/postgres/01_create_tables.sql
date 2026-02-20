-- init.sql para Postgres
CREATE TABLE authentication (
    uuid      VARCHAR(36) PRIMARY KEY,
    user_id   VARCHAR(100) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_active BOOLEAN      DEFAULT TRUE
);

CREATE TABLE roles (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE authentication_roles (
    auth_uuid VARCHAR(36) NOT NULL,
    role_id   INTEGER NOT NULL,
    PRIMARY KEY (auth_uuid, role_id),
    FOREIGN KEY (auth_uuid) REFERENCES authentication(uuid) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);